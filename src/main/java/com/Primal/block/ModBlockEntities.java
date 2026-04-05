package com.Primal.block;

import com.Primal.PrimalMagic;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class ModBlockEntities {
    public static final BlockEntityType<MagicBindingTableBlockEntity> MAGIC_BINDING_TABLE_BE =
            Registry.register(Registries.BLOCK_ENTITY_TYPE, Identifier.of(PrimalMagic.MOD_ID, "magic_binding_be"),
                    FabricBlockEntityTypeBuilder.create(MagicBindingTableBlockEntity::new,
                            ModBlocks.MAGIC_BINDING_TABLE).build());

    public static void registerBlockEntities() {
        PrimalMagic.LOGGER.info("Registering Block Entities for " + PrimalMagic.MOD_ID);
    }
}