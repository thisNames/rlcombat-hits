package com.animator70.rlcombat_hits.network;

// 我的类
import com.animator70.rlcombat_hits.RLCombatHits;

// Minecraft 类
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;

// Forge 类
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

/**
 * 网络通道：服务端 -> 客户端 的战斗反馈包。
 */
@SuppressWarnings("removal")
public final class Network {
    // 协议版本号（用于兼容性检查）
    private static final String PROTOCOL_VERSION = "1";

    // 网络通道
    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(RLCombatHits.MODID, "main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals);

    // 包 ID 计数器
    private static int packetId = 0;

    private Network() {
    }

    public static void register() {
        CHANNEL.registerMessage(packetId++, CombatFeedbackPacket.class,
                CombatFeedbackPacket::encode,
                CombatFeedbackPacket::decode,
                CombatFeedbackPacket::handle);
    }

    /**
     * 广播战斗反馈给所有能看到目标实体的玩家（含目标自身），携带命中坐标。
     */
    public static void sendCombatFeedback(Entity target, boolean critical) {
        CHANNEL.send(
                PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> target),
                new CombatFeedbackPacket(
                        target.getX(),
                        target.getY(),
                        target.getZ(),
                        critical));
    }
}
