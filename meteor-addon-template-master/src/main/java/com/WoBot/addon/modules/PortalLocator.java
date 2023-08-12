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

public class PortalLocator extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private PrintWriter writer;

    private final Setting<Boolean> logCoordinates = sgGeneral.add(new BoolSetting.Builder()
            .name("log-coordinates")
            .description("Log the coordinates of Nether portal blocks to a text file.")
            .defaultValue(true)
            .build()
    );

    private long lastCheckTime = 0;
    private static final long CHECK_INTERVAL_MS = 1000; // Check every second

    public PortalLocator() {
        super(Addon.CATEGORY, "Portal Locator", "Records the coordinates of Nether portal blocks - used with WoBot for Heatmap");
    }

    @Override
    public void onActivate() {
        if (logCoordinates.get()) {
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
        if (writer == null) {
            return;
        }

        BlockPos playerPos = mc.player.getBlockPos();
        for (int x = -100; x <= 100; x++) {
            for (int y = -100; y <= 100; y++) {
                for (int z = -100; z <= 100; z++) {
                    BlockPos pos = playerPos.add(x, y, z);
                    if (mc.world.getBlockState(pos).getBlock() == Blocks.NETHER_PORTAL) {
                        writer.println("Nether portal found at X: " + pos.getX() + " Y: " + pos.getY() + " Z: " + pos.getZ());
                    }
                }
            }
        }
    }
}