package WoBot.modules;

import WoBot.WOBOT;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.player.ChatUtils;

public class Test extends Module {
    public Test() {
        super(WOBOT.CATEGORY, "Test", "Your custom module description.");
    }

    @Override
    public void onActivate() {
        ChatUtils.info("Get Tested BoZo");
    }
}
