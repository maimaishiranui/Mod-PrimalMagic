package com.Primal.screen;

import com.Primal.PrimalMagic;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.util.Identifier;
import net.minecraft.resource.featuretoggle.FeatureSet;

public class ModScreenHandlers {
    public static final ScreenHandlerType<MagicBindingTableScreenHandler> MAGIC_BINDING_TABLE_SCREEN_HANDLER =
            Registry.register(Registries.SCREEN_HANDLER, Identifier.of(PrimalMagic.MOD_ID, "magic_binding_table"),
                    new ScreenHandlerType<>(MagicBindingTableScreenHandler::new, FeatureSet.empty())); // 使用 FeatureSet.empty()

    public static void registerScreenHandlers() {
        PrimalMagic.LOGGER.info("Registering Screen Handlers for " + PrimalMagic.MOD_ID);
    }
}

