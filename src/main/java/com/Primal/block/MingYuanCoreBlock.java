package com.Primal.block;

import com.Primal.PrimalMagic;
import com.Primal.entity.ModEntities;
import com.Primal.entity.MingYuanEntity;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.structure.StructurePlacementData;
import net.minecraft.structure.StructureTemplate;
import net.minecraft.structure.StructureTemplateManager;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.Heightmap;
import net.minecraft.world.World;
import net.minecraft.util.math.random.Random;

import java.util.Optional;

public class MingYuanCoreBlock extends Block {
    public MingYuanCoreBlock(Settings settings) {
        super(settings);
    }

    @Override
    public BlockState onBreak(World world, BlockPos pos, BlockState state, PlayerEntity player) {
        triggerSummonRitual(world, pos, player);
        return super.onBreak(world, pos, state, player);
    }

    @Override
    protected ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
        triggerSummonRitual(world, pos, player);
        return ActionResult.SUCCESS;
    }

    private void triggerSummonRitual(World world, BlockPos pos, PlayerEntity player) {
        if (world.isClient() || !(world instanceof ServerWorld serverWorld)) return;

        serverWorld.setBlockState(pos, Blocks.AIR.getDefaultState(), 3);

        // 1. 【超强弹射逻辑】将玩家弹射出 100 格左右
        if (player != null && !player.isCreative()) {
            // 计算从核心指向玩家的水平向量
            double dx = player.getX() - (pos.getX() + 0.5);
            double dz = player.getZ() - (pos.getZ() + 0.5);
            Vec3d launchVec = new Vec3d(dx, 0, dz).normalize().multiply(6.5); // 6.5 的水平初速度非常快

            // 给予玩家一个强大的斜向上抛力
            player.setVelocity(launchVec.x, 1.5, launchVec.z); // Y轴 1.5 确保玩家飞得很高
            player.velocityModified = true;

            // 为了不让玩家直接摔死，给予 15 秒的缓降和极高抗性
            player.addStatusEffect(new StatusEffectInstance(StatusEffects.RESISTANCE, 300, 4, false, false));
            player.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOW_FALLING, 200, 0, false, false));

            player.sendMessage(Text.literal("§c§l一股未知的虚空洪流将你震飞！"), true);
        }

        // 2. 特效与音效
        serverWorld.spawnParticles(ParticleTypes.EXPLOSION_EMITTER, pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, 10, 1, 1, 1, 0.1);
        serverWorld.playSound(null, pos, SoundEvents.ENTITY_GENERIC_EXPLODE.value(), SoundCategory.BLOCKS, 5.0f, 0.5f);

        // 3. 加载并生成 NBT (下沉 2 格)
        Identifier nbtId = Identifier.of(PrimalMagic.MOD_ID, "mingyuan_palace");
        StructureTemplateManager templateManager = serverWorld.getStructureTemplateManager();
        Optional<StructureTemplate> optionalTemplate = templateManager.getTemplate(nbtId);

        if (optionalTemplate.isPresent()) {
            StructureTemplate template = optionalTemplate.get();
            Vec3i size = template.getSize();

            // --- 关键点：pos.add(..., -2, ...) 让建筑整体下沉 2 格 ---
            BlockPos placePos = pos.add(-size.getX() / 2, -2, -size.getZ() / 2);

            StructurePlacementData placementData = new StructurePlacementData();
            template.place(serverWorld, placePos, placePos, placementData, serverWorld.random, 2);

            // 4. 召唤 Boss (同步下沉)
            MingYuanEntity boss = ModEntities.MINGYUAN.create(serverWorld);
            if (boss != null) {
                // 这里的 Y 坐标改为 pos.getY() - 1，这样它会正好刷在大殿沉下去后的地板上
                boss.refreshPositionAndAngles(pos.getX() + 0.5, pos.getY() - 1.0, pos.getZ() + 0.5, 0, 0);
                serverWorld.spawnEntity(boss);
            }

            // 5. 污染逻辑
            corruptTerrain(serverWorld, pos);

        } else {
            PrimalMagic.LOGGER.error("无法加载 NBT 建筑：找不到 " + nbtId);
        }
    }

    private void corruptTerrain(ServerWorld world, BlockPos center) {
        Random random = world.getRandom();
        int radius = 50;
        for (int i = 0; i < 2000; i++) {
            int rx = random.nextInt(radius * 2) - radius;
            int rz = random.nextInt(radius * 2) - radius;
            if (rx * rx + rz * rz <= radius * radius) {
                BlockPos surfacePos = world.getTopPosition(Heightmap.Type.WORLD_SURFACE, center.add(rx, 0, rz)).down();
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
}