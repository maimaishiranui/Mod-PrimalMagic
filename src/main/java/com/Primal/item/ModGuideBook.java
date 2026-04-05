package com.Primal.item;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.WrittenBookContentComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.RawFilteredPair;
import net.minecraft.text.Text;
import java.util.ArrayList;
import java.util.List;

public class ModGuideBook {

    public static ItemStack create() {
        ItemStack book = new ItemStack(Items.WRITTEN_BOOK);
        List<RawFilteredPair<Text>> pages = new ArrayList<>();

        // --- 第 1 页：欢迎 ---
        pages.add(RawFilteredPair.of(Text.literal("§1§l原初魔法：向导§r\n\n欢迎！这本书将指引你从凡人走向原初法师之路。\n\n作者：Mariah shiranui\n版本：0.1a-1.21")));

        // --- 第 2 页：核心方块：魔法绑定台 ---
        pages.add(RawFilteredPair.of(Text.literal("§n§0核心方块：§r\n\n§l魔法绑定台§r\n[石][石][石]\n[石][洗][石]\n[石][石][石]\n(洗=洗练石)\n\n这是所有附魔与强化的基础。")));

        // --- 第 3 页：法杖阶梯 (1/2) ---
        pages.add(RawFilteredPair.of(Text.literal("§n§0法杖合成 (1)：§r\n\n§l学识权杖§r\n[金][铁][ ]\n[铁][棒][线]\n[ ][线][棒]\n\n§l大师权杖§r\n[石][石][ ]\n[石][学][石]\n[ ][石][石]")));

        // --- 第 4 页：法杖阶梯 (2/2) ---
        pages.add(RawFilteredPair.of(Text.literal("§n§0法杖合成 (2)：§r\n\n§l教皇权杖§r\n[铁][铁][ ]\n[铁][大][铁]\n[ ][铁][铁]\n\n§l权力法杖§r\n[钻][钻][ ]\n[钻][教][钻]\n[ ][钻][钻]")));

        // --- 第 5 页：原初之巅 ---
        pages.add(RawFilteredPair.of(Text.literal("§n§0终极法杖：§r\n\n§l原初法杖§r\n[下][下][ ]\n[下][权][下]\n[ ][下][下]\n(下=下界合金块)\n\n拥有最高的耐久度！")));

        // --- 第 6 页：绑定仪式 ---
        pages.add(RawFilteredPair.of(Text.literal("§n§5绑定与洗练：§r\n\n1. §l绑定§r：\n法杖 + 幻化魔咒\n§c(50%成功率)§0\n\n2. §l洗练§r：\n法杖 + 洗练石\n(100%清除法术)")));

        // --- 第 7 页：魔导书系统 ---
        pages.add(RawFilteredPair.of(Text.literal("§n§1魔导书升级：§r\n\n在绑定台中：\n§l魔导绪论§r + §l灵媒石§r\n= §l高阶魔导书§r\n\n高阶书可与护符结合，产生强大的咒文。")));

        WrittenBookContentComponent content = new WrittenBookContentComponent(
                RawFilteredPair.of("原初向导手册"),
                "Mariah shiranui",
                0,
                pages,
                true
        );

        book.set(DataComponentTypes.WRITTEN_BOOK_CONTENT, content);
        return book;
    }
}