package WoBot.modules;

import WoBot.WOBOT;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class PortalLocator extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private PrintWriter writer;

    private final Setting<Boolean> logToFile = sgGeneral.add(new BoolSetting.Builder()
            .name("log-to-file")
            .description("Log the coordinates of Nether portal blocks to a text file.")
            .defaultValue(true)
            .build()
    );

    private Set<BlockPos> recordedPortals = new HashSet<>();

    private static final long CHECK_INTERVAL_MS = 250; // 1 second interval
    private ScheduledExecutorService executorService;

    public PortalLocator() {
        super(WOBOT.CATEGORY, "Portal Locator", "Records the coordinates of Nether portal blocks - used with WoBot for Heatmap");
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

        // Start a scheduled executor to continuously check portal blocks
        executorService = Executors.newScheduledThreadPool(1);
        executorService.scheduleAtFixedRate(this::savePortalCoordinates, 0, CHECK_INTERVAL_MS, TimeUnit.MILLISECONDS);
    }

    @Override
    public void onDeactivate() {
        if (writer != null) {
            writer.close(); // Close the PrintWriter to save changes
        }

        // Shutdown the executor service when the module is deactivated
        if (executorService != null) {
            executorService.shutdownNow();
        }
    }

    private void savePortalCoordinates() {
        BlockPos playerPos = mc.player.getBlockPos();
        int playerY = mc.player.getBlockPos().getY() - 3;
        for (int x = -168; x <= 168; x+= 2 ){
            for (int y = -playerY; y <= 0; y+= 3 ) {
                for (int z = -168; z <= 168; z+= 2 ) {
                    BlockPos pos = playerPos.add(x, y, z);
                    if (mc.world.getBlockState(pos).getBlock() == Blocks.NETHER_PORTAL &&
                            recordedPortals.add(pos)) {
                        if (logToFile.get()) {
                            writer.println("Nether portal found at X: " + pos.getX() + " Y: " + pos.getY() + " Z: " + pos.getZ());
                            writer.flush(); // Flush the buffer to update the file immediately
                        }
                    }
                }
            }
        }
    }
}
