package com.opopnomi.mod;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.DamageSource;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceContext;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.fml.network.PacketDistributor;

public final class SkillManager {
    private SkillManager() {}

    private static final Map<UUID, State> STATES = new HashMap<>();

    private static final class State {
        final long[] cooldownEnd = new long[Skills.COUNT];
        int activeType = -1;
        long activeStart = 0L;
    }

    private static State state(ServerPlayerEntity player) {
        return STATES.computeIfAbsent(player.getUUID(), k -> new State());
    }

    // ------------------------------------------------------------------
    // Buah: dapat / hilang
    // ------------------------------------------------------------------

    public static void grantFruit(ServerPlayerEntity player) {
        cancelActive(player);
        STATES.remove(player.getUUID());
        DevilFruitData.setFruit(player, true);
        DevilFruitData.setSelected(player, 0);
        sync(player);
        player.displayClientMessage(new StringTextComponent(
                "Kamu memakan Gomu Gomu no Mi! Tubuhmu kini seperti karet. "
                        + "Tekan R untuk ganti skill, G untuk memakai skill.")
                .withStyle(TextFormatting.LIGHT_PURPLE), false);
    }

    public static void removeFruit(ServerPlayerEntity player) {
        boolean had = DevilFruitData.hasFruit(player);
        cancelActive(player);
        STATES.remove(player.getUUID());
        DevilFruitData.setFruit(player, false);
        DevilFruitData.setSelected(player, 0);
        sync(player);
        if (had) {
            player.displayClientMessage(new StringTextComponent("Kekuatan Gomu Gomu no Mi telah hilang.")
                    .withStyle(TextFormatting.YELLOW), false);
        } else {
            player.displayClientMessage(new StringTextComponent("Kamu tidak memiliki kekuatan buah iblis.")
                    .withStyle(TextFormatting.RED), false);
        }
    }

    public static void forget(ServerPlayerEntity player) {
        STATES.remove(player.getUUID());
    }

    // ------------------------------------------------------------------
    // Input pemain
    // ------------------------------------------------------------------

    public static void cycle(ServerPlayerEntity player) {
        if (!DevilFruitData.hasFruit(player)) {
            return;
        }
        int next = (DevilFruitData.getSelected(player) + 1) % Skills.COUNT;
        DevilFruitData.setSelected(player, next);
        sync(player);
    }

    public static void use(ServerPlayerEntity player) {
        if (!DevilFruitData.hasFruit(player) || !player.isAlive() || player.isSpectator()) {
            return;
        }
        State s = state(player);
        if (s.activeType >= 0) {
            return;
        }
        int type = DevilFruitData.getSelected(player);
        long now = player.level.getGameTime();
        if (now < s.cooldownEnd[type]) {
            double sec = (s.cooldownEnd[type] - now) / 20.0D;
            player.displayClientMessage(new StringTextComponent(
                    String.format(java.util.Locale.ROOT, "%s masih cooldown (%.1f detik)", Skills.NAMES[type], sec))
                    .withStyle(TextFormatting.RED), true);
            return;
        }
        s.activeType = type;
        s.activeStart = now;
        s.cooldownEnd[type] = now + Skills.COOLDOWN_TICKS[type];
        sendAnim(player, type);
        sync(player);
    }

    // ------------------------------------------------------------------
    // Tick server
    // ------------------------------------------------------------------

    public static void tick(ServerPlayerEntity player) {
        State s = STATES.get(player.getUUID());
        if (s == null || s.activeType < 0) {
            return;
        }
        if (!player.isAlive() || !DevilFruitData.hasFruit(player)) {
            cancelActive(player);
            return;
        }
        long el = player.level.getGameTime() - s.activeStart;

        switch (s.activeType) {
            case Skills.PISTOL:
                if (el == Skills.PISTOL_HIT_TICK) {
                    playHitSound(player, 1.0F);
                    for (LivingEntity t : findTargets(player, Skills.PISTOL_RANGE, 0.35D, true)) {
                        hit(player, t, Skills.PISTOL_DAMAGE, Skills.PISTOL_KNOCKBACK, 0.25D, false);
                    }
                }
                break;
            case Skills.SLING:
                if (el == Skills.SLING_CHARGE_TICKS) {
                    player.level.playSound(null, player.getX(), player.getY(), player.getZ(),
                            SoundEvents.PLAYER_ATTACK_KNOCKBACK, SoundCategory.PLAYERS, 1.5F, 0.7F);
                    for (LivingEntity t : findTargets(player, Skills.SLING_RANGE, 0.6D, false)) {
                        hit(player, t, Skills.SLING_DAMAGE, Skills.SLING_KNOCKBACK, Skills.SLING_KNOCKBACK_UP, false);
                    }
                }
                break;
            case Skills.GATLING:
                if (el < Skills.GATLING_TICKS && el % Skills.GATLING_HIT_INTERVAL == 0) {
                    List<LivingEntity> targets = findTargets(player, Skills.GATLING_RANGE, 0.5D, true);
                    if (!targets.isEmpty()) {
                        playHitSound(player, 0.6F);
                    }
                    for (LivingEntity t : targets) {
                        hit(player, t, Skills.GATLING_DAMAGE, 0.0D, 0.0D, true);
                    }
                }
                break;
            default:
                break;
        }

        if (el >= Skills.totalTicks(s.activeType)) {
            s.activeType = -1;
        }
    }

