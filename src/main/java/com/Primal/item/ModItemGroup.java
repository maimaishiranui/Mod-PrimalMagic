package com.Primal.item;

import com.Primal.PrimalMagic;
import com.Primal.block.ModBlocks;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class ModItemGroup {

    public static final RegistryKey<ItemGroup> PRIMAL_GROUP = register("primalmagic_group");

    private static RegistryKey<ItemGroup> register(String id) {
        return RegistryKey.of(RegistryKeys.ITEM_GROUP, Identifier.of(PrimalMagic.MOD_ID, id));
    }


   /* public static void registerModItemGroup() {
        Registry.register(Registries.ITEM_GROUP,PRIMAL_GROUP,
            ItemGroup.create(ItemGroup.Row.TOP,7)
                    .displayName(Text.translatable("itemGroup.primalmagic_group"))
                    .icon(()->new ItemStack(ModItems.SCHOLAR_SCEPTRE))
                    .entries((displayContext, entries) -> {
                        entries.add(ModItems.MASTER_SCEPTRE);
                        entries.add(ModItems.PASTORAL_SCEPTRE);
                        entries.add(ModItems.POWER_SCEPTRE);
                        entries.add(ModItems.ORIGINAL_SCEPTRE);
                        entries.add(ModItems.MAGIC_INTRODUCTION);
                        entries.add(ModItems.REFINING_STONE);
                        entries.add(ModItems.SPIRITUAL_MEDIUM_STONE);
                        entries.add(ModItems.ADVANCED_MAGIC_INTRODUCTION);
                        entries.add(ModItems.ILLUSIONARY_TRANSFORMATION_CURSE);
                        entries.add(ModItems.BREEZE_TALISMAN);
                        entries.add(ModItems.THUNERCLAP_TALISMAN);
                        entries.add(ModItems.SPIRITWOOD_TALISMAN);
                        entries.add(ModItems.ORIGINALSTREAM_TALISMAN);
                        entries.add(ModItems.FLAME_TALISMAN);
                        entries.add(ModItems.FROZEN_TALISMAN);
                    }).build());

        PrimalMagic.LOGGER.info("Primal Magic Group Registered");
        //物品栏注册

    }
*/

    public  static final ItemGroup PRIMALMAGIC_GROUP=Registry.register(Registries.ITEM_GROUP,Identifier.of(PrimalMagic.MOD_ID,"primalmagic_group"),
            ItemGroup.create(null,-1).displayName(Text.translatable("itemGroup.primalmagic_group"))
                    .icon(()-> new ItemStack(ModItems.SCHOLAR_SCEPTRE))
                    .entries((displayContext, entries) -> {

                        //物品加入物品栏的注册
                        entries.add(ModItems.SCHOLAR_SCEPTRE);
                        entries.add(ModItems.MASTER_SCEPTRE);
                        entries.add(ModItems.PASTORAL_SCEPTRE);
                        entries.add(ModItems.POWER_SCEPTRE);
                        entries.add(ModItems.ORIGINAL_SCEPTRE);
                        entries.add(ModItems.MAGIC_INTRODUCTION);
                        entries.add(ModItems.REFINING_STONE);
                        entries.add(ModItems.SPIRITUAL_MEDIUM_STONE);
                        entries.add(ModItems.ADVANCED_MAGIC_INTRODUCTION);
                        entries.add(ModItems.ILLUSIONARY_TRANSFORMATION_CURSE);
                        entries.add(ModItems.BREEZE_TALISMAN);
                        entries.add(ModItems.THUNERCLAP_TALISMAN);
                        entries.add(ModItems.SPIRITWOOD_TALISMAN);
                        entries.add(ModItems.ORIGINALSTREAM_TALISMAN);
                        entries.add(ModItems.FLAME_TALISMAN);
                        entries.add(ModItems.FROZEN_TALISMAN);
                        entries.add(ModItems.ROCK_TALISMAN);
                        //方块加入物品栏的注册

                        entries.add(ModBlocks.SPIRITUAL_MEDIUM_STONE_BLOCK);
                        entries.add(ModBlocks.MAGIC_BINDING_TABLE);
                        entries.add(ModBlocks.ILLUSIONARY_BLOCK);
                        entries.add(ModBlocks.REFINING_STONE_BLOCK);
                        entries.add(ModBlocks.MINGYUAN_CORE);

                        //Boss掉落物的加入物品栏的注册
                        entries.add(ModItems.SOUL_OF_MINGYUAN);
                        entries.add(ModItems.ELEMENTAL_MASTERMIND);
                        entries.add(ModItems.VASTNESS_ZEPHYR);
                        entries.add(ModItems.VASTNESS_EMBER);
                        entries.add(ModItems.VASTNESS_GLACIATE);
                        entries.add(ModItems.VASTNESS_STREAM);
                        entries.add(ModItems.VASTNESS_ADAMANTROCK);
                        entries.add(ModItems.VASTNESS_THUNDERCLAP);
                        entries.add(ModItems.VASTNESS_VERDANT);
                    }).build());

    public static void registerItemGroups() {
        PrimalMagic.LOGGER.info("Primal Magic Group Registered");
        //初始化物品栏的方法
    }
}
