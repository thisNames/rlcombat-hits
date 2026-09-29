package com.animator70.rlcombat_hits.client;

// 我的类
import com.animator70.rlcombat_hits.ModSounds;

// Minecraft 类
import net.minecraft.client.Minecraft;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;

// Java 类
import java.util.Random;

/**
 * 音效播放模块：命中 / 暴击音效。
 */
public final class SoundEffects {
    // 随机数生成器，避免同一攻击重复播放同一音效
    private static final Random RANDOM = new Random();

    private SoundEffects() {
    }

    /**
     * 在指定位置播放命中 / 暴击音效
     */
    public static void play(double x, double y, double z, boolean critical) {
        Minecraft mc = Minecraft.getInstance();

        if (mc.level == null) {
            return;
        }

        // 是否暴击？播放：暴击音效，还是命中音效
        SoundEvent sound = critical ? ModSounds.CRITICAL_STRIKE.get() : ModSounds.SWORD_SLASH.get();

        // 暴击音效放大音量，避免被原版受伤音效掩盖；音量与音调都带随机浮动
        float baseVolume = critical ? 1.2F : 0.9F;

        // 音量与音调随机浮动
        float volume = baseVolume * (0.8F + RANDOM.nextFloat() * 0.2F);
        float pitch = 0.9F + RANDOM.nextFloat() * 0.2F;

        // 播放本地音效
        mc.level.playLocalSound(x, y, z,
                sound,
                SoundSource.PLAYERS,
                volume,
                pitch,
                false);
    }
}
