package org.pizuk.tquality.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.world.item.ItemStack;
import org.pizuk.tquality.Tquality;
import org.pizuk.tquality.utils.StatsUtils;
import slimeknights.tconstruct.library.tools.part.ToolPartItem;

import java.util.Objects;

public class QualityCommand {
    public static void register(LiteralArgumentBuilder<CommandSourceStack> subCommand) {
        subCommand.requires(source -> source.hasPermission(ModCommands.PERMISSION_LEVEL))
                // quality query
                .then(Commands.literal("query")
                        .executes(QualityCommand::getQuality))

                // quality set <quality>
                .then(Commands.literal("set")
                        .then(Commands.argument("quality", FloatArgumentType.floatArg(0, 1.2f))
                                .executes(ctx -> setQuality(ctx, false))))

                // quality random
                .then(Commands.literal("random")
                        .executes(ctx -> setQuality(ctx, true)))

                // quality upgrade <deltaQuality>
                .then(Commands.literal("upgrade")
                        .then(Commands.argument("deltaQuality", FloatArgumentType.floatArg(-1.0f, 1.0f))
                                .executes(QualityCommand::upgrade)));
    }

    public static int setQuality(CommandContext<CommandSourceStack> ctx, boolean random) {
        CommandSourceStack source = ctx.getSource();
        ItemStack stack = Objects.requireNonNull(source.getPlayer()).getMainHandItem();
        if (stack.isEmpty()) {
            source.sendFailure(net.minecraft.network.chat.Component.translatable("tquality.quality_set_failure"));
            return 0;
        }
        float quality;
        if (random) {
            quality = Tquality.RANDOM.nextFloat();
        } else {
            quality = FloatArgumentType.getFloat(ctx, "quality");
        }
        if (stack.getItem() instanceof ToolPartItem) {
            StatsUtils.injectStatsNbtToPart(stack, stack, quality);
        }
        stack.getOrCreateTag().putFloat(Tquality.QUALITY, quality);
        source.sendSuccess(() -> net.minecraft.network.chat.Component.translatable("tquality.quality_set_success").append(String.valueOf(quality)), true);
        return Command.SINGLE_SUCCESS;
    }

    public static int getQuality(CommandContext<CommandSourceStack> ctx) {
        CommandSourceStack source = ctx.getSource();
        ItemStack stack = Objects.requireNonNull(source.getPlayer()).getMainHandItem();
        if (stack.hasTag()) {
            float quality = stack.getTag().getFloat(Tquality.QUALITY);
            if (quality == 0) {
                quality = stack.getTag().getCompound(Tquality.PART_DATA).getFloat(Tquality.QUALITY);
            }
            float finalQuality = quality;
            source.sendSuccess(() -> net.minecraft.network.chat.Component.translatable("tquality.quality_query_success").append(String.valueOf(finalQuality)), true);
            return Command.SINGLE_SUCCESS;
        }
        source.sendFailure(net.minecraft.network.chat.Component.translatable("tquality.quality_query_failure"));
        return 0;
    }

    public static int upgrade(CommandContext<CommandSourceStack> ctx) {
        CommandSourceStack source = ctx.getSource();
        ItemStack stack = Objects.requireNonNull(source.getPlayer()).getMainHandItem();
        if (stack.isEmpty() || !stack.hasTag() || !(stack.getItem() instanceof ToolPartItem) || stack.getTag().getBoolean(Tquality.DEFAULT_QUALITY)) {
            source.sendFailure(net.minecraft.network.chat.Component.translatable("tquality.quality_set_failure"));
            return 0;
        }
        float deltaQuality = FloatArgumentType.getFloat(ctx, "deltaQuality");
        StatsUtils.updateStatsByQuality(stack, deltaQuality);
        source.sendSuccess(() -> net.minecraft.network.chat.Component.translatable("tquality.quality_upgrade_success"), true);
        return Command.SINGLE_SUCCESS;
    }
}
