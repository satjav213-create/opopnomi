package com.opopnomi.mod;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;

/** Efek stun: mob dimatikan AI-nya sementara, player diberi slowness berat. */
public final class StunHandler {
    private StunHandler() {}

    private static final String STUN = "opopnomi_stun";
    private static final String WAS_NO_AI = "opopnomi_was_no_ai";

    public static void stun(LivingEntity target, int ticks) {
        CompoundNBT data = target.getPersistentData();
        if (target instanceof MobEntity) {
            MobEntity mob = (MobEntity) target;
            if (data.getInt(STUN) <= 0) {
                data.putBoolean(WAS_NO_AI, mob.isNoAi());
            }
            mob.setNoAi(true);
        } else if (target instanceof PlayerEntity) {
            target.addEffect(new EffectInstance(Effects.MOVEMENT_SLOWDOWN, ticks, 9, false, false));
            target.addEffect(new EffectInstance(Effects.DIG_SLOWDOWN, ticks, 4, false, false));
            target.addEffect(new EffectInstance(Effects.WEAKNESS, ticks, 4, false, false));
        }
        data.putInt(STUN, ticks);
    }

    /** Dipanggil tiap tick untuk setiap makhluk hidup (sisi server). */
    public static void update(LivingEntity entity) {
        CompoundNBT data = entity.getPersistentData();
        int remaining = data.getInt(STUN);
        if (remaining <= 0) {
            return;
        }
        remaining--;
        if (remaining <= 0) {
            data.remove(STUN);
            if (entity instanceof MobEntity && !data.getBoolean(WAS_NO_AI)) {
                ((MobEntity) entity).setNoAi(false);
            }
            data.remove(WAS_NO_AI);
        } else {
            data.putInt(STUN, remaining);
        }
    }
}
