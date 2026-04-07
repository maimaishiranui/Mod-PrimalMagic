package com.Primal.screen;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;

public class MagicBindingTableScreenHandler extends ScreenHandler {
    private final Inventory inventory;

    public MagicBindingTableScreenHandler(int syncId, PlayerInventory playerInventory) {
        this(syncId, playerInventory, new SimpleInventory(3));
    }

    public MagicBindingTableScreenHandler(int syncId, PlayerInventory playerInventory, Inventory inventory) {
        super(ModScreenHandlers.MAGIC_BINDING_TABLE_SCREEN_HANDLER, syncId);
        checkSize(inventory, 3);
        this.inventory = inventory;
        inventory.onOpen(playerInventory.player);

        // 1. 魔法绑定台的 3 个自定义槽位
        this.addSlot(new Slot(inventory, 0, 48, 35));   // 输入 1
        this.addSlot(new Slot(inventory, 1, 76, 35));   // 输入 2
        this.addSlot(new Slot(inventory, 2, 134, 35));  // 输出结果

        // 2. 【核心修复】添加玩家主背包 (3行 x 9列)
        // 标准间距是 18 像素
        for (int row = 0; row < 3; ++row) {
            for (int col = 0; col < 9; ++col) {
                // 参数说明: 物品栏, 索引, 屏幕X坐标, 屏幕Y坐标
                // 索引从 9 开始，X 从 8 开始每格递增 18，Y 从 84 开始每行递增 18
                this.addSlot(new Slot(playerInventory, col + row * 9 + 9, 8 + col * 18, 84 + row * 18));
            }
        }

        // 3. 【核心修复】添加玩家快捷栏 (1行 x 9列)
        for (int col = 0; col < 9; ++col) {
            // 快捷栏索引是 0-8，Y 坐标通常固定在 142
            this.addSlot(new Slot(playerInventory, col, 8 + col * 18, 142));
        }
    }

    @Override
    public ItemStack quickMove(PlayerEntity player, int invSlot) {
        ItemStack newStack = ItemStack.EMPTY;
        Slot slot = this.slots.get(invSlot);
        if (slot != null && slot.hasStack()) {
            ItemStack originalStack = slot.getStack();
            newStack = originalStack.copy();
            if (invSlot < this.inventory.size()) {
                if (!this.insertItem(originalStack, this.inventory.size(), this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.insertItem(originalStack, 0, this.inventory.size(), false)) {
                return ItemStack.EMPTY;
            }

            if (originalStack.isEmpty()) {
                slot.setStack(ItemStack.EMPTY);
            } else {
                slot.markDirty();
            }
        }
        return newStack;
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return this.inventory.canPlayerUse(player);
    }

    private void addPlayerInventory(PlayerInventory playerInventory) {
        for (int i = 0; i < 3; ++i) {
            for (int l = 0; l < 9; ++l) {
                this.addSlot(new Slot(playerInventory, l + i * 9 + 9, 8 + i * 18, 84 + i * 18));
            }
        }
    }

    private void addPlayerHotbar(PlayerInventory playerInventory) {
        for (int i = 0; i < 9; ++i) {
            this.addSlot(new Slot(playerInventory, i, 8 + i * 18, 142));
        }
    }
}