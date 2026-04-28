package com.Primal.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.entity.Entity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;

public class MiragePortalBlock extends Block {
    // 像地狱门一样的薄片形状
    protected static final VoxelShape SHAPE = Block.createCuboidShape(0.0, 0.0, 6.0, 16.0, 16.0, 10.0);

    public MiragePortalBlock(Settings settings) {
        super(settings);
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return SHAPE;
    }

    // 实体碰撞即传送
    @Override
    public void onEntityCollision(BlockState state, World world, BlockPos pos, Entity entity) {
        if (!world.isClient && entity instanceof ServerPlayerEntity player) {
            RegistryKey<World> mirageKey = RegistryKey.of(RegistryKeys.WORLD, Identifier.of("primalmagic", "mirage"));
            ServerWorld mirageWorld = player.getServer().getWorld(mirageKey);
            if (mirageWorld != null) {
                // 传送到 Mirage 孤岛 (0, 70, 0)
                player.teleport(mirageWorld, 0.5, 70.0, 0.5, player.getYaw(), player.getPitch());
            }
        }
    }
}