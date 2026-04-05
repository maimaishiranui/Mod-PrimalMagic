package com.Primal.datagen;

import com.Primal.PrimalMagic;
import com.Primal.block.ModBlocks;
import net.minecraft.registry.Registerable;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.structure.rule.RuleTest;
import net.minecraft.structure.rule.TagMatchRuleTest;
import net.minecraft.util.Identifier;
import net.minecraft.world.gen.feature.ConfiguredFeature;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.OreFeatureConfig;

import java.util.List;

public class ModConfiguredFeatures {
    // 1. 定义 3 个 Key
    public static final RegistryKey<ConfiguredFeature<?, ?>> SPIRITUAL_MEDIUM_ORE_KEY = registerKey("spiritual_medium_ore");
    public static final RegistryKey<ConfiguredFeature<?, ?>> REFINING_STONE_ORE_KEY = registerKey("refining_stone_ore");
    public static final RegistryKey<ConfiguredFeature<?, ?>> ILLUSIONARY_ORE_KEY = registerKey("illusionary_ore");

    public static void bootstrap(Registerable<ConfiguredFeature<?, ?>> context) {
        RuleTest stoneReplaceables = new TagMatchRuleTest(BlockTags.STONE_ORE_REPLACEABLES);
        RuleTest deepslateReplaceables = new TagMatchRuleTest(BlockTags.DEEPSLATE_ORE_REPLACEABLES);

        // 注册：灵媒石 (替换石头和深板岩)
        register(context, SPIRITUAL_MEDIUM_ORE_KEY, Feature.ORE, new OreFeatureConfig(List.of(
                OreFeatureConfig.createTarget(stoneReplaceables, ModBlocks.SPIRITUAL_MEDIUM_STONE_BLOCK.getDefaultState()),
                OreFeatureConfig.createTarget(deepslateReplaceables, ModBlocks.SPIRITUAL_MEDIUM_STONE_BLOCK.getDefaultState())), 1));

        // 注册：洗练石
        register(context, REFINING_STONE_ORE_KEY, Feature.ORE, new OreFeatureConfig(List.of(
                OreFeatureConfig.createTarget(stoneReplaceables, ModBlocks.REFINING_STONE_BLOCK.getDefaultState())), 1));

        // 注册：幻化石 (这就是你报错里缺少的那个！)
        register(context, ILLUSIONARY_ORE_KEY, Feature.ORE, new OreFeatureConfig(List.of(
                OreFeatureConfig.createTarget(stoneReplaceables, ModBlocks.ILLUSIONARY_BLOCK.getDefaultState())), 1));
    }

    public static RegistryKey<ConfiguredFeature<?, ?>> registerKey(String name) {
        return RegistryKey.of(RegistryKeys.CONFIGURED_FEATURE, Identifier.of(PrimalMagic.MOD_ID, name));
    }

    private static <FC extends net.minecraft.world.gen.feature.FeatureConfig, F extends Feature<FC>> void register(Registerable<ConfiguredFeature<?, ?>> context, RegistryKey<ConfiguredFeature<?, ?>> key, F feature, FC configuration) {
        context.register(key, new ConfiguredFeature<>(feature, configuration));
    }
}