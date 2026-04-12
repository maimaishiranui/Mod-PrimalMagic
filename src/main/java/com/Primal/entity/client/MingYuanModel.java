package com.Primal.entity.client;

import com.Primal.PrimalMagic;
import com.Primal.entity.MingYuanEntity;
import net.minecraft.util.Identifier;
import software.bernie.geckolib.model.GeoModel;

public class MingYuanModel extends GeoModel<MingYuanEntity> {
    // MingYuanModel.java 示例
    @Override
    public Identifier getModelResource(MingYuanEntity animatable) {
        return Identifier.of(PrimalMagic.MOD_ID, "geo/mingyuan.geo.json");
    }
    @Override
    public Identifier getTextureResource(MingYuanEntity animatable) {
        return Identifier.of(PrimalMagic.MOD_ID, "textures/entity/mingyuan.png");
    }
    @Override
    public Identifier getAnimationResource(MingYuanEntity animatable) {
        return Identifier.of(PrimalMagic.MOD_ID, "animations/mingyuan.animation.json");
    }
}