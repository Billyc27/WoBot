package com.WoBot.addon.modules;

import com.WoBot.addon.Addon;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import meteordevelopment.meteorclient.events.world.TickEvent;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.Set;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

// ...

public class PortalLocator extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private PrintWriter writer;

    private final Setting<Boolean> logToFile = sgGeneral.add(new BoolSetting.Builder()
            .name("log-to-file")
            .description("Log the coordinates of Nether portal blocks to a text file.")
            .defaultValue(true)
            .build()
    );

    private final Setting<Boolean> logToDiscord = sgGeneral.add(new BoolSetting.Builder()
            .name("log-to-discord")
            .description("Log the coordinates of Nether portal blocks to a Discord webhook.")
            .defaultValue(true)
            .build()
    );

    private long lastCheckTime = 0;
    private static final long CHECK_INTERVAL_MS = 1000; // Check every second

    private Set<BlockPos> recordedPortals = new HashSet<>();
    private String discordWebhookUrl = "YOUR_DISCORD_WEBHOOK_HERE!"; //i think it is laggy when using so be careful you game doesnt freeze / crash

    public PortalLocator() {
        super(Addon.CATEGORY, "Portal Locator", "Records the coordinates of Nether portal blocks - used with WoBot for Heatmap");
    }

    @Override
    public void onActivate() {
        if (logToFile.get()) {
            try {
                writer = new PrintWriter(new FileWriter("portal_coordinates.txt", true)); // Append mode
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onDeactivate() {
        if (writer != null) {
            writer.close();
        }
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastCheckTime >= CHECK_INTERVAL_MS) {
            lastCheckTime = currentTime;
            savePortalCoordinates();
        }
    }

    private void savePortalCoordinates() {
        BlockPos playerPos = mc.player.getBlockPos();
        for (int x = -100; x <= 100; x++) {
            for (int y = -100; y <= 100; y++) {
                for (int z = -100; z <= 100; z++) {
                    BlockPos pos = playerPos.add(x, y, z);
                    if (mc.world.getBlockState(pos).getBlock() == Blocks.NETHER_PORTAL &&
                            recordedPortals.add(pos)) {
                        String message = "Nether portal found at X: " + pos.getX() + " Y: " + pos.getY() + " Z: " + pos.getZ();
                        if (logToFile.get()) {
                            writer.println(message);
                        }
                        if (logToDiscord.get()) {
                            sendToDiscordWebhook(message);
                        }
                    }
                }
            }
        }
    }

    private void sendToDiscordWebhook(String message) {
        try {
            URL url = new URL(discordWebhookUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setDoOutput(true);

            String json = "{\"content\": \"" + message + "\"}";
            byte[] postData = json.getBytes(StandardCharsets.UTF_8);

            connection.getOutputStream().write(postData);

            int responseCode = connection.getResponseCode();
            if (responseCode == 204) {
                System.out.println("Successfully sent message to Discord webhook.");
            } else {
                System.err.println("Failed to send message to Discord webhook. Response code: " + responseCode);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
