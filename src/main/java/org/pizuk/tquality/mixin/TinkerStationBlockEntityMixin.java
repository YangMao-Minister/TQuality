package org.pizuk.tquality.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.pizuk.tquality.utils.StatsUtils;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import slimeknights.tconstruct.library.recipe.tinkerstation.ITinkerStationRecipe;
import slimeknights.tconstruct.library.recipe.tinkerstation.building.ToolBuildingRecipe;
import slimeknights.tconstruct.tables.block.entity.inventory.LazyResultContainer;
import slimeknights.tconstruct.tables.block.entity.inventory.TinkerStationContainerWrapper;
import slimeknights.tconstruct.tables.block.entity.table.RetexturedTableBlockEntity;
import slimeknights.tconstruct.tables.block.entity.table.TinkerStationBlockEntity;

import javax.annotation.Nullable;
import java.util.List;

@Mixin(TinkerStationBlockEntity.class)
public abstract class TinkerStationBlockEntityMixin extends RetexturedTableBlockEntity implements LazyResultContainer.ILazyCrafter {

    @Final
    @Shadow
    private TinkerStationContainerWrapper inventoryWrapper;

    @Shadow
    @Nullable
    private ITinkerStationRecipe lastRecipe;

    public TinkerStationBlockEntityMixin(BlockEntityType<?> type, BlockPos pos, BlockState state, Component name, int size) {
        super(type, pos, state, name, size);
    }

    @Inject(method = "calcResult", at = @At("RETURN"), cancellable = true, remap = false)
    public void calcResult(Player player, CallbackInfoReturnable<ItemStack> cir) {
        if (!(lastRecipe instanceof ToolBuildingRecipe)) {
            return;
        }
        ItemStack stack = cir.getReturnValue();
        List<ItemStack> inputs = new java.util.ArrayList<>();
        for (int i = 0; i < inventoryWrapper.getInputCount(); i++) {
            ItemStack input = inventoryWrapper.getInput(i);
            if (input.isEmpty())
                break;
            inputs.add(input);
        }
        cir.setReturnValue(StatsUtils.getModifiedToolItem(stack, inputs, false));
    }
}
