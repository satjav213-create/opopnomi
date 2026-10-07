package com.opopnomi.mod;

import java.util.function.Supplier;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.simple.SimpleChannel;

public final class ModNetwork {
    private ModNetwork() {}

    private static final String PROTOCOL = "1";

    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(OpOpNoMi.MODID, "main"),
            () -> PROTOCOL,
            PROTOCOL::equals,
            PROTOCOL::equals);

    public static void register() {
        int id = 0;
        CHANNEL.registerMessage(id++, ActionPacket.class, ActionPacket::encode, ActionPacket::decode, ActionPacket::handle);
        CHANNEL.registerMessage(id++, StatePacket.class, StatePacket::encode, StatePacket::decode, StatePacket::handle);
        CHANNEL.registerMessage(id++, AnimPacket.class, AnimPacket::encode, AnimPacket::decode, AnimPacket::handle);
    }

    /** Client -> Server. action 0 = ganti skill, 1 = pakai skill. */
    public static class ActionPacket {
        public final int action;

        public ActionPacket(int action) {
            this.action = action;
        }

        public static void encode(ActionPacket msg, PacketBuffer buf) {
            buf.writeByte(msg.action);
        }

        public static ActionPacket decode(PacketBuffer buf) {
            return new ActionPacket(buf.readByte());
        }

        public static void handle(ActionPacket msg, Supplier<NetworkEvent.Context> ctx) {
            NetworkEvent.Context c = ctx.get();
            c.enqueueWork(() -> {
                ServerPlayerEntity player = c.getSender();
                if (player == null) {
                    return;
                }
                if (msg.action == 0) {
                    SkillManager.cycle(player);
                } else if (msg.action == 1) {
                    SkillManager.use(player);
                }
            });
            c.setPacketHandled(true);
        }
    }

    /** Server -> Client. Status buah, skill terpilih, dan sisa cooldown (tick). */
    public static class StatePacket {
        public final boolean has;
        public final int selected;
        public final int[] cooldown;

        public StatePacket(boolean has, int selected, int[] cooldown) {
            this.has = has;
            this.selected = selected;
            this.cooldown = cooldown;
        }

        public static void encode(StatePacket msg, PacketBuffer buf) {
            buf.writeBoolean(msg.has);
            buf.writeVarInt(msg.selected);
            for (int i = 0; i < Skills.COUNT; i++) {
                buf.writeVarInt(msg.cooldown[i]);
            }
        }

        public static StatePacket decode(PacketBuffer buf) {
            boolean has = buf.readBoolean();
            int selected = buf.readVarInt();
            int[] cd = new int[Skills.COUNT];
            for (int i = 0; i < Skills.COUNT; i++) {
                cd[i] = buf.readVarInt();
            }
            return new StatePacket(has, selected, cd);
        }

        public static void handle(StatePacket msg, Supplier<NetworkEvent.Context> ctx) {
            NetworkEvent.Context c = ctx.get();
            c.enqueueWork(() -> {
                ClientState.hasFruit = msg.has;
                ClientState.selected = msg.selected;
                System.arraycopy(msg.cooldown, 0, ClientState.cooldown, 0, Skills.COUNT);
                if (!msg.has) {
                    ClientState.anims.clear();
                }
            });
            c.setPacketHandled(true);
        }
    }

    /** Server -> Client. Memulai animasi tangan karet pada sebuah player. type -1 = berhenti. */
    public static class AnimPacket {
        public final int entityId;
        public final int type;

        public AnimPacket(int entityId, int type) {
            this.entityId = entityId;
            this.type = type;
        }

        public static void encode(AnimPacket msg, PacketBuffer buf) {
            buf.writeInt(msg.entityId);
            buf.writeInt(msg.type);
        }

        public static AnimPacket decode(PacketBuffer buf) {
            return new AnimPacket(buf.readInt(), buf.readInt());
        }

        public static void handle(AnimPacket msg, Supplier<NetworkEvent.Context> ctx) {
            NetworkEvent.Context c = ctx.get();
            c.enqueueWork(() -> {
                if (msg.type < 0) {
                    ClientState.anims.remove(msg.entityId);
                } else {
                    ClientState.anims.put(msg.entityId, new ClientState.Anim(msg.type, ClientState.clientTicks));
                }
            });
            c.setPacketHandled(true);
        }
    }
}
