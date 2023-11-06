package WoBot.modules;

import WoBot.WOBOT;
import baritone.api.BaritoneAPI;
import baritone.api.pathing.goals.Goal;
import baritone.api.pathing.goals.GoalXZ;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.misc.input.Input;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import meteordevelopment.orbit.EventHandler;
import meteordevelopment.orbit.EventPriority;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class RobotWalk extends Module {

    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final Setting<Boolean> Direction = sgGeneral.add(new BoolSetting.Builder()
        .name("current-direction")
        .description("Toggled On = South, Toggled Off = North!")
        .defaultValue(false)
        .build()
    );
    private final Setting<Boolean> walkDirection = sgGeneral.add(new BoolSetting.Builder()
        .name("walk-direction")
        .description("False = west, True = East!")
        .defaultValue(false)
        .build()
    );
    private int boundary = 250000;
    private int goalX = 0;
    private int facing = 0;
    private int count = 0;
    private int groundPos = 0;
    private boolean  gotGoal = false;
    private Goal goal = null; // Store the current goal for detection
    private boolean slowed = false;
    private boolean upped = false;
    private final ExecutorService scheduler = Executors.newSingleThreadExecutor();
    private boolean isActivated = false; // Track module activation
    public RobotWalk() {
        super(WOBOT.CATEGORY, "robot-walk", "Walks the player in a straight line until a boundary is reached.");
    }

    @Override
    public void onActivate() {
        if (!Direction.get()) {
            setPlayerYaw(180);
            facing = 180;
        } else if (Direction.get()) {
            setPlayerYaw(0);
            facing = 0;
        }
        isActivated = true;
    }

    @EventHandler(priority = EventPriority.HIGH)
    private void onTick(TickEvent.Pre event) {
        BoundaryDirection boundaryDirection = checkBoundaryDirection();
        if (boundaryDirection != BoundaryDirection.NONE) {
            if (goal == null) {
                Vec3d playerPos = mc.player.getPos();
                // When entering a new boundary (e.g., crossing into the north boundary), stop walking.
                setPressed(mc.options.forwardKey, false);
                // Rotate the player immediately to face the boundary direction.
                setPlayerYaw(getYawForBoundaryDirection(boundaryDirection));
                goalX = (int) Math.floor(mc.player.getX());
                upped = false;
                ChatUtils.sendPlayerMsg(".toggle flight off");
            }
            if (groundPos == mc.player.getY() && !gotGoal) {
                reachGoal();
                gotGoal = true;
            }
            groundPos = (int) Math.floor(mc.player.getY());

        }else {
            // Continue walking.
            setPlayerYaw(facing);
            if (goal == null) {
                if (!upped) {
                    ChatUtils.sendPlayerMsg("#stop");
                    ChatUtils.sendPlayerMsg(".vclip 100");
                    ChatUtils.sendPlayerMsg(".toggle flight on");
                    gotGoal = false;
                    upped = true;
                }
                setPressed(mc.options.forwardKey, true);
                if (count == 2000) {
                    setPlayerPositionToMiddle();
                    count = 0;
                }
            }else{
                setPressed(mc.options.forwardKey, false);
            }
            if (boundaryDirection == BoundaryDirection.NONE) {
                slowed = false;
            }
        }
        if (goal != null && boundaryDirection == BoundaryDirection.NONE) {
            goal = null;
        }
    }

    @Override
    public void onDeactivate() {
        // Stop walking and rotating when the module is deactivated.
        if (!Direction.get()) {
            setPlayerYaw(180); // Adjust the target yaw as needed.
            facing = 180;
        } else if (Direction.get()) {
            setPlayerYaw(0);
            facing = 0;
        }
        setPressed(mc.options.forwardKey, false);
    }

    private void setPressed(KeyBinding key, boolean pressed) {
        key.setPressed(pressed);
        Input.setKeyState(key, pressed);
    }

    private void setPlayerYaw(float yaw) {
        // Calculate the difference between the current yaw and the target yaw.
        float deltaYaw = yaw - mc.player.getYaw();

        // Normalize the deltaYaw to stay within -180 to 180 degrees.
        deltaYaw = MathHelper.wrapDegrees(deltaYaw);

        // Set the new yaw directly to the player.
        mc.player.setYaw(yaw);
    }

    private void reachGoal() {
        ChatUtils.sendPlayerMsg(".toggle tab-checker on");
        if (checkBoundaryDirection() == BoundaryDirection.NEGATIVE_Z) {
            int goalZ = -249998;
            if (walkDirection.get()) {
                goalX -= 336;
            } else if (!walkDirection.get()) {
                goalX += 336;
            }
            goal = new GoalXZ(goalX, goalZ);
        } else if (checkBoundaryDirection() == BoundaryDirection.POSITIVE_Z) {
            int goalZ = 249998;
            if (walkDirection.get()) {
                goalX -= 336;
            } else if (!walkDirection.get()) {
                goalX += 336;
            }
            goal = new GoalXZ(goalX, goalZ);
        }
        BaritoneAPI.getProvider().getPrimaryBaritone().getCustomGoalProcess().setGoalAndPath(goal);
    }
    private void setPlayerPositionToMiddle() {
        double x = MathHelper.floor(mc.player.getX()) + 0.5;
        double z = MathHelper.floor(mc.player.getZ()) + 0.5;
        mc.player.setPosition(x, mc.player.getY(), z);
        mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(mc.player.getX(), mc.player.getY(), mc.player.getZ(), mc.player.isOnGround()));
    }
    private BoundaryDirection checkBoundaryDirection() {
        Vec3d playerPos = mc.player.getPos();
        if (playerPos.x > boundary) {
            return BoundaryDirection.POSITIVE_X;
        } else if (playerPos.x < -boundary) {
            return BoundaryDirection.NEGATIVE_X;
        } else if (playerPos.z > boundary) {
            return BoundaryDirection.POSITIVE_Z;
        } else if (playerPos.z < -boundary) {
            return BoundaryDirection.NEGATIVE_Z;
        } else {
            return BoundaryDirection.NONE;
        }
    }

    private float getYawForBoundaryDirection(BoundaryDirection direction) {
        switch (direction) {
            case POSITIVE_X -> {
                return 90;
            }
            case NEGATIVE_X -> {
                return -90;
            }
            case POSITIVE_Z -> {
                Direction.set(true);
                facing = 180;
                return 180;
            }
            case NEGATIVE_Z -> {
                Direction.set(false);
                facing = 0;
                return 0;
            }
            default -> {
                return 0;
            }
        }
    }

    private enum BoundaryDirection {
        NONE,
        POSITIVE_X,
        NEGATIVE_X,
        POSITIVE_Z,
        NEGATIVE_Z
    }
}
