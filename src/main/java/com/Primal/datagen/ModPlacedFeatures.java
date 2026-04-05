package com.Primal.datagen;

import com.Primal.PrimalMagic;
import net.minecraft.registry.Registerable;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;
import net.minecraft.world.gen.YOffset;
import net.minecraft.world.gen.feature.ConfiguredFeature;
import net.minecraft.world.gen.feature.PlacedFeature;
import net.minecraft.world.gen.placementmodifier.*;

import java.util.List;

public class ModPlacedFeatures {
    // 1. 定义 3 个 Placed Key
    public static final RegistryKey<PlacedFeature> SPIRITUAL_MEDIUM_ORE_PLACED_KEY = registerKey("spiritual_medium_ore_placed");
    public static final RegistryKey<PlacedFeature> REFINING_STONE_ORE_PLACED_KEY = registerKey("refining_stone_ore_placed");
    public static final RegistryKey<PlacedFeature> ILLUSIONARY_ORE_PLACED_KEY = registerKey("illusionary_ore_placed");

    public static void bootstrap(Registerable<PlacedFeature> context) {
        var configuredLookup = context.getRegistryLookup(RegistryKeys.CONFIGURED_FEATURE);

        // 注册灵媒石生成 (100次尝试，越高越多)
        register(context, SPIRITUAL_MEDIUM_ORE_PLACED_KEY, configuredLookup.getOrThrow(ModConfiguredFeatures.SPIRITUAL_MEDIUM_ORE_KEY),
                modifiers(CountPlacementModifier.of(100), HeightRangePlacementModifier.trapezoid(YOffset.fixed(-16), YOffset.fixed(480))));

        // 注册洗练石生成
        register(context, REFINING_STONE_ORE_PLACED_KEY, configuredLookup.getOrThrow(ModConfiguredFeatures.REFINING_STONE_ORE_KEY),
                modifiers(CountPlacementModifier.of(100), HeightRangePlacementModifier.trapezoid(YOffset.fixed(-16), YOffset.fixed(480))));

        // 注册幻化石生成 (补齐这一行！)
        register(context, ILLUSIONARY_ORE_PLACED_KEY, configuredLookup.getOrThrow(ModConfiguredFeatures.ILLUSIONARY_ORE_KEY),
                modifiers(CountPlacementModifier.of(100), HeightRangePlacementModifier.trapezoid(YOffset.fixed(-16), YOffset.fixed(480))));
    }

    private static List<PlacementModifier> modifiers(PlacementModifier countModifier, PlacementModifier heightModifier) {
        return List.of(countModifier, SquarePlacementModifier.of(), heightModifier, BiomePlacementModifier.of());
    }

    public static RegistryKey<PlacedFeature> registerKey(String name) {
        return RegistryKey.of(RegistryKeys.PLACED_FEATURE, Identifier.of(PrimalMagic.MOD_ID, name));
    }

    private static void register(Registerable<PlacedFeature> context, RegistryKey<PlacedFeature> key, RegistryEntry<ConfiguredFeature<?, ?>> config, List<PlacementModifier> modifiers) {
        context.register(key, new PlacedFeature(config, List.copyOf(modifiers)));
    }
}