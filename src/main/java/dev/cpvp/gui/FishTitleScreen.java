package dev.cpvp.gui;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.gui.screen.option.OptionsScreen;
import net.minecraft.client.gui.screen.world.SelectWorldScreen;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;

import static dev.cpvp.gui.RenderUtil.*;

/** Replaces the vanilla title screen. */
public final class FishTitleScreen extends Screen {
    private static final String[] LABELS = { "Singleplayer", "Multiplayer", "Options", "Quit Game" };
    private final List<int[]> rects = new ArrayList<>();

    public FishTitleScreen() { super(Text.literal("Fish v1")); }

    @Override public boolean shouldCloseOnEsc() { return false; }

    @Override
    public void render(DrawContext c, int mx, int my, float delta) {
        c.fillGradient(0, 0, width, height, 0xFF000000, 0xFF05070C);

        float s = 3.2f;
        int lw = logoWidth(s);
        logo(c, (width - lw) / 2, height / 2 - 120, s);
        centerText(c, "Minecraft 1.21.1", width / 2, height / 2 - 120 + Math.round(16 * s) + 8, DIM, 0.85f);

        rects.clear();
        int bw = 210, bh = 26, x = (width - bw) / 2, y = height / 2 - 30;
        for (int i = 0; i < LABELS.length; i++) {
            int by = y + i * (bh + 8);
            boolean hov = mx >= x && mx < x + bw && my >= by && my < by + bh;
            c.fill(x, by, x + bw, by + bh, hov ? 0xFF101010 : 0xFF000000);
            outline(c, x, by, x + bw, by + bh, BORDER);
            if (hov) c.fill(x, by + bh - 1, x + bw, by + bh, ACCENT);
            centerText(c, LABELS[i], width / 2, by + 8, hov ? 0xFFFFFFFF : TEXT, 1f);
            rects.add(new int[] { x, by, bw, bh });
        }
        text(c, "Fish v1", 8, height - 14, DIM, 0.8f);
    }

    @Override
    public boolean mouseClicked(double mx, double my, int button) {
        for (int i = 0; i < rects.size(); i++) {
            int[] r = rects.get(i);
            if (mx >= r[0] && mx < r[0] + r[2] && my >= r[1] && my < r[1] + r[3]) {
                switch (i) {
                    case 0 -> client.setScreen(new SelectWorldScreen(this));
                    case 1 -> client.setScreen(new MultiplayerScreen(this));
                    case 2 -> client.setScreen(new OptionsScreen(this, client.options));
                    default -> client.scheduleStop();
                }
                return true;
            }
        }
        return super.mouseClicked(mx, my, button);
    }
}
