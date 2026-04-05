package com.Primal.screen;

import com.Primal.PrimalMagic;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class MagicBindingTableScreen extends HandledScreen<MagicBindingTableScreenHandler> {
    // 确保图片路径正确
    private static final Identifier TEXTURE = Identifier.of(PrimalMagic.MOD_ID, "textures/gui/container/magic_binding_table.png");

    public MagicBindingTableScreen(MagicBindingTableScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
        // 设置窗口基础宽高
        this.backgroundWidth = 176;
        this.backgroundHeight = 166;
    }

    @Override
    protected void init() {
        super.init();
        // 标题垂直偏移一下，避开顶部的装饰
        titleY = 6;
        playerInventoryTitleY = 72;
    }

    @Override
    protected void drawBackground(DrawContext context, float delta, int mouseX, int mouseY) {
        RenderSystem.setShader(GameRenderer::getPositionTexProgram);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, TEXTURE);
        int x = (width - backgroundWidth) / 2;
        int y = (height - backgroundHeight) / 2;

        // 1. 绘制星空背景
        context.drawTexture(TEXTURE, x, y, 0, 0, backgroundWidth, backgroundHeight, 256, 256);

        // 2. 绘制上方三个魔法槽位的底色
        // 注意：这里我们传入 3 个参数：context, x 坐标, y 坐标
        drawMagicSlot(context, x + 47, y + 34);
        drawMagicSlot(context, x + 75, y + 34);
        drawMagicSlot(context, x + 133, y + 34);

        // 3. 绘制下方玩家背包区域的底色（0x80000000 是半透明黑）
        // 1.21 的 fill 参数为: (x1, y1, x2, y2, color)
        context.fill(x + 7, y + 83, x + 7 + 162, y + 83 + 54, 0x80000000);
        context.fill(x + 7, y + 141, x + 7 + 162, y + 141 + 18, 0x80000000);
    }

    // 修复方法的定义：确保它接收 3 个参数
    private void drawMagicSlot(DrawContext context, int x, int y) {
        context.fill(x, y, x + 18, y + 18, 0x80000000);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        renderBackground(context, mouseX, mouseY, delta);
        super.render(context, mouseX, mouseY, delta);
        drawMouseoverTooltip(context, mouseX, mouseY);
    }

    // 在 MagicBindingTableScreen.java 中添加：
    @Override
    protected void drawForeground(DrawContext context, int mouseX, int mouseY) {
        // 0x000000 是纯黑色，0xFFFFFF 是白色
        // 我们把标题改为黑色，并调整一下位置
        context.drawText(this.textRenderer, this.title, this.titleX, this.titleY, 0x000000, false);
        context.drawText(this.textRenderer, this.playerInventoryTitle, this.playerInventoryTitleX, this.playerInventoryTitleY, 0x000000, false);
    }


}