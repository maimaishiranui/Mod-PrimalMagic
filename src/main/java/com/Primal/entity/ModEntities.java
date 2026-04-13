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
                    // 推荐根据模型调大：宽 2.5 格，高 4.5 格 (根据实际体型微调)
                    .dimensions(1.4f, 2.5f)
                    .build()
    );

    public static void register() {
        PrimalMagic.LOGGER.info("Registering Entities for " + PrimalMagic.MOD_ID);
    }
}