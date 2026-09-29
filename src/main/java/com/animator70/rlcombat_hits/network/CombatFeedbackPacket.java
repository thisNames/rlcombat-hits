package com.animator70.rlcombat_hits.network;

// 我的类
import com.animator70.rlcombat_hits.client.SoundEffects;

// Minecraft 类
import net.minecraft.network.FriendlyByteBuf;

// Forge 类
import net.minecraftforge.network.NetworkEvent;

// Java
import java.util.function.Supplier;

/**
 * 服务端发送到客户端的战斗反馈包（命中/暴击音效），携带命中位置坐标。
 */
public class CombatFeedbackPacket {
    private final double x;
    private final double y;
    private final double z;
    private final boolean critical;

    public CombatFeedbackPacket(double x, double y, double z, boolean critical) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.critical = critical;
    }

    public static void encode(CombatFeedbackPacket msg, FriendlyByteBuf buf) {
        buf.writeDouble(msg.x);
        buf.writeDouble(msg.y);
        buf.writeDouble(msg.z);
        buf.writeBoolean(msg.critical);
    }

    public static CombatFeedbackPacket decode(FriendlyByteBuf buf) {
        return new CombatFeedbackPacket(
                buf.readDouble(), buf.readDouble(), buf.readDouble(), buf.readBoolean());
    }

    public static void handle(CombatFeedbackPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> SoundEffects.play(msg.x, msg.y, msg.z, msg.critical));
        ctx.get().setPacketHandled(true);
    }
}
