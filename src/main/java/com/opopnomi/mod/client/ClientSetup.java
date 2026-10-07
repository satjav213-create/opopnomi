package com.opopnomi.mod.client;

import org.lwjgl.glfw.GLFW;

import com.opopnomi.mod.OpOpNoMi;

import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(modid = OpOpNoMi.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientSetup {

    public static KeyBinding SWITCH_KEY;
    public static KeyBinding USE_KEY;

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        SWITCH_KEY = new KeyBinding("key.opopnomi.switch", GLFW.GLFW_KEY_R, "key.categories.opopnomi");
        USE_KEY = new KeyBinding("key.opopnomi.use", GLFW.GLFW_KEY_G, "key.categories.opopnomi");
        ClientRegistry.registerKeyBinding(SWITCH_KEY);
        ClientRegistry.registerKeyBinding(USE_KEY);
    }
}
