package com.Primal.entity.client;

import com.Primal.entity.MingYuanEntity;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class MingYuanRenderer extends GeoEntityRenderer<MingYuanEntity> {
    public MingYuanRenderer(EntityRendererFactory.Context renderManager) {
        super(renderManager, new MingYuanModel());

        // --- 核心修复：设置视觉缩放 ---
        // 如果它现在只有 1/4 格大，你可以把它放大 4 倍甚至更多
        // 第一个参数是 X，第二个是 Y，第三个是 Z
        // 建议试一下 3.0f 或 5.0f，直到它看起来像个 Boss
        this.withScale(1.5f);
    }

    // 可选：如果你还想微调碰撞箱的大小（让玩家更难打中或更容易打中）
    // 需要去 ModEntities.java 里的 .dimensions(宽, 高) 修改
}