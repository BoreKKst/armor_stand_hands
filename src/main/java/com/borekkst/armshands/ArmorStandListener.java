package com.borekkst.armshands;

import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.inventory.EquipmentSlot;

public class ArmorStandListener implements Listener {
    private final ArmsHands plugin;

    public ArmorStandListener(ArmsHands plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInteract(PlayerInteractAtEntityEvent event) {
        if (!(event.getRightClicked() instanceof ArmorStand)) return;
        if (event.getHand() != EquipmentSlot.HAND) return;

        Player player = event.getPlayer();
        ArmorStand stand = (ArmorStand) event.getRightClicked();

        if (player.isSneaking()) {
            event.setCancelled(true);
            boolean hasArms = stand.hasArms();
            stand.setArms(!hasArms);

            if (!hasArms) {
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.prefix") + plugin.getConfig().getString("messages.stand-toggled")));
                player.playSound(stand.getLocation(), Sound.ENTITY_ARMOR_STAND_PLACE, 1.0f, 1.2f);
            } else {
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.prefix") + "&7Руки стойки скрыты."));
                player.playSound(stand.getLocation(), Sound.ENTITY_ITEM_FRAME_REMOVE_ITEM, 1.0f, 0.8f);
            }
        }
    }
}
