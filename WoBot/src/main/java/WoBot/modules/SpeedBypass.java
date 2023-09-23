package WoBot.modules;

import WoBot.WOBOT;
import meteordevelopment.meteorclient.events.game.ReceiveMessageEvent;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.misc.input.Input;
import net.minecraft.client.option.KeyBinding;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.network.packet.c2s.play.VehicleMoveC2SPacket;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class SpeedBypass extends Module
{
    private int chatMessageCount;
    private final ScheduledExecutorService executor;

    public SpeedBypass() {
        super(WOBOT.CATEGORY, "Speed Bypasser", "Bypasses Speed Limit On AVAS.CC");
                this.chatMessageCount = 0;
        this.executor = Executors.newScheduledThreadPool(1);
    }

    @EventHandler
    private void onMessageReceived(final ReceiveMessageEvent event) {
        final String message = event.getMessage().getString();
        this.checkChatMessage(message);
    }

    private void checkChatMessage(final String message) {
        final String searchText = "WARNING: You are moving too fast!";
        if (message.contains(searchText)) {
            ++chatMessageCount;
            if (chatMessageCount > 1) {
                ChatUtils.sendPlayerMsg(".toggle timer");
                setPressed(mc.options.forwardKey, false);
                for (int i = 0; i < 601; ++i) {
                    this.executor.schedule(this::Bypass, i * 100L, TimeUnit.MILLISECONDS);
                }
                chatMessageCount = 1;
                setPressed(mc.options.forwardKey, true);
                ChatUtils.sendPlayerMsg(".toggle timer");
                ChatUtils.info("Bypassed Limit!");
            }
        }
    }

    private void Bypass() {
        this.executeVClip(55);
    }

    public void onActivate() {
        ChatUtils.sendPlayerMsg(".toggle timer");
        setPressed(mc.options.forwardKey, false);
        for (int i = 0; i < 601; ++i) {
            this.executor.schedule(this::Bypass, i * 100L, TimeUnit.MILLISECONDS);
        }
        setPressed(mc.options.forwardKey, true);
        ChatUtils.sendPlayerMsg(".toggle timer");
        ChatUtils.info("Bypassed Limit!");
    }
    private void setPressed(KeyBinding key, boolean pressed) {
        key.setPressed(pressed);
        Input.setKeyState(key, pressed);
    }
    private void executeVClip(double blocks) {
        int packetsRequired = (int) Math.ceil(Math.abs(blocks / 10));

        if (packetsRequired > 20) {
            packetsRequired = 1;
        }

        if (mc.player.hasVehicle()) {
            for (int packetNumber = 0; packetNumber < (packetsRequired - 1); packetNumber++) {
                mc.player.networkHandler.sendPacket(new VehicleMoveC2SPacket(mc.player.getVehicle()));
            }
            mc.player.getVehicle().setPosition(mc.player.getVehicle().getX(), mc.player.getVehicle().getY() + blocks, mc.player.getVehicle().getZ());
            mc.player.networkHandler.sendPacket(new VehicleMoveC2SPacket(mc.player.getVehicle()));
        } else {
            for (int packetNumber = 0; packetNumber < (packetsRequired - 1); packetNumber++) {
                mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.OnGroundOnly(true));
            }
            mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(mc.player.getX(), mc.player.getY() + blocks, mc.player.getZ(), true));
            mc.player.setPosition(mc.player.getX(), mc.player.getY() + blocks, mc.player.getZ());
        }
    }
}
