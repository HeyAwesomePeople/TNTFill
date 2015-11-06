package me.HeyAwesomePeople.TNTFill;

import com.intellectualcrafters.plot.PS;
import com.intellectualcrafters.plot.api.PlotAPI;
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
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class TNTFill extends JavaPlugin implements Listener {
    public static TNTFill instance;
    private File fileconfig = new File(this.getDataFolder() + File.separator + "config.yml");
    private File blocksConfig = new File(this.getDataFolder() + File.separator + "blocks.yml");
    private File msgconfig = new File(this.getDataFolder() + File.separator + "messages.yml");

    public List<UUID> autoFill = new ArrayList<UUID>();

    public MessagesConfig messagesConfig;
    public BlocksConfig blocksconfig;
    public DispenserLogger dispenserLogger;

    public PlotAPI api;

    @Override
    public void onEnable() {
        instance = this;

        messagesConfig = new MessagesConfig();
        blocksconfig = new BlocksConfig();
        dispenserLogger = new DispenserLogger(this);

        getServer().getPluginManager().registerEvents(new TNTListener(this), this);
        getServer().getPluginManager().registerEvents(dispenserLogger, this);

        if (!fileconfig.exists())
            this.saveDefaultConfig();
        if (!msgconfig.exists())
            this.messagesConfig.saveDefaultConfig();
        if (!blocksConfig.exists())
            this.blocksconfig.saveDefaultConfig();

        final Plugin plotSquared = getServer().getPluginManager().getPlugin("PlotSquared");
        if (plotSquared != null && !plotSquared.isEnabled()) {
            PS.log("&c[PlotStop] Could not find PlotSquared!");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        //noinspection deprecation
        api = new PlotAPI(null);
        dispenserLogger.loadData();
    }

    @Override
    public void onDisable() {
        reloadConfig();
        dispenserLogger.stopRepeater();
        dispenserLogger.saveData();
    }

    public String getMessage(String s) {
        return ChatColor.translateAlternateColorCodes('&', messagesConfig.getCustomConfig().getString(s));
    }

    public String prefix() {
        return ChatColor.translateAlternateColorCodes('&', messagesConfig.getCustomConfig().getString("prefix")) + " ";
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
            p.sendMessage(ChatColor.GOLD + "/tntfill (amount) - Fill nearby dispensers with TNT");
            p.sendMessage(ChatColor.GOLD + "/tntfill auto - Toggle auto tnt fill");
            p.sendMessage(ChatColor.GOLD + "/tntclear (radius) - Clear all tnt within a radius");
            return false;
        }
        if (commandLabel.equalsIgnoreCase("tntfill")) {
            if (args.length != 1) {
                p.sendMessage(prefix() + getMessage("invalidArguments"));
                return false;
            }
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
                Integer am = Integer.parseInt(args[0]);
                if (p.hasPermission("tntfill.use.bypasslimit")) {
                    p.sendMessage(prefix() + getMessage("tntFill.success").replace("%amount%", am + "").replace("%filled%", tntFillRadius(-1, am, p) + ""));
                    return false;
                }
                Integer cap = getFillingCap(p);
                if (am > cap) {
                    p.sendMessage(prefix() + getMessage("tntFill.success").replace("%amount%", cap + "").replace("%filled%", tntFillRadius(-1, cap, p) + ""));
                } else {
                    p.sendMessage(prefix() + getMessage("tntFill.success").replace("%amount%", am + "").replace("%filled%", tntFillRadius(-1, am, p) + ""));
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
                    p.sendMessage(prefix() + getMessage("tntClear.success").replace("%radius%", args[0]).replace("%cleared%", clearAllTNTWithinRadius(Integer.parseInt(args[0]), p) + ""));
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

    public Integer tntFillRadius(int radius, int amount, Player p) {
        if (radius == -1) {
            radius = getConfig().getInt("tntFillRadius");
        }
        int tntsFilled = 0;
        if (api.getPlot(p.getLocation()) == null) return 0;
        if (!dispenserLogger.dispensers.containsKey(api.getPlot(p.getLocation()))) return 0;
        for (Location l : dispenserLogger.dispensers.get(api.getPlot(p.getLocation()))) {
            Block block = l.getBlock();
            if (l.distance(p.getLocation()) > radius) continue;
            if (block.getType().equals(Material.DISPENSER)) {
                if (block.getState() == null) continue;
                Dispenser dis = (Dispenser) block.getState();
                for (int i = 0; i < amount; i++) {
                    dis.getInventory().addItem(new ItemStack(Material.TNT));
                }
                tntsFilled++;
                dis.update();
            }
        }
        return tntsFilled;
    }

    public Integer clearAllTNTWithinRadius(int radius, Player p) {
        int tntsCleared = 0;
        if (api.getPlot(p.getLocation()) == null) return 0;
        if (!dispenserLogger.dispensers.containsKey(api.getPlot(p.getLocation()))) return 0;
        for (Location l : dispenserLogger.dispensers.get(api.getPlot(p.getLocation()))) {
            Block block = l.getBlock();
            if (l.distance(p.getLocation()) > radius) continue;
            if (block.getType().equals(Material.DISPENSER)) {
                if (block.getState() == null) continue;
                Dispenser dis = (Dispenser) block.getState();
                for (int i = 0; i < dis.getInventory().getSize(); i++) {
                    dis.getInventory().clear(i);
                }
                tntsCleared++;
                dis.update();
            }
        }
        return tntsCleared;
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

