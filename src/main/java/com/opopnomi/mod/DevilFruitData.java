package com.opopnomi.mod;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;

/** Menyimpan status buah iblis di data persisten player (hanya dipakai di sisi server). */
public final class DevilFruitData {
    private DevilFruitData() {}

    public static final String PERSISTED = "PlayerPersisted";
    private static final String KEY_HAS = "gomu";
    private static final String KEY_SELECTED = "selected";

    private static CompoundNBT data(PlayerEntity player) {
        CompoundNBT root = player.getPersistentData();
        if (!root.contains(PERSISTED, 10)) {
            root.put(PERSISTED, new CompoundNBT());
        }
        CompoundNBT persisted = root.getCompound(PERSISTED);
        if (!persisted.contains(OpOpNoMi.MODID, 10)) {
            persisted.put(OpOpNoMi.MODID, new CompoundNBT());
        }
        return persisted.getCompound(OpOpNoMi.MODID);
    }

    public static boolean hasFruit(PlayerEntity player) {
        return data(player).getBoolean(KEY_HAS);
    }

    public static void setFruit(PlayerEntity player, boolean value) {
        data(player).putBoolean(KEY_HAS, value);
    }

    public static int getSelected(PlayerEntity player) {
        int s = data(player).getInt(KEY_SELECTED);
        return (s < 0 || s >= Skills.COUNT) ? 0 : s;
    }

    public static void setSelected(PlayerEntity player, int selected) {
        data(player).putInt(KEY_SELECTED, selected);
    }
}
