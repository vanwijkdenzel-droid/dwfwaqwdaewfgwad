package dev.cpvp.module;

public enum Category {
    COMBAT("Combat"), RENDER("Render"), UTILITY("Utility"), WORLD("World"),
    INVENTORY("Inventory"), NETWORK("Network"),
    SETTINGS("Settings"); // not a category window - opened from the nav
    public final String label;
    Category(String label) { this.label = label; }
}
