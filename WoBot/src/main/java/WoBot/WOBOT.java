package WoBot;

import WoBot.commands.*;
import WoBot.modules.*;
import com.mojang.logging.LogUtils;
import meteordevelopment.meteorclient.addons.MeteorAddon;
import meteordevelopment.meteorclient.commands.Commands;
import meteordevelopment.meteorclient.systems.modules.Category;
import meteordevelopment.meteorclient.systems.modules.Modules;
import org.slf4j.Logger;

public class WOBOT extends MeteorAddon {
    public static final Logger LOG = LogUtils.getLogger();
    public static final Category CATEGORY = new Category("WoBot");

    @Override
    public void onInitialize() {
        LOG.info("Initializing WoBot Addon");

        // Modules
        Modules.get().add(new PortalLocator());
        Modules.get().add(new AxisViewer());
        Modules.get().add(new PatternWalk());
        Modules.get().add(new DiscordRPC());
        Modules.get().add(new VillagerRoller());
        Modules.get().add(new AutoTNT());
        Modules.get().add(new ChatBot());
        Modules.get().add(new LavaCast());
        Modules.get().add(new AutoWither());
        // Commands
        Commands.add(new Gift());
        Commands.add(new Coordinates());
        Commands.add(new Disconnect());
        Commands.add(new AutoClip());
        Commands.add(new HeadItem());
        Commands.add(new DelItem());
    }

    @Override
    public void onRegisterCategories() {
        Modules.registerCategory(CATEGORY);
    }

    @Override
    public String getPackage() {
        return "WoBot";
    }
}
