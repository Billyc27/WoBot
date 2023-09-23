package WoBot.modules;

import WoBot.WOBOT;
import baritone.api.BaritoneAPI;
import baritone.api.pathing.goals.Goal;
import baritone.api.pathing.goals.GoalXZ;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import meteordevelopment.meteorclient.utils.misc.input.Input;
import meteordevelopment.orbit.EventHandler;
import meteordevelopment.orbit.EventPriority;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class RobotWalk extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private int goalX;
    private int goalZ;
    private int checkCount;
    private boolean flightSet = true;
    private double boundary = 250000.0;
    private Goal goal;
    private Vec3d goalPosition;
    private final int moveDistance = 336;
    private int currentYaw = 180;
    private boolean hasGoalBeenSet = false;

    public RobotWalk() {
        super(WOBOT.CATEGORY, "Robot Walk", "Walks the player within a set of boundaries to load all possible blocks");
    }

    @Override
    public void onActivate() {
        // Call the setPlayerYaw method to set the player's rotation.
        setPlayerYaw(180); // Adjust the target yaw as needed.
    }

    @EventHandler(priority = EventPriority.HIGH)
    private void onTick(TickEvent.Pre event) {
        BoundaryDirection boundaryDirection = checkBoundaryDirection();

        if (!hasGoalBeenSet && boundaryDirection != BoundaryDirection.NONE) {
            // Only set a new goal if one hasn't been set already
            hasGoalBeenSet = true;
            setPressed(mc.options.forwardKey, false);
            ChatUtils.sendPlayerMsg(".toggle flight");
            flightSet = false;

            if (boundaryDirection == BoundaryDirection.NEGATIVE_Z || boundaryDirection == BoundaryDirection.POSITIVE_Z) {
                setPlayerYaw(-90);
            } else if (boundaryDirection == BoundaryDirection.NEGATIVE_X || boundaryDirection == BoundaryDirection.POSITIVE_X) {
                setPlayerYaw(180);
            }

            // Calculate the new position in front of the player.
            Vec3d playerPos = mc.player.getPos();
            Vec3d newPosition = calculateNewGoalWithinBoundary(playerPos);
            goalX = (int) newPosition.x;
            goalZ = (int) newPosition.z;

            goal = new GoalXZ(goalX, goalZ);
            checkCount++;
            BaritoneAPI.getProvider().getPrimaryBaritone().getCustomGoalProcess().setGoalAndPath(goal);
        } else if (boundaryDirection == BoundaryDirection.NONE) {
            // Reset the goal tracking when not at a boundary
            hasGoalBeenSet = false;
            setPlayerYaw(currentYaw);
            // Continue walking
            setPressed(mc.options.forwardKey, true);
            if (!flightSet) {
                ChatUtils.sendPlayerMsg(".toggle flight");
                flightSet = true;
            }
        } else {
            // Stop walking when the robot walk is disabled.
            setPressed(mc.options.forwardKey, false);
        }

        // Check if the goal has been completed
        checkGoalCompletion();
    }

    private void setPressed(KeyBinding key, boolean pressed) {
        key.setPressed(pressed);
        Input.setKeyState(key, pressed);
    }

    private void checkGoalCompletion() {
        Vec3d playerPos = mc.player.getPos();
        if (goalX == (int) playerPos.x && goalZ == (int) playerPos.z) {
            // Rotate the player
            setPlayerYaw(currentYaw);

            // Calculate a new goal within the boundary
            Vec3d newPosition = calculateNewGoalWithinBoundary(playerPos);
            goalX = (int) newPosition.x;
            goalZ = (int) newPosition.z;
            goal = new GoalXZ(goalX, goalZ);

            // Set forward movement key to "pressed" to continue walking
            setPressed(mc.options.forwardKey, true);

            // Reset the check count
            checkCount = 0;
        }
    }


    @Override
    public void onDeactivate() {
        // Stop walking and rotating when the module is deactivated.
        setPressed(mc.options.forwardKey, false);
    }

    private void setPlayerYaw(float yaw) {
        // Calculate the difference between the current yaw and the target yaw.
        float deltaYaw = yaw - mc.player.getYaw();

        // Normalize the deltaYaw to stay within -180 to 180 degrees.
        deltaYaw = MathHelper.wrapDegrees(deltaYaw);

        // Set the new yaw directly to the player.
        mc.player.setYaw(yaw);
    }

    private BoundaryDirection checkBoundaryDirection() {
        Vec3d playerPos = mc.player.getPos();
        if (playerPos.x > boundary) {
            currentYaw = 90;
            return BoundaryDirection.POSITIVE_X;
        } else if (playerPos.x < -boundary) {
            currentYaw = -90;
            return BoundaryDirection.NEGATIVE_X;
        } else if (playerPos.z > boundary) {
            currentYaw = 180;
            return BoundaryDirection.POSITIVE_Z;
        } else if (playerPos.z < -boundary) {
            currentYaw = 0;
            return BoundaryDirection.NEGATIVE_Z;
        } else {
            return BoundaryDirection.NONE;
        }
    }

    private Vec3d calculateNewGoalWithinBoundary(Vec3d playerPos) {
        // Calculate the new X coordinate by adding the move distance to the current X position
        double newX = playerPos.x + moveDistance;
        boundary = 249999;
        // Keep the Z coordinate within the boundary without any change
        double newZ = Math.max(Math.min(playerPos.z, boundary), -boundary);

        boundary = 250000;
        return new Vec3d(newX, playerPos.y, newZ);
    }

    private enum BoundaryDirection {
        NONE,
        POSITIVE_X,
        NEGATIVE_X,
        POSITIVE_Z,
        NEGATIVE_Z
    }
}
