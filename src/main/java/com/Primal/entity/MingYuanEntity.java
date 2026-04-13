package com.Primal.entity;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.*;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.SmallFireballEntity;
import net.minecraft.entity.projectile.thrown.SnowballEntity;
import net.minecraft.entity.projectile.WindChargeEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

public class MingYuanEntity extends HostileEntity implements GeoEntity {
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    // 数据同步：用于通知客户端播放“警戒”动画
    private static final TrackedData<Boolean> IS_WARNING = DataTracker.registerData(MingYuanEntity.class, TrackedDataHandlerRegistry.BOOLEAN);

    private static final RawAnimation WARNING = RawAnimation.begin().thenLoop("warning");
    private static final RawAnimation WALK = RawAnimation.begin().thenLoop("walk");
    private static final RawAnimation ATTACK = RawAnimation.begin().thenPlay("attack");
    private static final RawAnimation DEAD = RawAnimation.begin().thenPlay("dead");

    public MingYuanEntity(EntityType<? extends HostileEntity> entityType, World world) {
        super(entityType, world);
    }

    @Override
    protected void initDataTracker(DataTracker.Builder builder) {
        super.initDataTracker(builder);
        builder.add(IS_WARNING, false);
    }

    // --- 1. 设置属性：300血量 ---
    public static DefaultAttributeContainer.Builder setAttributes() {
        return HostileEntity.createHostileAttributes()
                .add(EntityAttributes.GENERIC_MAX_HEALTH, 300.0D)
                .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 10.0D)
                .add(EntityAttributes.GENERIC_FOLLOW_RANGE, 40.0D) // 追踪范围
                .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.23D);
    }

    // --- 2. 编写 AI 目标：让 Boss 攻击玩家 ---
    @Override
    protected void initGoals() {
        this.goalSelector.add(1, new SwimGoal(this));
        // 自定义三连发远程攻击目标
        this.goalSelector.add(2, new MingYuanAttackGoal(this, 1.0D, 40));
        this.goalSelector.add(3, new WanderAroundFarGoal(this, 0.8D));
        this.goalSelector.add(4, new LookAtEntityGoal(this, PlayerEntity.class, 32.0F));

        // 目标选择器：主动锁定 30 格内的生存模式玩家
        this.targetSelector.add(1, new ActiveTargetGoal<>(this, PlayerEntity.class, true));
        this.targetSelector.add(2, new RevengeGoal(this));
    }

    @Override
    public void tick() {
        super.tick();
        if (!this.getWorld().isClient) {
            // --- 天气感应：雷雨天获得抗性 II ---
            if (this.getWorld().isRaining() || this.getWorld().isThundering()) {
                this.addStatusEffect(new StatusEffectInstance(StatusEffects.RESISTANCE, 40, 1, false, false));
            }

            // --- 警戒逻辑：30格内有玩家则设为 Warning 状态 ---
            PlayerEntity closestPlayer = this.getWorld().getClosestPlayer(this, 30.0);
            this.setWarning(closestPlayer != null && this.getTarget() == null);
        }
    }

    public void setWarning(boolean warning) {
        this.dataTracker.set(IS_WARNING, warning);
    }

    public boolean isWarning() {
        return this.dataTracker.get(IS_WARNING);
    }

    // --- 3. 动画控制器逻辑 ---
    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "controller", 5, state -> {
            if (this.isDead()) return state.setAndContinue(DEAD);

            // 优先级：正在攻击 > 正在移动 > 警戒范围内有人 > 静止
            if (this.handSwinging) return state.setAndContinue(ATTACK);
            if (state.isMoving()) return state.setAndContinue(WALK);
            if (this.isWarning()) return state.setAndContinue(WARNING);

            return state.setAndContinue(WARNING); // 如果你想让它完全不动，这里可以返回空或特定的 idle
        }));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }

    // =========================================================
    // 自定义 AI 攻击类：三连发逻辑
    // =========================================================

    static class MingYuanAttackGoal extends Goal {
        private final MingYuanEntity boss;
        private int attackTicks = 0;

        public MingYuanAttackGoal(MingYuanEntity boss, double speed, int interval) {
            this.boss = boss;
        }

        @Override
        public boolean canStart() {
            return boss.getTarget() != null && boss.getTarget().isAlive();
        }

        @Override
        public void tick() {
            LivingEntity target = boss.getTarget();
            if (target == null) return;

            boss.getLookControl().lookAt(target, 30.0F, 30.0F);
            attackTicks++;

            // 三阶段射击逻辑
            if (attackTicks == 20) { // 1秒后发射雪球
                shootSnowball(target);
            } else if (attackTicks == 40) { // 2秒后发射烈焰
                shootFireball(target);
            } else if (attackTicks == 60) { // 3秒后发射风弹
                shootWindCharge(target);
                attackTicks = -20; // 冷却循环
            }
        }

        private void shootSnowball(LivingEntity target) {
            SnowballEntity snowball = new SnowballEntity(boss.getWorld(), boss);
            // 设置发射位置为 Boss 眼睛高度
            snowball.setPosition(boss.getX(), boss.getEyeY(), boss.getZ());

            double d = target.getX() - boss.getX();
            double e = target.getBodyY(0.5) - boss.getEyeY();
            double f = target.getZ() - boss.getZ();

            snowball.setVelocity(d, e, f, 1.5f, 1.0f);
            boss.getWorld().spawnEntity(snowball);

            target.setFrozenTicks(100);
            target.damage(boss.getWorld().getDamageSources().freeze(), 2.0f);
        }

        private void shootFireball(LivingEntity target) {
            // 1. 计算指向目标的向量
            Vec3d velocity = new Vec3d(target.getX() - boss.getX(),
                    target.getBodyY(0.5) - boss.getEyeY(),
                    target.getZ() - boss.getZ()).normalize();

            // 2. 根据报错修改：使用 (World, x, y, z, velocityVec) 构造函数
            SmallFireballEntity fireball = new SmallFireballEntity(
                    boss.getWorld(),
                    boss.getX(), boss.getEyeY(), boss.getZ(),
                    velocity
            );

            // 3. 设置主人，防止 Boss 伤到自己
            fireball.setOwner(boss);

            boss.getWorld().spawnEntity(fireball);
            boss.swingHand(Hand.MAIN_HAND);
        }

        private void shootWindCharge(LivingEntity target) {
            // 1. 使用 1.21 最稳妥的创建方式：通过 EntityType 创建实例
            WindChargeEntity wind = net.minecraft.entity.EntityType.WIND_CHARGE.create(boss.getWorld());

            if (wind != null) {
                // 2. 设置发射位置（Boss 的眼睛高度）
                wind.refreshPositionAndAngles(boss.getX(), boss.getEyeY(), boss.getZ(), boss.getYaw(), boss.getPitch());

                // 3. 设置主人，防止 Boss 伤到自己
                wind.setOwner(boss);

                // 4. 计算并设置飞行速度
                double d = target.getX() - boss.getX();
                double e = target.getBodyY(0.5) - boss.getEyeY();
                double f = target.getZ() - boss.getZ();
                wind.setVelocity(d, e, f, 1.5f, 1.0f);

                // 5. 将风弹生成在世界中
                boss.getWorld().spawnEntity(wind);

                // --- 施加你要求的 Debuff 效果 ---
                target.addStatusEffect(new StatusEffectInstance(StatusEffects.BLINDNESS, 20, 0)); // 失明 1s
                target.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 40, 1));  // 减速 2s
                target.addStatusEffect(new StatusEffectInstance(StatusEffects.WEAKNESS, 20, 0));  // 虚弱 1s
                target.damage(boss.getWorld().getDamageSources().magic(), 1.0f);
            }
        }
    }
}