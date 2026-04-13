package com.Primal.entity;

import com.Primal.component.ModDataComponentTypes;
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
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.animation.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.List;

public class MingYuanEntity extends HostileEntity implements GeoEntity {
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    // --- 数据追踪器：用于同步动画状态 ---
    private static final TrackedData<Boolean> IS_ATTACKING = DataTracker.registerData(MingYuanEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
    private static final TrackedData<Integer> ATTACK_TYPE = DataTracker.registerData(MingYuanEntity.class, TrackedDataHandlerRegistry.INTEGER);
    private static final TrackedData<Boolean> IS_WARNING = DataTracker.registerData(MingYuanEntity.class, TrackedDataHandlerRegistry.BOOLEAN);

    // --- 动画定义 (需匹配 JSON 名) ---
    private static final RawAnimation WALK = RawAnimation.begin().thenLoop("walk");
    private static final RawAnimation ATTACK_1 = RawAnimation.begin().thenPlay("attack1");
    private static final RawAnimation ATTACK_2 = RawAnimation.begin().thenPlay("attack2");
    // 暂用 walk 代替 warning，如果你 JSON 补了 warning 请修改此处
    private static final RawAnimation WARNING_ANIM = RawAnimation.begin().thenLoop("walk");

    public MingYuanEntity(EntityType<? extends HostileEntity> entityType, World world) {
        super(entityType, world);
    }

    @Override
    protected void initDataTracker(DataTracker.Builder builder) {
        super.initDataTracker(builder);
        builder.add(IS_ATTACKING, false);
        builder.add(ATTACK_TYPE, 0);
        builder.add(IS_WARNING, false);
    }

    public static DefaultAttributeContainer.Builder setAttributes() {
        return HostileEntity.createHostileAttributes()
                .add(EntityAttributes.GENERIC_MAX_HEALTH, 300.0D)
                .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.23D)
                .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 10.0D)
                .add(EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE, 1.0D)
                .add(EntityAttributes.GENERIC_FOLLOW_RANGE, 40.0D);
    }

    @Override
    protected void initGoals() {
        this.goalSelector.add(1, new SwimGoal(this));
        this.goalSelector.add(2, new MingYuanAttackGoal(this));
        this.goalSelector.add(3, new WanderAroundFarGoal(this, 0.7D));
        this.goalSelector.add(4, new LookAtEntityGoal(this, PlayerEntity.class, 8.0F));
        this.targetSelector.add(1, new ActiveTargetGoal<>(this, PlayerEntity.class, true));
        this.targetSelector.add(2, new RevengeGoal(this));
    }

    @Override
    public void tick() {
        super.tick();
        if (!this.getWorld().isClient) {
            // 1. 天气感应：雨天抗性 II
            if (this.getWorld().isRaining()) {
                this.addStatusEffect(new StatusEffectInstance(StatusEffects.RESISTANCE, 40, 1, false, false));
            }

            // 2. 警戒逻辑判断 (30格范围内有生存玩家)
            PlayerEntity closestPlayer = this.getWorld().getClosestPlayer(this, 30.0);
            this.dataTracker.set(IS_WARNING, closestPlayer != null && this.getTarget() == null);
        }
    }

    @Override
    public void onDeath(DamageSource source) {
        super.onDeath(source);
        if (!this.getWorld().isClient && this.getWorld() instanceof ServerWorld serverWorld) {
            // 黑色紫色螺旋 + 爆炸特效
            for (int i = 0; i < 100; i++) {
                double angle = i * 0.3;
                double radius = i * 0.02;
                double px = this.getX() + Math.cos(angle) * radius;
                double pz = this.getZ() + Math.sin(angle) * radius;
                double py = this.getY() + (i * 0.03);
                serverWorld.spawnParticles(ParticleTypes.PORTAL, px, py, pz, 1, 0, 0, 0, 0.1);
                if (i % 2 == 0) serverWorld.spawnParticles(ParticleTypes.SQUID_INK, px, py, pz, 1, 0, 0, 0, 0.02);
            }
            serverWorld.spawnParticles(ParticleTypes.LARGE_SMOKE, this.getX(), this.getY() + 1.0, this.getZ(), 40, 0.5, 0.5, 0.5, 0.1);
            serverWorld.spawnParticles(ParticleTypes.DRAGON_BREATH, this.getX(), this.getY() + 1.0, this.getZ(), 30, 0.3, 0.3, 0.3, 0.05);

            this.playSound(SoundEvents.ENTITY_ENDERMAN_TELEPORT, 1.5f, 0.5f);
            this.playSound(SoundEvents.ENTITY_GENERIC_EXPLODE.value(), 1.0f, 0.8f);

            this.dropItem(com.Primal.item.ModItems.SOUL_OF_MINGYUAN);
            this.dropItem(com.Primal.item.ModItems.ANCIENT_KEY);
        }
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "controller", 4, state -> {
            if (this.isDead()) return PlayState.STOP;

            // 1. 攻击状态
            if (this.dataTracker.get(IS_ATTACKING)) {
                int type = this.dataTracker.get(ATTACK_TYPE);
                return state.setAndContinue(type == 2 ? ATTACK_2 : ATTACK_1);
            }

            // 2. 行走状态 (高灵敏判定)
            if (state.getLimbSwingAmount() > 0.01F || state.isMoving()) {
                return state.setAndContinue(WALK);
            }

            // 3. 警戒状态
            if (this.dataTracker.get(IS_WARNING)) {
                return state.setAndContinue(WARNING_ANIM);
            }

            return state.setAndContinue(WALK); // 默认循环
        }));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() { return this.cache; }

    // =========================================================
    // AI 攻击类：三连发射击
    // =========================================================
    static class MingYuanAttackGoal extends Goal {
        private final MingYuanEntity boss;
        private int timer = 0;

        public MingYuanAttackGoal(MingYuanEntity boss) { this.boss = boss; }

        @Override
        public boolean canStart() { return boss.getTarget() != null && boss.getTarget().isAlive(); }

        @Override
        public void start() {
            boss.dataTracker.set(IS_ATTACKING, true);
            boss.dataTracker.set(ATTACK_TYPE, boss.getWorld().random.nextInt(2) + 1);
            this.timer = 0;
        }

        @Override
        public void stop() {
            boss.dataTracker.set(IS_ATTACKING, false);
            boss.dataTracker.set(ATTACK_TYPE, 0);
        }

        @Override
        public void tick() {
            LivingEntity target = boss.getTarget();
            if (target == null) return;
            boss.getLookControl().lookAt(target, 30, 30);

            timer++;
            if (timer == 30) shootProjectile(target, 1);      // 1.5s 雪球
            else if (timer == 60) shootProjectile(target, 2); // 3.0s 火球
            else if (timer == 90) {
                shootProjectile(target, 3);                   // 4.5s 风弹
                timer = -10;
                boss.dataTracker.set(ATTACK_TYPE, boss.getWorld().random.nextInt(2) + 1);
            }
        }

        private void shootProjectile(LivingEntity target, int type) {
            if (boss.getWorld().isClient) return;
            ServerWorld world = (ServerWorld) boss.getWorld();

            // 发射点偏移 2.5 格，防止自撞卡死
            Vec3d lookVec = boss.getRotationVec(1.0f);
            double sx = boss.getX() + lookVec.x * 2.5;
            double sy = boss.getEyeY() + lookVec.y * 2.5;
            double sz = boss.getZ() + lookVec.z * 2.5;
            Vec3d vel = new Vec3d(target.getX() - sx, target.getBodyY(0.5) - sy, target.getZ() - sz).normalize();

            if (type == 1) { // 冰霜雪球
                SnowballEntity ball = new SnowballEntity(boss.getWorld(), boss);
                ball.setPosition(sx, sy, sz);
                ball.setVelocity(vel.x, vel.y, vel.z, 1.3f, 0);
                boss.getWorld().spawnEntity(ball);
                target.damage(boss.getWorld().getDamageSources().freeze(), 2.0f);
                world.spawnParticles(ParticleTypes.SNOWFLAKE, sx, sy, sz, 10, 0.2, 0.2, 0.2, 0.05);
            }
            else if (type == 2) { // 烈焰弹
                SmallFireballEntity f = new SmallFireballEntity(boss.getWorld(), sx, sy, sz, vel);
                f.setOwner(boss);
                boss.getWorld().spawnEntity(f);
                world.spawnParticles(ParticleTypes.FLAME, sx, sy, sz, 15, 0.3, 0.3, 0.3, 0.05);
            }
            else if (type == 3) { // 虚空风弹
                target.addStatusEffect(new StatusEffectInstance(StatusEffects.BLINDNESS, 20, 0));
                target.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 40, 1));
                target.addStatusEffect(new StatusEffectInstance(StatusEffects.WEAKNESS, 20, 0));
                target.damage(boss.getWorld().getDamageSources().magic(), 1.0f);
                target.takeKnockback(1.5, -lookVec.x, -lookVec.z);
                world.spawnParticles(ParticleTypes.GUST, sx, sy, sz, 2, 0.2, 0.2, 0.2, 0.01);
                world.playSound(null, boss.getBlockPos(), SoundEvents.ENTITY_BREEZE_SHOOT, SoundCategory.HOSTILE, 1.0f, 1.0f);
            }
            boss.swingHand(Hand.MAIN_HAND);
        }
    }
}