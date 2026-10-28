package com.borekkst.armshands;

import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.inventory.EquipmentSlot;

public class ArmorStandListener implements Listener {
    private final ArmsHands plugin;
    private final AnimationManager animationManager;

    public ArmorStandListener(ArmsHands plugin) {
        this.plugin = plugin;
        this.animationManager = new AnimationManager(plugin);
    }

    @EventHandler
    public void onEntitySpawn(EntitySpawnEvent event) {
        if (event.getEntity() instanceof ArmorStand) {
            ArmorStand stand = (ArmorStand) event.getEntity();
            stand.setArms(true); // Всегда с руками по умолчанию
        }
    }

    @EventHandler
    public void onInteract(PlayerInteractAtEntityEvent event) {
        if (!(event.getRightClicked() instanceof ArmorStand)) return;
        if (event.getHand() != EquipmentSlot.HAND) return;

        Player player = event.getPlayer();
        ArmorStand stand = (ArmorStand) event.getRightClicked();

        if (player.isSneaking()) {
            event.setCancelled(true);
            
            // Включаем руки и запускаем Bedrock-анимацию движения рук
            stand.setArms(true);
            animationManager.playBedrockArmsAnimation(stand);

            player.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.prefix") + "&aАктивирована анимация рук в стиле Bedrock!"));
            player.playSound(stand.getLocation(), Sound.ENTITY_ARMOR_STAND_PLACE, 1.0f, 1.2f);
        }
    }
}
