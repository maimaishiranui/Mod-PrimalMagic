package com.Primal.entity;

import com.Primal.PrimalMagic;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class ModEntities {
    // 注册实体，ID 为 "mingyuan"
    public static final EntityType<MingYuanEntity> MINGYUAN = Registry.register(
            Registries.ENTITY_TYPE,
            Identifier.of(PrimalMagic.MOD_ID, "mingyuan"),
            EntityType.Builder.create(MingYuanEntity::new, SpawnGroup.MONSTER)
                    // 设置 Boss 的碰撞箱大小 (宽 1.0, 高 2.0，可根据你的模型自行修改)
                    .dimensions(1.0f, 2.0f)
                    .build()
    );

    public static void register() {
        PrimalMagic.LOGGER.info("Registering Entities for " + PrimalMagic.MOD_ID);
    }
}