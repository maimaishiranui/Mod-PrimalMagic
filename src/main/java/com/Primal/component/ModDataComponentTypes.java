package com.Primal.component;

import com.Primal.PrimalMagic;
import com.mojang.serialization.Codec;
import net.minecraft.component.ComponentType;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import java.util.function.UnaryOperator;

public class ModDataComponentTypes {

    // 基础组件
    public static final ComponentType<String> BOUND_MAGIC = register("bound_magic",
            builder -> builder.codec(Codec.STRING).packetCodec(PacketCodecs.STRING));

    public static final ComponentType<String> TALISMAN_TYPE = register("talisman_type",
            builder -> builder.codec(Codec.STRING).packetCodec(PacketCodecs.STRING));

    // 冷却时间组件
    public static final ComponentType<Long> SKILL_1_LAST_USE = register("skill_1_last_use",
            builder -> builder.codec(Codec.LONG).packetCodec(PacketCodecs.VAR_LONG));

    public static final ComponentType<Long> SKILL_2_LAST_USE = register("skill_2_last_use",
            builder -> builder.codec(Codec.LONG).packetCodec(PacketCodecs.VAR_LONG));

    // 冰霜状态组件
    public static final ComponentType<Integer> BEAM_HIT_TICKS = register("beam_hit_ticks",
            builder -> builder.codec(Codec.INT).packetCodec(PacketCodecs.VAR_INT));

    public static final ComponentType<Integer> BARRAGE_TICKS_LEFT = register("barrage_ticks_left",
            builder -> builder.codec(Codec.INT).packetCodec(PacketCodecs.VAR_INT));

    // --- 新增：风系技能 2 的固定中心坐标组件 ---
    // 我们用 Double 类型来存储释放技能那一刻的精准位置
    public static final ComponentType<Double> SKILL_2_POS_X = register("skill_2_pos_x",
            builder -> builder.codec(Codec.DOUBLE).packetCodec(PacketCodecs.DOUBLE));

    public static final ComponentType<Double> SKILL_2_POS_Y = register("skill_2_pos_y",
            builder -> builder.codec(Codec.DOUBLE).packetCodec(PacketCodecs.DOUBLE));

    public static final ComponentType<Double> SKILL_2_POS_Z = register("skill_2_pos_z",
            builder -> builder.codec(Codec.DOUBLE).packetCodec(PacketCodecs.DOUBLE));

    //flame相关组件
    // 在 ModDataComponentTypes.java 中添加：
// 记录技能 1 光环剩余时间
    public static final ComponentType<Integer> FLAME_AURA_TICKS = register("flame_aura_ticks", builder -> builder.codec(Codec.INT).packetCodec(PacketCodecs.VAR_INT));
    // 记录技能 2 强化状态剩余时间
    public static final ComponentType<Integer> FLAME_BARRAGE_TICKS = register("flame_barrage_ticks", builder -> builder.codec(Codec.INT).packetCodec(PacketCodecs.VAR_INT));
    // 记录技能 2 期间的杀敌数
    public static final ComponentType<Integer> FLAME_KILL_COUNT = register("flame_kill_count", builder -> builder.codec(Codec.INT).packetCodec(PacketCodecs.VAR_INT));



    private static <T> ComponentType<T> register(String name, UnaryOperator<ComponentType.Builder<T>> builderOperator) {
        return Registry.register(Registries.DATA_COMPONENT_TYPE,
                Identifier.of(PrimalMagic.MOD_ID, name),
                (builderOperator.apply(ComponentType.builder())).build());
    }

    //新增：originalstream符文武器的持续时间组件
    // 在 ModDataComponentTypes.java 中添加：
// 记录源流射线命中目标的刻数
    public static final ComponentType<Integer> STREAM_BEAM_TICKS = register("stream_beam_ticks", builder -> builder.codec(Codec.INT).packetCodec(PacketCodecs.VAR_INT));
    // 记录技能 2 已经吸收水分的秒数（用于爱心堆叠）
    public static final ComponentType<Integer> STREAM_ABSORB_LAYERS = register("stream_absorb_layers", builder -> builder.codec(Codec.INT).packetCodec(PacketCodecs.VAR_INT));

    public static void registerDataComponentTypes() {
        PrimalMagic.LOGGER.info("Registering Data Component Types for " + PrimalMagic.MOD_ID);
    }
}