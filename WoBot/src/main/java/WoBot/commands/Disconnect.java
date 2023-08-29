package WoBot.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import meteordevelopment.meteorclient.commands.Command;
import net.minecraft.command.CommandSource;
import net.minecraft.network.packet.s2c.play.DisconnectS2CPacket;
import net.minecraft.text.Text;

import static com.mojang.brigadier.Command.SINGLE_SUCCESS;
import static meteordevelopment.meteorclient.MeteorClient.mc;
public class Disconnect extends Command {

    public Disconnect() {
        super("disconnect", "Disconnects you from the server.");
    }

    @Override
    public void build(LiteralArgumentBuilder<CommandSource> builder) {
        builder.executes(ctx -> {
            mc.player.networkHandler.onDisconnect(new DisconnectS2CPacket(Text.literal("Forced Disconnect From WoBot Addon!.")));
            return SINGLE_SUCCESS;
        });
    }
}
