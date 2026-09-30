package com.animator70.rlcombat_hits;

// 我的类
import com.animator70.rlcombat_hits.config.RLCombatConfig;
import com.animator70.rlcombat_hits.network.Network;

// Forge 类
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

/**
 * 主模组类
 * RLCombatHits
 */
@Mod(RLCombatHits.MODID)
public class RLCombatHits {
    // 模组 ID
    public static final String MODID = "rlcombat_hits";

    /**
     * 构造函数
     */
    public RLCombatHits(FMLJavaModLoadingContext context) {
        // 注册事件总线
        IEventBus modEventBus = context.getModEventBus();

        // 注册自定义音效
        ModSounds.SOUND_EVENTS.register(modEventBus);

        // 注册网络通道
        Network.register();

        // 注册配置（生成武器标签 TOML 文件）
        RLCombatConfig.register();

        // 监听配置加载/重载，刷新武器标签列表
        modEventBus.addListener(RLCombatConfig::onConfigLoad);
        modEventBus.addListener(RLCombatConfig::onConfigReload);
    }
}
