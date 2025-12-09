package org.pizuk.tquality.events.listener;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.pizuk.tquality.Tquality;
import org.pizuk.tquality.utils.ModUtils;
import org.pizuk.tquality.utils.StatsUtils;
import slimeknights.mantle.client.SafeClientAccess;
import slimeknights.mantle.client.TooltipKey;
import slimeknights.tconstruct.tables.block.entity.inventory.PartBuilderContainerWrapper;

@Mod.EventBusSubscriber(modid = Tquality.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class TQEventListeners {

    @SubscribeEvent
    public static void onCraft(PlayerEvent.ItemCraftedEvent event) {
        if (event.getEntity().level().isClientSide() || !(event.getInventory() instanceof PartBuilderContainerWrapper container)) {
            return;
        }
//        ItemStack result = event.getCrafting();
//        result = StatsUtils.getModifiedPartItem(result, container.getStack(), true).copy();
    }

    @SubscribeEvent
    public static void onToolTipEvent(ItemTooltipEvent event) {
        TooltipKey key = SafeClientAccess.getTooltipKey();
        ItemStack stack = event.getItemStack();
        if (key != TooltipKey.SHIFT || !stack.hasTag() || !stack.getTag().contains(Tquality.QUALITY)) {
            return;
        }
        float quality = stack.getTag().getFloat(Tquality.QUALITY);
        event.getToolTip().add(Component.translatable("tquality.tooltip.quality").append(": ").withStyle(ChatFormatting.DARK_GRAY).append(
                Component.literal(String.valueOf(quality)).withStyle(Style.EMPTY.withColor(ModUtils.getQualityColor(quality)))));
    }
}
