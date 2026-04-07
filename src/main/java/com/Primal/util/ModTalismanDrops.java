package com.Primal.util;

import com.Primal.block.ModBlocks;
import com.Primal.item.ModItems;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.minecraft.advancement.AdvancementEntry;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.*;
import net.minecraft.entity.passive.SnowGolemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import net.minecraft.text.Text;

import java.util.Random;

public class ModTalismanDrops {
    private static final Random RANDOM = new Random();

    // =========================================================
    // 1. 行为触发逻辑 (只有主动做动作才会有 15% 几率)
    // =========================================================

    /**
     * 尝试给予农牧随机奖励
     * 在主类中被【种地】或【喂动物】的行为调用
     */
    public static void tryGiveFarmingRandomReward(PlayerEntity player) {
        if (!player.getWorld().isClient && RANDOM.nextFloat() < 0.15f) {
            player.getInventory().offerOrDrop(new ItemStack(ModItems.SPIRITWOOD_TALISMAN));
            player.sendMessage(Text.literal("§2[自然觉醒] 在辛勤的耕耘中，你获得了一枚灵木护符!"), true);
        }
    }

    // =========================================================
    // 2. 循环检查逻辑 (放在 Tick 循环里运行)
    // =========================================================

    /**
     * 检查农牧成就奖励
     * 建议每秒运行一次 (20 ticks)
     */
    public static void handleFarmingLogic(PlayerEntity player) {
        if (player instanceof ServerPlayerEntity serverPlayer) {
            // 仅检查成就，不包含 15% 的随机概率，防止每秒都在发护符
            if (checkFarmingAdvancements(serverPlayer)) {
                if (!player.getCommandTags().contains("awarded_spiritwood")) {
                    player.getInventory().offerOrDrop(new ItemStack(ModItems.SPIRITWOOD_TALISMAN));
                    player.addCommandTag("awarded_spiritwood");
                    player.sendMessage(Text.literal("§2[成就奖赏] 恭喜你完成所有农牧成就，获得灵木护符!"), false);
                }
            }
        }
    }

    /**
     * 检查微风护符获取 (急速移动)
     * 建议每一刻都运行 (1 tick)
     */
    public static void handleBreezeCheck(ServerPlayerEntity player) {
        Vec3d velocity = player.getVelocity();
        // 降低门槛到 0.8，只要处于较快的垂直位移中
        if (Math.abs(velocity.y) > 0.8) {
            // 因为每刻都在检查，所以概率设低 (0.5% 左右)
            if (RANDOM.nextFloat() < 0.005f) {
                // 如果背包里还没有，则给予 (防止一次跳楼给一堆)
                if (!player.getInventory().contains(new ItemStack(ModItems.BREEZE_TALISMAN))) {
                    player.getInventory().offerOrDrop(new ItemStack(ModItems.BREEZE_TALISMAN));
                    player.sendMessage(Text.literal("§f[微风感应] 极速穿行中，你领悟了风的真谛!"), true);
                }
            }
        }
    }

    // =========================================================
    // 3. 矿石与战斗掉落逻辑 (保持稳定)
    // =========================================================

    public static void registerMineLogic() {
        PlayerBlockBreakEvents.AFTER.register((world, player, pos, state, blockEntity) -> {
            if (world.isClient) return;
            if (player instanceof PlayerEntity playerEntity) {
                Block b = state.getBlock();
                if (isValuableOre(b) && RANDOM.nextFloat() < 0.30f) {
                    playerEntity.getInventory().offerOrDrop(new ItemStack(ModItems.ROCK_TALISMAN));
                }
            }
        });
    }

    public static void handleEntityDrops(LivingEntity entity, DamageSource source) {
        if (entity.getWorld().isClient) return;
        ServerWorld world = (ServerWorld) entity.getWorld();

        // Flame: 火烧
        if (entity instanceof HostileEntity && (entity.isOnFire() || source.isOf(net.minecraft.entity.damage.DamageTypes.ON_FIRE))) {
            if (RANDOM.nextFloat() < 0.35f) dropItem(entity, ModItems.FLAME_TALISMAN);
        }

        // Stream: 水下
        if (entity instanceof HostileEntity && entity.isSubmergedIn(FluidTags.WATER)) {
            if (entity instanceof ElderGuardianEntity) dropItem(entity, ModItems.ORIGINALSTREAM_TALISMAN);
            else if (entity instanceof GuardianEntity && RANDOM.nextFloat() < 0.55f) dropItem(entity, ModItems.ORIGINALSTREAM_TALISMAN);
            else if (entity instanceof DrownedEntity && RANDOM.nextFloat() < 0.35f) dropItem(entity, ModItems.ORIGINALSTREAM_TALISMAN);
        }

        // Thunder: 雷雨
        if (entity instanceof HostileEntity && world.isThundering()) {
            if (entity instanceof CreeperEntity creeper && creeper.shouldRenderOverlay()) {
                if (RANDOM.nextFloat() < 0.85f) dropItem(entity, ModItems.THUNERCLAP_TALISMAN);
            } else if (RANDOM.nextFloat() < 0.30f) dropItem(entity, ModItems.THUNERCLAP_TALISMAN);
        }

        // Frozen: 雪傀儡
        if (entity instanceof SnowGolemEntity) {
            boolean isSnowing = world.isRaining() && world.getBiome(entity.getBlockPos()).value().getPrecipitation(entity.getBlockPos()) == net.minecraft.world.biome.Biome.Precipitation.SNOW;
            float chance = isSnowing ? 0.40f : 0.30f;
            if (RANDOM.nextFloat() < chance) dropItem(entity, ModItems.FROZEN_TALISMAN);
        }
    }

    // =========================================================
    // 4. 辅助私有方法
    // =========================================================

    private static boolean isValuableOre(Block b) {
        return b == Blocks.DIAMOND_ORE || b == Blocks.DEEPSLATE_DIAMOND_ORE ||
                b == Blocks.GOLD_ORE || b == Blocks.DEEPSLATE_GOLD_ORE ||
                b == Blocks.EMERALD_ORE || b == Blocks.DEEPSLATE_EMERALD_ORE ||
                b == Blocks.LAPIS_ORE || b == Blocks.DEEPSLATE_LAPIS_ORE ||
                b == ModBlocks.REFINING_STONE_BLOCK ||
                b == ModBlocks.SPIRITUAL_MEDIUM_STONE_BLOCK ||
                b == ModBlocks.ILLUSIONARY_BLOCK;
    }

    private static boolean checkFarmingAdvancements(ServerPlayerEntity player) {
        String[] required = {
                "husbandry/root", "husbandry/plant_any_seed", "husbandry/bake_bread",
                "husbandry/make_a_cake", "husbandry/plant_ancient_seed", "husbandry/breed_an_animal",
                "husbandry/breed_all_animals", "husbandry/tame_an_animal", "husbandry/ride_a_boat_with_a_goat"
        };
        for (String id : required) {
            AdvancementEntry entry = player.getServer().getAdvancementLoader().get(Identifier.of("minecraft", id));
            if (entry == null || !player.getAdvancementTracker().getProgress(entry).isDone()) return false;
        }
        return true;
    }

    private static void dropItem(Entity entity, Item item) {
        entity.dropStack(new ItemStack(item));
    }
}