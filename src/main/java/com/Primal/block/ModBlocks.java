package com.Primal.block;

import com.Primal.PrimalMagic;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.MapColor;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import oshi.jna.platform.mac.SystemB;

public class ModBlocks {
    public static final Block SPIRITUAL_MEDIUM_STONE_BLOCK = register("spiritual_medium_stone_block",new Block(AbstractBlock.Settings.create().requiresTool().strength(3.0f,3.0f)));

    public static final Block REFINING_STONE_BLOCK = register("refining_stone_block",new Block(AbstractBlock.Settings.create().requiresTool().strength(3.0f,3.0f)));

    public static final Block ILLUSIONARY_BLOCK = register("illusionary_block",new Block(AbstractBlock.Settings.create().requiresTool().strength(66.0f,45.5f)));

    public static final Block MAGIC_BINDING_TABLE = register("magic_binding_table",
            new MagicBindingTableBlock(AbstractBlock.Settings.create().mapColor(MapColor.BLACK).requiresTool().strength(2.5f, 2.5f).nonOpaque()));

    //注册mingyuan核心的方块

    public static final Block MINGYUAN_CORE = register("mingyuan_core",
            new MingYuanCoreBlock(AbstractBlock.Settings.create().strength(-1.0f, 3600000.0f))); // 基岩硬度

    public static  void registerBlockItem(String id,Block block){
        Item item = Registry.register(Registries.ITEM,Identifier.of(PrimalMagic.MOD_ID,id),new BlockItem(block,new Item.Settings()));
            if(item instanceof BlockItem){
                ((BlockItem)item).appendBlocks(Item.BLOCK_ITEMS,item);
            }
    }

    public static Block register(String id, Block block) {
        registerBlockItem(id,block);
        return Registry.register(Registries.BLOCK, Identifier.of(PrimalMagic.MOD_ID,id), block);
    }

    public static void registerModBlock(){
        PrimalMagic.LOGGER.info("Registering Mod Block");//初始化方法
    }
}
