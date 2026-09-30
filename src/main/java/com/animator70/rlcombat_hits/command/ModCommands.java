package com.animator70.rlcombat_hits.command;

// 我的类
import com.animator70.rlcombat_hits.RLCombatHits;

// Forge 类
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * 注册模组指令（FORGE 总线）
 */
@Mod.EventBusSubscriber(modid = RLCombatHits.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class ModCommands {
    private ModCommands() {
    }

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        CombatCommands.register(event.getDispatcher());
    }
}
