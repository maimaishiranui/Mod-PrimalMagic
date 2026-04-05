package com.Primal.datagen;

import com.Primal.block.ModBlocks;
import com.Primal.item.ModItems;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricRecipeProvider;
import net.minecraft.data.server.recipe.RecipeExporter;
import net.minecraft.data.server.recipe.ShapedRecipeJsonBuilder;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.Items;
import net.minecraft.recipe.book.RecipeCategory;
import net.minecraft.registry.RegistryWrapper;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class ModRecipesProvider extends FabricRecipeProvider {

    // 替换为你的物品注册类中的实际物品引用
    public static final List<ItemConvertible> ILLUSIONARY_Block= List.of(ModItems.ILLUSIONARY_TRANSFORMATION_CURSE);
    public static final List<ItemConvertible> REFINING_STONE_Block = List.of(ModItems.REFINING_STONE);
    public static final List<ItemConvertible> SPIRITUAL_MEDIUM_STONE_Block = List.of(ModItems.SPIRITUAL_MEDIUM_STONE);

    public ModRecipesProvider(FabricDataOutput output, CompletableFuture<RegistryWrapper.WrapperLookup> registriesFuture) {
        super(output, registriesFuture);
    }

    @Override
    public void generate(RecipeExporter exporter) {

            //熔炉的烧制配方

        //幻化石
        offerSmelting(exporter,ILLUSIONARY_Block,RecipeCategory.MISC,ModItems.ILLUSIONARY_TRANSFORMATION_CURSE
                , 8.0f, 200, "ic_from_smelting_ib"
                    );

        //洗炼石
        offerSmelting(exporter,REFINING_STONE_Block,RecipeCategory.MISC,ModItems.REFINING_STONE
                , 2.0f, 150, "rs_from_smelting_rsb"
                    );

        //灵媒石
        offerSmelting(exporter,SPIRITUAL_MEDIUM_STONE_Block,RecipeCategory.MISC,ModItems.SPIRITUAL_MEDIUM_STONE
                , 2.0f, 150, "sms_from_smelting_smsb"
                    );


            //高炉的烧制配方

        offerBlasting(exporter,ILLUSIONARY_Block,RecipeCategory.MISC,ModItems.ILLUSIONARY_TRANSFORMATION_CURSE
                , 10.0f, 150, "ic_from_blasting_ib"
                    );

        offerBlasting(exporter,REFINING_STONE_Block,RecipeCategory.MISC,ModItems.REFINING_STONE
                , 2.5f, 80, "rs_from_blasting_rsb"
                    );

        offerBlasting(exporter,SPIRITUAL_MEDIUM_STONE_Block,RecipeCategory.MISC,ModItems.SPIRITUAL_MEDIUM_STONE
                , 1.0f, 75, "sms_from_blasting_smsb"
                    );



        // 有序合成配方（如法杖）
        ShapedRecipeJsonBuilder.create(RecipeCategory.MISC, ModItems.SCHOLAR_SCEPTRE, 1)
                .pattern("GI ")  // 第一行
                .pattern("I#L")  // 第二行
                .pattern(" L#")  // 第三行
                .input('G', Items.GOLD_BLOCK)  // 绑定键 'G' 到金块
                .input('I', Items.IRON_INGOT)  // 绑定键 'I' 到铁锭
                .input('#', Items.STICK)       // 绑定键 '#' 到木棍
                .input('L', Items.STRING)      // 绑定键 'L' 到线
                .group("scholar_sceptre")      // 设置配方组名
                .criterion(hasItem(Items.GOLD_BLOCK), conditionsFromItem(Items.GOLD_BLOCK))  // 可以解锁对应配方的触发条件
                .offerTo(exporter);            // 提交配方


        // magic_introduction 配方 1
        ShapedRecipeJsonBuilder.create(RecipeCategory.MISC, ModItems.MAGIC_INTRODUCTION)
                .pattern("BMB")
                .pattern("RES")
                .pattern("III")
                .input('B', Items.BOOK)
                .input('M', Items.WRITTEN_BOOK) // 自引用配方（需确认合理性）
                .input('R', ModItems.REFINING_STONE)
                .input('E', Items.END_CRYSTAL)
                .input('S', Items.SPECTRAL_ARROW) // 原 JSON 中 "spiritual_medium_stone" 可能为笔误，此处修正为原版物品
                .input('I', ModItems.ILLUSIONARY_TRANSFORMATION_CURSE)
                .group("primalmagic:magic_introduction")
                .criterion(hasItem(Items.END_CRYSTAL), conditionsFromItem(Items.END_CRYSTAL))
                .offerTo(exporter);

        // breeze_talisman 配方
        ShapedRecipeJsonBuilder.create(RecipeCategory.MISC, ModItems.BREEZE_TALISMAN)
                .pattern("LZL")
                .pattern("LEL")
                .pattern("LEL")
                .input('L', Items.LAPIS_BLOCK)
                .input('Z', ModItems.VASTNESS_ZEPHYR)
                .input('E', ModItems.ELEMENTAL_MASTERMIND)
                .group("breeze_talisman")
                .criterion(hasItem(ModItems.VASTNESS_ZEPHYR), conditionsFromItem(ModItems.VASTNESS_ZEPHYR))
                .offerTo(exporter);

        // flame_talisman 配方
        ShapedRecipeJsonBuilder.create(RecipeCategory.MISC, ModItems.FLAME_TALISMAN)
                .pattern("LML")
                .pattern("LEL")
                .pattern("LEL")
                .input('L', Items.LAPIS_BLOCK)
                .input('M', ModItems.VASTNESS_EMBER)
                .input('E', ModItems.ELEMENTAL_MASTERMIND)
                .group("flame_talisman")
                .criterion(hasItem(ModItems.VASTNESS_EMBER), conditionsFromItem(ModItems.VASTNESS_EMBER))
                .offerTo(exporter);


        ShapedRecipeJsonBuilder.create(RecipeCategory.DECORATIONS, ModBlocks.MAGIC_BINDING_TABLE)
                .pattern("SSS")
                .pattern("S#S")
                .pattern("SSS")
                .input('S', Items.STONE)               // 暂时用石头，你可以改成你想要的材料
                .input('#', ModItems.REFINING_STONE)    // 使用你的洗练石作为核心
                .criterion(hasItem(ModItems.REFINING_STONE), conditionsFromItem(ModItems.REFINING_STONE))
                .offerTo(exporter);



        //有序合成的配方模板
        /*

        // flame_talisman 配方
        ShapedRecipeJsonBuilder.create(RecipeCategory.MISC, ModItems.FLAME_TALISMAN)
            .pattern("LML")
            .pattern("LEL")
            .pattern("LEL")
            .input('L', Items.LAPIS_BLOCK)
            .input('M', ModItems.VASTNESS_EMBER)
            .input('E', ModItems.ELEMENTAL_MASTERMIND)
            .group("flame_talisman")
            .criterion(hasItem(ModItems.VASTNESS_EMBER), conditionsFromItem(ModItems.VASTNESS_EMBER))
            .offerTo(exporter);
         */









        //无序合成配方（示例）
        /*
         ShapelessRecipeJsonBuilder.create(RecipeCategory.BUILDING_BLOCKS, ModBlocks.ICE_ETHER_ORE)
           .input(ModItems.RAW_ICE_ETHER)
           .input(Items.STONE)
           .criterion("has_raw_ice_ether", conditionsFromItem(ModItems.RAW_ICE_ETHER))
           .criterion("has_stone", conditionsFromItem(Items.STONE))
           .offerTo(exporter, Identifier.of(TutorialMod.MOD_ID, "ice_ether_ore"));
         */



    }
}