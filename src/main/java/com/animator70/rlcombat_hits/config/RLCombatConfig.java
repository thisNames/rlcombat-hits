package com.animator70.rlcombat_hits.config;

// 我的类
import com.animator70.rlcombat_hits.combat.CombatJudge;

// Forge 类
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.config.ModConfigEvent;

// Java 类
import java.util.List;

/**
 * 模组配置：通过 TOML 文件定义用于武器判定的标签数组。
 * 采用「去适配别人」的策略：将原版/第三方模组的物品标签集中到配置，
 * 而非要求第三方模组适配本模组的自定义标签。
 */
public final class RLCombatConfig {
    // 构建产物：配置规格与分组实例（对外仅保留规格用于注册/事件比对）
    private static final ForgeConfigSpec SPEC;
    private static final Combat COMBAT;

    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();

        COMBAT = new Combat(builder);

        SPEC = builder.build();
    }

    private RLCombatConfig() {
    }

    // ===================== 对外 API =====================

    /**
     * 注册 COMMON 配置（生成 TOML 文件）
     */
    @SuppressWarnings("removal")
    public static void register() {
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, SPEC);
    }

    /**
     * 配置加载完成：解析标签数组到 CombatJudge
     */
    public static void onConfigLoad(ModConfigEvent.Loading event) {
        if (event.getConfig().getSpec() == SPEC) {
            applyWeaponTags();
        }
    }

    /**
     * 配置重载：重新解析标签数组到 CombatJudge
     */
    public static void onConfigReload(ModConfigEvent.Reloading event) {
        if (event.getConfig().getSpec() == SPEC) {
            applyWeaponTags();
        }
    }

    // ===================== 内部实现 =====================

    private static void applyWeaponTags() {
        CombatJudge.reloadWeaponTags(COMBAT.weaponTags.get());
    }

    // ===================== 配置项（内部类，与外界隔离） =====================

    /**
     * 战斗相关配置项
     */
    private static final class Combat {
        final ForgeConfigSpec.ConfigValue<List<? extends String>> weaponTags;

        Combat(ForgeConfigSpec.Builder builder) {
            builder.comment("武器判定标签").push("combat");

            weaponTags = builder
                    .comment("用于判定近战武器的物品标签列表（格式：命名空间:路径）")
                    .defineList("weaponTags", List.of(
                            "minecraft:swords",
                            "minecraft:axes",
                            "minecraft:pickaxes",
                            "minecraft:shovels",
                            "minecraft:hoes",
                            "forge:tools/swords",
                            "forge:tools/axes",
                            "forge:tools/pickaxes",
                            "forge:tools/shovels",
                            "forge:tools/hoes"),
                            obj -> obj instanceof String);

            builder.pop();
        }
    }
}
