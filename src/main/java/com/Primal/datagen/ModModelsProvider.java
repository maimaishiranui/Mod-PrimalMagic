package com.Primal.datagen;

import com.Primal.block.ModBlocks;
import com.Primal.item.ModItems;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricModelProvider;
import net.minecraft.data.client.*;
import net.minecraft.util.Identifier;

public class ModModelsProvider extends FabricModelProvider {
    public ModModelsProvider(FabricDataOutput output) {
        super(output);
    }

    @Override
    public void generateBlockStateModels(BlockStateModelGenerator generator) {
        // --- 1. 基础方块（六面相同） ---
        // 每个方块只能在这里出现【一次】
        generator.registerSimpleCubeAll(ModBlocks.REFINING_STONE_BLOCK);
        generator.registerSimpleCubeAll(ModBlocks.ILLUSIONARY_BLOCK);
        generator.registerSimpleCubeAll(ModBlocks.SPIRITUAL_MEDIUM_STONE_BLOCK);
        generator.registerSimpleCubeAll(ModBlocks.MINGYUAN_CORE);

        // --- 2. 魔法绑定台（自定义多面贴图） ---
        TextureMap textures = new TextureMap()
                .put(TextureKey.SIDE, Identifier.of("primalmagic", "block/magic_binding_table_side"))
                .put(TextureKey.FRONT, Identifier.of("primalmagic", "block/magic_binding_table_front"))
                .put(TextureKey.BACK, Identifier.of("primalmagic", "block/magic_binding_table_front"))
                .put(TextureKey.UP, Identifier.of("primalmagic", "block/magic_binding_table_top"))
                .put(TextureKey.DOWN, Identifier.of("minecraft", "block/diamond_block"))
                .put(TextureKey.PARTICLE, Identifier.of("primalmagic", "block/magic_binding_table_front"));

        Identifier modelId = Models.CUBE.upload(ModBlocks.MAGIC_BINDING_TABLE, textures, generator.modelCollector);
        generator.blockStateCollector.accept(BlockStateModelGenerator.createSingletonBlockState(ModBlocks.MAGIC_BINDING_TABLE, modelId));

        // 确保绑定台在物品栏里也不是黑紫格子
        generator.registerParentedItemModel(ModBlocks.MAGIC_BINDING_TABLE, modelId);
    }


    @Override
    public void generateItemModels(ItemModelGenerator itemModelGenerator) {
        // 物品模型生成
        itemModelGenerator.register(ModItems.SCHOLAR_SCEPTRE, Models.GENERATED);
        itemModelGenerator.register(ModItems.MASTER_SCEPTRE, Models.GENERATED);
        itemModelGenerator.register(ModItems.PASTORAL_SCEPTRE, Models.GENERATED);
        itemModelGenerator.register(ModItems.ORIGINAL_SCEPTRE, Models.GENERATED);
        itemModelGenerator.register(ModItems.THUNERCLAP_TALISMAN, Models.GENERATED);
        itemModelGenerator.register(ModItems.FLAME_TALISMAN, Models.GENERATED);
        itemModelGenerator.register(ModItems.BREEZE_TALISMAN, Models.GENERATED);
        itemModelGenerator.register(ModItems.FROZEN_TALISMAN, Models.GENERATED);
        itemModelGenerator.register(ModItems.ROCK_TALISMAN, Models.GENERATED);
        itemModelGenerator.register(ModItems.ORIGINALSTREAM_TALISMAN, Models.GENERATED);
        itemModelGenerator.register(ModItems.SPIRITWOOD_TALISMAN, Models.GENERATED);
        itemModelGenerator.register(ModItems.VASTNESS_ADAMANTROCK, Models.GENERATED);
        itemModelGenerator.register(ModItems.VASTNESS_GLACIATE, Models.GENERATED);
        itemModelGenerator.register(ModItems.VASTNESS_STREAM, Models.GENERATED);
        itemModelGenerator.register(ModItems.VASTNESS_VERDANT, Models.GENERATED);
        itemModelGenerator.register(ModItems.VASTNESS_ZEPHYR, Models.GENERATED);
        itemModelGenerator.register(ModItems.VASTNESS_THUNDERCLAP, Models.GENERATED);
        itemModelGenerator.register(ModItems.REFINING_STONE, Models.GENERATED);
        itemModelGenerator.register(ModItems.ELEMENTAL_MASTERMIND, Models.GENERATED);
        itemModelGenerator.register(ModItems.SOUL_OF_MINGYUAN, Models.GENERATED);
        itemModelGenerator.register(ModItems.SPIRITUAL_MEDIUM_STONE, Models.GENERATED);
        itemModelGenerator.register(ModItems.ILLUSIONARY_TRANSFORMATION_CURSE, Models.GENERATED);
        itemModelGenerator.register(ModItems.MAGIC_INTRODUCTION, Models.GENERATED);
        itemModelGenerator.register(ModItems.ADVANCED_MAGIC_INTRODUCTION, Models.GENERATED);
    }
}