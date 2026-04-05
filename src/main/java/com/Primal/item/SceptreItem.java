package com.Primal.item;

import com.Primal.component.ModDataComponentTypes;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
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
import net.minecraft.util.math.Box;
import net.minecraft.world.World;

import java.util.List;

public class SceptreItem extends Item {

    public SceptreItem(Settings settings) {
        super(settings);
    }

    // --- 1. 禁止修复 ---
    @Override
    public boolean canRepair(ItemStack stack, ItemStack ingredient) {
        return false;
    }

    // --- 2. 右键技能：幻化之波 (2格范围击退 + 伤害) ---
    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);

        // 检查魔法标识 (确保这里跟绑定台里写的 "Illusionary" 大小写一致)
        if (stack.contains(ModDataComponentTypes.BOUND_MAGIC) &&
                stack.get(ModDataComponentTypes.BOUND_MAGIC).equals("Illusionary")) {

            if (!world.isClient) {
                ServerWorld serverWorld = (ServerWorld) world;

                // 1. 释放冲击波粒子
                spawnWaveParticles(serverWorld, user);

                // 2. 作用范围 (2格)
                Box area = user.getBoundingBox().expand(2.0);
                List<Entity> entities = world.getOtherEntities(user, area);

                boolean hitSuccess = false; // 记录是否打中了人

                for (Entity entity : entities) {
                    if (entity instanceof LivingEntity target) {
                        // 造成的魔法伤害
                        target.damage(world.getDamageSources().magic(), 2.5f);

                        // 击退
                        double x = target.getX() - user.getX();
                        double z = target.getZ() - user.getZ();
                        target.takeKnockback(1.2, -x, -z);

                        // --- 【核心修复】在这里施加减速效果 ---
                        // 被击中者：缓慢 II，1秒
                        target.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 20, 1));
                        hitSuccess = true;
                    }
                }

                // 如果至少命中了一个目标，则给自己施加加速
                if (hitSuccess) {
                    // 施法者：速度 I，2秒
                    user.addStatusEffect(new StatusEffectInstance(StatusEffects.SPEED, 40, 0));
                    // 播放一个成功的清脆音效
                    world.playSound(null, user.getBlockPos(), SoundEvents.ENTITY_ARROW_HIT_PLAYER, SoundCategory.PLAYERS, 1.0f, 1.5f);
                }

                // 播放施法音效
                world.playSound(null, user.getBlockPos(), SoundEvents.ENTITY_ILLUSIONER_CAST_SPELL, SoundCategory.PLAYERS, 1.0f, 1.2f);

                // 设置 CD
                user.getItemCooldownManager().set(this, 200);
                stack.damage(1, user, LivingEntity.getSlotForHand(hand));
            }
            return TypedActionResult.success(stack);
        }
        return TypedActionResult.pass(stack);
    }

    // 辅助方法：抽取粒子逻辑让代码更整洁
    private void spawnWaveParticles(ServerWorld world, PlayerEntity user) {
        for (int i = 0; i < 360; i += 12) {
            double angle = Math.toRadians(i);
            double rx = Math.cos(angle) * 1.5;
            double rz = Math.sin(angle) * 1.5;
            world.spawnParticles(ParticleTypes.WITCH, user.getX() + rx, user.getY() + 0.8, user.getZ() + rz, 3, rx * 0.2, 0.1, rz * 0.2, 0.1);
        }
    }



    // --- 3. 物品说明 ---
    @Override
    public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
        // 基础绑定显示
        if (stack.contains(ModDataComponentTypes.BOUND_MAGIC)) {
            tooltip.add(Text.literal("已绑定: " + stack.get(ModDataComponentTypes.BOUND_MAGIC)).formatted(Formatting.DARK_PURPLE));
        }

        // 【新增】护符绑定显示
        if (stack.contains(ModDataComponentTypes.TALISMAN_TYPE)) {
            String typeName = stack.get(ModDataComponentTypes.TALISMAN_TYPE);
            tooltip.add(Text.literal("护符加持: " + typeName).formatted(Formatting.AQUA, Formatting.BOLD));
        }
    }
}