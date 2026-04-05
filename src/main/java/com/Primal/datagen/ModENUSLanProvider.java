package com.Primal.datagen;

import com.Primal.block.ModBlocks;
import com.Primal.item.ModItems;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricLanguageProvider;
import net.fabricmc.fabric.impl.transfer.fluid.CombinedProvidersImpl;
import net.minecraft.data.DataGenerator;
import net.minecraft.registry.RegistryWrapper;

import java.util.concurrent.CompletableFuture;

public class ModENUSLanProvider extends FabricLanguageProvider {
    public ModENUSLanProvider(FabricDataOutput dataOutput, CompletableFuture<RegistryWrapper.WrapperLookup> registryLookup) {
        super(dataOutput,"en_us",registryLookup);
    }

    @Override
    public void generateTranslations(RegistryWrapper.WrapperLookup registryLookup, TranslationBuilder translationBuilder) {

        //物品类

        //Boss掉落物--万象元素类
        translationBuilder.add(ModItems.VASTNESS_ADAMANTROCK,"Vastness Adamantrock");
        translationBuilder.add(ModItems.VASTNESS_EMBER,"Vastness Ember");
        translationBuilder.add(ModItems.VASTNESS_GLACIATE,"Vastness Glaciate");
        translationBuilder.add(ModItems.VASTNESS_VERDANT,"Vastness Verdant");
        translationBuilder.add(ModItems.VASTNESS_THUNDERCLAP ,"Vastness Thunderclap");
        translationBuilder.add(ModItems.VASTNESS_ZEPHYR ,"Vastness Zephyr");
        translationBuilder.add(ModItems.VASTNESS_STREAM,"Vastness Stream");

        //Boss掉落物--特级Boss类
        translationBuilder.add(ModItems.SOUL_OF_MINGYUAN,"Soul of Mingyuan");
        translationBuilder.add(ModItems.ELEMENTAL_MASTERMIND,"elemental Mastermind");
        //元素符文类

        translationBuilder.add(ModItems.BREEZE_TALISMAN,"Breeze Talisman");
        translationBuilder.add(ModItems.FLAME_TALISMAN,"Flame Talisman");
        translationBuilder.add(ModItems.FROZEN_TALISMAN,"Frozen Talisman");
        translationBuilder.add(ModItems.ORIGINALSTREAM_TALISMAN,"Originalstream Talisman");
        translationBuilder.add(ModItems.SPIRITWOOD_TALISMAN ,"Spiritwood Talisman");
        translationBuilder.add(ModItems.ROCK_TALISMAN,"Rock Talisman");

        //进阶物品类
        translationBuilder.add(ModItems.ILLUSIONARY_TRANSFORMATION_CURSE,"Illusionary Transformation Curse");
        translationBuilder.add(ModItems.REFINING_STONE,"Refining Stone");
        translationBuilder.add(ModItems.SPIRITUAL_MEDIUM_STONE,"Spiritual Medium Stone");


        //法杖武器类
        translationBuilder.add(ModItems.SCHOLAR_SCEPTRE,"scholar Sceptre");
        translationBuilder.add(ModItems.ORIGINAL_SCEPTRE,"original Sceptre");
        translationBuilder.add(ModItems.PASTORAL_SCEPTRE,"Pastoral Sceptre");
        translationBuilder.add(ModItems.POWER_SCEPTRE,"Power Sceptre");
        translationBuilder.add(ModItems.MASTER_SCEPTRE,"Master Sceptre");

        //学习魔法类
        translationBuilder.add(ModItems.MAGIC_INTRODUCTION,"Magic Introduction");
        translationBuilder.add(ModItems.ADVANCED_MAGIC_INTRODUCTION,"Advanced Magic Introduction");


        //方块类
        translationBuilder.add(ModBlocks.ILLUSIONARY_BLOCK,"Illusionary Block");
        translationBuilder.add(ModBlocks.REFINING_STONE_BLOCK,"Refining Stone Block");
        translationBuilder.add(ModBlocks.MAGIC_BINDING_TABLE,"Magic Binding Table");
        translationBuilder.add(ModBlocks.SPIRITUAL_MEDIUM_STONE_BLOCK,"Spiritual Medium Stone Block");


        //物品栏注册的数据文件生成
        translationBuilder.add("itemGroup.primalmagic_group","Primal");

    }
}
