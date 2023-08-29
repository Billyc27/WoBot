package WoBot.modules;

import WoBot.WOBOT;
import baritone.api.BaritoneAPI;
import baritone.api.pathing.goals.Goal;
import baritone.api.pathing.goals.GoalXZ;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.settings.StringSetting;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PatternWalk extends Module {

    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final Setting<String> filePath = sgGeneral.add(new StringSetting.Builder()
        .name("file-path")
        .description("The path to the .txt file containing goals.")
        .defaultValue("path/to/your/goals.txt")
        .build()
    );

    private List<String> goals;
    private int currentGoalIndex = 0;
    private Goal goal; // Store the current goal for detection

    private final ExecutorService fileExecutor = Executors.newSingleThreadExecutor();
    private final ExecutorService scheduler = Executors.newSingleThreadExecutor();

    public PatternWalk() {
        super(WOBOT.CATEGORY, "Pattern Walk", "Walks the player in a specific pattern to load chunks the most optimal way");
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        if (mc.player == null || goals.isEmpty()) return;

        String[] goalData = goals.get(0).split(",");
        double x = Double.parseDouble(goalData[0]);
        double z = Double.parseDouble(goalData[1]);
        double yaw = Double.parseDouble(goalData[2]);

        // Convert double coordinates to integers
        int goalX = (int) Math.floor(x);
        int goalZ = (int) Math.floor(z);

        // Create a new GoalXZ based on the integer x and z coordinates
        goal = new GoalXZ(goalX, goalZ);

        // Set the goal for Baritone to walk to
        BaritoneAPI.getProvider().getPrimaryBaritone().getCustomGoalProcess().setGoalAndPath(goal);

        // Convert player's position to BlockPos
        Vec3d playerPos = mc.player.getPos();
        BlockPos blockPos = new BlockPos((int) Math.floor(playerPos.getX()), (int) Math.floor(playerPos.getY()), (int) Math.floor(playerPos.getZ()));

        // Check if the goal has been reached
        if (goal != null && goal.isInGoal(blockPos)) {
            removeCompletedGoalFromFileAsync(filePath.get()); // Remove the completed goal from the file asynchronously
            goal = null; // Reset the goal for the next iteration
        }
    }

    private void removeCompletedGoalFromFileAsync(String filePath) {
        fileExecutor.submit(() -> {
            try {
                List<String> updatedGoals = new ArrayList<>();
                BufferedReader reader = new BufferedReader(new FileReader(filePath));

                String line;
                while ((line = reader.readLine()) != null) {
                    if (!line.equals(goals.get(0))) {
                        updatedGoals.add(line);
                    }
                }

                reader.close();

                // Write updated goals to the same file
                BufferedWriter writer = new BufferedWriter(new FileWriter(filePath));
                for (String goal : updatedGoals) {
                    writer.write(goal);
                    writer.newLine();
                }

                writer.close();

                // Schedule a task to update the goals in memory and reset the goal after a delay (e.g., 1 second)
                scheduler.submit(() -> {
                    try {
                        Thread.sleep(1000); // Delay in milliseconds (1 second)
                        goals = updatedGoals;
                        goal = null;
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                });
            } catch (IOException e) {
                ChatUtils.error("Error removing completed goal from file.");
            }
        });
    }

    @Override
    public void onActivate() {
        try {
            goals = readGoalsFromFile(filePath.get());
            currentGoalIndex = 0;
        } catch (IOException e) {
            ChatUtils.error("Error reading goals from file.");
            toggle();
        }
    }

    private List<String> readGoalsFromFile(String filePath) throws IOException {
        List<String> goalList = new ArrayList<>();
        BufferedReader reader = new BufferedReader(new FileReader(filePath));

        String line;
        while ((line = reader.readLine()) != null) {
            goalList.add(line);
        }

        reader.close();
        return goalList;
    }
}
