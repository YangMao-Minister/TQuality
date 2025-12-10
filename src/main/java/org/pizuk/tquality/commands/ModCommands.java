package org.pizuk.tquality.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.pizuk.tquality.Tquality;

import java.util.function.Consumer;

@Mod.EventBusSubscriber(modid = Tquality.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ModCommands {

    public static final int PERMISSION_LEVEL = 2;

    @SubscribeEvent
    public static void registerCommands(RegisterCommandsEvent event) {
        LiteralArgumentBuilder<CommandSourceStack> builder = Commands.literal(Tquality.MODID);
        register(builder, "quality", QualityCommand::register);

        event.getDispatcher().register(builder);
    }

    private static void register(LiteralArgumentBuilder<CommandSourceStack> root, String name,
                                 Consumer<LiteralArgumentBuilder<CommandSourceStack>> consumer) {
        LiteralArgumentBuilder<CommandSourceStack> subCommand = Commands.literal(name);
        consumer.accept(subCommand);
        root.then(subCommand);
    }
}