    public static void cancelActive(ServerPlayerEntity player) {
        State s = STATES.get(player.getUUID());
        if (s != null && s.activeType >= 0) {
            s.activeType = -1;
            sendAnim(player, -1);
        }
    }

    // ------------------------------------------------------------------
    // Sinkronisasi ke client
    // ------------------------------------------------------------------

    public static void sync(ServerPlayerEntity player) {
        State s = STATES.get(player.getUUID());
        long now = player.level.getGameTime();
        int[] cd = new int[Skills.COUNT];
        if (s != null) {
            for (int i = 0; i < Skills.COUNT; i++) {
                cd[i] = (int) Math.max(0L, s.cooldownEnd[i] - now);
            }
        }
        ModNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player),
                new ModNetwork.StatePacket(DevilFruitData.hasFruit(player), DevilFruitData.getSelected(player), cd));
    }

    private static void sendAnim(ServerPlayerEntity player, int type) {
        ModNetwork.CHANNEL.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> player),
                new ModNetwork.AnimPacket(player.getId(), type));
    }

    // ------------------------------------------------------------------
    // Hit detection
    // ------------------------------------------------------------------

    private static List<LivingEntity> findTargets(ServerPlayerEntity player, double range, double grow, boolean firstOnly) {
        Vector3d start = player.getEyePosition(1.0F);
        Vector3d look = player.getLookAngle();
        Vector3d end = start.add(look.x * range, look.y * range, look.z * range);

        BlockRayTraceResult blockHit = player.level.clip(new RayTraceContext(start, end,
                RayTraceContext.BlockMode.COLLIDER, RayTraceContext.FluidMode.NONE, player));
        if (blockHit.getType() != RayTraceResult.Type.MISS) {
            end = blockHit.getLocation();
        }

        AxisAlignedBB area = player.getBoundingBox()
                .expandTowards(end.x - start.x, end.y - start.y, end.z - start.z)
                .inflate(1.0D);

        List<Entity> candidates = player.level.getEntities(player, area,
                e -> e != player.getVehicle() && e instanceof LivingEntity && e.isAlive() && !e.isSpectator());
        candidates.sort(Comparator.comparingDouble((Entity e) -> e.distanceToSqr(start)));

        List<LivingEntity> result = new ArrayList<>();
        for (Entity e : candidates) {
            AxisAlignedBB box = e.getBoundingBox().inflate(grow);
            if (box.contains(start) || box.clip(start, end).isPresent()) {
                result.add((LivingEntity) e);
                if (firstOnly) {
                    break;
                }
            }
        }
        return result;
    }

    private static void hit(ServerPlayerEntity player, LivingEntity target, float damage,
                            double knockback, double knockbackUp, boolean stun) {
        target.invulnerableTime = 0;
        target.hurt(DamageSource.playerAttack(player), damage);

        if (knockback > 0.0D) {
            Vector3d look = player.getLookAngle();
            double hx = look.x;
            double hz = look.z;
            double len = Math.sqrt(hx * hx + hz * hz);
            if (len > 1.0E-4D) {
                hx /= len;
                hz /= len;
            } else {
                hx = 0.0D;
                hz = 0.0D;
            }
            target.setDeltaMovement(target.getDeltaMovement().add(hx * knockback, knockbackUp, hz * knockback));
            target.hurtMarked = true;
        }

        if (stun) {
            StunHandler.stun(target, Skills.GATLING_STUN_TICKS);
        }

        if (player.level instanceof ServerWorld) {
            ((ServerWorld) player.level).sendParticles(ParticleTypes.CRIT,
                    target.getX(), target.getY(0.5D), target.getZ(), 8, 0.3D, 0.3D, 0.3D, 0.25D);
        }
    }

    private static void playHitSound(ServerPlayerEntity player, float volume) {
        player.level.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.PLAYER_ATTACK_STRONG, SoundCategory.PLAYERS, volume, 1.0F);
    }
}
