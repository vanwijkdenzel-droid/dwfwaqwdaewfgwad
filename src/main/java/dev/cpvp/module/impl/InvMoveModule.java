package dev.cpvp.module.impl;

import dev.cpvp.gui.ClickGuiScreen;
import dev.cpvp.module.Category;
import dev.cpvp.module.Module;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;

/** Keep moving while an inventory / container / the ClickGUI is open. */
public final class InvMoveModule extends Module {
    public InvMoveModule() { super("InvMove", "Move with menus open", Category.INVENTORY); }

    @Override public void onTick() {
        if (!(mc.currentScreen instanceof HandledScreen) && !(mc.currentScreen instanceof ClickGuiScreen)) return;
        long win = mc.getWindow().getHandle();
        for (KeyBinding kb : new KeyBinding[] { mc.options.forwardKey, mc.options.backKey, mc.options.leftKey,
                mc.options.rightKey, mc.options.jumpKey, mc.options.sprintKey }) {
            InputUtil.Key k = InputUtil.fromTranslationKey(kb.getBoundKeyTranslationKey());
            kb.setPressed(k.getCategory() == InputUtil.Type.KEYSYM && InputUtil.isKeyPressed(win, k.getCode()));
        }
    }
}
