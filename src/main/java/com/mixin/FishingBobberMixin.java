package com.mixin;

import com.Primal.item.ModItems;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.FishingBobberEntity;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Random;

@Mixin(FishingBobberEntity.class)
public abstract class FishingBobberMixin {

    // 保留这个影子方法，用来获取鱼钩的主人
    @Shadow public abstract PlayerEntity getPlayerOwner();

    private static final Random RANDOM = new Random();

    /**
     * 注入到 use 方法的 RETURN（返回时刻）
     * cir.getReturnValue() 如果大于 0，说明玩家成功钓起了一件物品
     */
    @Inject(method = "use", at = @At("RETURN"))
    private void onFished(ItemStack usedItem, CallbackInfoReturnable<Integer> cir) {
        // 获取方法返回值
        int caughtResult = cir.getReturnValue();

        // 判定条件：返回值 > 0 说明钓鱼成功
        if (caughtResult > 0) {
            PlayerEntity player = this.getPlayerOwner();

            if (player != null && !player.getWorld().isClient) {
                // 20% 概率奖励源流护符
                if (RANDOM.nextFloat() < 0.20f) {
                    player.getInventory().offerOrDrop(new ItemStack(ModItems.ORIGINALSTREAM_TALISMAN));
                }
            }
        }
    }
}