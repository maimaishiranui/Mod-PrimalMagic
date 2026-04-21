package com.Primal.block;

import com.Primal.PrimalMagic;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.s2c.play.TitleS2CPacket;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class MingYuanCoreBlock extends Block {
    public MingYuanCoreBlock(Settings settings) {
        super(settings);
    }

    // 当区块加载时，核心方块会再次向主类报到
    @Override
    public void onBlockAdded(BlockState state, World world, BlockPos pos, BlockState oldState, boolean notify) {
        super.onBlockAdded(state, world, pos, oldState, notify);
        if (!world.isClient) {
            com.Primal.PrimalMagic.PALACE_CORE_POS = pos;
        }
    }

    // 增加一个加载逻辑，确保服务器重启后依然能定位
    public boolean onSyncedBlockEvent(BlockState state, World world, BlockPos pos, int id, int data) {
        com.Primal.PrimalMagic.PALACE_CORE_POS = pos;
        return false;
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

        // 0. 状态锁定
        com.Primal.PrimalMagic.IS_SUMMONING = true;
        com.Primal.PrimalMagic.PALACE_CORE_POS = null;

        // 1. 【终极强制击飞】
        if (player != null && !player.isCreative() && player instanceof ServerPlayerEntity sp) {
            // A. 预先给予无敌和抗性，防止任何意外
            sp.addStatusEffect(new StatusEffectInstance(StatusEffects.RESISTANCE, 400, 4, false, false));

            // B. 计算击飞向量 (以核心为圆心向外)
            double dx = sp.getX() - (pos.getX() + 0.5);
            double dz = sp.getZ() - (pos.getZ() + 0.5);
            // 如果玩家正好站在方块中心导致 dx/dz 为 0，给予一个默认方向
            if (Math.abs(dx) < 0.1 && Math.abs(dz) < 0.1) { dx = 1.0; }

            // 极其强劲的速度：水平 10.0，垂直 2.0
            Vec3d launchVec = new Vec3d(dx, 0, dz).normalize().multiply(10.0);

            // C. 【关键修复：坐标强制重置】
            // 在施加推力前，瞬间将玩家坐标强制提升 1.5 格。
            // 这一步是为了让玩家彻底离开地面的“摩擦力判定区”和“卡块判定区”。
            sp.requestTeleport(sp.getX(), sp.getY() + 1.5, sp.getZ());

            // D. 立即施加爆发速度
            sp.setVelocity(launchVec.x, 2.0, launchVec.z);
            sp.velocityModified = true;

            // E. 【最关键：立即同步】
            // 强制告诉客户端：你的坐标变了，速度也变了！
            sp.networkHandler.sendPacket(new net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket(sp));

            // F. 仪式文字
            sp.networkHandler.sendPacket(new net.minecraft.network.packet.s2c.play.TitleS2CPacket(Text.literal("§0§l万 象 湮 灭...")));
        }

        // 2. 仪式标记逻辑 (2秒后生成建筑)
        net.minecraft.entity.MarkerEntity ritualMarker = EntityType.MARKER.create(serverWorld);
        if (ritualMarker != null) {
            ritualMarker.setPosition(pos.toCenterPos());
            serverWorld.spawnEntity(ritualMarker);
            // 延迟 40 刻 (2秒)
            PrimalMagic.RITUAL_TASKS.put(world.getTime() + 40, pos);
        }

        // 3. 核心方块瞬间消失，防止任何碰撞残留
        serverWorld.setBlockState(pos, Blocks.AIR.getDefaultState(), 3);

        // 播放震耳欲聋的声波
        serverWorld.playSound(null, pos, SoundEvents.ENTITY_WARDEN_SONIC_BOOM, SoundCategory.BLOCKS, 2.0f, 0.5f);
    }

    private void startDelayedSummon(ServerWorld world, BlockPos pos, long time) {
        // 利用 1.21 的服务器任务调度系统
        world.getServer().execute(() -> {
            // 这里我们改用一种更简单的方式：利用 Marker 的 tick 或者主类循环
            // 为了保证 100% 成功，我们将具体的生成逻辑写在一个辅助方法里，并在主类调用。
            PrimalMagic.RITUAL_TASKS.put(time, pos);
        });
    }
}