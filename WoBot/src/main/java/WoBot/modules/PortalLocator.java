package WoBot.modules;

import WoBot.WOBOT;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.network.packet.s2c.play.ChunkData;

public class PortalLocator extends Module {
    public PortalLocator() {
        super(WOBOT.CATEGORY, "Chunk Info", "Records the blocks within a current section");
    }

    @EventHandler
    private void onChunkData(PacketEvent.Receive event) {
        if (event.packet instanceof ChunkData) {
            ChunkData packet = (ChunkData) event.packet;
            ChatUtils.warningPrefix("WoBot", String.valueOf(packet.getHeightmap()));
        }
    }
}
