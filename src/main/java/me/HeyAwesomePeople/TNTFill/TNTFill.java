package me.HeyAwesomePeople.TNTFill;

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

public class TNTFill extends JavaPlugin implements Listener {
    private File fileconfig = new File(this.getDataFolder() + File.separator + "config.yml");

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(new TNTListener(this), this);

        if (!fileconfig.exists()) {
            this.saveDefaultConfig();
        }
    }

    @Override
    public void onDisable() {
        reloadConfig();
    }

    public boolean onCommand(final CommandSender sender, Command cmd,
                             String commandLabel, final String[] args) {
        if (commandLabel.equalsIgnoreCase("tntfill")) {

        }
        if (commandLabel.equalsIgnoreCase("tntclear")) {
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
                        for (int i = 0; i <= dis.getInventory().getSize(); i++) {
                            dis.getInventory().addItem(new ItemStack(Material.TNT, 64));
                        }
                    }
                }
            }
        }
    }


}

