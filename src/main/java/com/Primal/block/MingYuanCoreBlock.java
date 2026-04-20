package com.Primal.block;

import com.Primal.PrimalMagic;
import com.Primal.entity.ModEntities;
import com.Primal.entity.MingYuanEntity;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.structure.StructurePlacementData;
import net.minecraft.structure.StructureTemplate;
import net.minecraft.structure.StructureTemplateManager;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.Heightmap;
import net.minecraft.world.World;

// 核心修复 3：引入 Minecraft 专属的 Random 类
import net.minecraft.util.math.random.Random;
import java.util.Optional;

public class MingYuanCoreBlock extends Block {
    public MingYuanCoreBlock(Settings settings) {
        super(settings);
    }

    // 核心修复 1：1.21 的 onUse 方法去掉了 Hand 参数，且变为 protected
    @Override
    protected ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
        triggerSummonRitual(world, pos);
        return ActionResult.SUCCESS;
    }

    // 核心修复 1.1：1.21 的 onProjectileHit 也变为 protected
    @Override
    protected void onProjectileHit(World world, BlockState state, BlockHitResult hit, ProjectileEntity projectile) {
        triggerSummonRitual(world, hit.getBlockPos());
    }

    // =========================================================
    // 核心召唤仪式逻辑
    // =========================================================
    private void triggerSummonRitual(World world, BlockPos pos) {
        if (world.isClient() || !(world instanceof ServerWorld serverWorld)) return;

        // 1. 震撼的爆炸音效与视觉特效 (不破坏实际地形)
        serverWorld.spawnParticles(ParticleTypes.EXPLOSION_EMITTER, pos.getX() + 0.5, pos.getY() + 1, pos.getZ() + 0.5, 3, 1, 1, 1, 0);
        serverWorld.playSound(null, pos, SoundEvents.ENTITY_GENERIC_EXPLODE.value(), SoundCategory.BLOCKS, 4.0f, 0.8f);
        serverWorld.playSound(null, pos, SoundEvents.ENTITY_WITHER_SPAWN, SoundCategory.HOSTILE, 1.0f, 0.5f);

        // 2. 加载 NBT 建筑文件
        Identifier nbtId = Identifier.of(PrimalMagic.MOD_ID, "mingyuan_palace");
        StructureTemplateManager templateManager = serverWorld.getStructureTemplateManager();
        Optional<StructureTemplate> optionalTemplate = templateManager.getTemplate(nbtId);

        if (optionalTemplate.isPresent()) {
            StructureTemplate template = optionalTemplate.get();
            // 核心修复 2：1.21 中 getSize 返回 Vec3i
            Vec3i size = template.getSize();

            // 动态计算偏移量
            BlockPos placePos = pos.add(-size.getX() / 2, -1, -size.getZ() / 2);

            StructurePlacementData placementData = new StructurePlacementData();
            template.place(serverWorld, placePos, placePos, placementData, serverWorld.random, 2);

            // 3. 召唤冥渊 Boss
            MingYuanEntity boss = ModEntities.MINGYUAN.create(serverWorld);
            if (boss != null) {
                boss.refreshPositionAndAngles(pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5, 0, 0);
                serverWorld.spawnEntity(boss);
            }

            // 4. 半径 50 格地形污染
            corruptTerrain(serverWorld, pos);

            // 5. 仪式完成，摧毁核心方块本身
            serverWorld.removeBlock(pos, false);

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