package WoBot.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import meteordevelopment.meteorclient.commands.Command;
import net.minecraft.command.CommandSource;

public class lockRotation extends Command {

    public lockRotation() {
        super("lock", "locks rotation to point you towards a destination");
    }

    @Override
    public void build(LiteralArgumentBuilder<CommandSource> builder) {

    }
}
