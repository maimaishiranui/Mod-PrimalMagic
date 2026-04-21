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

    // 添加近身计时器变量
    private int proximityTicks = 0;

    // 数据同步：用于控制动画切换
    private static final TrackedData<Boolean> IS_ATTACKING = DataTracker.registerData(MingYuanEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
    private static final TrackedData<Integer> ATTACK_ID = DataTracker.registerData(MingYuanEntity.class, TrackedDataHandlerRegistry.INTEGER);

    // 【动画定义】仅包含你 JSON 中存在的三个名字
    private static final RawAnimation WALK_ANIM = RawAnimation.begin().thenLoop("walk");
    private static final RawAnimation ATTACK_1 = RawAnimation.begin().thenPlay("attack1");
    private static final RawAnimation ATTACK_2 = RawAnimation.begin().thenPlay("attack2");

    public MingYuanEntity(EntityType<? extends HostileEntity> entityType, World world) {
        super(entityType, world);
    }

    @Override
    protected void initDataTracker(DataTracker.Builder builder) {
        super.initDataTracker(builder);
        builder.add(IS_ATTACKING, false);
        builder.add(ATTACK_ID, 1);
    }

    public static DefaultAttributeContainer.Builder setAttributes() {
        return HostileEntity.createHostileAttributes()
                .add(EntityAttributes.GENERIC_MAX_HEALTH, 300.0D)
                .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.4D) // 略微调高速度，更容易触发 walk 动画
                .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 10.0D)
                .add(EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE, 1.0D)
                .add(EntityAttributes.GENERIC_FOLLOW_RANGE, 40.0D);
    }

    @Override
    protected void initGoals() {
        this.goalSelector.add(1, new SwimGoal(this));
        this.goalSelector.add(2, new MingYuanAttackGoal(this));
        this.goalSelector.add(3, new WanderAroundFarGoal(this, 0.8D));
        this.goalSelector.add(4, new LookAtEntityGoal(this, PlayerEntity.class, 8.0F));
        this.targetSelector.add(1, new ActiveTargetGoal<>(this, PlayerEntity.class, true));
    }

    // 核心修改：让 Boss 免疫火焰、熔岩和火球伤害
    @Override
    public boolean isInvulnerableTo(DamageSource damageSource) {
        if (damageSource.isIn(net.minecraft.registry.tag.DamageTypeTags.IS_FIRE)) {
            return true;
        }
        return super.isInvulnerableTo(damageSource);
    }


    @Override
    public void tick() {
        super.tick();
        // 逻辑仅在服务器端运行
        if (this.getWorld().isClient) return;

        // --- 1. 天气感性 (保持原有) ---
        if (this.getWorld().isRaining()) {
            this.addStatusEffect(new StatusEffectInstance(StatusEffects.RESISTANCE, 40, 1, false, false));
        }

        // --- 2. 核心：近身监测 (3格半径) ---
        PlayerEntity nearbyPlayer = this.getWorld().getClosestPlayer(this, 3.0);
        if (nearbyPlayer != null && nearbyPlayer.isAlive() && !nearbyPlayer.isCreative()) {
            proximityTicks++;

            // [2秒判定 = 40刻] 屏幕摇晃 (用反胃效果模拟)
            if (proximityTicks >= 40) {
                nearbyPlayer.addStatusEffect(new StatusEffectInstance(StatusEffects.NAUSEA, 60, 0, false, false, false));
                // 待得越久，效果越强 (超过4秒开始黑暗)
                if (proximityTicks > 80) {
                    nearbyPlayer.addStatusEffect(new StatusEffectInstance(StatusEffects.DARKNESS, 40, 0, false, false, false));
                }
            }

            // [5秒判定 = 100刻] 黑暗与漂浮
            if (proximityTicks >= 100) {
                nearbyPlayer.addStatusEffect(new StatusEffectInstance(StatusEffects.DARKNESS, 100, 0));
                nearbyPlayer.addStatusEffect(new StatusEffectInstance(StatusEffects.LEVITATION, 40, 1)); // 漂浮2秒
                proximityTicks = 0; // 触发后重置
            }
        } else {
            proximityTicks = Math.max(0, proximityTicks - 1); // 玩家离开后计时器慢慢减回去
        }

        // --- 3. 核心：低血量强制击退 (HP < 30%) ---
        if (this.getHealth() / this.getMaxHealth() < 0.3f) {
            // 寻找 3格 内的玩家并弹开
            this.getWorld().getEntitiesByClass(PlayerEntity.class, this.getBoundingBox().expand(3.0), p -> !p.isCreative())
                    .forEach(p -> {
                        double dx = p.getX() - this.getX();
                        double dz = p.getZ() - this.getZ();
                        p.takeKnockback(2.0, -dx, -dz); // 强大的击退力
                        p.velocityModified = true;
                        this.playSound(SoundEvents.ENTITY_BREEZE_WIND_BURST.value(), 1.0f, 0.5f);
                    });
        }
    }

    // --- 死亡特效：黑紫粒子螺旋爆炸 ---
    @Override
    public void onDeath(DamageSource source) {
        super.onDeath(source);
        if (!this.getWorld().isClient && this.getWorld() instanceof ServerWorld serverWorld) {
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
            this.playSound(SoundEvents.ENTITY_GENERIC_EXPLODE.value(), 1.0f, 0.8f);
            this.dropItem(com.Primal.item.ModItems.SOUL_OF_MINGYUAN);
            this.dropItem(com.Primal.item.ModItems.ANCIENT_KEY);
        }
    }

    // --- 核心动画：动画控制器逻辑 ---
    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "controller", 2, state -> {
            if (this.isDead()) return software.bernie.geckolib.animation.PlayState.STOP;

            // 【优先判断移动】只要有位移，就播走路动画，解决“滑行”问题
            if (state.getLimbSwingAmount() > 0.05F || state.isMoving()) {
                return state.setAndContinue(WALK_ANIM);
            }

            // 【其次判断攻击】只有在不走路且攻击时，播攻击动画
            if (this.dataTracker.get(IS_ATTACKING)) {
                return state.setAndContinue(this.dataTracker.get(ATTACK_ID) == 2 ? ATTACK_2 : ATTACK_1);
            }

            return software.bernie.geckolib.animation.PlayState.STOP;
        }));
    }


    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }

    // --- AI 类：随机切换动作版 ---
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
            // 动作开始
            boss.dataTracker.set(IS_ATTACKING, true);
            boss.dataTracker.set(ATTACK_ID, boss.getWorld().random.nextInt(2) + 1);
            this.timer = 0;
        }

        @Override
        public void stop() {
            // 动作结束
            boss.dataTracker.set(IS_ATTACKING, false);
            this.timer = 0;
        }

        @Override
        public void tick() {
            LivingEntity target = boss.getTarget();
            if (target == null) return;
            boss.getLookControl().lookAt(target, 30, 30);

            // 如果距离玩家太远(>6格)，先走过去，不强行播放攻击动画
            if (boss.distanceTo(target) > 6.0) {
                boss.dataTracker.set(IS_ATTACKING, false);
            } else {
                boss.dataTracker.set(IS_ATTACKING, true);
            }
            timer++;

            if (timer == 30) shoot(target, 1);
            else if (timer == 60) shoot(target, 2);
            else if (timer == 90) {
                shoot(target, 3);
                timer = -10;
                // 每一轮结束后随机下一次动作
                boss.dataTracker.set(ATTACK_ID, boss.getWorld().random.nextInt(2) + 1);
            }
        }

        private void shoot(LivingEntity target, int type) {
            if (boss.getWorld().isClient) return;
            Vec3d look = boss.getRotationVec(1.0f);
            double sx = boss.getX() + look.x * 2.5; // 发射点位移
            double sy = boss.getEyeY() + look.y * 2.5;
            double sz = boss.getZ() + look.z * 2.5;
            Vec3d v = new Vec3d(target.getX() - sx, target.getBodyY(0.5) - sy, target.getZ() - sz).normalize();

            if (type == 1) { // 雪球
                SnowballEntity b = new SnowballEntity(boss.getWorld(), boss);
                b.setPosition(sx, sy, sz);
                b.setVelocity(v.x, v.y, v.z, 1.3f, 0);
                boss.getWorld().spawnEntity(b);
                target.damage(boss.getWorld().getDamageSources().freeze(), 2.0f);
            } else if (type == 2) { // 火球
                SmallFireballEntity f = new SmallFireballEntity(boss.getWorld(), sx, sy, sz, v);
                f.setOwner(boss);
                boss.getWorld().spawnEntity(f);
            } else { // 风弹
                target.addStatusEffect(new StatusEffectInstance(StatusEffects.BLINDNESS, 20, 0));
                target.takeKnockback(1.0, -look.x, -look.z);
                target.damage(boss.getWorld().getDamageSources().magic(), 1.0f);
            }
            boss.swingHand(Hand.MAIN_HAND);
        }
    }
}

