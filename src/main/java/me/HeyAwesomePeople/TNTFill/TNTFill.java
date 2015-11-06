package me.HeyAwesomePeople.TNTFill;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Dispenser;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.*;

public class TNTFill extends JavaPlugin implements Listener {
    public static TNTFill instance;
    private File fileconfig = new File(this.getDataFolder() + File.separator + "config.yml");

    public List<UUID> autoFill = new ArrayList<UUID>();

    public MessagesConfig messagesConfig;

    @Override
    public void onEnable() {
        instance = this;
        getServer().getPluginManager().registerEvents(new TNTListener(this), this);

        messagesConfig = new MessagesConfig();

        if (!fileconfig.exists()) {
            this.saveDefaultConfig();
        }
    }

    @Override
    public void onDisable() {
        reloadConfig();
    }

    public String getMessage(String s) {
        return ChatColor.translateAlternateColorCodes('&', messagesConfig.getCustomConfig().getString(s));
    }

    public String prefix() {
        return ChatColor.translateAlternateColorCodes('&', getConfig().getString("prefix")) + " ";
    }

    public boolean onCommand(final CommandSender sender, Command cmd,
                             String commandLabel, final String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Only players can run this command!");
            return false;
        }
        Player p = (Player) sender;
        if (args.length == 0) {
            p.sendMessage(ChatColor.BLUE + "=--= TNTFill =--=");
            p.sendMessage(ChatColor.GOLD + "/tntfill (amount) - Fill a dispenser with TNT");
            p.sendMessage(ChatColor.GOLD + "/tntfull auto - Toggle auto tnt fill");
            p.sendMessage(ChatColor.GOLD + "/tntclear (radius) - Clear all tnt within a radius");
        }
        if (commandLabel.equalsIgnoreCase("tntfill")) {
            if (args[0].equalsIgnoreCase("auto")) {
                if (!p.hasPermission("tntfill.auto")) {
                    p.sendMessage(prefix() + getMessage("noPermissions"));
                    return false;
                }
                if (autoFill.contains(p.getUniqueId())) {
                    autoFill.remove(p.getUniqueId());
                    p.sendMessage(prefix() + getMessage("tntFill.autoFillDisabled"));
                } else {
                    autoFill.add(p.getUniqueId());
                    p.sendMessage(prefix() + getMessage("tntFill.autoFillEnabled"));
                }
                return false;
            } else if (isInteger(args[0])) {
                if (!p.hasPermission("tntfill.use")) {
                    p.sendMessage(prefix() + getMessage("noPermissions"));
                    return false;
                }
                if (p.getTargetBlock((Set<Material>) null, 7) == null) {
                    p.sendMessage(prefix() + getMessage("tntFill.failedToGetTargetBlock"));
                    return false;
                }
                Block b = p.getTargetBlock((Set<Material>) null, 7);
                if (!b.getType().equals(Material.DISPENSER)) {
                    p.sendMessage(prefix() + getMessage("tntFill.blockIsNotDispenser"));
                    return false;
                }
                Dispenser dis = (Dispenser) b.getState();
                Integer am = Integer.parseInt(args[0]);
                Integer cap = getFillingCap(p);
                if (am > cap) {
                    for (int i = 0; i <= getFillingCap(p); i++) {
                        dis.getInventory().addItem(new ItemStack(Material.TNT));
                    }
                    p.sendMessage(prefix() + getMessage("tntFill.success").replace("%amount%", cap + ""));
                } else {
                    for (int i = 0; i <= am; i++) {
                        dis.getInventory().addItem(new ItemStack(Material.TNT));
                    }
                    p.sendMessage(prefix() + getMessage("tntFill.success").replace("%amount%", am + ""));
                }
                return false;
            } else {
                p.sendMessage(prefix() + getMessage("noSubcommand"));
            }
        }
        if (commandLabel.equalsIgnoreCase("tntclear")) {
            if (!p.hasPermission("tntfill.tntclear")) {
                p.sendMessage(prefix() + getMessage("noPermissions"));
                return false;
            }
            if (args.length == 1) {
                if (isInteger(args[0])) {
                    if (Integer.parseInt(args[0]) > getConfig().getInt("maxRadius")) {
                        p.sendMessage(prefix() + getMessage("tntClear.overMaxRadius").replace("%radius%", args[0]));
                        return false;
                    }
                    clearAllTNTWithinRadius(Integer.parseInt(args[0]), p);
                    p.sendMessage(prefix() + getMessage("tntClear.success").replace("%radius%", args[0]));
                } else {
                    p.sendMessage(prefix() + getMessage("tntClear.numberNotInteger"));
                }
            } else {
                p.sendMessage(prefix() + getMessage("invalidArguments"));
            }
        }
        return false;
    }

    public Integer getFillingCap(Player p) {
        int cap = 1;
        for (String s : getConfig().getConfigurationSection("fillingCap").getKeys(false)) {
            if (p.hasPermission("tntfill.fillcap." + s)) {
                cap = getConfig().getInt("fillingCap." + s);
            }
        }
        return cap;
    }

    public Integer clearAllTNTWithinRadius(int radius, Player p) {
        Location center = p.getLocation();
        int nR = -(Math.abs(radius));
        int r = Math.abs(radius);
        int totalRemoved = 0;
        for (int x = nR; x < r; x++) {
            for (int y = nR; y < r; y++) {
                for (int z = nR; z < r; z++) {
                    Block block = center.getWorld().getBlockAt(x, y, z);
                    if (block.getType().equals(Material.DISPENSER)) {
                        if (block.getState() == null) continue;
                        Dispenser dis = (Dispenser) block.getState();
                        for (ItemStack i : dis.getInventory().getContents()) {
                            if (i.getType().equals(Material.TNT)) {
                                totalRemoved++;
                                dis.getInventory().remove(i);
                            }
                        }
                        dis.update();
                    }
                }
            }
        }
        return totalRemoved;
    }

    public boolean isInteger(String s) {
        try {
            //noinspection ResultOfMethodCallIgnored
            Integer.parseInt(s);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

}

