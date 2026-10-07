package com.opopnomi.mod.client;

import java.util.Iterator;
import java.util.Locale;
import java.util.Map;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import com.opopnomi.mod.ClientState;
import com.opopnomi.mod.ModNetwork;
import com.opopnomi.mod.OpOpNoMi;
import com.opopnomi.mod.Skills;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.AbstractClientPlayerEntity;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.model.PlayerModel;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Quaternion;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderHandEvent;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = OpOpNoMi.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class ClientEvents {

    private static final int FULL_LIGHT = 15728880;
    private static ModelRenderer armModel;

    private static ModelRenderer getArmModel() {
        if (armModel == null) {
            // Tekstur lengan kanan skin player (64x64) mulai dari (40,16)
            armModel = new ModelRenderer(64, 64, 40, 16);
            armModel.addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F);
        }
        return armModel;
    }

    // ------------------------------------------------------------------
    // Tick: waktu client, cooldown, input tombol
    // ------------------------------------------------------------------

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        ClientState.clientTicks++;

        for (int i = 0; i < Skills.COUNT; i++) {
            if (ClientState.cooldown[i] > 0) {
                ClientState.cooldown[i]--;
            }
        }

        Iterator<Map.Entry<Integer, ClientState.Anim>> it = ClientState.anims.entrySet().iterator();
        while (it.hasNext()) {
            ClientState.Anim a = it.next().getValue();
            if (ClientState.clientTicks - a.startTick > Skills.totalTicks(a.type) + 2) {
                it.remove();
            }
        }

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || ClientSetup.SWITCH_KEY == null || ClientSetup.USE_KEY == null) {
            return;
        }
        while (ClientSetup.SWITCH_KEY.consumeClick()) {
            if (ClientState.hasFruit) {
                ModNetwork.CHANNEL.sendToServer(new ModNetwork.ActionPacket(0));
            }
        }
        while (ClientSetup.USE_KEY.consumeClick()) {
            if (ClientState.hasFruit) {
                ModNetwork.CHANNEL.sendToServer(new ModNetwork.ActionPacket(1));
            }
        }
    }

    @SubscribeEvent
    public static void onLoggedOut(ClientPlayerNetworkEvent.LoggedOutEvent event) {
        ClientState.reset();
    }

    // ------------------------------------------------------------------
    // Sembunyikan lengan asli saat skill aktif
    // ------------------------------------------------------------------

    @SubscribeEvent
    public static void onRenderHand(RenderHandEvent event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null && ClientState.anims.containsKey(mc.player.getId())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onLivingPre(RenderLivingEvent.Pre<?, ?> event) {
        LivingEntity entity = event.getEntity();
        if (entity instanceof PlayerEntity && ClientState.anims.containsKey(entity.getId())) {
            Object model = event.getRenderer().getModel();
            if (model instanceof PlayerModel) {
                PlayerModel<?> pm = (PlayerModel<?>) model;
                pm.leftArm.visible = false;
                pm.rightArm.visible = false;
                pm.leftSleeve.visible = false;
                pm.rightSleeve.visible = false;
            }
        }
    }

    @SubscribeEvent
    public static void onLivingPost(RenderLivingEvent.Post<?, ?> event) {
        LivingEntity entity = event.getEntity();
        if (entity instanceof PlayerEntity && ClientState.anims.containsKey(entity.getId())) {
            Object model = event.getRenderer().getModel();
            if (model instanceof PlayerModel) {
                PlayerModel<?> pm = (PlayerModel<?>) model;
                pm.leftArm.visible = true;
                pm.rightArm.visible = true;
                pm.leftSleeve.visible = true;
                pm.rightSleeve.visible = true;
            }
        }
    }

    // ------------------------------------------------------------------
    // Gambar tangan karet
    // ------------------------------------------------------------------

    @SubscribeEvent
    public static void onRenderWorld(RenderWorldLastEvent event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || ClientState.anims.isEmpty()) {
            return;
        }
        MatrixStack ms = event.getMatrixStack();
        float pt = event.getPartialTicks();
        Vector3d cam = mc.gameRenderer.getMainCamera().getPosition();
        IRenderTypeBuffer.Impl buffer = mc.renderBuffers().bufferSource();

        for (AbstractClientPlayerEntity player : mc.level.players()) {
            ClientState.Anim anim = ClientState.anims.get(player.getId());
            if (anim == null) {
                continue;
            }
            float el = (float) (ClientState.clientTicks - anim.startTick) + pt;
            renderPlayerArms(ms, buffer, player, anim.type, el, pt, cam);
        }
        buffer.endBatch();
    }

    private static void renderPlayerArms(MatrixStack ms, IRenderTypeBuffer buffer, AbstractClientPlayerEntity player,
                                         int type, float el, float pt, Vector3d cam) {
        if (el > Skills.totalTicks(type)) {
            return;
        }

        double px = MathHelper.lerp((double) pt, player.xo, player.getX());
        double py = MathHelper.lerp((double) pt, player.yo, player.getY());
        double pz = MathHelper.lerp((double) pt, player.zo, player.getZ());
        float yaw = MathHelper.lerp(pt, player.yRotO, player.yRot);
        float pitch = MathHelper.lerp(pt, player.xRotO, player.xRot);
        float yawR = yaw * ((float) Math.PI / 180F);
        float pitchR = pitch * ((float) Math.PI / 180F);
        double cosP = MathHelper.cos(pitchR);

        // arah pandang (unit vector)
        double dx = -MathHelper.sin(yawR) * cosP;
        double dy = -MathHelper.sin(pitchR);
        double dz = MathHelper.cos(yawR) * cosP;
        // vektor "kanan" pemain (horizontal)
        double rx = -MathHelper.cos(yawR);
        double rz = -MathHelper.sin(yawR);
        // tinggi bahu
        double sy = py + player.getBbHeight() * 0.72D;

        ResourceLocation skin = player.getSkinTextureLocation();

        switch (type) {
            case Skills.PISTOL: {
                float ext = el <= 4.0F ? el / 4.0F : Math.max(0.0F, 1.0F - (el - 4.0F) / 6.0F);
                double len = 0.75D + 7.0D * ext;
                for (int side = -1; side <= 1; side += 2) {
                    double lat = side * 0.37D;
                    drawArm(ms, buffer, skin,
                            px + rx * lat - cam.x, sy - cam.y, pz + rz * lat - cam.z,
                            dx, dy, dz, len, 1.0F);
                }
                break;
            }
            case Skills.SLING: {
                double sign;
                double len;
                if (el < Skills.SLING_CHARGE_TICKS) {
                    float ratio = el / (float) Skills.SLING_CHARGE_TICKS;
                    len = 0.75D + 8.0D * ratio;
                    sign = -1.0D;
                } else {
                    float t = el - Skills.SLING_CHARGE_TICKS;
                    float s = t < 3.0F
                            ? -8.0F + 22.0F * (t / 3.0F)
                            : 14.0F * Math.max(0.0F, 1.0F - (t - 3.0F) / 9.0F);
                    len = 0.75D + Math.abs(s);
                    sign = s < 0.0F ? -1.0D : 1.0D;
                }
                for (int side = -1; side <= 1; side += 2) {
                    double lat = side * 0.37D;
                    drawArm(ms, buffer, skin,
                            px + rx * lat - cam.x, sy - cam.y, pz + rz * lat - cam.z,
                            dx * sign, dy * sign, dz * sign, len, 1.0F);
                }
                break;
            }
            case Skills.GATLING: {
                int count = 10;
                for (int k = 0; k < count; k++) {
                    int side = (k % 2 == 0) ? 1 : -1;
                    boolean ghost = k >= 2;
                    double lat = side * 0.37D + (ghost ? Math.sin(k * 2.1D) * 0.25D : 0.0D);
                    double up = ghost ? Math.cos(k * 3.3D) * 0.32D : 0.0D;
                    float phase = (el * 0.31F + k * 0.19F) % 1.0F;
                    double len = 0.75D + 6.5D * Math.sin(phase * Math.PI);
                    drawArm(ms, buffer, skin,
                            px + rx * lat - cam.x, sy + up - cam.y, pz + rz * lat - cam.z,
                            dx, dy, dz, len, ghost ? 0.5F : 1.0F);
                }
                break;
            }
            default:
                break;
        }
    }

    private static void drawArm(MatrixStack ms, IRenderTypeBuffer buffer, ResourceLocation skin,
                                double x, double y, double z,
                                double dx, double dy, double dz,
                                double length, float alpha) {
        RenderType renderType = alpha < 0.99F ? RenderType.entityTranslucent(skin) : RenderType.entityCutoutNoCull(skin);
        IVertexBuilder vb = buffer.getBuffer(renderType);

        ms.pushPose();
        ms.translate(x, y, z);

        // putar sumbu +Y model agar mengarah ke (dx, dy, dz)
        double ax = dz;
        double az = -dx;
        double axisLen = Math.sqrt(ax * ax + az * az);
        if (axisLen > 1.0E-6D) {
            float angle = (float) Math.acos(MathHelper.clamp(dy, -1.0D, 1.0D));
            ms.mulPose(new Quaternion(new Vector3f((float) (ax / axisLen), 0.0F, (float) (az / axisLen)), angle, false));
        } else if (dy < 0.0D) {
            ms.mulPose(Vector3f.XP.rotationDegrees(180.0F));
        }

        // panjang dasar lengan = 12 piksel = 0.75 blok
        ms.scale(1.0F, (float) (length / 0.75D), 1.0F);
        getArmModel().render(ms, vb, FULL_LIGHT, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, alpha);
        ms.popPose();
    }

    // ------------------------------------------------------------------
    // HUD kiri bawah
    // ------------------------------------------------------------------

    @SubscribeEvent
    public static void onOverlay(RenderGameOverlayEvent.Post event) {
        if (event.getType() != RenderGameOverlayEvent.ElementType.ALL) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.options.hideGui || !ClientState.hasFruit) {
            return;
        }

        MatrixStack ms = event.getMatrixStack();
        FontRenderer font = mc.font;

        ClientState.Anim anim = ClientState.anims.get(mc.player.getId());
        int activeType = anim == null ? -1 : anim.type;
        long elapsed = anim == null ? 0L : ClientState.clientTicks - anim.startTick;

        int rowH = 11;
        int boxW = 168;
        int boxH = rowH * 5 + 6;
        int x = 6;
        int y = mc.getWindow().getGuiScaledHeight() - boxH - 6;

        AbstractGui.fill(ms, x - 3, y - 3, x + boxW, y + boxH - 3, 0x90000000);

        font.drawShadow(ms, "Gomu Gomu no Mi", x, y, 0xFFFF66CC);

        for (int i = 0; i < Skills.COUNT; i++) {
            int ry = y + rowH * (i + 1);
            boolean selected = ClientState.selected == i;
            String label = (selected ? "> " : "  ") + (i + 1) + ". " + Skills.NAMES[i];
            font.drawShadow(ms, label, x, ry, selected ? 0xFFFFE066 : 0xFFFFFFFF);

            String status;
            int color;
            if (activeType == i) {
                if (i == Skills.SLING && elapsed < Skills.SLING_CHARGE_TICKS) {
                    float left = (Skills.SLING_CHARGE_TICKS - elapsed) / 20.0F;
                    status = String.format(Locale.ROOT, "TARIK %.1fs", left);
                } else if (i == Skills.SLING) {
                    status = "LEPAS!";
                } else {
                    status = "AKTIF";
                }
                color = 0xFFFFAA00;
            } else if (ClientState.cooldown[i] > 0) {
                status = String.format(Locale.ROOT, "%.1fs", ClientState.cooldown[i] / 20.0F);
                color = 0xFFFF5555;
            } else {
                status = "SIAP";
                color = 0xFF55FF55;
            }
            font.drawShadow(ms, status, x + boxW - 8 - font.width(status), ry, color);
        }

        font.drawShadow(ms, "[R] Ganti skill   [G] Pakai skill", x, y + rowH * 4 + 2, 0xFFAAAAAA);
    }
}
