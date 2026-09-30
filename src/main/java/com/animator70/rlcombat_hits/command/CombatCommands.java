package com.animator70.rlcombat_hits.command;

// 我的类
import com.animator70.rlcombat_hits.RLCombatHits;
import com.animator70.rlcombat_hits.combat.CombatJudge;

// Brigadier 类
import com.mojang.brigadier.CommandDispatcher;

// Minecraft 类
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

// Java 类
import java.util.Collection;

/**
 * 指令实现：/rlcombat_hits clear|list
 * - clear：清空命中缓存
 * - list：查看命中缓存中的物品并统计数量
 */
public final class CombatCommands {
    private CombatCommands() {
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                Commands.literal(RLCombatHits.MODID).requires(source -> source.hasPermission(2))
                        // 清空缓存
                        .then(Commands.literal("clear")
                                .executes(ctx -> clear(ctx.getSource())))

                        // 查看缓存
                        .then(Commands.literal("list")
                                .executes(ctx -> list(ctx.getSource()))));
    }

    /**
     * 清空命中缓存，并回显清空的条目数量
     */
    private static int clear(CommandSourceStack source) {
        int cleared = CombatJudge.getCacheSize();

        CombatJudge.clearCache();

        source.sendSuccess(() -> Component.translatable("rlcombat_hits.command.clear", cleared), true);

        return cleared;
    }

    /**
     * 列出命中缓存中的物品，并显示数量统计
     */
    private static int list(CommandSourceStack source) {
        Collection<String> items = CombatJudge.getCachedItems();
        int count = items.size();

        source.sendSuccess(() -> Component.translatable("rlcombat_hits.command.list.header", count), false);

        for (String item : items) {
            source.sendSuccess(() -> Component.literal("  - " + item), false);
        }

        return count;
    }
}
