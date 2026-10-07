package com.opopnomi.mod;

/**
 * Konstanta skill. Ubah angka di sini untuk menyeimbangkan (balance) kekuatan skill.
 * 1 detik = 20 tick. Damage 2.0 = 1 hati.
 */
public final class Skills {
    private Skills() {}

    public static final int PISTOL = 0;
    public static final int SLING = 1;
    public static final int GATLING = 2;
    public static final int COUNT = 3;

    public static final String[] NAMES = {
            "Gomu Gomu no Pistol",
            "Gomu Gomu no Ketapel",
            "Gomu Gomu no Gatling"
    };

    // cooldown (tick) dihitung sejak skill dipakai
    public static final int[] COOLDOWN_TICKS = {40, 300, 1200};

    // Skill 1: Pistol
    public static final int PISTOL_TICKS = 10;
    public static final int PISTOL_HIT_TICK = 3;
    public static final double PISTOL_RANGE = 8.0D;
    public static final float PISTOL_DAMAGE = 8.0F;
    public static final double PISTOL_KNOCKBACK = 0.9D;

    // Skill 2: Ketapel (tarik 5 detik lalu lontar)
    public static final int SLING_CHARGE_TICKS = 100;
    public static final int SLING_THRUST_TICKS = 12;
    public static final double SLING_RANGE = 15.0D;
    public static final float SLING_DAMAGE = 16.0F;
    public static final double SLING_KNOCKBACK = 3.0D;
    public static final double SLING_KNOCKBACK_UP = 0.6D;

    // Skill 3: Gatling (ultimate, 10 detik)
    public static final int GATLING_TICKS = 200;
    public static final int GATLING_HIT_INTERVAL = 4;
    public static final double GATLING_RANGE = 7.0D;
    public static final float GATLING_DAMAGE = 3.0F;
    public static final int GATLING_STUN_TICKS = 100; // 5 detik

    public static int totalTicks(int type) {
        switch (type) {
            case PISTOL:
                return PISTOL_TICKS;
            case SLING:
                return SLING_CHARGE_TICKS + SLING_THRUST_TICKS;
            case GATLING:
                return GATLING_TICKS;
            default:
                return 0;
        }
    }
}
