package com.Primal.entity;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

public class MingYuanEntity extends HostileEntity implements GeoEntity {
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private BlockPos anchorPos;

    // 【核心修复】必须与 Blockbench 里的名字完全一致！
    private static final RawAnimation WARNING = RawAnimation.begin().thenLoop("warning");
    private static final RawAnimation WALK = RawAnimation.begin().thenLoop("walk");
    private static final RawAnimation ATTACK = RawAnimation.begin().thenPlay("attack");
    private static final RawAnimation DEAD = RawAnimation.begin().thenPlay("dead");

    // 添加一个标志位，用来控制攻击动画
    public boolean isAttacking = false;

    public MingYuanEntity(EntityType<? extends HostileEntity> entityType, World world) {
        super(entityType, world);
    }

    public static DefaultAttributeContainer.Builder setAttributes() {
        return HostileEntity.createHostileAttributes()
                .add(EntityAttributes.GENERIC_MAX_HEALTH, 300.0D) // 300 血量
                .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 10.0D)
                .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.25D)
                .add(EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE, 1.0D); // 防止被玩家击退
    }

    @Override
    public void tick() {
        super.tick();
        if (!this.getWorld().isClient) {
            // 禁足逻辑：如果不想要禁足，把这里注释掉即可
            if (anchorPos == null) anchorPos = this.getBlockPos();
            if (this.getPos().distanceTo(anchorPos.toCenterPos()) > 15.0) {
                this.teleport(anchorPos.getX(), anchorPos.getY(), anchorPos.getZ(), true);
                this.setHealth(this.getMaxHealth());
            }

            // 这里以后可以写你的自定义攻击 AI 逻辑
        }
    }

    @Override
    public void onDeath(DamageSource source) {
        super.onDeath(source);
        if (this.getWorld() instanceof ServerWorld serverWorld) {
            // 死亡时的黑色爆炸
            for (int i = 0; i < 40; i++) {
                serverWorld.spawnParticles(ParticleTypes.LARGE_SMOKE, this.getX(), this.getY() + 1.0, this.getZ(), 2, 0.5, 0.5, 0.5, 0.05);
                serverWorld.spawnParticles(ParticleTypes.SQUID_INK, this.getX(), this.getY() + 1.0, this.getZ(), 3, 0.3, 0.3, 0.3, 0.1);
            }
            this.playSound(SoundEvents.ENTITY_GENERIC_EXPLODE.value(), 1.0f, 0.8f);
        }
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "controller", 5, event -> {
            if (this.isDead()) {
                return event.setAndContinue(DEAD);
            }
            if (this.isAttacking) {
                return event.setAndContinue(ATTACK);
            }
            if (event.isMoving()) {
                return event.setAndContinue(WALK);
            }
            // 默认播放 warning 警戒动作
            return event.setAndContinue(WARNING);
        }));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }
}