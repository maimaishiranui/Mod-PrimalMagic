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
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.animation.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

public class MingYuanEntity extends HostileEntity implements GeoEntity {
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    private static final TrackedData<Boolean> IS_ATTACKING = DataTracker.registerData(MingYuanEntity.class, TrackedDataHandlerRegistry.BOOLEAN);

    // 动画定义 (请确保 JSON 里有 walk 和 attack1)
    private static final RawAnimation WALK = RawAnimation.begin().thenLoop("walk");
    private static final RawAnimation ATTACK = RawAnimation.begin().thenPlay("attack1");

    public MingYuanEntity(EntityType<? extends HostileEntity> entityType, World world) {
        super(entityType, world);
    }

    @Override
    protected void initDataTracker(DataTracker.Builder builder) {
        super.initDataTracker(builder);
        builder.add(IS_ATTACKING, false);
    }

    public static DefaultAttributeContainer.Builder setAttributes() {
        return HostileEntity.createHostileAttributes()
                .add(EntityAttributes.GENERIC_MAX_HEALTH, 300.0D)
                .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.23D)
                .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 10.0D)
                .add(EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE, 1.0D) // 满级击退抗性，防止卡死
                .add(EntityAttributes.GENERIC_FOLLOW_RANGE, 40.0D);
    }

    @Override
    protected void initGoals() {
        this.goalSelector.add(1, new SwimGoal(this));
        this.goalSelector.add(2, new MingYuanAttackGoal(this));
        this.goalSelector.add(3, new WanderAroundFarGoal(this, 0.7D));
        this.goalSelector.add(4, new LookAtEntityGoal(this, PlayerEntity.class, 8.0F));
        this.targetSelector.add(1, new ActiveTargetGoal<>(this, PlayerEntity.class, true));
    }

    @Override
    public void tick() {
        super.tick();
        if (!this.getWorld().isClient) {
            // 天气感应：雨天抗性提升
            if (this.getWorld().isRaining()) {
                this.addStatusEffect(new StatusEffectInstance(StatusEffects.RESISTANCE, 100, 1, false, false));
            }
        }
    }

    @Override
    public void onDeath(DamageSource source) {
        super.onDeath(source);
        if (!this.getWorld().isClient && this.getWorld() instanceof ServerWorld serverWorld) {
            // 黑色粒子爆炸
            serverWorld.spawnParticles(ParticleTypes.LARGE_SMOKE, this.getX(), this.getY() + 1, this.getZ(), 30, 0.5, 0.5, 0.5, 0.02);
            serverWorld.spawnParticles(ParticleTypes.SQUID_INK, this.getX(), this.getY() + 1, this.getZ(), 20, 0.3, 0.3, 0.3, 0.1);
            this.playSound(SoundEvents.ENTITY_GENERIC_EXPLODE.value(), 1.0f, 1.0f);

            // 掉落
            this.dropItem(com.Primal.item.ModItems.SOUL_OF_MINGYUAN);
            this.dropItem(com.Primal.item.ModItems.ANCIENT_KEY);
        }
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "controller", 5, state -> {
            if (this.isDead()) return PlayState.STOP;
            if (this.dataTracker.get(IS_ATTACKING)) return state.setAndContinue(ATTACK);
            return state.setAndContinue(state.isMoving() ? WALK : WALK); // 暂时用 walk 代替 idle
        }));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() { return this.cache; }

    // =========================================================
    // 性能优化版 AI 攻击类
    // =========================================================
    static class MingYuanAttackGoal extends Goal {
        private final MingYuanEntity boss;
        private int timer = 0;

        public MingYuanAttackGoal(MingYuanEntity boss) { this.boss = boss; }

        @Override
        public boolean canStart() {
            return boss.getTarget() != null && boss.getTarget().isAlive();
        }

        @Override
        public void start() {
            boss.dataTracker.set(IS_ATTACKING, true);
            this.timer = 0;
        }

        @Override
        public void stop() {
            boss.dataTracker.set(IS_ATTACKING, false);
        }

        @Override
        public void tick() {
            LivingEntity target = boss.getTarget();
            if (target == null) return;

            boss.getLookControl().lookAt(target, 30, 30);

            timer++;
            // 将频率降低，给电脑喘息空间
            if (timer == 30) { // 1.5秒发第一弹
                shootProjectile(target, 1);
            } else if (timer == 60) { // 3秒发第二弹
                shootProjectile(target, 2);
            } else if (timer == 90) { // 4.5秒发第三弹
                shootProjectile(target, 3);
                timer = -10; // 进入 2 秒的休息期
            }
        }

        /**
         * 统一发射逻辑，加入了【发射点位移】防止自撞卡死
         */
        private void shootProjectile(LivingEntity target, int type) {
            if (boss.getWorld().isClient) return;

            // 【关键修复】计算发射点：在 Boss 眼睛位置 加上 准星方向 * 2.5 格偏移
            // 这样子弹会直接生成在 Boss 身体外，不会发生自撞死循环
            Vec3d lookVec = boss.getRotationVec(1.0f);
            double spawnX = boss.getX() + lookVec.x * 2.5;
            double spawnY = boss.getEyeY() + lookVec.y * 2.5;
            double spawnZ = boss.getZ() + lookVec.z * 2.5;

            Vec3d velocity = new Vec3d(target.getX() - spawnX,
                    target.getBodyY(0.5) - spawnY,
                    target.getZ() - spawnZ).normalize();

            if (type == 1) { // 雪球
                SnowballEntity ball = new SnowballEntity(boss.getWorld(), boss);
                ball.setPosition(spawnX, spawnY, spawnZ);
                ball.setVelocity(velocity.x, velocity.y, velocity.z, 1.2f, 1.0f);
                boss.getWorld().spawnEntity(ball);
                target.damage(boss.getWorld().getDamageSources().freeze(), 2.0f);
            }
            else if (type == 2) { // 火球
                SmallFireballEntity f = new SmallFireballEntity(boss.getWorld(), spawnX, spawnY, spawnZ, velocity);
                f.setOwner(boss);
                boss.getWorld().spawnEntity(f);
            }
            else if (type == 3) { // 模拟风弹 (暂时使用简单伤害 + 击退代替，最安全)
                target.addStatusEffect(new StatusEffectInstance(StatusEffects.BLINDNESS, 20, 0));
                target.takeKnockback(1.5, -lookVec.x, -lookVec.z);
                target.damage(boss.getWorld().getDamageSources().magic(), 1.0f);

                // 仅在此时播放一个风声音
                boss.getWorld().playSound(null, boss.getBlockPos(), SoundEvents.ENTITY_BREEZE_SHOOT, SoundCategory.HOSTILE, 1.0f, 1.0f);
            }

            boss.swingHand(Hand.MAIN_HAND);
        }
    }
}