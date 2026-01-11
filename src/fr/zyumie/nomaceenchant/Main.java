package fr.zyumie.nomaceenchant;

import java.io.File;

import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.event.player.PlayerJoinEvent;

public class Main extends JavaPlugin implements Listener {

	private long lastConfigModified;
	private boolean alreadyCleared = false;
	
    @Override
    public void onEnable() {
        saveDefaultConfig();
        getServer().getPluginManager().registerEvents(this, this);

        File configFile = new File(getDataFolder(), "config.yml");
        lastConfigModified = configFile.lastModified();

        getServer().getScheduler().runTaskTimer(this, () -> {
            long modified = configFile.lastModified();
            if (modified != lastConfigModified) {
                lastConfigModified = modified;
                reloadConfig();

                if (getConfig().getBoolean("clear-existing-mace-enchants") && !alreadyCleared) {
                    clearExistingMaces();
                    alreadyCleared = true;
                }
            }
        }, 20L, 20L * 5); // check toutes les 5 secondes

    }
    
    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        if (!getConfig().getBoolean("clear-existing-mace-enchants")) return;

        for (ItemStack item : event.getPlayer().getInventory().getContents()) {
            if (item != null && item.getType() == Material.MACE) {
                clearEnchants(item);
            }
        }
    }


    // Bloque l'enchantement à la table
    @EventHandler
    public void onEnchant(EnchantItemEvent event) {
        ItemStack item = event.getItem();
        if (item.getType() == Material.MACE) {
            event.setCancelled(true);
            clearEnchants(item);
        }
    }

    // Bloque l'enchantement via enclume (livres)
    @EventHandler
    public void onAnvil(PrepareAnvilEvent event) {
        ItemStack result = event.getResult();
        if (result != null && result.getType() == Material.MACE) {
            event.setResult(null);
        }
    }


    // Supprime les enchants d'une mace
    private void clearEnchants(ItemStack item) {
        item.getEnchantments().keySet()
                .forEach(enchant -> item.removeEnchantment(enchant));
    }

    // Clear les enchants des maces déjà existantes
    private void clearExistingMaces() {
        getServer().getOnlinePlayers().forEach(player -> {
            for (ItemStack item : player.getInventory().getContents()) {
                if (item != null && item.getType() == Material.MACE) {
                    clearEnchants(item);
                }
            }
        });
    }
}
