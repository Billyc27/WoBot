package WoBot.utils;

import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import net.minecraft.network.packet.c2s.play.ClickSlotC2SPacket;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;

import static meteordevelopment.meteorclient.MeteorClient.mc;

public class MainUtils {

    public static void clickSlotPacket(int fromIndex, int toIndex, SlotActionType type) {
        ScreenHandler sh = mc.player.currentScreenHandler;
        Slot slot = sh.getSlot(fromIndex);
        Int2ObjectArrayMap stack = new Int2ObjectArrayMap();
        stack.put(fromIndex, slot.getStack());
        mc.player.networkHandler.sendPacket(new ClickSlotC2SPacket(sh.syncId, sh.getRevision(), slot.id, toIndex, type, sh.getSlot(fromIndex).getStack(), stack));
    }

}
