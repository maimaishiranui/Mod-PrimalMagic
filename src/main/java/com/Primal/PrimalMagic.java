package com.Primal;

import com.Primal.block.ModBlockEntities;
import com.Primal.block.ModBlocks;
import com.Primal.component.ModDataComponentTypes;
import com.Primal.datagen.ModPlacedFeatures;
import com.Primal.item.ModGuideBook;
import com.Primal.item.ModItemGroup;
import com.Primal.item.ModItems;
import com.Primal.item.SceptreItem;
import com.Primal.screen.ModScreenHandlers;
import com.Primal.util.ModTalismanDrops; // 确保你有这个工具类

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.gen.GenerationStep;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PrimalMagic implements ModInitializer {
	public static final String MOD_ID = "primalmagic";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		// ================= 1. 基础组件与物品注册 =================
		ModDataComponentTypes.registerDataComponentTypes();
		ModItems.registerModItems();
		ModItemGroup.registerItemGroups();
		ModBlocks.registerModBlock();
		ModBlockEntities.registerBlockEntities();
		ModScreenHandlers.registerScreenHandlers();

		// ================= 2. 战斗逻辑监听 (烈焰左键连发) =================
		// 监听左键点击方块
		AttackBlockCallback.EVENT.register((player, world, hand, pos, direction) -> {
			ItemStack stack = player.getStackInHand(hand);
			if (!world.isClient && stack.getItem() instanceof SceptreItem && stack.contains(ModDataComponentTypes.FLAME_BARRAGE_TICKS)) {
				((SceptreItem)stack.getItem()).executeFlameSkill2((ServerWorld)world, player, stack);
				return ActionResult.SUCCESS;
			}
			return ActionResult.PASS;
		});

		// 监听左键点击实体
		AttackEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
			ItemStack stack = player.getStackInHand(hand);
			if (!world.isClient && stack.getItem() instanceof SceptreItem && stack.contains(ModDataComponentTypes.FLAME_BARRAGE_TICKS)) {
				((SceptreItem)stack.getItem()).executeFlameSkill2((ServerWorld)world, player, stack);
				return ActionResult.SUCCESS;
			}
			return ActionResult.PASS;
		});

		// ================= 3. 护符获取逻辑注册 (核心新增) =================

		// 3.1 矿石掉落 (Rock)
		ModTalismanDrops.registerMineLogic();

		// 3.2 实体死亡掉落 (Flame, Stream, Thunder, Frozen)
		ServerLivingEntityEvents.AFTER_DEATH.register((entity, damageSource) -> {
			ModTalismanDrops.handleEntityDrops(entity, damageSource);
		});

		// --- 1. 种植/喂食逻辑 (只有动作发生时才执行 15% 抽奖) ---
		UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
			if (!world.isClient && hand == Hand.MAIN_HAND) {
				ItemStack item = player.getStackInHand(hand);
				// 如果是种地动作
				if (item.getItem().toString().contains("seed") || item.isOf(net.minecraft.item.Items.WHEAT_SEEDS)) {
					ModTalismanDrops.tryGiveFarmingRandomReward(player); // <--- 调用随机奖励方法
				}
			}
			return ActionResult.PASS;
		});

		UseEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
			if (!world.isClient && entity instanceof net.minecraft.entity.passive.AnimalEntity) {
				ModTalismanDrops.tryGiveFarmingRandomReward(player); // <--- 调用随机奖励方法
			}
			return ActionResult.PASS;
		});

		// --- 2. 服务器 Tick 检查 (处理成就和微风) ---
		ServerTickEvents.END_SERVER_TICK.register(server -> {
			int tickCounter = server.getTicks();

			for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
				// A. 微风检查：每刻都检查 (因为自由落体非常快)，概率在方法内部控制
				ModTalismanDrops.handleBreezeCheck(player);

				// B. 成就检查：每秒检查一次即可 (节省资源)
				if (tickCounter % 20 == 0) {
					ModTalismanDrops.handleFarmingLogic(player);
				}
			}
		});
		// ================= 4. 世界生成注入 (矿石) =================
		BiomeModifications.addFeature(BiomeSelectors.foundInOverworld(),
				GenerationStep.Feature.UNDERGROUND_ORES, ModPlacedFeatures.SPIRITUAL_MEDIUM_ORE_PLACED_KEY);

		BiomeModifications.addFeature(BiomeSelectors.foundInOverworld(),
				GenerationStep.Feature.UNDERGROUND_ORES, ModPlacedFeatures.REFINING_STONE_ORE_PLACED_KEY);

		BiomeModifications.addFeature(BiomeSelectors.foundInOverworld(),
				GenerationStep.Feature.UNDERGROUND_ORES, ModPlacedFeatures.ILLUSIONARY_ORE_PLACED_KEY);

		// ================= 5. 向导书发放逻辑 =================
		ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
			var player = handler.getPlayer();
			if (!player.getCommandTags().contains("received_primal_guide")) {
				player.getInventory().insertStack(ModGuideBook.create());
				player.addCommandTag("received_primal_guide");
				player.sendMessage(Text.literal("§d[原初魔法]§r 魔法的力量已觉醒，请查收你的向导书！"), false);
			}
		});

		LOGGER.info("Primal Magic Initialized with Talisman Drops!");
	}
}