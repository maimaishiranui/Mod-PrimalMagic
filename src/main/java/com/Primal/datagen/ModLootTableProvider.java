package com.Primal.datagen;

import com.Primal.block.ModBlocks;
import com.Primal.item.ModItems;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricBlockLootTableProvider;
import net.minecraft.block.Block;
import net.minecraft.data.DataGenerator;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.item.Item;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.entry.ItemEntry;
import net.minecraft.loot.entry.LootPoolEntry;
import net.minecraft.loot.function.ApplyBonusLootFunction;
import net.minecraft.loot.function.SetContentsLootFunction;
import net.minecraft.loot.provider.number.UniformLootNumberProvider;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;

import java.util.concurrent.CompletableFuture;

public class ModLootTableProvider extends FabricBlockLootTableProvider {
    public ModLootTableProvider(FabricDataOutput dataOutput, CompletableFuture<RegistryWrapper.WrapperLookup> registryLookup) {
        super(dataOutput, registryLookup);


    }

    @Override
    public void generate() {
        // 1. 魔法绑定台：掉落自身（默认不受时运影响）
        addDrop(ModBlocks.MAGIC_BINDING_TABLE);

        // 2. 幻化方块、洗练石方块、灵媒石方块
        // 需求：掉落特定物品，且固定掉落1个，不受时运（附魔）影响

        // 如果你【不想要】丝绸之触（即用丝触挖也只掉物品，不掉方块本身）：
        addDrop(ModBlocks.ILLUSIONARY_BLOCK, drops(ModItems.ILLUSIONARY_TRANSFORMATION_CURSE));
        addDrop(ModBlocks.REFINING_STONE_BLOCK, drops(ModItems.REFINING_STONE));
        addDrop(ModBlocks.SPIRITUAL_MEDIUM_STONE_BLOCK, drops(ModItems.SPIRITUAL_MEDIUM_STONE));

    /*
       注意：如果你希望“丝绸之触”能挖出方块本身，但“普通挖掘”只掉1个且没时运加成，请使用：
       addDrop(ModBlocks.REFINING_STONE_BLOCK, dropsWithSilkTouch(ModBlocks.REFINING_STONE_BLOCK, ItemEntry.builder(ModItems.REFINING_STONE)));
    */
    }
    
    public LootTable.Builder copperOreLinkDrops(Block drop, Item dropItem) {
        RegistryWrapper.Impl<Enchantment> impl = this.registryLookup.getWrapperOrThrow(RegistryKeys.ENCHANTMENT);
        return this.dropsWithSilkTouch(
                drop,
                (LootPoolEntry.Builder<?>) this.applyExplosionDecay(
                        drop,
                        ItemEntry.builder(dropItem)
                                .apply(net.minecraft.loot.function.SetCountLootFunction.builder(UniformLootNumberProvider.create(1.0F, 2.0F)))
                                .apply(ApplyBonusLootFunction.oreDrops(impl.getOrThrow(Enchantments.FORTUNE)))
                )
        );
    }


}
