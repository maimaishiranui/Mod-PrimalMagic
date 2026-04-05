package com.Primal.block;

import com.Primal.component.ModDataComponentTypes;
import com.Primal.item.ModItems;
import com.Primal.item.SceptreItem;
import com.Primal.screen.MagicBindingTableScreenHandler;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventories;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.Random;

public class MagicBindingTableBlockEntity extends BlockEntity implements ImplementedInventory, NamedScreenHandlerFactory {
    private final DefaultedList<ItemStack> inventory = DefaultedList.ofSize(3, ItemStack.EMPTY);
    private static final Random RANDOM = new Random();

    public MagicBindingTableBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.MAGIC_BINDING_TABLE_BE, pos, state);
    }

    @Override
    public DefaultedList<ItemStack> getItems() {
        return inventory;
    }


    @Override
    public Text getDisplayName() {
        return Text.translatable("container.primalmagic.magic_binding_table");
    }


    @Nullable
    @Override
    public ScreenHandler createMenu(int syncId, PlayerInventory playerInventory, PlayerEntity player) {
        return new MagicBindingTableScreenHandler(syncId, playerInventory, this);
    }

    @Override
    protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        super.writeNbt(nbt, registryLookup);
        Inventories.writeNbt(nbt, inventory, registryLookup);
    }

    @Override
    public void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        super.readNbt(nbt, registryLookup);
        Inventories.readNbt(nbt, inventory, registryLookup);
    }

    // --- 核心逻辑 Tick ---
    public static void tick(World world, BlockPos pos, BlockState state, MagicBindingTableBlockEntity entity) {
        if (world.isClient) return;

        ItemStack leftStack = entity.getStack(0);    // 左槽位 (载体)
        ItemStack middleStack = entity.getStack(1);  // 中槽位 (消耗材料)
        ItemStack resultStack = entity.getStack(2);  // 右槽位 (产出结果)

        // 只有输出槽为空时才开始逻辑判断
        if (resultStack.isEmpty() && !leftStack.isEmpty() && !middleStack.isEmpty()) {

            // ---------------------------------------------------------
            // 逻辑 1: 魔导书升级 (魔导绪论 + 灵媒石 -> 高阶魔导书)
            // ---------------------------------------------------------
            if (leftStack.isOf(ModItems.MAGIC_INTRODUCTION) && middleStack.isOf(ModItems.SPIRITUAL_MEDIUM_STONE)) {
                entity.setStack(2, new ItemStack(ModItems.ADVANCED_MAGIC_INTRODUCTION));
                entity.craftSuccess(world, pos,  1, 1,SoundEvents.BLOCK_ENCHANTMENT_TABLE_USE);
                return;
            }

            // ---------------------------------------------------------
            // 逻辑 2: 咒文合成 (高阶魔导书 + 护符 -> 虚空咒文)
            // ---------------------------------------------------------
            if (leftStack.isOf(ModItems.ADVANCED_MAGIC_INTRODUCTION)) {
                Item curseResult = getVastnessCurseFromTalisman(middleStack.getItem());
                if (curseResult != null) {
                    entity.setStack(2, new ItemStack(curseResult));
                    entity.craftSuccess(world, pos, 1, 1,SoundEvents.BLOCK_AMETHYST_BLOCK_CHIME);
                    return;
                }
            }

            // ---------------------------------------------------------
            // 逻辑 3: 基础法杖绑定 (法杖 + 幻化魔咒 -> 幻化法杖)
            // 50% 成功率逻辑
            // ---------------------------------------------------------
            if (leftStack.getItem() instanceof SceptreItem && middleStack.isOf(ModItems.ILLUSIONARY_TRANSFORMATION_CURSE)) {
                // 只有未绑定的法杖才能绑定基础魔法
                if (!leftStack.contains(ModDataComponentTypes.BOUND_MAGIC)) {
                    ItemStack baseSceptre = leftStack.copy();
                    entity.removeStack(0, 1);
                    entity.removeStack(1, 1);

                    if (RANDOM.nextFloat() < 0.5f) { // 50% 成功
                        baseSceptre.set(ModDataComponentTypes.BOUND_MAGIC, "Illusionary");
                        entity.setStack(2, baseSceptre);
                        world.playSound(null, pos, SoundEvents.BLOCK_AMETHYST_BLOCK_CHIME, SoundCategory.BLOCKS, 1f, 1.2f);
                    } else {
                        // 失败：返还原样，材料吞掉
                        entity.setStack(2, baseSceptre);
                        world.playSound(null, pos, SoundEvents.BLOCK_LAVA_EXTINGUISH, SoundCategory.BLOCKS, 1f, 1f);
                    }
                    entity.markDirty();
                    return;
                }
            }

            // ---------------------------------------------------------
            // 逻辑 4: 法杖洗练 (绑定法杖 + 洗练石 -> 干净法杖)
            // ---------------------------------------------------------
            if (middleStack.isOf(ModItems.REFINING_STONE)) {
                if (leftStack.contains(ModDataComponentTypes.BOUND_MAGIC)) {
                    ItemStack cleaned = leftStack.copy();
                    cleaned.remove(ModDataComponentTypes.BOUND_MAGIC);
                    cleaned.remove(ModDataComponentTypes.TALISMAN_TYPE); // 同时移除护符

                    entity.setStack(2, cleaned);
                    entity.craftSuccess(world, pos, 1, 1, SoundEvents.BLOCK_BREWING_STAND_BREW);
                    return;
                }
            }

            // ---------------------------------------------------------
            // 逻辑 5: 二次绑定 (幻化法杖 + 护符 -> 护符法杖)
            // 这里的技能逻辑在 SceptreItem 类中实现，此处仅记录属性
            // ---------------------------------------------------------
            if (leftStack.getItem() instanceof SceptreItem && leftStack.contains(ModDataComponentTypes.BOUND_MAGIC)) {
                if (leftStack.get(ModDataComponentTypes.BOUND_MAGIC).equals("Illusionary") && !leftStack.contains(ModDataComponentTypes.TALISMAN_TYPE)) {

                    String talismanName = getTalismanName(middleStack.getItem());
                    if (talismanName != null) {
                        ItemStack talismanSceptre = leftStack.copy();
                        talismanSceptre.set(ModDataComponentTypes.TALISMAN_TYPE, talismanName);

                        entity.setStack(2, talismanSceptre);
                        entity.craftSuccess(world, pos, 1, 1, SoundEvents.ITEM_TOTEM_USE);
                    }
                }
            }
        }
    }

    // 辅助方法：材料与咒文的对应关系
    private static @Nullable Item getVastnessCurseFromTalisman(Item talisman) {
        if (talisman == ModItems.BREEZE_TALISMAN) return ModItems.VASTNESS_ZEPHYR;
        if (talisman == ModItems.FLAME_TALISMAN) return ModItems.VASTNESS_EMBER;
        if (talisman == ModItems.FROZEN_TALISMAN) return ModItems.VASTNESS_GLACIATE;
        if (talisman == ModItems.THUNERCLAP_TALISMAN) return ModItems.VASTNESS_THUNDERCLAP;
        if (talisman == ModItems.ROCK_TALISMAN) return ModItems.VASTNESS_ADAMANTROCK;
        if (talisman == ModItems.ORIGINALSTREAM_TALISMAN) return ModItems.VASTNESS_STREAM;
        if (talisman == ModItems.SPIRITWOOD_TALISMAN) return ModItems.VASTNESS_VERDANT;
        return null;
    }

    // 辅助方法：获取护符名称字符串
    private static @Nullable String getTalismanName(Item talisman) {
        if (talisman == ModItems.BREEZE_TALISMAN) return "Breeze";
        if (talisman == ModItems.FLAME_TALISMAN) return "Flame";
        if (talisman == ModItems.FROZEN_TALISMAN) return "Frozen";
        if (talisman == ModItems.THUNERCLAP_TALISMAN) return "Thunderclap";
        if (talisman == ModItems.ROCK_TALISMAN) return "Rock";
        if (talisman == ModItems.ORIGINALSTREAM_TALISMAN) return "Stream";
        if (talisman == ModItems.SPIRITWOOD_TALISMAN) return "Spiritwood";
        return null;
    }

    // 辅助方法：封装成功的清理和音效
    private void craftSuccess(World world, BlockPos pos, int leftDecr, int midDecr, net.minecraft.sound.SoundEvent sound) {
        // 减少左槽位和中槽位的堆叠数量
        this.removeStack(0, leftDecr);
        this.removeStack(1, midDecr);

        // 播放音效
        world.playSound(null, pos, sound, SoundCategory.BLOCKS, 1f, 1f);

        // 标记方块数据已更改，需要保存
        this.markDirty();
    }
}