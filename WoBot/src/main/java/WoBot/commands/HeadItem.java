package WoBot.commands;

import WoBot.utils.MainUtils;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import meteordevelopment.meteorclient.commands.Command;
import net.minecraft.screen.slot.SlotActionType;

import static meteordevelopment.meteorclient.MeteorClient.mc;

public class HeadItem extends Command {
    public HeadItem() {
        super("head-item", "Allows you to put any item in your head slot.", "head");
    }

    public void build(LiteralArgumentBuilder builder) {
        builder.executes((context) -> {
            MainUtils.clickSlotPacket(mc.player.getInventory().selectedSlot + 36, 39, SlotActionType.SWAP);
            return 1;
        });
    }
}
