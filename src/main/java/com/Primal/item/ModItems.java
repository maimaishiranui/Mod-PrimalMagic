package com.Primal.item;

import com.Primal.PrimalMagic;
import com.Primal.block.ModBlocks;
import net.minecraft.item.BlockItem;
import  net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class ModItems {

    // 1. 学识权杖 (Scholar) - 金级耐久度 (32)
    public static final Item SCHOLAR_SCEPTRE = registerItem("scholar_sceptre",
            new SceptreItem(new Item.Settings().maxCount(1).maxDamage(32).fireproof()));

    // 2. 大师权杖 (Master) - 石级耐久度 (131)
    public static final Item MASTER_SCEPTRE = registerItem("master_sceptre",
            new SceptreItem(new Item.Settings().maxCount(1).maxDamage(131).fireproof()));

    // 3. 教皇权杖 (Pastoral) - 铁级耐久度 (250)
    public static final Item PASTORAL_SCEPTRE = registerItem("pastoral_sceptre",
            new SceptreItem(new Item.Settings().maxCount(1).maxDamage(250).fireproof()));

    // 4. 权力法杖 (Power) - 750 耐久度 (介于铁与钻之间)
    public static final Item POWER_SCEPTRE = registerItem("power_sceptre",
            new SceptreItem(new Item.Settings().maxCount(1).maxDamage(750).fireproof()));

    // 5. 原初法杖 (Original) - 钻石级耐久度 (1561)
    public static final Item ORIGINAL_SCEPTRE = registerItem("original_sceptre",
            new SceptreItem(new Item.Settings().maxCount(1).maxDamage(1561).fireproof()));


    public static final Item MAGIC_INTRODUCTION = registerItem("magic_introduction",
            new Item(new Item.Settings()));

    public static final Item ADVANCED_MAGIC_INTRODUCTION = registerItem("advanced_magic_introduction",
            new Item(new Item.Settings()));

    public static  final  Item SPIRITUAL_MEDIUM_STONE = registerItem("spiritual_medium_stone",new Item(new Item.Settings().fireproof()));
    //灵媒石
    public static  final  Item REFINING_STONE = registerItem("refining_stone",new Item(new Item.Settings().fireproof()));
    //洗练石
    public static  final  Item ILLUSIONARY_TRANSFORMATION_CURSE = registerItem("illusionary_transformation_curse",new Item(new Item.Settings().fireproof()));
    //幻化魔咒


    //===符文武器==//
    public static  final  Item BREEZE_TALISMAN = registerItem("breeze_talisman",new Item(new Item.Settings().fireproof()));

    public static  final  Item THUNERCLAP_TALISMAN = registerItem("thunderclap_talisman",new Item(new Item.Settings().fireproof()));

    public static  final  Item SPIRITWOOD_TALISMAN = registerItem("spiritwood_talisman",new Item(new Item.Settings().fireproof()));

    public static  final  Item ORIGINALSTREAM_TALISMAN = registerItem("originalstream_talisman",new Item(new Item.Settings().fireproof()));

    public static  final  Item FLAME_TALISMAN = registerItem("flame_talisman",new Item(new Item.Settings().fireproof()));

    public static  final  Item FROZEN_TALISMAN = registerItem("frozen_talisman",new Item(new Item.Settings().fireproof()));

    public static final Item ROCK_TALISMAN = registerItem("rock_talisman",new Item(new Item.Settings().fireproof()));

    //===Boss掉落物===//
    public static final Item SOUL_OF_MINGYUAN = registerItem("soul_of_mingyuan",new Item(new Item.Settings().fireproof()));
    //进入原初维度时必须击败的Boss所需的掉落物
    public static final Item ELEMENTAL_MASTERMIND = registerItem("elemental_mastermind",new Item(new Item.Settings().fireproof()));
    //元素之母掉落物
    public static final Item VASTNESS_ZEPHYR = registerItem("vastness_zephyr",new Item(new Item.Settings().fireproof()));
    //虚空之风掉落物
    public static final Item VASTNESS_EMBER = registerItem("vastness_ember",new Item(new Item.Settings().fireproof()));
    //虚空之火掉落物
    public static final Item VASTNESS_GLACIATE = registerItem("vastness_glaciate",new Item(new Item.Settings().fireproof()));
    //虚空之冰掉落物
    public static final Item VASTNESS_STREAM = registerItem("vastness_stream",new Item(new Item.Settings().fireproof()));
    //虚空之水掉落物
    public static final Item VASTNESS_ADAMANTROCK = registerItem("vastness_adamantrock",new Item(new Item.Settings().fireproof()));
    //虚空之土掉落物
    public static final Item VASTNESS_THUNDERCLAP = registerItem("vastness_thunderclap",new Item(new Item.Settings().fireproof()));
    //虚空之雷掉落物
    public static final Item VASTNESS_VERDANT = registerItem("vastness_verdant",new Item(new Item.Settings().fireproof()));
    //虚空之木掉落物


    private static  Item registerItem(String id, Item item) {
      //  return Registry.register(Registries.ITEM, RegistryKey.of(Registries.ITEM.getKey(),Identifier.of(PrimalMagic.MOD_ID,id)), item);
        return Registry.register(Registries.ITEM,Identifier.of(PrimalMagic.MOD_ID,id), item);

    }//注册方法

    public static void registerModItems() {
        PrimalMagic.LOGGER.info("Registering Mod Items");
        //初始化之后打印一行日志
    }//用于初始化的方法
}
