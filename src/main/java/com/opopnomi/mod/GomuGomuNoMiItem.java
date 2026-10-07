package com.opopnomi.mod;

import java.util.List;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

public class GomuGomuNoMiItem extends Item {

    public GomuGomuNoMiItem(Properties properties) {
        super(properties);
    }

    @Override
    public ActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
        ItemStack stack = player.getItemInHand(hand);
        boolean has = world.isClientSide ? ClientState.hasFruit : DevilFruitData.hasFruit(player);
        if (has) {
            if (!world.isClientSide) {
                player.displayClientMessage(new StringTextComponent("Kamu sudah memiliki kekuatan buah iblis!")
                        .withStyle(TextFormatting.RED), true);
            }
            return ActionResult.fail(stack);
        }
        return super.use(world, player, hand);
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, World world, LivingEntity entity) {
        ItemStack result = super.finishUsingItem(stack, world, entity);
        if (!world.isClientSide && entity instanceof ServerPlayerEntity) {
            SkillManager.grantFruit((ServerPlayerEntity) entity);
        }
        return result;
    }

    @Override
    public void appendHoverText(ItemStack stack, World world, List<ITextComponent> tooltip, ITooltipFlag flag) {
        tooltip.add(new StringTextComponent("Buah iblis misterius. Makan untuk menjadi manusia karet.")
                .withStyle(TextFormatting.LIGHT_PURPLE));
    }
}
