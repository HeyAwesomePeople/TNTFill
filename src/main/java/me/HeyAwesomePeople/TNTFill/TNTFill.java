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
        return getConfig().getString("prefix");
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
                    p.sendMessage(prefix() + ChatColor.RED + "You do not have permissions to use this command!");
                    return false;
                }
                if (autoFill.contains(p.getUniqueId())) {
                    autoFill.remove(p.getUniqueId());
                    p.sendMessage(ChatColor.translateAlternateColorCodes('&', prefix()));
                } else {
                    autoFill.add(p.getUniqueId());
                    p.sendMessage(ChatColor.translateAlternateColorCodes('&', prefix()));
                }
                return false;
            } else if (isInteger(args[0])) {
                if (!p.hasPermission("tntfill.perform")) {
                    p.sendMessage(prefix() + ChatColor.RED + "You do not have permissions to use this command!");
                    return false;
                }
                if (p.getTargetBlock((Set<Material>) null, 7) == null) {
                    // TODO message
                    return false;
                }
                Block b = p.getTargetBlock((Set<Material>) null, 7);
                if (!b.getType().equals(Material.DISPENSER)) {
                    // TODO message
                    return false;
                }
                Dispenser dis = (Dispenser) b.getState();
                for (int i = 0; i <= 128; i++) { //TODO change amount
                    dis.getInventory().addItem(new ItemStack(Material.TNT));
                }
                p.sendMessage(ChatColor.translateAlternateColorCodes('&', prefix() + "".replace("%amount%", 128 + /* TODO */ "")));
                // TODO fill dispenser
            } else {
                p.sendMessage(prefix() + ChatColor.RED + "Invalid subcommand!");
            }
        }
        if (commandLabel.equalsIgnoreCase("tntclear")) {
            if (args.length == 0) {

            } else {

            }
        }
        return false;
    }

    public void clearAllTNTWithinRadius(int radius, Player p) {
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
                        dis.getInventory().clear();
                    }
                }
            }
        }
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

