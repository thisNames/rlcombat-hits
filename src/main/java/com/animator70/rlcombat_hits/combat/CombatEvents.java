package com.animator70.rlcombat_hits.combat;

// 我的类
import com.animator70.rlcombat_hits.RLCombatHits;
import com.animator70.rlcombat_hits.network.Network;

// Minecraft 类
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

// Forge 类
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.CriticalHitEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

// Java
import java.util.HashMap;
import java.util.Map;

/**
 * 服务端权威事件监听：命中与暴击判定，并通过网络包下发到客户端播放音效
 */
@Mod.EventBusSubscriber(modid = RLCombatHits.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class CombatEvents {
    // 关联同一攻击的暴击状态（目标 -> 是否暴击），仅在服务端线程内读写
    private static final Map<LivingEntity, Boolean> CRIT_FLAGS = new HashMap<>();

    // 每个玩家上一次播放音效的 tick，用于防抖（同一 tick 只发一次，避免横扫/双持重复）
    private static final Map<Player, Long> LAST_SOUND_TICK = new HashMap<>();

    /**
     * 暴击判定：记录暴击状态，供紧随其后的 LivingHurtEvent 消费
     */
    @SubscribeEvent
    public static void onCriticalHit(CriticalHitEvent event) {
        // 只在服务端处理（客户端预测攻击时也会触发一次 CriticalHitEvent，需过滤）
        if (event.getEntity().level().isClientSide) {
            return;
        }

        if (!(event.getTarget() instanceof LivingEntity living)) {
            return;
        }

        // CriticalHitEvent 仅在主手攻击（左键）链路触发，无条件记录即可标记「本次为主手攻击」；
        // 是否持武器由紧随其后的 LivingHurtEvent 按主/副手分别判定。
        CRIT_FLAGS.put(living, CombatJudge.isCritical(event));

        if (CRIT_FLAGS.size() > 16) {
            CRIT_FLAGS.clear();
        }
    }

    /**
     * 命中判定：攻击命中目标时（无论是否造成伤害）下发反馈包
     */
    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        if (event.getEntity().level().isClientSide) {
            return;
        }

        if (!(event.getSource().getEntity() instanceof Player attacker)) {
            return;
        }

        LivingEntity target = event.getEntity();
        Boolean critBoxed = CRIT_FLAGS.remove(target);

        // 有暴击记录 = 主手攻击（左键）；无记录 = 副手攻击（右键，副手攻击模组）
        boolean isMainHand = critBoxed != null;
        boolean critical = critBoxed != null && critBoxed;

        // 按实际攻击的手判断是否武器，避免「主手空手攻击却因副手武器而误触发」
        boolean isWeapon = isMainHand ? CombatJudge.isMainHandWeapon(attacker) : CombatJudge.isOffHandWeapon(attacker);

        if (!isWeapon) {
            return;
        }

        // 防抖：每个玩家同一 tick 只发一次（横扫次要目标、双持同时攻击等）
        long gameTime = event.getEntity().level().getGameTime();
        Long last = LAST_SOUND_TICK.get(attacker);

        if (last != null && gameTime - last < 1) {
            return;
        }

        LAST_SOUND_TICK.put(attacker, gameTime);

        if (LAST_SOUND_TICK.size() > 64) {
            LAST_SOUND_TICK.clear();
        }

        Network.sendCombatFeedback(target, critical);
    }
}
