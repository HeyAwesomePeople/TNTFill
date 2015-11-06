package me.HeyAwesomePeople.TNTFill;

import com.intellectualcrafters.plot.object.Plot;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;

public class DispenserLogger implements Listener {
    private TNTFill plugin = null;

    private BukkitTask task = null;

    public DispenserLogger(TNTFill p) {
        this.plugin = p;
        startRepeater();
    }

    public HashMap<Plot, List<Location>> dispensers = new HashMap<Plot, List<Location>>();

    @EventHandler
    public void placeDispenser(BlockPlaceEvent e) {
        if (e.getBlock().getType() == null) return;
        if (!e.getBlock().getType().equals(Material.DISPENSER)) return;
        if (e.getBlock().getState() == null) return;
        if (plugin.api.getPlot(e.getBlock().getLocation()) != null) {
            Plot p = plugin.api.getPlot(e.getBlock().getLocation());
            if (dispensers.containsKey(p)) {
                dispensers.get(p).add(e.getBlock().getLocation());
            } else {
                List<Location> list = new ArrayList<Location>();
                list.add(e.getBlock().getLocation());
                dispensers.put(p, list);
            }
        }
    }

    @EventHandler
    public void breakDispenser(BlockBreakEvent e) {
        if (e.getBlock().getType() == null) return;
        if (!e.getBlock().getType().equals(Material.DISPENSER)) return;
        if (e.getBlock().getState() == null) return;
        if (plugin.api.getPlot(e.getBlock().getLocation()) != null) {
            Plot p = plugin.api.getPlot(e.getBlock().getLocation());
            if (dispensers.containsKey(p)) {
                dispensers.get(p).remove(e.getBlock().getLocation());
            }
        }
    }

    public void startRepeater() {
        task = Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, new Runnable() {
            public void run() {
                saveData();
            }
        }, 20L, plugin.getConfig().getInt("saveDataTickRate"));
    }

    public void stopRepeater() {
        this.task.cancel();
    }

    public void saveData() {
        for (Plot plot : dispensers.keySet()) {
            plugin.blocksconfig.getCustomConfig().set("plots."
                    + Utils.locationToString((Location) plot.getHome().toBukkitLocation()), getLocStrings(dispensers.get(plot)));
        }
        plugin.blocksconfig.saveCustomConfig();
    }

    public List<String> getLocStrings(List<Location> l) {
        List<String> list = new ArrayList<String>();
        for (Location loc : l) {
            list.add(Utils.locationToString(loc));
        }
        return list;
    }

    public void loadData() {
        Bukkit.getConsoleSender().sendMessage(ChatColor.GREEN + "[TNTFill] Loading data from config...");
        if (!plugin.blocksconfig.getCustomConfig().contains("plots")) {
            Bukkit.getConsoleSender().sendMessage(ChatColor.DARK_GREEN + "[TNTFill] No data to load. :)");
            return;
        }
        for (String s : plugin.blocksconfig.getCustomConfig().getConfigurationSection("plots").getKeys(false)) {
            List<Location> locations = removeDuplicates(plugin.blocksconfig.getCustomConfig().getStringList("plots." + s));
            Location plotLocation = Utils.stringToLocation(s);
            //noinspection ConstantConditions
            if (plugin.api.getPlot(plotLocation) == null) {
                continue;
            }
            dispensers.put(plugin.api.getPlot(plotLocation), locations);
        }
        Bukkit.getConsoleSender().sendMessage(ChatColor.DARK_GREEN + "[TNTFill] Loaded all data.");
    }

    public List<Location> removeDuplicates(List<String> l) {
        Set<String> s = new LinkedHashSet<String>(l);
        List<Location> locs = new ArrayList<Location>();
        for (String stringLoc : s) {
            if (Utils.stringToLocation(stringLoc) == null) continue;
            locs.add(Utils.stringToLocation(stringLoc));
        }
        return locs;
    }

}
