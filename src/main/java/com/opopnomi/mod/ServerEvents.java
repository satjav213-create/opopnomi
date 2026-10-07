package com.opopnomi.mod;

import net.minecraft.command.Commands;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = OpOpNoMi.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ServerEvents {

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("removedf").executes(ctx -> {
            ServerPlayerEntity player = ctx.getSource().getPlayerOrException();
            SkillManager.removeFruit(player);
            return 1;
        }));
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase == TickEvent.Phase.END && event.player instanceof ServerPlayerEntity) {
            SkillManager.tick((ServerPlayerEntity) event.player);
        }
    }

    @SubscribeEvent
    public static void onLivingUpdate(LivingEvent.LivingUpdateEvent event) {
        LivingEntity entity = event.getEntityLiving();
        if (!entity.level.isClientSide) {
            StunHandler.update(entity);
        }
    }

    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        if (event.getEntityLiving() instanceof ServerPlayerEntity) {
            SkillManager.cancelActive((ServerPlayerEntity) event.getEntityLiving());
        }
    }

    @SubscribeEvent
    public static void onClone(PlayerEvent.Clone event) {
        CompoundNBT old = event.getOriginal().getPersistentData();
        if (old.contains(DevilFruitData.PERSISTED)) {
            event.getPlayer().getPersistentData().put(DevilFruitData.PERSISTED,
                    old.getCompound(DevilFruitData.PERSISTED).copy());
        }
    }

    @SubscribeEvent
    public static void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getPlayer() instanceof ServerPlayerEntity) {
            SkillManager.sync((ServerPlayerEntity) event.getPlayer());
        }
    }

    @SubscribeEvent
    public static void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getPlayer() instanceof ServerPlayerEntity) {
            SkillManager.forget((ServerPlayerEntity) event.getPlayer());
        }
    }

    @SubscribeEvent
    public static void onRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (event.getPlayer() instanceof ServerPlayerEntity) {
            SkillManager.sync((ServerPlayerEntity) event.getPlayer());
        }
    }

    @SubscribeEvent
    public static void onChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.getPlayer() instanceof ServerPlayerEntity) {
            SkillManager.sync((ServerPlayerEntity) event.getPlayer());
        }
    }
}
