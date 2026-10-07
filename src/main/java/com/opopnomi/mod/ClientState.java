package com.opopnomi.mod;

import java.util.HashMap;
import java.util.Map;

/** Data sisi client. Sengaja tanpa import class client agar aman dimuat di server. */
public final class ClientState {
    private ClientState() {}

    public static boolean hasFruit = false;
    public static int selected = 0;
    public static final int[] cooldown = new int[Skills.COUNT];
    public static long clientTicks = 0L;
    public static final Map<Integer, Anim> anims = new HashMap<>();

    public static final class Anim {
        public final int type;
        public final long startTick;

        public Anim(int type, long startTick) {
            this.type = type;
            this.startTick = startTick;
        }
    }

    public static void reset() {
        hasFruit = false;
        selected = 0;
        for (int i = 0; i < cooldown.length; i++) {
            cooldown[i] = 0;
        }
        anims.clear();
    }
}
