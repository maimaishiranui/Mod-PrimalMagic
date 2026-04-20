package com.Primal.datagen;

import com.Primal.block.ModBlocks;
import com.Primal.item.ModItems;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricLanguageProvider;
import net.minecraft.registry.RegistryWrapper;
import java.util.concurrent.CompletableFuture;

public class ModZHCNLanProvider extends FabricLanguageProvider {
    public ModZHCNLanProvider(FabricDataOutput dataOutput, CompletableFuture<RegistryWrapper.WrapperLookup> registryLookup) {
        super(dataOutput, "zh_cn", registryLookup);
    }

    @Override
    public void generateTranslations(RegistryWrapper.WrapperLookup registryLookup, TranslationBuilder translationBuilder) {
        // --- 1. 方块类 (对于 BlockItem 来说，注册方块的翻译通常就足够了) ---
        translationBuilder.add(ModBlocks.MAGIC_BINDING_TABLE, "魔法绑定台");
        translationBuilder.add(ModBlocks.ILLUSIONARY_BLOCK, "幻化石方块");
        translationBuilder.add(ModBlocks.REFINING_STONE_BLOCK, "洗练石方块");
        translationBuilder.add(ModBlocks.SPIRITUAL_MEDIUM_STONE_BLOCK, "灵媒石方块");
        translationBuilder.add(ModBlocks.MINGYUAN_CORE, "冥渊核心");

        // --- 2. 基础物品 ---
        translationBuilder.add(ModItems.SCHOLAR_SCEPTRE, "学识权杖");
        translationBuilder.add(ModItems.MASTER_SCEPTRE, "大师权杖");
        translationBuilder.add(ModItems.PASTORAL_SCEPTRE, "教皇权杖");
        translationBuilder.add(ModItems.POWER_SCEPTRE, "权力法杖");
        translationBuilder.add(ModItems.ORIGINAL_SCEPTRE, "原初法杖");
        translationBuilder.add(ModItems.REFINING_STONE, "洗练石");
        translationBuilder.add(ModItems.SPIRITUAL_MEDIUM_STONE, "灵媒石");
        translationBuilder.add(ModItems.ILLUSIONARY_TRANSFORMATION_CURSE, "幻化魔咒");
        translationBuilder.add(ModItems.MAGIC_INTRODUCTION, "魔导绪论");
        translationBuilder.add(ModItems.ADVANCED_MAGIC_INTRODUCTION, "高阶魔导书");

        // --- 3. Boss 掉落物 ---
        translationBuilder.add(ModItems.VASTNESS_ADAMANTROCK, "虚空之岩");
        translationBuilder.add(ModItems.VASTNESS_EMBER, "虚空之火");
        translationBuilder.add(ModItems.VASTNESS_GLACIATE, "虚空之冰");
        translationBuilder.add(ModItems.VASTNESS_VERDANT, "虚空之木");
        translationBuilder.add(ModItems.VASTNESS_THUNDERCLAP, "虚空之雷");
        translationBuilder.add(ModItems.VASTNESS_ZEPHYR, "虚空之风");
        translationBuilder.add(ModItems.VASTNESS_STREAM, "虚空之水");
        translationBuilder.add(ModItems.SOUL_OF_MINGYUAN, "冥渊之魂");
        translationBuilder.add(ModItems.ELEMENTAL_MASTERMIND, "元素之母");
        translationBuilder.add(ModItems.ANCIENT_KEY,"远古密钥");

        // --- 4. 元素符文 (Talisman) ---
        translationBuilder.add(ModItems.BREEZE_TALISMAN, "微风护符");
        translationBuilder.add(ModItems.FLAME_TALISMAN, "烈焰护符");
        translationBuilder.add(ModItems.FROZEN_TALISMAN, "冰霜护符");
        translationBuilder.add(ModItems.ORIGINALSTREAM_TALISMAN, "源流护符");
        translationBuilder.add(ModItems.SPIRITWOOD_TALISMAN, "灵木护符");
        translationBuilder.add(ModItems.ROCK_TALISMAN, "岩石护符");
        translationBuilder.add(ModItems.THUNERCLAP_TALISMAN, "雷兆护符");

        // --- 5. 界面与分组 ---
        translationBuilder.add("container.primalmagic.magic_binding_table", "魔法绑定台");
        translationBuilder.add("itemGroup.primalmagic_group", "原初魔法");
    }
}