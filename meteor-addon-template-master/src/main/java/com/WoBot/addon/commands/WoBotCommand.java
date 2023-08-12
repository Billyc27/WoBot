package com.WoBot.addon.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import meteordevelopment.meteorclient.commands.Command;
import net.minecraft.command.CommandSource;

import static com.mojang.brigadier.Command.SINGLE_SUCCESS;

public class WoBotCommand extends Command {
    public WoBotCommand() {
        super("gift", "Sends You A Present");
    }

    @Override
    public void build(LiteralArgumentBuilder<CommandSource> builder) {
        builder.executes(context -> {
            info(":]");
            return SINGLE_SUCCESS;
        });
    }
}
