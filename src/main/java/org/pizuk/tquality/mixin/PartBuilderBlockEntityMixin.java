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
import slimeknights.tconstruct.library.recipe.partbuilder.IPartBuilderRecipe;
import slimeknights.tconstruct.library.recipe.partbuilder.Pattern;
import slimeknights.tconstruct.tables.block.entity.inventory.LazyResultContainer;
import slimeknights.tconstruct.tables.block.entity.inventory.PartBuilderContainerWrapper;
import slimeknights.tconstruct.tables.block.entity.table.PartBuilderBlockEntity;
import slimeknights.tconstruct.tables.block.entity.table.RetexturedTableBlockEntity;

@Mixin(PartBuilderBlockEntity.class)
public abstract class PartBuilderBlockEntityMixin extends RetexturedTableBlockEntity implements LazyResultContainer.ILazyCrafter {

    @Shadow
    @Final
    private PartBuilderContainerWrapper inventoryWrapper;

    public PartBuilderBlockEntityMixin(BlockEntityType<?> type, BlockPos pos, BlockState state, Component name, int size) {
        super(type, pos, state, name, size);
    }


    @Inject(method = "calcResult", at = @At("RETURN"), cancellable = true, remap = false)
    public void calcResult(Player player, CallbackInfoReturnable<ItemStack> cir) {
        ItemStack stack = cir.getReturnValue();
        cir.setReturnValue(StatsUtils.getModifiedPartItem(stack, inventoryWrapper.getStack(), false));
    }
}
