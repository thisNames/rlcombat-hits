package com.animator70.rlcombat_hits;

// Minecraft 类
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;

// Forge 类
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/**
 * 注册本模组自定义的命中与暴击音效。
 */
public class ModSounds {
    // 注册模组音频
    public static final DeferredRegister<SoundEvent> SOUND_EVENTS = DeferredRegister
            .create(ForgeRegistries.SOUND_EVENTS, RLCombatHits.MODID);

    // 命中音效
    public static final RegistryObject<SoundEvent> SWORD_SLASH = register("swordslash");
    // 暴击音效
    public static final RegistryObject<SoundEvent> CRITICAL_STRIKE = register("criticalstrike");

    @SuppressWarnings("removal")
    private static RegistryObject<SoundEvent> register(String name) {
        return SOUND_EVENTS.register(name,
                () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(RLCombatHits.MODID + ":" + name)));
    }
}
