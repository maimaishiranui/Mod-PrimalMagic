package com.Primal.item;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import java.util.List;

public class AncientKeyItem extends Item {
    public AncientKeyItem(Settings settings) {
        super(settings);
    }

    // 让物品在物品栏里发光
    @Override
    public boolean hasGlint(ItemStack stack) {
        return true;
    }

    @Override
    public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
        tooltip.add(Text.literal("感受到一股强烈的虚空拉扯感...").formatted(Formatting.GRAY, Formatting.ITALIC));
        tooltip.add(Text.literal("似乎能开启通往某个孤立空间的裂缝").formatted(Formatting.DARK_PURPLE));
        super.appendTooltip(stack, context, tooltip, type);
    }
}