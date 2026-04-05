package com.Primal.component;

import com.Primal.PrimalMagic;
import net.minecraft.component.ComponentType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import java.util.function.UnaryOperator;

public class ModDataComponentTypes {
    // 存储绑定的魔法名称（字符串）
    public static final ComponentType<String> BOUND_MAGIC = register("bound_magic", builder -> builder.codec(net.minecraft.util.dynamic.Codecs.NON_EMPTY_STRING));

    private static <T> ComponentType<T> register(String name, UnaryOperator<ComponentType.Builder<T>> builderOperator) {
        return Registry.register(Registries.DATA_COMPONENT_TYPE, Identifier.of(PrimalMagic.MOD_ID, name), (builderOperator.apply(ComponentType.builder())).build());
    }

    // 存储护符类型 (如 "Breeze", "Flame" 等)
    public static final ComponentType<String> TALISMAN_TYPE = register("talisman_type", builder -> builder.codec(net.minecraft.util.dynamic.Codecs.NON_EMPTY_STRING));

    public static void registerDataComponentTypes() {
        PrimalMagic.LOGGER.info("Registering Data Component Types for " + PrimalMagic.MOD_ID);
    }
}