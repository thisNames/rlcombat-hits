package com.animator70.rlcombat_hits.combat;

// Minecraft 类
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DiggerItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.TridentItem;

// Forge 类
import net.minecraftforge.event.entity.player.CriticalHitEvent;

// Java 类
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 核心判断逻辑模块（纯逻辑，双端可用）
 * 集中收敛：是否为武器/工具、是否为暴击。
 *
 * 判断顺序：标签 -> 继承关系 -> 攻击属性兜底。
 * 双缓存：正/负结果都缓存，避免武器与非武器重复执行完整判断。
 * - 命中缓存：判定为「武器」的物品 ID 字符串，命中直接返回 true
 * - 未命中缓存：判定为「非武器」的物品 ID 字符串，命中直接返回 false
 */
@SuppressWarnings("deprecation")
public final class CombatJudge {
    // 武器标签列表：由 TOML 配置加载，运行时解析为 TagKey<Item>
    private static volatile List<TagKey<Item>> WEAPON_TAGS = List.of();

    // 命中缓存：判定为「武器」的物品 ID 字符串集合，线程安全
    private static final Set<String> HIT_CACHE = ConcurrentHashMap.newKeySet();

    // 未命中缓存：判定为「非武器」的物品 ID 字符串集合（避免非武器每次重复完整判断），线程安全
    private static final Set<String> MISS_CACHE = ConcurrentHashMap.newKeySet();

    private CombatJudge() {
    }

    /**
     * 判断主手是否持有武器/工具（对应左键主手攻击）
     */
    public static boolean isMainHandWeapon(Player player) {
        return isWeapon(player.getMainHandItem());
    }

    /**
     * 判断副手是否持有武器/工具（对应副手攻击模组：右键副手武器触发攻击）
     */
    public static boolean isOffHandWeapon(Player player) {
        return isWeapon(player.getOffhandItem());
    }

    /**
     * 统一收敛入口：判断物品是否为武器/工具。
     * 缓存命中直接返回 true；否则依次走标签、继承、属性三个独立判断，命中则写入缓存。
     */
    public static boolean isWeapon(ItemStack stack) {
        // 空物品直接返回
        if (stack.isEmpty()) {
            return false;
        }

        // 获取物品 ID
        String id = getItemId(stack);

        // 正向缓存命中：直接返回 true
        if (HIT_CACHE.contains(id)) {
            return true;
        }

        // 负向缓存命中：直接返回 false（非武器无需重复判断）
        if (MISS_CACHE.contains(id)) {
            return false;
        }

        // 三个独立判断统一收敛（标签 -> 继承 -> 属性兜底）
        boolean result = matchesWeaponTag(stack)
                || isWeaponSubclass(stack.getItem())
                || hasAttackDamageAttribute(stack.getItem());

        // 首次判断：结果写入对应缓存（正负都缓存，避免重复判断）
        (result ? HIT_CACHE : MISS_CACHE).add(id);

        return result;
    }

    /**
     * 判断 1：标签判断（优先，覆盖匠魂 3 等不继承原版 Sword 类的自定义武器）
     */
    private static boolean matchesWeaponTag(ItemStack stack) {
        for (TagKey<Item> tag : WEAPON_TAGS) {
            if (stack.is(tag)) {
                return true;
            }
        }

        return false;
    }

    /**
     * 判断 2：继承关系判断（原版武器/工具子类兜底）
     */
    private static boolean isWeaponSubclass(Item item) {
        return item instanceof SwordItem || item instanceof TridentItem || item instanceof DiggerItem;
    }

    /**
     * 判断 3：攻击伤害属性判断（最终兜底，覆盖完全自定义的近战武器）
     */
    private static boolean hasAttackDamageAttribute(Item item) {
        return item.getDefaultAttributeModifiers(EquipmentSlot.MAINHAND).containsKey(Attributes.ATTACK_DAMAGE);
    }

    /**
     * 获取物品 ID 字符串（如 minecraft:pickaxe、othermod:pickaxe）
     */
    private static String getItemId(ItemStack stack) {
        ResourceLocation key = BuiltInRegistries.ITEM.getKey(stack.getItem());

        return key == null ? stack.getItem().getDescriptionId() : key.toString();
    }

    /**
     * 从 TOML 配置加载武器标签字符串列表，解析为 TagKey<Item>（配置加载/重载时调用）
     */
    public static void reloadWeaponTags(List<? extends String> tagStrings) {
        List<TagKey<Item>> tags = new ArrayList<>(tagStrings.size());

        for (String tagString : tagStrings) {
            ResourceLocation location = ResourceLocation.tryParse(tagString);

            if (location != null) {
                tags.add(TagKey.create(Registries.ITEM, location));
            }
        }

        WEAPON_TAGS = List.copyOf(tags);
    }

    /**
     * 清空正/负缓存（供 /rlcombat_hits clear 指令调用）
     */
    public static void clearCache() {
        HIT_CACHE.clear();
        MISS_CACHE.clear();
    }

    /**
     * 获取命中缓存中的所有物品 ID（供 /rlcombat_hits list 指令调用）
     */
    public static Collection<String> getCachedItems() {
        return List.copyOf(HIT_CACHE);
    }

    /**
     * 获取命中缓存数量
     */
    public static int getCacheSize() {
        return HIT_CACHE.size();
    }

    /**
     * 判断是否为暴击。
     * - isVanillaCritical：原版跳劈暴击
     * - damageModifier > 1.0：其他模组通过 setDamageModifier 实现的自定义暴击
     * （随机暴击、暴击附魔、自带暴击属性的武器等）
     */
    public static boolean isCritical(CriticalHitEvent event) {
        return event.isVanillaCritical() || event.getDamageModifier() > 1.0F;
    }
}
