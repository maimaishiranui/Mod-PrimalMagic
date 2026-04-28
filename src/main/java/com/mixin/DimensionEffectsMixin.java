package com.mixin;

import net.minecraft.client.render.DimensionEffects;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(DimensionEffects.Overworld.class)
public class DimensionEffectsMixin {

    // 注意：这个逻辑会影响所有使用 Overworld 效果的维度。
    // 如果你只想影响 Mirage 维度，需要判断当前的 World 注册键。
    @Inject(method = "adjustFogColor", at = @At("RETURN"), cancellable = true)
    private void changeSkyWithWeather(Vec3d color, float sunHeight, CallbackInfoReturnable<Vec3d> cir) {
        // 这里可以获取当前世界的天气强度
        // 如果 rainGradient > 0，可以手动修改返回的颜色向量
        // 这是一个比较高级的渲染修改点，建议你先用 JSON 调好基础颜色。
    }
}