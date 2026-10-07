package com.opopnomi.mod;

import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(OpOpNoMi.MODID)
public class OpOpNoMi {
    public static final String MODID = "opopnomi";

    public OpOpNoMi() {
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        ModItems.ITEMS.register(modBus);
        modBus.addListener(this::commonSetup);
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(ModNetwork::register);
    }
}
