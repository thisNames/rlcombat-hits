package com.animator70.rlcombat_hits.combat;

// 我的类
import com.animator70.rlcombat_hits.RLCombatHits;

// Minecraft 类
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
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 核心判断逻辑模块（纯逻辑，无副作用，双端可用）
 * 集中收敛：是否为武器/工具、是否为暴击
 */
@SuppressWarnings({ "removal", "deprecation" })
public final class CombatJudge {
    // 本模组定义的近战武器标签，第三方模组可通过数据包扩展
    public static final TagKey<Item> MELEE_WEAPONS = TagKey.create(
            Registries.ITEM,
            new ResourceLocation(RLCombatHits.MODID, "melee_weapons"));

    // LRU 缓存物品是否为武器/工具：accessOrder=true 访问即刷新，容量满时淘汰最久未使用项
    private static final int WEAPON_CACHE_MAX = 256;
    private static final Map<Item, Boolean> WEAPON_CACHE = new LinkedHashMap<>(16, 0.75F, true) {
        @Override
        protected boolean removeEldestEntry(Map.Entry<Item, Boolean> eldest) {
            return size() > WEAPON_CACHE_MAX;
        }
    };

    private CombatJudge() {
    }

    /**
     * 判断物品是否为武器/工具（带 LRU 缓存，结果仅取决于物品类型）
     */
    public static boolean isWeaponOrTool(ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }

        Item item = stack.getItem();

        synchronized (WEAPON_CACHE) {
            Boolean cached = WEAPON_CACHE.get(item);

            if (cached != null) {
                return cached;
            }

            boolean result = isWeaponInternal(item);

            WEAPON_CACHE.put(item, result);

            return result;
        }
    }

    /**
     * 判断主手是否持有武器/工具（对应左键主手攻击）
     */
    public static boolean isMainHandWeapon(Player player) {
        return isWeaponOrTool(player.getMainHandItem());
    }

    /**
     * 判断副手是否持有武器/工具（对应副手攻击模组：右键副手武器触发攻击）
     */
    public static boolean isOffHandWeapon(Player player) {
        return isWeaponOrTool(player.getOffhandItem());
    }

    /**
     * 判断物品是否为武器/工具（内部实现）
     */
    private static boolean isWeaponInternal(Item item) {
        ItemStack stack = new ItemStack(item);

        // 1. 标签判断（优先，覆盖匠魂 3 等不继承原版 Sword 类的自定义武器）
        if (stack.is(MELEE_WEAPONS)) {
            return true;
        }

        // 2. 子类判断（兜底）
        if (item instanceof SwordItem || item instanceof TridentItem || item instanceof DiggerItem) {
            return true;
        }

        // 3. 攻击伤害属性判断（最终兜底，覆盖完全自定义的近战武器）
        return item.getDefaultAttributeModifiers(EquipmentSlot.MAINHAND).containsKey(Attributes.ATTACK_DAMAGE);
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
