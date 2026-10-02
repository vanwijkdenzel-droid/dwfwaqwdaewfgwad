package dev.cpvp.module.impl;

import dev.cpvp.mixin.HandledScreenAccessor;
import dev.cpvp.mixin.MouseAccessor;
import dev.cpvp.module.Category;
import dev.cpvp.module.Module;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.item.Items;
import net.minecraft.screen.slot.Slot;
import org.lwjgl.glfw.GLFW;

/** When you open your inventory, the mouse cursor jumps onto a totem of undying. */
public final class InvTotemModule extends Module {
    private Screen last;
    private int wait = -1;

    public InvTotemModule() { super("InvTotem", "Cursor jumps to a totem when inventory opens", Category.INVENTORY); }

    @Override public void onTick() {
        Screen s = mc.currentScreen;
        if (s != last) { last = s; wait = (s instanceof InventoryScreen) ? 1 : -1; }
        if (wait > 0) { wait--; if (wait == 0 && s instanceof InventoryScreen inv) moveToTotem(inv); }
    }

    private void moveToTotem(InventoryScreen screen) {
        HandledScreenAccessor acc = (HandledScreenAccessor) (Object) screen;
        for (Slot slot : screen.getScreenHandler().slots) {
            if (slot.id < 9 || slot.id > 44 || !slot.getStack().isOf(Items.TOTEM_OF_UNDYING)) continue;
            double scale = mc.getWindow().getScaleFactor();
            double px = (acc.cpvp$getX() + slot.x + 8) * scale;
            double py = (acc.cpvp$getY() + slot.y + 8) * scale;
            GLFW.glfwSetCursorPos(mc.getWindow().getHandle(), px, py);
            ((MouseAccessor) (Object) mc.mouse).cpvp$setX(px);
            ((MouseAccessor) (Object) mc.mouse).cpvp$setY(py);
            return;
        }
    }
}
