package com.Primal;

import com.Primal.screen.MagicBindingTableScreen;
import com.Primal.screen.ModScreenHandlers;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.gui.screen.ingame.HandledScreens;

public class PrimalMagicClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        HandledScreens.register(ModScreenHandlers.MAGIC_BINDING_TABLE_SCREEN_HANDLER, MagicBindingTableScreen::new);
    }
}
