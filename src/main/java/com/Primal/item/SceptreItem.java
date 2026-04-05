package com.Primal.item;

import com.Primal.component.ModDataComponentTypes;
import net.minecraft.block.Blocks;
import net.minecraft.component.ComponentType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.thrown.SnowballEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.UseAction;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class SceptreItem extends Item {
    // 追踪实体被雪球击中的时间 (仅在服务端使用)
    private static final Map<UUID, Integer> SNOWBALL_HIT_TRACKER = new HashMap<>();

    public SceptreItem(Settings settings) {
        super(settings);
    }

    @Override
    public boolean canRepair(ItemStack stack, ItemStack ingredient) {
        return false;
    }

    // --- 使得法杖可以像弓一样长按 ---
    @Override
    public UseAction getUseAction(ItemStack stack) {
        return UseAction.BOW;
    }

    @Override
    public int getMaxUseTime(ItemStack stack, LivingEntity user) {
        return 72000;
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);
        if (world.isClient) return TypedActionResult.pass(stack);

        ServerWorld serverWorld = (ServerWorld) world;
        String boundMagic = stack.get(ModDataComponentTypes.BOUND_MAGIC);
        String talisman = stack.get(ModDataComponentTypes.TALISMAN_TYPE);
        long currentTime = world.getTime();

        // ---------------------------------------------------------
        // 1. 护符逻辑 (优先级最高)
        // ---------------------------------------------------------
        if (talisman != null) {
            // --- Frozen ---
            if ("Frozen".equals(talisman)) {
                if (user.isSneaking()) {
                    if (isSkillReady(stack, ModDataComponentTypes.SKILL_2_LAST_USE, currentTime, 360)) {
                        stack.set(ModDataComponentTypes.BARRAGE_TICKS_LEFT, 70);
                        stack.set(ModDataComponentTypes.SKILL_2_LAST_USE, currentTime);
                        user.addStatusEffect(new StatusEffectInstance(StatusEffects.SPEED, 100, 2, false, false, true));
                        return TypedActionResult.consume(stack);
                    }
                } else {
                    if (isSkillReady(stack, ModDataComponentTypes.SKILL_1_LAST_USE, currentTime, 300)) {
                        user.setCurrentHand(hand);
                        user.addStatusEffect(new StatusEffectInstance(StatusEffects.SPEED, 120, 2, false, false, true));
                        return TypedActionResult.consume(stack);
                    }
                }
            }
            // --- 烈焰护符 (Flame)  ---
            if ("Flame".equals(talisman)) {
                if (user.isSneaking()) {
                    // --- 技能 2：涅槃爆发 (CD 28s) ---
                    if (isSkillReady(stack, ModDataComponentTypes.SKILL_2_LAST_USE, currentTime, 560)) {
                        // 1. 恢复最大生命值的 35%
                        user.heal(user.getMaxHealth() * 0.35f);
                        // 2. 获得力量 3 和 迅捷 3 (15s)
                        user.addStatusEffect(new StatusEffectInstance(StatusEffects.STRENGTH, 300, 2));
                        user.addStatusEffect(new StatusEffectInstance(StatusEffects.SPEED, 300, 2));
                        // 3. 开启 15s 强化状态，重置杀敌计
                        stack.set(ModDataComponentTypes.FLAME_BARRAGE_TICKS, 300);
                        stack.set(ModDataComponentTypes.FLAME_KILL_COUNT, 0);

                        stack.set(ModDataComponentTypes.SKILL_2_LAST_USE, currentTime);
                        world.playSound(null, user.getBlockPos(), SoundEvents.ENTITY_GENERIC_EXPLODE.value(), SoundCategory.PLAYERS, 1.0f, 2.0f);
                        return TypedActionResult.success(stack);
                    } else {
                        sendCooldownMessage(user, "涅槃爆发 冷却中...");
                    }
                } else {
                    // --- 技能 1：烈焰迸发 (CD 18s) ---
                    if (isSkillReady(stack, ModDataComponentTypes.SKILL_1_LAST_USE, currentTime, 360)) {
                        // 1. 扣除当前血量的 25% (保留至少 1 点血，防止自杀)
                        float damageAmount = user.getHealth() * 0.25f;
                        user.damage(world.getDamageSources().magic(), Math.max(damageAmount, 0.5f));

                        // 2. 获得抗性 3 (15s) 和 生命恢复 2 (5s)
                        user.addStatusEffect(new StatusEffectInstance(StatusEffects.RESISTANCE, 300, 2));
                        user.addStatusEffect(new StatusEffectInstance(StatusEffects.REGENERATION, 100, 1));

                        // 3. 执行扇形攻击 (10伤害)
                        executeFlameSkill1(serverWorld, user); // 内部伤害需改为 10.0f

                        // 4. 开启 10s 旋转火环
                        stack.set(ModDataComponentTypes.FLAME_AURA_TICKS, 200);

                        stack.set(ModDataComponentTypes.SKILL_1_LAST_USE, currentTime);
                        return TypedActionResult.success(stack);
                    } else {
                        sendCooldownMessage(user, "烈焰迸发 冷却中...");
                    }
                }
            }

            // --- Breeze ---
            else if ("Breeze".equals(talisman)) {
                if (user.isSneaking()) {
                    if (isSkillReady(stack, ModDataComponentTypes.SKILL_2_LAST_USE, currentTime, 360)) {
                        stack.set(ModDataComponentTypes.BARRAGE_TICKS_LEFT, 120);
                        stack.set(ModDataComponentTypes.SKILL_2_POS_X, user.getX());
                        stack.set(ModDataComponentTypes.SKILL_2_POS_Y, user.getY());
                        stack.set(ModDataComponentTypes.SKILL_2_POS_Z, user.getZ());
                        stack.set(ModDataComponentTypes.SKILL_2_LAST_USE, currentTime);
                        user.addStatusEffect(new StatusEffectInstance(StatusEffects.SPEED, 100, 2, false, false, true));
                        return TypedActionResult.consume(stack);
                    }
                } else {
                    if (isSkillReady(stack, ModDataComponentTypes.SKILL_1_LAST_USE, currentTime, 200)) {
                        executeBreezeWindBolt(serverWorld, user);
                        stack.set(ModDataComponentTypes.SKILL_1_LAST_USE, currentTime);
                        user.addStatusEffect(new StatusEffectInstance(StatusEffects.SPEED, 100, 2, false, false, true));
                    }
                }
                return TypedActionResult.success(stack);
            }
            // --- Stream ---
            else if ("Stream".equals(talisman)) {
                if (user.isSneaking()) {
                    if (isSkillReady(stack, ModDataComponentTypes.SKILL_2_LAST_USE, currentTime, 440)) {
                        stack.set(ModDataComponentTypes.BARRAGE_TICKS_LEFT, 100);
                        stack.set(ModDataComponentTypes.SKILL_2_LAST_USE, currentTime);
                        user.addStatusEffect(new StatusEffectInstance(StatusEffects.SPEED, 100, 2, false, false, true));
                        return TypedActionResult.success(stack);
                    }
                } else {
                    if (isSkillReady(stack, ModDataComponentTypes.SKILL_1_LAST_USE, currentTime, 160)) {
                        user.setCurrentHand(hand);
                        user.addStatusEffect(new StatusEffectInstance(StatusEffects.SPEED, 160, 2, false, false, true));
                        return TypedActionResult.consume(stack);
                    }
                }
            }
            // 如果有护符但不是上述任何一种，不继续往下走
            return TypedActionResult.pass(stack);
        }

        // ---------------------------------------------------------
        // 2. 基础幻化逻辑 (当没有绑定护符时触发)
        // ---------------------------------------------------------
        if ("Illusionary".equals(boundMagic)) {
            if (isSkillReady(stack, ModDataComponentTypes.SKILL_1_LAST_USE, currentTime, 200)) { // 10s CD
                executeBasicIllusionary(serverWorld, user, stack, hand);
                stack.set(ModDataComponentTypes.SKILL_1_LAST_USE, currentTime);

                // 【核心修复】同时也给基础技能增加 5 秒速度 III
                user.addStatusEffect(new StatusEffectInstance(StatusEffects.SPEED, 100, 2, false, false, true));

                return TypedActionResult.success(stack);
            } else {
                sendCooldownMessage(user, "幻化之波 冷却中...");
            }
        }

        return TypedActionResult.pass(stack);
    }
    // =========================================================
    // 持续释放与计时逻辑 (新增)
    // =========================================================

    // 处理射线长按 (Skill 1)
    @Override
    public void usageTick(World world, LivingEntity user, ItemStack stack, int remainingUseTicks) {
        if (world.isClient || !(user instanceof PlayerEntity player)) return;

        int usedTicks = getMaxUseTime(stack, user) - remainingUseTicks;
        // 射线最大持续 8 秒 (160刻)
        if (usedTicks > 160) {
            player.stopUsingItem();
            return;
        }

        String talisman = stack.get(ModDataComponentTypes.TALISMAN_TYPE);

        // --- 修复点：在这里添加源流射线的执行判定 ---
        if ("Frozen".equals(talisman)) {
            executeFrozenBeam(world, player, stack);
        }
        else if ("Stream".equals(talisman)) {
            // 调用你之前写的 executeStreamBeam 方法
            executeStreamBeam(world, player, stack, usedTicks);
        }
    }

    @Override
    public void onStoppedUsing(ItemStack stack, World world, LivingEntity user, int remainingUseTicks) {
        if (!world.isClient) {
            // 停止使用时进入 CD
            stack.set(ModDataComponentTypes.SKILL_1_LAST_USE, world.getTime());
            stack.remove(ModDataComponentTypes.BEAM_HIT_TICKS); // 清除命中记录
        }
    }

    // 处理计时

    @Override
    public void inventoryTick(ItemStack stack, World world, Entity entity, int slot, boolean selected) {
        // 核心安全检查：仅在服务端运行，且使用者必须是玩家
        if (world.isClient || !(entity instanceof PlayerEntity player)) return;

        String talisman = stack.get(com.Primal.component.ModDataComponentTypes.TALISMAN_TYPE);

        // =========================================================
        // 1. 常驻被动逻辑 (只要手持法杖就生效)
        // =========================================================
        if (selected && talisman != null) {
            // --- Stream (源流) 被动：夜视与水肺 ---
            if ("Stream".equals(talisman)) {
                player.addStatusEffect(new StatusEffectInstance(StatusEffects.NIGHT_VISION, 210, 0, false, false, false));
                player.addStatusEffect(new StatusEffectInstance(StatusEffects.WATER_BREATHING, 210, 0, false, false, false));
            }
        }

        // =========================================================
        // 2. 持续性技能逻辑 (处理各种持续时间组件)
        // =========================================================

        // --- Flame (烈焰) 技能 1：旋转火环逻辑 ---
        if (stack.contains(com.Primal.component.ModDataComponentTypes.FLAME_AURA_TICKS)) {
            int auraTicks = stack.get(com.Primal.component.ModDataComponentTypes.FLAME_AURA_TICKS);
            if (auraTicks > 0 && selected) { // 只有手持时才显示火环
                executeFlameAura(world, player, auraTicks);
                stack.set(com.Primal.component.ModDataComponentTypes.FLAME_AURA_TICKS, auraTicks - 1);
            } else if (auraTicks <= 0) {
                stack.remove(com.Primal.component.ModDataComponentTypes.FLAME_AURA_TICKS);
            }
        }

        // --- Flame (烈焰) 技能 2：涅槃强化状态计时 ---
        if (stack.contains(com.Primal.component.ModDataComponentTypes.FLAME_BARRAGE_TICKS)) {
            int barrageTicks = stack.get(com.Primal.component.ModDataComponentTypes.FLAME_BARRAGE_TICKS);
            if (barrageTicks > 0) {
                // 强化状态下产生的粒子反馈 (脚下冒火)
                if (world.getTime() % 5 == 0) {
                    ((ServerWorld)world).spawnParticles(net.minecraft.particle.ParticleTypes.FLAME, player.getX(), player.getY(), player.getZ(), 5, 0.2, 0.1, 0.2, 0.02);
                }
                stack.set(com.Primal.component.ModDataComponentTypes.FLAME_BARRAGE_TICKS, barrageTicks - 1);
            } else {
                stack.remove(com.Primal.component.ModDataComponentTypes.FLAME_BARRAGE_TICKS);
                stack.remove(com.Primal.component.ModDataComponentTypes.FLAME_KILL_COUNT);
            }
        }

        // --- 其他护符通用持续计时 (Frozen / Breeze / Stream) ---
        if (stack.contains(com.Primal.component.ModDataComponentTypes.BARRAGE_TICKS_LEFT)) {
            int ticksLeft = stack.get(com.Primal.component.ModDataComponentTypes.BARRAGE_TICKS_LEFT);
            if (ticksLeft > 0 && selected) {

                // 分支：Frozen (冰霜重击射线)
                if ("Frozen".equals(talisman)) {
                    executeFrozenHeavyBeam(world, player, stack, ticksLeft);
                }
                // 分支：Breeze (大旋风黑洞)
                else if ("Breeze".equals(talisman)) {
                    double x = stack.getOrDefault(com.Primal.component.ModDataComponentTypes.SKILL_2_POS_X, player.getX());
                    double y = stack.getOrDefault(com.Primal.component.ModDataComponentTypes.SKILL_2_POS_Y, player.getY());
                    double z = stack.getOrDefault(com.Primal.component.ModDataComponentTypes.SKILL_2_POS_Z, player.getZ());
                    executeVortexLogic(world, player, new Vec3d(x, y, z), 15.0, 3.0f, true, ticksLeft);
                }
                // 分支：Stream (源流领域吸血)
                else if ("Stream".equals(talisman)) {
                    executeStreamAOE(world, player, stack, ticksLeft);
                }

                stack.set(com.Primal.component.ModDataComponentTypes.BARRAGE_TICKS_LEFT, ticksLeft - 1);
            } else {
                // 时间结束，清理所有相关组件
                stack.remove(com.Primal.component.ModDataComponentTypes.BARRAGE_TICKS_LEFT);
                if ("Stream".equals(talisman)) stack.remove(com.Primal.component.ModDataComponentTypes.STREAM_ABSORB_LAYERS);
                if ("Breeze".equals(talisman)) {
                    stack.remove(com.Primal.component.ModDataComponentTypes.SKILL_2_POS_X);
                    stack.remove(com.Primal.component.ModDataComponentTypes.SKILL_2_POS_Y);
                    stack.remove(com.Primal.component.ModDataComponentTypes.SKILL_2_POS_Z);
                }
            }
        }
    }

    // =========================================================
    // 冰霜技能实现细节 (新增)
    // =========================================================

    private void executeFrozenBeam(World world, PlayerEntity user, ItemStack stack) {
        ServerWorld serverWorld = (ServerWorld) world;
        Vec3d start = user.getEyePos();
        Vec3d dir = user.getRotationVec(1.0f);
        Vec3d end = start.add(dir.multiply(8.0));

        // 1. 亮蓝色射线粒子
        for (double d = 0; d < 8.0; d += 0.5) {
            Vec3d pos = start.add(dir.multiply(d));
            serverWorld.spawnParticles(ParticleTypes.SOUL_FIRE_FLAME, pos.x, pos.y, pos.z, 1, 0, 0, 0, 0);
        }

        // 2. 射线检测
        EntityHitResult hit = net.minecraft.entity.projectile.ProjectileUtil.raycast(user, start, end, user.getBoundingBox().stretch(dir.multiply(8.0)).expand(1.0), e -> e instanceof LivingEntity && e != user, 64.0);

        if (hit != null && hit.getEntity() instanceof LivingEntity target) {
            // 施加冻结状态
            target.setFrozenTicks(target.getFrozenTicks() + 15);

            // 伤害阶梯
            int hitCount = stack.getOrDefault(ModDataComponentTypes.BEAM_HIT_TICKS, 0) + 1;
            stack.set(ModDataComponentTypes.BEAM_HIT_TICKS, hitCount);

            float baseDamage = 2.0f;
            float extraDamage = (hitCount / 20); // 每秒+1
            float totalDamage = Math.min(baseDamage + extraDamage, 6.0f);

            target.damage(world.getDamageSources().freeze(), totalDamage / 5.0f); // 平滑每刻伤害

            if (world.getTime() % 10 == 0) {
                world.playSound(null, target.getBlockPos(), SoundEvents.BLOCK_POWDER_SNOW_STEP, SoundCategory.PLAYERS, 1f, 1.2f);
            }
        } else {
            stack.set(ModDataComponentTypes.BEAM_HIT_TICKS, 0);
        }
    }

    private void executeSnowballBarrage(World world, PlayerEntity user) {
        if (world.getTime() % 3 != 0) return; // 每3刻发一颗雪球

        SnowballEntity snowball = new SnowballEntity(world, user);
        snowball.setVelocity(user, user.getPitch(), user.getYaw(), 0.0f, 1.5f, 4.0f);
        world.spawnEntity(snowball);

        // 检测准星范围内的敌人 (模拟持续击中)
        Box checkArea = user.getBoundingBox().expand(8.0);
        List<LivingEntity> targets = world.getEntitiesByClass(LivingEntity.class, checkArea, e -> e != user && user.canSee(e));

        for (LivingEntity target : targets) {
            UUID id = target.getUuid();
            int hits = SNOWBALL_HIT_TRACKER.getOrDefault(id, 0) + 3;
            SNOWBALL_HIT_TRACKER.put(id, hits);

            if (hits >= 40) { // 持续2秒 (40刻)
                freezeInIceCage(world, target);
                SNOWBALL_HIT_TRACKER.remove(id);
            }
        }
    }

    private void executeFrozenHeavyBeam(World world, PlayerEntity user, ItemStack stack, int ticksLeft) {
        if (!(world instanceof ServerWorld serverWorld)) return;

        Vec3d start = user.getEyePos();
        Vec3d dir = user.getRotationVec(1.0f);
        Vec3d end = start.add(dir.multiply(10.0)); // 射程 10 格

        // 1. 视觉：双色复合射线 (蓝白交替)
        for (double d = 0; d < 10.0; d += 0.4) {
            Vec3d pos = start.add(dir.multiply(d));
            if ((int)(d * 10) % 2 == 0) {
                serverWorld.spawnParticles(ParticleTypes.SOUL_FIRE_FLAME, pos.x, pos.y, pos.z, 1, 0, 0, 0, 0);
            } else {
                serverWorld.spawnParticles(ParticleTypes.SNOWFLAKE, pos.x, pos.y, pos.z, 2, 0.05, 0.05, 0.05, 0.01);
            }
        }

        // 2. 射线碰撞检测
        EntityHitResult hit = net.minecraft.entity.projectile.ProjectileUtil.raycast(
                user, start, end, user.getBoundingBox().stretch(dir.multiply(10.0)).expand(1.0),
                e -> e instanceof LivingEntity && e != user, 100.0
        );

        if (hit != null && hit.getEntity() instanceof LivingEntity target) {
            // 每 0.5s (10刻) 造成 8 点伤害
            if (ticksLeft % 10 == 0) {
                target.damage(world.getDamageSources().freeze(), 8.0f);
                world.playSound(null, target.getBlockPos(), SoundEvents.BLOCK_GLASS_BREAK, SoundCategory.PLAYERS, 1.0f, 2.0f);

                // 冻结判定：利用已有的 SNOWBALL_HIT_TRACKER 记录命中
                UUID id = target.getUuid();
                int hits = SNOWBALL_HIT_TRACKER.getOrDefault(id, 0) + 1;
                SNOWBALL_HIT_TRACKER.put(id, hits);

                // 连续命中 2 次 (即照射 1 秒)
                if (hits >= 2) {
                    freezeInIceCage(world, target);
                    SNOWBALL_HIT_TRACKER.remove(id); // 成功后清除记录
                }
            }
        }
    }

    private void freezeInIceCage(World world, LivingEntity target) {
        if (world.isClient) return;
        BlockPos pos = target.getBlockPos();

        // 【核心修复】锁死实体位置：设置速度为0并施加超级减速
        target.setVelocity(Vec3d.ZERO);
        target.velocityDirty = true;
        // 施加 Slowness 200 (127级以上就无法移动)，持续 2 秒 (40刻)
        target.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 40, 200, false, false));

        // 在目标位置生成 3x3x3 冰笼
        for (int x = -1; x <= 1; x++) {
            for (int y = 0; y <= 2; y++) {
                for (int z = -1; z <= 1; z++) {
                    BlockPos p = pos.add(x, y, z);
                    // 仅填充空气，不破坏地形
                    if (world.getBlockState(p).isAir()) {
                        world.setBlockState(p, Blocks.ICE.getDefaultState());
                    }
                }
            }
        }

        // 附加负面效果
        target.addStatusEffect(new StatusEffectInstance(StatusEffects.MINING_FATIGUE, 40, 2)); // 挖掘疲劳 III
        target.addStatusEffect(new StatusEffectInstance(StatusEffects.WEAKNESS, 40, 1));        // 虚弱

        world.playSound(null, pos, SoundEvents.BLOCK_GLASS_BREAK, SoundCategory.BLOCKS, 1f, 0.5f);
    }
    // =========================================================


    //flame talisman的技能实现细节
    // --- 烈焰技能 1：火焰迸发 (120°扇形) ---
    private void executeFlameSkill1(ServerWorld world, PlayerEntity user) {
        Vec3d lookDir = user.getRotationVec(1.0F);
        // 产生大量火焰粒子
        for (int i = 0; i < 60; i++) {
            double angle = (world.random.nextDouble() - 0.5) * Math.toRadians(120);
            Vec3d particleDir = lookDir.rotateY((float) angle);
            world.spawnParticles(ParticleTypes.FLAME, user.getX(), user.getY() + 1.2, user.getZ(),
                    1, particleDir.x, 0.1, particleDir.z, 0.3);
        }
        // 伤害判定
        Box area = user.getBoundingBox().expand(3.5);
        List<LivingEntity> targets = world.getEntitiesByClass(LivingEntity.class, area, e -> e != user);
        for (LivingEntity target : targets) {
            Vec3d toTarget = target.getPos().subtract(user.getPos()).normalize();
            if (toTarget.dotProduct(lookDir) > 0.5 && user.distanceTo(target) <= 4.0) {
                target.damage(world.getDamageSources().onFire(), 8.0f);
                target.setOnFireFor(5);
            }
        }
        world.playSound(null, user.getBlockPos(), SoundEvents.ENTITY_GHAST_SHOOT, SoundCategory.PLAYERS, 1.0f, 1.0f);
    }

    @Override
    //处理左键火球和杀敌重置
    public boolean postHit(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        if (attacker.getWorld().isClient) return true;

        String talisman = stack.get(ModDataComponentTypes.TALISMAN_TYPE);

        // 只有在技能 2 持续期间
        if ("Flame".equals(talisman) && stack.contains(ModDataComponentTypes.FLAME_BARRAGE_TICKS)) {
            // 1. 发射火球 (直接调用技能 2 的 execute 代码)
            executeFlameSkill2((ServerWorld) attacker.getWorld(), (PlayerEntity) attacker, stack);

            // 2. 杀敌计数逻辑
            if (!target.isAlive() || target.getHealth() <= 0) {
                int kills = stack.getOrDefault(ModDataComponentTypes.FLAME_KILL_COUNT, 0) + 1;
                stack.set(ModDataComponentTypes.FLAME_KILL_COUNT, kills);

                // 如果杀敌满 3 个，重置大招 CD
                if (kills >= 3) {
                    stack.remove(ModDataComponentTypes.SKILL_2_LAST_USE);
                    stack.set(ModDataComponentTypes.FLAME_KILL_COUNT, 0); // 重置计数
                    attacker.getWorld().playSound(null, attacker.getBlockPos(), SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, SoundCategory.PLAYERS, 1.0f, 1.0f);
                }
            }
        }
        return true;
    }

    // --- 烈焰技能 2：远程火焰弹 (20格飞行 + 碰撞爆炸) ---
    public void executeFlameSkill2(ServerWorld world, PlayerEntity user, ItemStack sceptre) {
        Vec3d start = user.getEyePos();
        Vec3d direction = user.getRotationVec(1.0f);
        Vec3d end = start.add(direction.multiply(20.0)); // 最大射程 20 格

        // 1. 射线检测：寻找撞击点（方块或实体）
        // 检测方块
        net.minecraft.world.RaycastContext context = new net.minecraft.world.RaycastContext(
                start, end, net.minecraft.world.RaycastContext.ShapeType.COLLIDER,
                net.minecraft.world.RaycastContext.FluidHandling.NONE, user);
        net.minecraft.util.hit.BlockHitResult blockHit = world.raycast(context);

        Vec3d finalImpactPoint = blockHit.getPos();

        // 检测实体 (在起点到方块撞击点之间寻找生物)
        net.minecraft.util.hit.EntityHitResult entityHit = net.minecraft.entity.projectile.ProjectileUtil.raycast(
                user, start, finalImpactPoint,
                user.getBoundingBox().stretch(direction.multiply(20.0)).expand(1.0),
                (entity) -> entity instanceof LivingEntity && !entity.isSpectator() && entity.isAlive(),
                20.0 * 20.0
        );

        if (entityHit != null) {
            finalImpactPoint = entityHit.getPos();
        }

        // 2. 视觉特效：生成弹道粒子线 (让玩家看到“飞过去”的效果)
        double distance = start.distanceTo(finalImpactPoint);
        for (double d = 0; d < distance; d += 0.5) {
            Vec3d particlePos = start.add(direction.multiply(d));
            world.spawnParticles(ParticleTypes.FLAME, particlePos.x, particlePos.y, particlePos.z,
                    2, 0.05, 0.05, 0.05, 0.02);
            if (d % 2 == 0) { // 每 2 格加一个大烟雾点
                world.spawnParticles(ParticleTypes.LARGE_SMOKE, particlePos.x, particlePos.y, particlePos.z,
                        1, 0, 0, 0, 0);
            }
        }

        // 3. 爆炸效果：在撞击点触发 3x3 范围伤害
        world.spawnParticles(ParticleTypes.EXPLOSION_EMITTER, finalImpactPoint.x, finalImpactPoint.y, finalImpactPoint.z,
                1, 0, 0, 0, 0);
        world.playSound(null, user.getBlockX(), user.getBlockY(), user.getBlockZ(),
                SoundEvents.ENTITY_GENERIC_EXPLODE.value(), SoundCategory.PLAYERS, 1.0f, 1.2f);

        // 定义 3x3 的爆炸伤害范围 (半径 1.5)
        Box explosionArea = new Box(finalImpactPoint.subtract(1.5, 1.5, 1.5), finalImpactPoint.add(1.5, 1.5, 1.5));
        List<LivingEntity> targets = world.getEntitiesByClass(LivingEntity.class, explosionArea, e -> e != user);

        boolean killedSomeone = false;
        for (LivingEntity target : targets) {
            float hpBefore = target.getHealth();

            // 扣除 10 点血量 (5 颗心)
            target.damage(world.getDamageSources().explosion(user, user), 10.0f);
            target.setOnFireFor(5); // 附带灼烧

            // 检查是否击杀
            if (!target.isAlive() || (hpBefore > 0 && target.getHealth() <= 0)) {
                killedSomeone = true;
            }
        }

        // 4. 击杀奖励逻辑
        if (killedSomeone) {
            // 重置技能 1 (火焰迸发) 的冷却
            sceptre.remove(ModDataComponentTypes.SKILL_1_LAST_USE);
            // 恢复 2.5 血量
            user.heal(2.5f);
            // 成功反馈特效
            world.spawnParticles(ParticleTypes.HAPPY_VILLAGER, user.getX(), user.getY() + 2, user.getZ(),
                    15, 0.5, 0.5, 0.5, 0.1);
            world.playSound(null, user.getBlockPos(), SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, SoundCategory.PLAYERS, 1.0f, 1.0f);
        }
    }

    private void executeFlameAura(World world, PlayerEntity user, int tick) {
        if (!(world instanceof ServerWorld serverWorld)) return;

        double radius = 5.0;
        // 1秒转一圈：360度 / 20刻 = 18度每刻
        double angle = Math.toRadians(tick * 18.0);

        double px = user.getX() + Math.cos(angle) * radius;
        double pz = user.getZ() + Math.sin(angle) * radius;
        double py = user.getY() + 1.0;

        // 产生显眼的火焰粒子
        serverWorld.spawnParticles(ParticleTypes.FLAME, px, py, pz, 5, 0.1, 0.1, 0.1, 0.05);
        serverWorld.spawnParticles(ParticleTypes.LAVA, px, py, pz, 1, 0, 0, 0, 0);

        // 伤害逻辑
        Box area = new Box(px - 1.0, py - 1.0, pz - 1.0, px + 1.0, py + 1.0, pz + 1.0);
        List<LivingEntity> targets = world.getEntitiesByClass(LivingEntity.class, area, e -> e != user);
        for (LivingEntity target : targets) {
            target.damage(world.getDamageSources().onFire(), 1.5f);
        }
    }

    //--------------------------------------------------
    // --- Breeze 护符技能实现细节 (新增) ---
    // --- 技能 1：发射风弹并产生旋风 ---
    private void executeBreezeWindBolt(ServerWorld world, PlayerEntity user) {
        // 1. 发射视觉风弹
        net.minecraft.entity.projectile.WindChargeEntity windCharge = new net.minecraft.entity.projectile.WindChargeEntity(user, world, user.getX(), user.getEyeY(), user.getZ());
        windCharge.setVelocity(user, user.getPitch(), user.getYaw(), 0.0F, 1.5F, 1.0F);
        world.spawnEntity(windCharge);

        // 2. 射线预判落点 (射程 30 格)
        Vec3d start = user.getEyePos();
        Vec3d dir = user.getRotationVec(1.0f);
        Vec3d end = start.add(dir.multiply(30.0));

        net.minecraft.world.RaycastContext context = new net.minecraft.world.RaycastContext(
                start, end, net.minecraft.world.RaycastContext.ShapeType.COLLIDER,
                net.minecraft.world.RaycastContext.FluidHandling.NONE, user);
        net.minecraft.util.hit.BlockHitResult blockHit = world.raycast(context);

        Vec3d vortexPos = blockHit.getPos();

        // 检查实体拦截
        net.minecraft.util.hit.EntityHitResult entityHit = net.minecraft.entity.projectile.ProjectileUtil.raycast(
                user, start, vortexPos,
                user.getBoundingBox().stretch(dir.multiply(30.0)).expand(1.0),
                (e) -> e instanceof LivingEntity && !e.isSpectator() && e.isAlive(),
                30.0 * 30.0
        );

        if (entityHit != null) {
            vortexPos = entityHit.getPos();
        }

        // 3. 在落点生成 7x7x7 (半径 3.5) 的旋风，持续 5 秒
        spawnTornadoLogicCloud(world, user, vortexPos, 5, 3.5);

        world.playSound(null, user.getBlockPos(), SoundEvents.ENTITY_BREEZE_SHOOT, SoundCategory.PLAYERS, 1.0f, 1.0f);
    }

    // --- 技能 1 旋风的逻辑云载体 ---
    private void spawnTornadoLogicCloud(ServerWorld world, PlayerEntity caster, Vec3d pos, int durationSecs,double radius) {
        net.minecraft.entity.AreaEffectCloudEntity cloud = new net.minecraft.entity.AreaEffectCloudEntity(world, pos.x, pos.y, pos.z);
        cloud.setRadius(3.0f);
        cloud.setDuration(durationSecs * 20);
        cloud.setWaitTime(0);
        cloud.setParticleType(ParticleTypes.GUST);
        world.spawnEntity(cloud);

        for (int i = 0; i < durationSecs * 20; i++) {
            final int tick = i;
            world.getServer().execute(() -> {
                if (cloud.isAlive()) {
                    executeVortexLogic(world, caster, cloud.getPos(), 3.5, 1.5f, false, tick);
                }
            });
        }
    }

    // --- 【核心核心】全自动强力吸引与螺旋粒子算法 ---
    private void executeVortexLogic(World world, PlayerEntity caster, Vec3d center, double radius, float damage, boolean isMega, int tickLeft) {
        if (!(world instanceof ServerWorld serverWorld)) return;

        // 1. 旋风粒子：螺旋上升的锥形
        int particleCount = isMega ? 12 : 5; // 增加小旋风的粒子密度
        for (int i = 0; i < particleCount; i++) {
            double angle = (tickLeft * 0.7) + (i * (Math.PI * 2 / particleCount));

            // 高度循环 (0 到 6格高)
            double heightLimit = isMega ? 5.0 : 6.0;
            double pyOffset = (tickLeft + i * 10) % (heightLimit * 10) * 0.1;

            // 半径随高度变宽：底部 0.5 -> 顶部 等于设置的 radius
            double currentRadius = 0.5 + (pyOffset / heightLimit) * (radius - 0.5);

            double px = center.x + Math.cos(angle) * currentRadius;
            double pz = center.z + Math.sin(angle) * currentRadius;
            double py = center.y + pyOffset;

            serverWorld.spawnParticles(ParticleTypes.CLOUD, px, py, pz, 1, 0, 0, 0, 0.01);
            if (tickLeft % 3 == 0) {
                serverWorld.spawnParticles(ParticleTypes.GUST, px, py, pz, 1, 0, 0, 0, 0.01);
            }
        }

        // 2. 强力牵引逻辑 (球形判定)
        Box box = new Box(center.subtract(radius, radius, radius), center.add(radius, radius, radius));
        List<LivingEntity> targets = world.getEntitiesByClass(LivingEntity.class, box,
                e -> e != caster && e.isAlive() && e.getPos().distanceTo(center) <= radius);

        for (LivingEntity target : targets) {
            Vec3d diff = center.subtract(target.getPos());
            double dist = diff.length();

            // 增强拉力：0.3 是一个小黑洞的强度
            double pullStrength = isMega ? 0.35 : 0.3;
            if (dist > 1.0) {
                Vec3d pullVel = diff.normalize().multiply(pullStrength);
                // 向上提一点，增加挣扎感
                target.addVelocity(pullVel.x, 0.1, pullVel.z);
                target.velocityModified = true;
            }

            // 持续结算
            if (tickLeft % 10 == 0) {
                target.damage(world.getDamageSources().magic(), damage);
                target.addStatusEffect(new StatusEffectInstance(StatusEffects.LEVITATION, 20, 0));
                if (isMega) {
                    target.addStatusEffect(new StatusEffectInstance(StatusEffects.WEAKNESS, 120, 0));
                }
            }
        }

        // 环境音效
        if (tickLeft % 20 == 0) {
            world.playSound(null, BlockPos.ofFloored(center), SoundEvents.ENTITY_BREEZE_IDLE_GROUND, SoundCategory.PLAYERS, 0.5f, 1.5f);
        }
    }


    //------------------------------------------------------
    //original stream技能实现
    // --- 技能 1：吸水射线 ---
    private void executeStreamBeam(World world, PlayerEntity user, ItemStack stack, int usedTicks) {
        if (!(world instanceof ServerWorld serverWorld)) return;

        Vec3d start = user.getEyePos();
        Vec3d dir = user.getRotationVec(1.0f);

        // 视觉：高可见度的喷射气泡粒子
        for (double d = 0; d < 10.0; d += 0.3) {
            Vec3d pos = start.add(dir.multiply(d));
            // 亮蓝色的水滴和气泡
            serverWorld.spawnParticles(ParticleTypes.BUBBLE, pos.x, pos.y, pos.z, 1, 0, 0, 0, 0.02);
            if (usedTicks % 4 == 0) {
                serverWorld.spawnParticles(ParticleTypes.FALLING_WATER, pos.x, pos.y, pos.z, 1, 0.1, 0.1, 0.1, 0.01);
            }
        }

        // 逻辑：射线碰撞检测 (10格)
        Vec3d end = start.add(dir.multiply(10.0));
        EntityHitResult hit = net.minecraft.entity.projectile.ProjectileUtil.raycast(
                user, start, end,
                user.getBoundingBox().stretch(dir.multiply(10.0)).expand(1.0),
                e -> e instanceof LivingEntity && e != user,
                100.0
        );

        if (hit != null && hit.getEntity() instanceof LivingEntity target) {
            // 每秒 3.5 伤害 (每刻约 0.175)
            target.damage(world.getDamageSources().magic(), 0.175f);

            // 2秒后 (40刻) 触发虚弱和凋零
            if (usedTicks > 40) {
                target.addStatusEffect(new StatusEffectInstance(StatusEffects.WITHER, 40, 1));
                target.addStatusEffect(new StatusEffectInstance(StatusEffects.WEAKNESS, 40, 1));
            }

            // 【修复后的粒子】在敌人身上产生水爆开的效果
            serverWorld.spawnParticles(ParticleTypes.BUBBLE_POP, target.getX(), target.getEyeY(), target.getZ(), 5, 0.2, 0.2, 0.2, 0.05);

            if (usedTicks % 12 == 0) {
                world.playSound(null, target.getBlockPos(), SoundEvents.ENTITY_GENERIC_DRINK, SoundCategory.PLAYERS, 0.5f, 1.4f);
            }
        }
    }

    // --- 技能 2：源流领域 (爱心叠加) ---
    private void executeStreamAOE(World world, PlayerEntity user, ItemStack stack, int ticksLeft) {
        ServerWorld serverWorld = (ServerWorld) world;
        double radius = 6.0;

        // 1. 旋转粒子 (两个暗蓝色点围绕玩家)
        double angle = ticksLeft * 0.4;
        for (int i = 0; i < 2; i++) {
            double offset = i * Math.PI;
            double px = user.getX() + Math.cos(angle + offset) * 1.5;
            double pz = user.getZ() + Math.sin(angle + offset) * 1.5;
            serverWorld.spawnParticles(ParticleTypes.NAUTILUS, px, user.getY() + 1.0, pz, 5, 0.1, 0.1, 0.1, 0.05);
        }

        // 2. 吸收逻辑
        Box box = user.getBoundingBox().expand(radius);
        List<LivingEntity> targets = world.getEntitiesByClass(LivingEntity.class, box, e -> e != user && e.isAlive());

        if (!targets.isEmpty()) {
            for (LivingEntity target : targets) {
                target.damage(world.getDamageSources().magic(), 0.15f); // 每秒 3 伤害
                serverWorld.spawnParticles(ParticleTypes.BUBBLE, target.getX(), target.getY() + 1, target.getZ(), 3, 0.2, 0.2, 0.2, 0.02);
            }

            // 每秒 (20刻) 检查一次叠加
            if (ticksLeft % 20 == 0) {
                int layers = stack.getOrDefault(ModDataComponentTypes.STREAM_ABSORB_LAYERS, 0) + 1;
                layers = Math.min(layers, 5); // 最多 5 层 (即增加 100% 生命)
                stack.set(ModDataComponentTypes.STREAM_ABSORB_LAYERS, layers);

                // 刷新药水效果：生命提升 (层数 * 2 对应额外的血量槽)
                user.addStatusEffect(new StatusEffectInstance(StatusEffects.HEALTH_BOOST, 400, layers - 1, false, false, true));
                user.addStatusEffect(new StatusEffectInstance(StatusEffects.RESISTANCE, 200, 0));
                user.addStatusEffect(new StatusEffectInstance(StatusEffects.STRENGTH, 200, 1));

                // 立即回复刚才提升的那部分血量
                user.heal(4.0f);

                world.playSound(null, user.getBlockPos(), SoundEvents.ENTITY_PLAYER_SPLASH_HIGH_SPEED, SoundCategory.PLAYERS, 1.0f, 1.0f);
            }
        }
    }


    //------------------------------------------------------


    // --- 基础幻化技能：幻化之波 (原逻辑) ---
    private void executeBasicIllusionary(ServerWorld world, PlayerEntity user, ItemStack stack, Hand hand) {
        // 粒子
        for (int i = 0; i < 360; i += 12) {
            double angle = Math.toRadians(i);
            double rx = Math.cos(angle) * 1.5;
            double rz = Math.sin(angle) * 1.5;
            world.spawnParticles(ParticleTypes.WITCH, user.getX() + rx, user.getY() + 0.8, user.getZ() + rz, 3, rx * 0.2, 0.1, rz * 0.2, 0.1);
        }
        // 效果
        List<LivingEntity> targets = world.getEntitiesByClass(LivingEntity.class, user.getBoundingBox().expand(2.0), e -> e != user);
        boolean hitAny = false;
        for (LivingEntity target : targets) {
            target.damage(world.getDamageSources().magic(), 2.5f);
            target.takeKnockback(1.2, user.getX() - target.getX(), user.getZ() - target.getZ());
            target.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 20, 1));
            hitAny = true;
        }
        if (hitAny) user.addStatusEffect(new StatusEffectInstance(StatusEffects.SPEED, 40, 0));

        world.playSound(null, user.getBlockPos(), SoundEvents.ENTITY_ILLUSIONER_CAST_SPELL, SoundCategory.PLAYERS, 1.0f, 1.2f);
        stack.damage(1, user, LivingEntity.getSlotForHand(hand));
    }



    // ================= 辅助方法 =================

    private boolean isSkillReady(ItemStack stack, ComponentType<Long> component, long currentTime, int cooldownTicks) {
        if (!stack.contains(component)) return true;
        return (currentTime - stack.get(component)) >= cooldownTicks;
    }

    private void sendCooldownMessage(PlayerEntity player, String msg) {
        player.sendMessage(Text.literal("§c" + msg), true);
    }

    @Override
    public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
        String bound = stack.get(ModDataComponentTypes.BOUND_MAGIC);
        String talisman = stack.get(ModDataComponentTypes.TALISMAN_TYPE);

        // 优先级：护符描述 > 基础绑定描述
        if ("Flame".equals(talisman)) {
            tooltip.add(Text.literal("护符: 烈焰").formatted(Formatting.GOLD, Formatting.BOLD));
            tooltip.add(Text.literal("右键: 火焰迸发 ").formatted(Formatting.GRAY));
            tooltip.add(Text.literal("Shift+右键: 远程火焰弹").formatted(Formatting.GRAY));
        }
        else if ("Frozen".equals(talisman)) {
            tooltip.add(Text.literal("护符: 冰霜").formatted(Formatting.AQUA, Formatting.BOLD));
            tooltip.add(Text.literal("长按右键: 极寒射线 (阶梯伤害)").formatted(Formatting.GRAY));
            tooltip.add(Text.literal("Shift+右键: 雪球连弹 ").formatted(Formatting.GRAY));
        }
        else if ("Breeze".equals(talisman)) {
            tooltip.add(Text.literal("护符: 微风").formatted(Formatting.WHITE, Formatting.BOLD));
            tooltip.add(Text.literal("右键: 风弹旋风 (落点吸引)").formatted(Formatting.GRAY));
            tooltip.add(Text.literal("Shift+右键: 肃清风暴").formatted(Formatting.GRAY));
        }
        else if ("Stream".equals(talisman)) {
            tooltip.add(Text.literal("护符: 源流").formatted(Formatting.DARK_AQUA, Formatting.BOLD));
            tooltip.add(Text.literal("被动: 夜视 & 水肺").formatted(Formatting.BLUE));
            tooltip.add(Text.literal("长按右键: 吸水射线 ").formatted(Formatting.GRAY));
            tooltip.add(Text.literal("Shift+右键: 源流领域 ").formatted(Formatting.GRAY));
        }
        // 如果没有绑定护符，但绑定了基础幻化魔法
        else if ("Illusionary".equals(bound)) {
            tooltip.add(Text.literal("已绑定: 幻化").formatted(Formatting.DARK_PURPLE, Formatting.ITALIC));
            tooltip.add(Text.literal("右键: 幻化盈波").formatted(Formatting.GRAY));
        }
        else {
            tooltip.add(Text.literal("未绑定法术").formatted(Formatting.GRAY));
        }

        // 统一显示的不可修理提示
        tooltip.add(Text.literal("德拉姆の消失诅咒").formatted(Formatting.RED, Formatting.STRIKETHROUGH));

        super.appendTooltip(stack, context, tooltip, type);
    }

}