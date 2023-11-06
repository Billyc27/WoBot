package WoBot.modules;

import WoBot.WOBOT;
import meteordevelopment.meteorclient.events.game.ReceiveMessageEvent;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.settings.StringListSetting;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.network.PlayerListEntry;

import java.util.List;

public class TabChecker extends Module {

    private final SettingGroup sgAdminList = settings.createGroup("Admin List");
    private final Setting<List<String>> adminList = sgAdminList.add(new StringListSetting.Builder()
        .name("admin-list")
        .description("List of administrators")
        .build()
    );
    private final SettingGroup sgGeneral = settings.createGroup("Print To Chat");
    private final Setting<Boolean> printToChat = sgGeneral.add(new BoolSetting.Builder()
        .name("print-to-chat")
        .description("prints the list to chat for player to see info")
        .defaultValue(false)
        .build()
    );

    boolean printChat = printToChat.get();
    private boolean adminFound = false; // Initialize the boolean to false
    private boolean tpsLow = false;

    public TabChecker() {
        super(WOBOT.CATEGORY, "tab-checker", "Detects Currently online players");
    }

    @Override
    public void onActivate() {
        checkTPS();
        checkPlayersOnline();
        writeAdminListToChat();
        comparePlayersToAdmins();
    }

    private void checkPlayersOnline() {
        int onlinePlayers = 0;
        printChat = printToChat.get();
        if (printChat) ChatUtils.warningPrefix("WoBot", "Players online: ");
        for (PlayerListEntry playerEntry : mc.getNetworkHandler().getPlayerList()) {
            String playerName = playerEntry.getProfile().getName();
            if (printChat) ChatUtils.infoPrefix("WoBot", playerName);
            onlinePlayers++;
        }

        if (printChat)ChatUtils.warningPrefix("WoBot", "Total Players: " + onlinePlayers);
    }

    private void checkTPS() {
        ChatUtils.sendPlayerMsg("/tps");
    }

    @EventHandler
    private void onMessageReceived(final ReceiveMessageEvent event) {
        final String message = event.getMessage().getString();
        this.checkChatMessage(message);
    }

    private void checkChatMessage(final String message) {
        final String searchText = "TPS: ";
        if (message.contains(searchText)) {
            String tps = message;
            int inTps = Integer.parseInt(tps);
            tpsLow = false;
            tpsLow = inTps < 16;
            if (tpsLow) {
                ChatUtils.sendPlayerMsg(".settings flight speed 0.16");
            }
        }
    }

    public void setAdminList(List<String> newAdminList) {
        adminList.set(newAdminList);
    }

    public List<String> getAdminList() {
        return adminList.get();
    }

    private void writeAdminListToChat() {
        printChat = printToChat.get();
        List<String> adminListValues = adminList.get();

        if (adminListValues.isEmpty() && printChat) ChatUtils.errorPrefix("WoBot", "Admin List is empty.");
        else {
            if (printChat) ChatUtils.errorPrefix("WoBot", "Admin List:");
            for (String admin : adminListValues) {
                if (printChat) ChatUtils.errorPrefix("WoBot", admin);
            }
        }
    }

    private void comparePlayersToAdmins() {
        printChat = printToChat.get();
        List<String> adminListValues = adminList.get();

        if (adminListValues.isEmpty()) {
            return; // Admin list is empty, nothing to compare
        }

        for (PlayerListEntry playerEntry : mc.getNetworkHandler().getPlayerList()) {
            String playerName = playerEntry.getProfile().getName();
            if (adminListValues.contains(playerName)) {
                if (printChat) ChatUtils.infoPrefix("WoBot", playerName + " is an admin.");
                ChatUtils.sendPlayerMsg(".settings flight speed 0.035");
                adminFound = true; // Set the boolean to true if an admin is found
            }
        }
    }

    public boolean isAdminFound() {
        return adminFound;
    }
    public boolean isTpsLow() {
        return tpsLow;
    }
}
