package com.borekkst.armshands;
import org.bukkit.Particle;
import org.bukkit.entity.ArmorStand;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.EulerAngle;

public class AnimationManager {
    private final ArmsHands plugin;
    public AnimationManager(ArmsHands plugin) { this.plugin = plugin; }
    public void playEpicAnimation(ArmorStand stand) {
        if (!plugin.getConfig().getBoolean("animations.particle-effects-enabled", true)) return;
        new BukkitRunnable() {
            double angle = 0;
            @Override
            public void run() {
                if (!stand.isValid() || stand.isDead()) { this.cancel(); return; }
                float rightArmRotX = (float) Math.sin(angle) * 45;
                stand.setRightArmPose(new EulerAngle(Math.toRadians(rightArmRotX), 0, 0));
                stand.getWorld().spawnParticle(Particle.FIREWORK, stand.getLocation().add(0, 1, 0), 2, 0.2, 0.2, 0.2, 0.01);
                angle += 0.2;
                if (angle > Math.PI * 2) angle = 0;
            }
        }.runTaskTimer(plugin, 0L, 5L);
    }
}
