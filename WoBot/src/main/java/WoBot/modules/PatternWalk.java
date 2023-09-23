package WoBot.modules;

import WoBot.WOBOT;
import baritone.api.BaritoneAPI;
import baritone.api.pathing.goals.Goal;
import baritone.api.pathing.goals.GoalXZ;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.settings.StringSetting;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.io.*;
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

    private Goal goal; // Store the current goal for detection
    private String currentGoalLine; // Store the current goal line

    private final ExecutorService scheduler = Executors.newSingleThreadExecutor();

    private boolean isActivated = false; // Track module activation

    public PatternWalk() {
        super(WOBOT.CATEGORY, "Pattern Walk", "Walks the player in a specific pattern to load chunks the most optimal way");
    }

    @Override
    public void onActivate() {
        isActivated = true;
        scheduler.submit(this::startPatternWalk);
    }

    @Override
    public void onDeactivate() {
        isActivated = false;
    }

    private void startPatternWalk() {
        while (isActivated) {
            try {
                BufferedReader reader = new BufferedReader(new FileReader(filePath.get()));
                String line = reader.readLine();
                reader.close();

                if (line != null) {
                    currentGoalLine = line; // Store the current goal line
                    String[] goalData = currentGoalLine.split(",");
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
                        removeCompletedGoalFromFile(filePath.get()); // Remove the completed goal from the file
                        goal = null; // Reset the goal for the next iteration
                    }
                } else {
                    // No more goals in the file, deactivate the module
                    isActivated = false;
                }
            } catch (IOException e) {
                ChatUtils.error("Error reading goals from file.");
            }
        }
    }

    private void removeCompletedGoalFromFile(String filePath) {
        try {
            File file = new File(filePath);
            File tempFile = new File(filePath + ".tmp");
            BufferedReader reader = new BufferedReader(new FileReader(file));
            BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile));

            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.equals(currentGoalLine)) {
                    writer.write(line);
                    writer.newLine();
                }
            }

            reader.close();
            writer.close();

            if (file.delete()) {
                tempFile.renameTo(file);
            }

            // Reset the current goal line for the next iteration
            currentGoalLine = null;
        } catch (IOException e) {
            ChatUtils.error("Error removing completed goal from file.");
        }
    }
}
