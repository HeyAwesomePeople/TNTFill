package me.HeyAwesomePeople.TNTFill;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Dispenser;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.inventory.ItemStack;

public class TNTListener implements Listener {

    private TNTFill plugin = null;

    public TNTListener(TNTFill p) {
        this.plugin = p;
    }


    @EventHandler
    public void onPlayerPlaceDispenser(BlockPlaceEvent e) {
        if (e.getPlayer().hasPermission("tntfill.autofill")) {
            if (e.getBlock().getType().equals(Material.DISPENSER)) {
                if (e.getBlock().getState() == null) return;
                Dispenser dis = (Dispenser) e.getBlock().getState();
                for (int i = 0; i <= dis.getInventory().getSize(); i++) {
                    dis.getInventory().addItem(new ItemStack(Material.TNT, 64));
                }
                e.getPlayer().sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messageOnPlace").replace("%player%", e.getPlayer().getName())));
            }
        }
    }

    @EventHandler
    public void itemSpawn(ItemSpawnEvent e) {
        if (plugin.getConfig().getBoolean("dropItems")) {
            e.setCancelled(true);
        }
    }

}
