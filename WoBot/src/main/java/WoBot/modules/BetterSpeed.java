package WoBot.modules;

import WoBot.WOBOT;
import meteordevelopment.meteorclient.events.game.ReceiveMessageEvent;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.world.Timer;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import meteordevelopment.orbit.EventHandler;

public class BetterSpeed extends Module {

    private final SettingGroup sgGeneral = settings.createGroup("General");
    private final Setting<Boolean> showCooldownMessage = sgGeneral.add(new BoolSetting.Builder()
        .name("Show Cooldown Message")
        .description("Display a chat message when the cooldown is activated.")
        .defaultValue(true)
        .build()
    );

    private int chatMessageCount = 0;
    private boolean timerOverride = false;
    private long cooldownStartTime = 0;

    Module timerModule = Modules.get().get(Timer.class);

    private static final long ONE_MINUTE_MILLIS = 60000;
    private static final int MESSAGE_THRESHOLD = 9; // Changed threshold to 9

    private final Setting<Integer> multiplier = sgGeneral.add(new IntSetting.Builder()
        .name("Multiplier")
        .description("How much to multiply the Speed by")
        .defaultValue(2)
        .min(1)
        .sliderMax(20)
        .build()
    );

    private int multiplier1 = (multiplier.get() * 8);
    public BetterSpeed() {
        super(WOBOT.CATEGORY, "Better Speed", "Can be used to travel faster with an oversight in the speed limit");
    }

    @EventHandler
    private void onMessageReceived(ReceiveMessageEvent event) {
        String message = event.getMessage().getString();
        checkChatMessage(message);
    }

    private void checkChatMessage(String message) {
        String searchText = "WARNING: You are moving too fast!";

        if (message.contains(searchText)) {
            chatMessageCount++;

            if (chatMessageCount >= MESSAGE_THRESHOLD) {
                timerOverride = true;
                cooldownStartTime = System.currentTimeMillis();

                if (showCooldownMessage.get()) {
                    sendCooldownMessage();
                }

                // Set the Timer module's multiplier to 8
                ((Timer) timerModule).setOverride(8);

                // Reset the count to 1 to start counting again
                resetChatMessageCount();

                // Reset the Timer module's multiplier back to 16 after the cooldown
                resetTimerAfterCooldown();
            }
        }
    }

    @Override
    public void onActivate() {
        super.onActivate();
        resetChatMessageCount();
        ((Timer) timerModule).setOverride(multiplier1);
    }

    @Override
    public void onDeactivate() {
        ((Timer) timerModule).setOverride(1);
        super.onDeactivate();
    }

    private void resetChatMessageCount() {
        chatMessageCount = 0;
    }

    private void sendCooldownMessage() {
        ChatUtils.info("Cooldown Started");
    }

    private void resetTimerAfterCooldown() {
        new Thread(() -> {
            try {
                Thread.sleep(ONE_MINUTE_MILLIS);
                if (timerOverride) {
                    timerOverride = false;
                    ChatUtils.info("Cooldown Finished, You're back up to speed");
                    ((Timer) timerModule).setOverride(multiplier1);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }
}
