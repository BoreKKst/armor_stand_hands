package com.voidpulse.hands;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockRedstoneEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.EulerAngle;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

public final class ArmorStandHands extends JavaPlugin implements Listener {

    private static ArmorStandHands instance;
    private NamespacedKey handTypeKey;
    private final Map<ArmorStand, BukkitTask> activeAnimations = new HashMap<>();
    private Connection connection;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();
        initDatabase();

        handTypeKey = new NamespacedKey(this, "hand_type");
        getServer().getPluginManager().registerEvents(this, this);
        
        getLogger().info("[armor_stand_hands] Плагин запущен с приколюхами (Голограммы + Частицы + SQLite + API)!");
    }

    @Override
    public void onDisable() {
        activeAnimations.values().forEach(BukkitTask::cancel);
        activeAnimations.clear();
        closeDatabase();
        getLogger().info("[armor_stand_hands] Плагин выключен.");
    }

    public static ArmorStandHands getInstance() {
        return instance;
    }

    public void animateStandAPI(ArmorStand stand, String handType, boolean active) {
        handleBedrockAnimation(stand, handType, active);
    }

    private void initDatabase() {
        try {
            File dataFolder = getDataFolder();
            if (!dataFolder.exists()) { dataFolder.mkdirs(); }
            File dbFile = new File(dataFolder, "hands_database.db");
            String url = "jdbc:sqlite:" + dbFile.getAbsolutePath();
            
            connection = DriverManager.getConnection(url);
            try (Statement statement = connection.createStatement()) {
                statement.execute("CREATE TABLE IF NOT EXISTS stand_logs (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "stand_uuid TEXT, " +
                        "action TEXT)");
            }
            getLogger().info("База данных SQLite успешно подключена!");
        } catch (Exception e) {
            getLogger().severe("Ошибка инициализации базы данных: " + e.getMessage());
        }
    }

    private void closeDatabase() {
        try {
            if (connection != null && !connection.isClosed()) { connection.close(); }
        } catch (Exception ignored) {}
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEntityEvent event) {
        if (!(event.getRightClicked() instanceof ArmorStand)) return;
        if (!getConfig().getBoolean("features.enable-hands-customization", true)) return;

        ArmorStand stand = (ArmorStand) event.getRightClicked();
        Player player = event.getPlayer();
        ItemStack itemInHand = player.getInventory().getItemInMainHand();

        // Приколюха: Магические частицы при клике
        Location loc = stand.getLocation().add(0, 1.0, 0);
        if (loc.getWorld() != null) {
            loc.getWorld().spawnParticle(Particle.SPELL_WITCH, loc, 25, 0.3, 0.3, 0.3, 0.05);
            loc.getWorld().playSound(loc, Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1.0f, 1.2f);
        }

        if (!itemInHand.getType().isAir()) {
            event.setCancelled(true);

            String itemName = itemInHand.hasItemMeta() && itemInHand.getItemMeta().hasDisplayName() ? 
                    itemInHand.getItemMeta().getDisplayName() : itemInHand.getType().name();

            if (player.isSneaking()) {
                stand.setItem(EquipmentSlot.OFF_HAND, itemInHand.clone());
                stand.getPersistentDataContainer().set(handTypeKey, PersistentDataType.STRING, "LEFT");
                player.sendMessage("§a[VoidPulse] Предмет закреплен в §eлевой руке§a!");
            } else {
                stand.setItem(EquipmentSlot.HAND, itemInHand.clone());
                stand.getPersistentDataContainer().set(handTypeKey, PersistentDataType.STRING, "RIGHT");
                player.sendMessage("§a[VoidPulse] Предмет закреплен в §eправой руке§a!");
            }

            // Приколюха: Включаем кастомную голограмму с именем предмета над стойкой
            stand.setCustomName("§b✨ Рука: §f" + itemName);
            stand.setCustomNameVisible(true);

            player.playSound(player.getLocation(), Sound.ENTITY_ITEM_FRAME_ADD_ITEM, 1.0f, 1.0f);
        }
    }

    @EventHandler
    public void onRedstone(BlockRedstoneEvent event) {
        if (!getConfig().getBoolean("features.enable-redstone-control", true)) return;

        Block block = event.getBlock();
        int power = event.getNewCurrent();
        int oldPower = event.getOldCurrent();

        if (power > 0 && oldPower == 0) {
            triggerMovement(block.getLocation(), true);
        } else if (power == 0 && oldPower > 0) {
            triggerMovement(block.getLocation(), false);
        }
    }

    private void triggerMovement(Location powerLoc, boolean active) {
        if (powerLoc.getWorld() == null) return;

        powerLoc.getWorld().getEntitiesByClass(ArmorStand.class).forEach(stand -> {
            if (stand.getLocation().distanceSquared(powerLoc) <= 9.0) {
                String handType = stand.getPersistentDataContainer().get(handTypeKey, PersistentDataType.STRING);
                if (handType != null) {
                    handleBedrockAnimation(stand, handType, active);
                }
            }
        });
    }

    private void handleBedrockAnimation(ArmorStand stand, String handType, boolean active) {
        if (!getConfig().getBoolean("features.enable-bedrock-animations", true)) return;

        if (activeAnimations.containsKey(stand)) {
            activeAnimations.get(stand).cancel();
            activeAnimations.remove(stand);
        }

        if (!active) {
            resetPose(stand, handType);
            return;
        }

        // Приколюха: Частицы пламени во время редстоун-анимации
        if (stand.getLocation().getWorld() != null) {
            stand.getLocation().getWorld().spawnParticle(Particle.FLAME, stand.getLocation().add(0, 1, 0), 15, 0.2, 0.5, 0.2, 0.02);
            stand.getLocation().getWorld().playSound(stand.getLocation(), Sound.ENTITY_BLAZE_SHOOT, 0.5f, 1.5f);
        }

        double speedMultiplier = getConfig().getDouble("animation-settings.speed", 0.35);
        double amplitude = getConfig().getDouble("animation-settings.amplitude", 1.25);
        int maxTicks = getConfig().getInt("animation-settings.duration-ticks", 30);

        BukkitTask task = Bukkit.getScheduler().runTaskTimer(this, new Runnable() {
            double step = 0;

            @Override
            public void run() {
                double angle = Math.sin(step * speedMultiplier) * amplitude;
                EulerAngle current = handType.equals("RIGHT") ? stand.getRightArmPose() : stand.getLeftArmPose();
                
                EulerAngle newPose = new EulerAngle(current.getX() + angle * 0.15, current.getY(), current.getZ());

                if (handType.equals("RIGHT")) {
                    stand.setRightArmPose(newPose);
                } else {
                    stand.setLeftArmPose(newPose);
                }

                step++;
                
                if (step > maxTicks) {
                    resetPose(stand, handType);
                    activeAnimations.remove(stand);
                    this.cancel();
                }
            }
        }, 0L, 1L);

        activeAnimations.put(stand, task);
    }

    private void resetPose(ArmorStand stand, String handType) {
        if (handType.equals("RIGHT")) {
            stand.setRightArmPose(new EulerAngle(0, 0, 0));
        } else {
            stand.setLeftArmPose(new EulerAngle(0, 0, 0));
        }
    }
}
