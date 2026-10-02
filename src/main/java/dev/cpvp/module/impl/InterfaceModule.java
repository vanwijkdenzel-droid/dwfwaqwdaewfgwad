package dev.cpvp.module.impl;

import dev.cpvp.module.Category;
import dev.cpvp.module.Module;
import dev.cpvp.setting.BooleanSetting;
import dev.cpvp.setting.NumberSetting;

import java.awt.Color;

/** ClickGUI options: theme colour (hue / saturation), scrolling and animation speed. */
public final class InterfaceModule extends Module {
    public final BooleanSetting blur = add(new BooleanSetting("Background Blur", true));
    public final NumberSetting hue         = add(new NumberSetting("Theme Hue", 0, 360, 1, 218));
    public final NumberSetting saturation  = add(new NumberSetting("Theme Saturation", 20, 100, 1, 85));
    public final NumberSetting scrollSpeed = add(new NumberSetting("Scroll Speed", 10, 60, 2, 28));
    public final NumberSetting animSpeed   = add(new NumberSetting("Animation Speed", 6, 30, 1, 18));

    public InterfaceModule() {
        super("Interface", "Theme colour and GUI behaviour", Category.SETTINGS);
        setEnabled(true);
    }

    public int accent() {
        return 0xFF000000 | Color.HSBtoRGB((float) (hue.get() / 360.0), (float) (saturation.get() / 100.0), 1.0f);
    }
}
