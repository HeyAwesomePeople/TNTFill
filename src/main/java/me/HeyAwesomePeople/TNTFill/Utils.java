package me.HeyAwesomePeople.TNTFill;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;

public class Utils {
    private TNTFill plugin = TNTFill.instance;

    public static String convertColor(String s) {
        return ChatColor.translateAlternateColorCodes('&', s);
    }

    public static String locationToString(Location loc) {
        String s = "";
        s += loc.getBlockX();
        s += "_";
        s += loc.getBlockY();
        s += "_";
        s += loc.getBlockZ();
        s += "_";
        s += loc.getWorld().getName();
        return s;
    }

    public static Location stringToLocation(String s) {
        String[] l = s.split("_");
        if (Bukkit.getWorld(l[3]) == null) {
            return null;
        }
        return new Location(Bukkit.getWorld(l[3]), (double) Integer.parseInt(l[0]), (double) Integer.parseInt(l[1]), (double) Integer.parseInt(l[2]));
    }

}
