package com.Primal;

import com.Primal.block.ModBlockEntities;
import com.Primal.block.ModBlocks;
import com.Primal.component.ModDataComponentTypes;
import com.Primal.datagen.ModPlacedFeatures;
import com.Primal.item.ModItemGroup;
import com.Primal.item.ModItems;
import com.Primal.item.SceptreItem;
import com.Primal.screen.ModScreenHandlers;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.world.gen.GenerationStep;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import com.Primal.item.ModGuideBook;

public class PrimalMagic implements ModInitializer {
	public static final String MOD_ID = "primalmagic";

	//此记录器用于将文本写入控制台和日志文件。
	//最好使用 mod id 作为 logger 的名称。
	//这样，就可以清楚地知道哪个 Mod 编写了信息、警告和错误。
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override

	public void onInitialize() {
		ModDataComponentTypes.registerDataComponentTypes();
		ModItems.registerModItems();
		ModItemGroup.registerItemGroups();
		ModBlocks.registerModBlock();
		ModBlockEntities.registerBlockEntities();
		ModScreenHandlers.registerScreenHandlers();


		//监听方块和状态
		AttackBlockCallback.EVENT.register((player, world, hand, pos, direction) -> {
			ItemStack stack = player.getStackInHand(hand);
			if (!world.isClient && stack.getItem() instanceof SceptreItem && stack.contains(ModDataComponentTypes.FLAME_BARRAGE_TICKS)) {
				((SceptreItem)stack.getItem()).executeFlameSkill2((ServerWorld)world, player, stack);
				return ActionResult.SUCCESS; // 拦截挖矿动作
			}
			return ActionResult.PASS;
		});

// 2. 监听左键点击空气/实体
		AttackEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
			ItemStack stack = player.getStackInHand(hand);
			if (!world.isClient && stack.getItem() instanceof SceptreItem && stack.contains(ModDataComponentTypes.FLAME_BARRAGE_TICKS)) {
				((SceptreItem)stack.getItem()).executeFlameSkill2((ServerWorld)world, player, stack);
				return ActionResult.SUCCESS; // 拦截普通攻击动作
			}
			return ActionResult.PASS;
		});

		// 注入灵媒石
		BiomeModifications.addFeature(BiomeSelectors.foundInOverworld(),
				GenerationStep.Feature.UNDERGROUND_ORES, ModPlacedFeatures.SPIRITUAL_MEDIUM_ORE_PLACED_KEY);

		// 注入洗练石
		BiomeModifications.addFeature(BiomeSelectors.foundInOverworld(),
				GenerationStep.Feature.UNDERGROUND_ORES, ModPlacedFeatures.REFINING_STONE_ORE_PLACED_KEY);

		// 注入幻化石
		BiomeModifications.addFeature(BiomeSelectors.foundInOverworld(),
				GenerationStep.Feature.UNDERGROUND_ORES, ModPlacedFeatures.ILLUSIONARY_ORE_PLACED_KEY);


		//向导书发放

// PrimalMagic.java 里的关键部分
		ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
			var player = handler.getPlayer();

			// 检查玩家是否领过手册
			if (!player.getCommandTags().contains("received_primal_guide")) {
				player.getInventory().insertStack(ModGuideBook.create());
				player.addCommandTag("received_primal_guide");

				player.sendMessage(Text.literal("§d[原初魔法]§r 魔法的力量已觉醒，请查收你的向导书！"), false);
			}
		});

		LOGGER.info("Primal Magic initialized!");
	}
}