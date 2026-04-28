package com.Primal.item;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionOptions;

public class AncientKeyItem extends Item {
    public AncientKeyItem(Settings settings) {
        super(settings);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        if (!world.isClient && user instanceof ServerPlayerEntity player) {
            // 1. 获取幻景维度的 Key
            RegistryKey<World> mirageWorldKey = RegistryKey.of(RegistryKeys.WORLD, Identifier.of("primalmagic", "mirage"));
            ServerWorld mirageWorld = player.getServer().getWorld(mirageWorldKey);

            if (mirageWorld != null) {
                // 2. 传送逻辑 (传送至孤岛中心点 0, 70, 0)
                player.teleport(mirageWorld, 0.5, 70.0, 0.5, player.getYaw(), player.getPitch());

                // 3. 传送音效与提示
                player.sendMessage(Text.literal("§b钥匙发出强烈的光芒，世界正在崩塌..."), true);
                return TypedActionResult.success(user.getStackInHand(hand));
            }
        }
        return super.use(world, user, hand);
    }
}