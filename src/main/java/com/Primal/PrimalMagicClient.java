package com.Primal;

import com.Primal.entity.ModEntities;
import com.Primal.screen.MagicBindingTableScreen;
import com.Primal.screen.ModScreenHandlers;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.gui.screen.ingame.HandledScreens;
import com.Primal.entity.client.MingYuanRenderer;

public class PrimalMagicClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        HandledScreens.register(ModScreenHandlers.MAGIC_BINDING_TABLE_SCREEN_HANDLER, MagicBindingTableScreen::new);

        EntityRendererRegistry.register(ModEntities.MINGYUAN, MingYuanRenderer::new);

        PrimalMagic.LOGGER.info("Primal Magic Client Initialized!");
    }
}
