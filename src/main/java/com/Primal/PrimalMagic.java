package com.Primal;

import com.Primal.block.ModBlockEntities;
import com.Primal.block.ModBlocks;
import com.Primal.component.ModDataComponentTypes;
import com.Primal.datagen.ModPlacedFeatures;
import com.Primal.entity.MingYuanEntity;
import com.Primal.entity.ModEntities;
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
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.block.Blocks;
import net.minecraft.entity.MarkerEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.structure.StructurePlacementData;
import net.minecraft.structure.StructureTemplate;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.Heightmap;
import net.minecraft.world.gen.GenerationStep;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

public class PrimalMagic implements ModInitializer {
	public static final String MOD_ID = "primalmagic";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);


	// 在 PrimalMagic 类中添加变量,用于管理mingyuan核心的坐标和召唤任务
	public static BlockPos PALACE_CORE_POS = null;
	public static boolean IS_SUMMONING = false; // 召唤状态锁定，防止重复触发
	public static final java.util.Map<Long, BlockPos> RITUAL_TASKS = new java.util.HashMap<>();
	public static BlockBox PALACE_BOUNDS = null; // 记录神殿的范围

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

			//mingyuan核心的核心组件
			long currentTime = server.getOverworld().getTime();

			// --- 1. 处理延时生成 (2秒逻辑) ---
			if (RITUAL_TASKS.containsKey(currentTime)) {
				BlockPos pos = RITUAL_TASKS.get(currentTime);
				ServerWorld world = server.getOverworld();
				completePalaceSummon(world, pos);
				RITUAL_TASKS.remove(currentTime);
			}

			// 2. 增强版屏幕标题警告逻辑
			if (currentTime % 20 == 0) { // 每秒检查一次
				for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
					// 如果变量丢失，尝试从玩家周围搜寻核心（保险措施）
					if (PALACE_CORE_POS == null) {
								// 这个搜寻范围不要太大，防止卡顿
						BlockPos.findClosest(player.getBlockPos(), 100, 100, pos ->
												player.getWorld().getBlockState(pos).isOf(com.Primal.block.ModBlocks.MINGYUAN_CORE))
										.ifPresent(pos -> PALACE_CORE_POS = pos);
							}

							if (PALACE_CORE_POS != null) {
								double dist = player.getPos().distanceTo(PALACE_CORE_POS.toCenterPos());

								// 100-80格警告
								if (dist <= 100 && dist > 80) {
									player.networkHandler.sendPacket(new net.minecraft.network.packet.s2c.play.TitleFadeS2CPacket(10, 40, 10)); // 渐入渐出
									player.networkHandler.sendPacket(new net.minecraft.network.packet.s2c.play.SubtitleS2CPacket(Text.literal("§7你不该来到这里")));
									player.networkHandler.sendPacket(new net.minecraft.network.packet.s2c.play.TitleS2CPacket(Text.literal("§0§l遗迹警告")));
								}
								// 30格警告
								else if (dist <= 30 && dist > 10) {
									player.networkHandler.sendPacket(new net.minecraft.network.packet.s2c.play.SubtitleS2CPacket(Text.literal("§c想挑战未知吗？")));
									player.networkHandler.sendPacket(new net.minecraft.network.packet.s2c.play.TitleS2CPacket(Text.literal("§4§l死亡警告")));
								}
							}
						}
					}


			//-----------法杖类的相关计时组件-------------------

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

		//-------------------6.生物实体注册-------------------
		ModEntities.register();
		FabricDefaultAttributeRegistry.register(ModEntities.MINGYUAN, MingYuanEntity.setAttributes());

		//-------------------7.地形结构注册 -------------------
		//时刻监听循环
		ServerTickEvents.END_SERVER_TICK.register(server -> {
			ServerWorld world = server.getOverworld();
			long currentTime = world.getTime();

			// 1. 处理所有延时任务 (包含建筑生成和门户激活)
			if (RITUAL_TASKS.containsKey(currentTime)) {
				BlockPos pos = RITUAL_TASKS.get(currentTime);

				// 逻辑判定：如果是核心方块的坐标，执行建筑生成
				// 如果是遗迹中心的坐标，执行门户激活
				if (world.isAir(pos)) {
					activateMiragePortal(world, pos);
				} else {
					completePalaceSummon(world, pos);
				}

				RITUAL_TASKS.remove(currentTime);
			}

			// 2. 每 0.5 秒检测一次玩家是否拿着钥匙靠近遗迹
			if (currentTime % 10 == 0) {
				for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
					if (player.getMainHandStack().isOf(com.Primal.item.ModItems.ANCIENT_KEY)) {
						BlockPos portalCenter = findNearbyPortalFrame(player);
						if (portalCenter != null) {
							// 检查该位置是否已经激活，防止重复触发
							if (!world.getBlockState(portalCenter).isOf(ModBlocks.MIRAGE_PORTAL)) {
								startPortalRitual(player, portalCenter);
							}
						}
					}
				}
			}
		});



		LOGGER.info("Primal Magic Initialized with Talisman Drops!");
	}

	//实现建筑召唤的方法
	// 在 PrimalMagic.java 内部修改 completePalaceSummon 方法：

	private void completePalaceSummon(ServerWorld world, BlockPos pos) {
		Identifier nbtId = Identifier.of("primalmagic", "mingyuan_palace");
		Optional<StructureTemplate> template = world.getStructureTemplateManager().getTemplate(nbtId);

		if (template.isPresent()) {
			StructureTemplate t = template.get();
			Vec3i size = t.getSize();
			// 建筑下沉 2 格
			BlockPos placePos = pos.add(-size.getX() / 2, -2, -size.getZ() / 2);

			// --- 核心修复：更坚固的地基 ---
			// 我们在 45x45 的范围内铺设一层厚实的平滑砂岩
			for (int x = -1; x <= size.getX(); x++) {
				for (int z = -1; z <= size.getZ(); z++) {
					BlockPos foundation = placePos.add(x, -1, z);
					// 填充 2 层深的地基，防止空洞
					world.setBlockState(foundation, Blocks.SMOOTH_SANDSTONE.getDefaultState(), 3);
					world.setBlockState(foundation.down(), Blocks.SMOOTH_SANDSTONE.getDefaultState(), 3);
				}
			}

			// 放置建筑
			t.place(world, placePos, placePos, new StructurePlacementData(), world.random, 2);

			// 标记范围：保护建筑不被破坏
			PALACE_BOUNDS = new net.minecraft.util.math.BlockBox(
					placePos.getX(), placePos.getY(), placePos.getZ(),
					placePos.getX() + size.getX(), placePos.getY() + size.getY(), placePos.getZ() + size.getZ()
			);

			// 地形污染
			corruptTerrain(world, pos);

			// --- 核心修复：Boss 安全生成 ---
			MingYuanEntity boss = ModEntities.MINGYUAN.create(world);
			if (boss != null) {
				// 将 Boss 刷在核心原位置往上 8 格（建筑中空部分）
				boss.refreshPositionAndAngles(pos.getX() + 0.5, pos.getY() + 8.0, pos.getZ() + 0.5, 0, 0);
				boss.addStatusEffect(new StatusEffectInstance(StatusEffects.RESISTANCE, 200, 4));
				boss.addStatusEffect(new StatusEffectInstance(StatusEffects.GLOWING, 300, 0)); // 发光，让玩家远距离能看到
				world.spawnEntity(boss);
			}
		}
	}

	private void activateMiragePortal(net.minecraft.server.world.ServerWorld world, net.minecraft.util.math.BlockPos center) {

		// 1. 【新增】寻找并销毁飞到门中心的远古之钥
		java.util.List<net.minecraft.entity.ItemEntity> keys = world.getEntitiesByClass(
				net.minecraft.entity.ItemEntity.class,
				new net.minecraft.util.math.Box(center).expand(3.0),
				e -> e.getStack().isOf(com.Primal.item.ModItems.ANCIENT_KEY)
		);
		for (net.minecraft.entity.ItemEntity key : keys) {
			key.discard(); // 让钥匙实体消失
		}

		// 2. 填充 3x3 红色传送门方块
		for (int y = 0; y < 3; y++) {
			for (int x = -1; x <= 1; x++) {
				net.minecraft.util.math.BlockPos p = center.add(x, y, 0);
				if (world.isAir(p)) {
					world.setBlockState(p, com.Primal.block.ModBlocks.MIRAGE_PORTAL.getDefaultState(), 3);
				}
			}
		}

		// 3. 华丽收尾音效
		world.playSound(null, center, net.minecraft.sound.SoundEvents.BLOCK_END_PORTAL_SPAWN, net.minecraft.sound.SoundCategory.BLOCKS, 1f, 1f);
	}

	// 辅助方法：地形污染 (同样挪进 PrimalMagic 类)
	private void corruptTerrain(ServerWorld world, BlockPos center) {
		net.minecraft.util.math.random.Random random = world.getRandom();
		int radius = 50;
		for (int i = 0; i < 3000; i++) { // 增加循环次数让效果更明显
			int rx = random.nextInt(radius * 2) - radius;
			int rz = random.nextInt(radius * 2) - radius;
			if (rx * rx + rz * rz <= radius * radius) {
				BlockPos surfacePos = world.getTopPosition(Heightmap.Type.WORLD_SURFACE, center.add(rx, 0, rz)).down();

				// 判定是否是沙漠方块
				if (world.getBlockState(surfacePos).isOf(Blocks.SAND) || world.getBlockState(surfacePos).isOf(Blocks.SANDSTONE)) {
					if (random.nextBoolean()) {
						world.setBlockState(surfacePos, Blocks.RED_NETHER_BRICKS.getDefaultState(), 3);
					} else {
						world.setBlockState(surfacePos, Blocks.NETHER_WART_BLOCK.getDefaultState(), 3);
					}
				}
			}
		}
	}

	public BlockPos findNearbyPortalFrame(ServerPlayerEntity player) {
		ServerWorld world = player.getServerWorld();
		// 在玩家周围 10 格内寻找 Marker 实体
		List<MarkerEntity> markers = world.getEntitiesByClass(
				net.minecraft.entity.MarkerEntity.class,
				player.getBoundingBox().expand(10.0),
				entity -> entity.getCommandTags().contains("primalmagic:portal_marker")
		);

		if (!markers.isEmpty()) {
			return markers.get(0).getBlockPos();
		}
		return null;
	}

	private void startPortalRitual(net.minecraft.server.network.ServerPlayerEntity player, net.minecraft.util.math.BlockPos center) {
		net.minecraft.server.world.ServerWorld world = player.getServerWorld();

		// 1. 消耗钥匙
		if (!player.isCreative()) {
			player.getMainHandStack().decrement(1);
		}

		// 2. 【核心修复】创建一个“掉落物”形式的远古之钥
		net.minecraft.entity.ItemEntity flyingKey = new net.minecraft.entity.ItemEntity(
				world,
				player.getX(), player.getEyeY(), player.getZ(),
				new net.minecraft.item.ItemStack(com.Primal.item.ModItems.ANCIENT_KEY)
		);

		// 设置为：无限延迟拾取(玩家捡不起来)、无重力(悬浮)、发光
		flyingKey.setPickupDelayInfinite();
		flyingKey.setNoGravity(true);
		flyingKey.setGlowing(true);

		// 给钥匙一个指向大门中心的“吸引力”(速度)
		// 0.05 的系数会让它刚好在 2 秒左右慢悠悠地飘到中心点
		net.minecraft.util.math.Vec3d targetPos = center.toCenterPos().add(0, 1.5, 0);
		net.minecraft.util.math.Vec3d velocity = targetPos.subtract(flyingKey.getPos()).multiply(0.05);
		flyingKey.setVelocity(velocity);

		// 召唤飞行的钥匙
		world.spawnEntity(flyingKey);

		// 3. 注册 2 秒后的延迟任务 (40刻)
		long triggerTime = world.getTime() + 40;
		RITUAL_TASKS.put(triggerTime, center);

		// 4. 视觉特效
		world.spawnParticles(net.minecraft.particle.ParticleTypes.WITCH, center.getX() + 0.5, center.getY() + 1.5, center.getZ() + 0.5, 50, 0.5, 0.5, 0.5, 0.1);
		world.playSound(null, center, net.minecraft.sound.SoundEvents.BLOCK_AMETHYST_BLOCK_CHIME, net.minecraft.sound.SoundCategory.BLOCKS, 2.0f, 1.0f);
	}
}