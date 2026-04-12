package com.Primal.entity.client;

import com.Primal.entity.MingYuanEntity;
import net.minecraft.client.render.entity.EntityRendererFactory;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class MingYuanRenderer extends GeoEntityRenderer<MingYuanEntity> {
    public MingYuanRenderer(EntityRendererFactory.Context renderManager) {
        super(renderManager, new MingYuanModel());
    }
}