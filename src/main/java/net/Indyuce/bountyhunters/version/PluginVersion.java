package net.Indyuce.bountyhunters.version;

import org.bukkit.Bukkit;

public class PluginVersion {
    public final String version;
    public final int[] integers;

    public PluginVersion() {
        this.version = Bukkit.getServer().getClass().getPackage().getName().replace(".", ",").split(",")[3];
        String[] split = version.substring(1).split("_");
        this.integers = new int[]{Integer.parseInt(split[0]), Integer.parseInt(split[1])};
    }

    public boolean isBelowOrEqual(int first, int second) {
        return first > integers[0] || second >= integers[1];
    }

    public boolean isStrictlyHigher(int first, int second) {
        return first < integers[0] || second < integers[1];
    }

    public int getRevisionNumber() {
        return Integer.parseInt(version.split("_")[2].replaceAll("[^0-9]", ""));
    }

    public int[] toNumbers() {
        return integers;
    }

    @Override
    public String toString() {
        return version;
    }
}
