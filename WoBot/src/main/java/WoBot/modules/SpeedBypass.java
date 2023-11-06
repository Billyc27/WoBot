package WoBot.modules;

import WoBot.WOBOT;
import meteordevelopment.meteorclient.events.game.ReceiveMessageEvent;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.misc.input.Input;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class SpeedBypass extends Module
{
    private int chatMessageCount;
    private final ScheduledExecutorService executor;

    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Boolean> Activator = sgGeneral.add(new BoolSetting.Builder()
        .name("Bypass On Activate")
        .description("Whether To Performs Bypass On Activate Or Not")
        .defaultValue(false)
        .build()
    );

    private final Setting<Boolean> Walker = sgGeneral.add(new BoolSetting.Builder()
        .name("AutoWalk On Bypass Complete")
        .description("Whether Or Not To Continue Walking After Bypassing")
        .defaultValue(true)
        .build()
    );

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
                chatMessageCount = 1;
                for (int i = 0; i < 601; ++i) {
                    final int delay = i * 100;
                    final int finalI = i;
                    this.executor.schedule(() -> {
                        this.Bypass(); // Call the Bypass method
                        if (finalI == 600) {
                            if (Walker.get()) {
                                setPressed(mc.options.forwardKey, true);
                            }
                            ChatUtils.sendPlayerMsg(".toggle timer on");
                            ChatUtils.sendPlayerMsg(".toggle flight on");
                            ChatUtils.info("Bypassed Limit!");
                            chatMessageCount++;
                        } else if (finalI == 0) {
                            ChatUtils.sendPlayerMsg(".toggle timer off");
                            ChatUtils.sendPlayerMsg(".toggle flight off");
                            setPressed(mc.options.forwardKey, false);
                        }
                    }, delay, TimeUnit.MILLISECONDS);
                }

            }
            }
        }
    private void Bypass() {
        this.executeVClip(55);
    }

    public void onActivate() {
        if (Activator.get()) {
            for (int i = 0; i < 601; ++i) {
                final int delay = i * 100;
                final int finalI = i;
                this.executor.schedule(() -> {
                    this.Bypass(); // Call the Bypass method
                    if (finalI == 600) {
                        if (Walker.get()) {
                            setPressed(mc.options.forwardKey, true);
                        }
                        ChatUtils.sendPlayerMsg(".toggle timer on");
                        ChatUtils.sendPlayerMsg(".toggle flight on");
                        ChatUtils.info("Bypassed Limit!");
                        chatMessageCount++;
                    } else if (finalI == 0) {
                        ChatUtils.sendPlayerMsg(".toggle timer off");
                        ChatUtils.sendPlayerMsg(".toggle flight off");
                        setPressed(mc.options.forwardKey, false);
                    }
                }, delay, TimeUnit.MILLISECONDS);
            }
        }
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

        for (int packetNumber = 0; packetNumber < (packetsRequired - 1); packetNumber++) {
            mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.OnGroundOnly(true));
        }
        mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(mc.player.getX(), mc.player.getY() + blocks, mc.player.getZ(), true));
        mc.player.setPosition(mc.player.getX(), mc.player.getY() + blocks, mc.player.getZ());
    }
}
