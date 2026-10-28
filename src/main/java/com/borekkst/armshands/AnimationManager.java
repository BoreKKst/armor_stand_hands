package com.borekkst.armshands;

import org.bukkit.Particle;
import org.bukkit.entity.ArmorStand;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.EulerAngle;

public class AnimationManager {
    private final ArmsHands plugin;

    public AnimationManager(ArmsHands plugin) {
        this.plugin = plugin;
    }

    // Режим Bedrock-анимации рук (плавное махание / стойка стража)
    public void playBedrockArmsAnimation(ArmorStand stand) {
        new BukkitRunnable() {
            double tick = 0;

            @Override
            public void run() {
                if (!stand.isValid() || stand.isDead()) {
                    this.cancel();
                    return;
                }

                // Синусоидальные углы для создания эффекта анимации рук как в Bedrock
                float rightArmAngle = (float) Math.sin(tick) * 35.0f;
                float leftArmAngle = (float) Math.cos(tick) * 35.0f;

                // Применяем повороты для правой и левой руки (в радианах)
                stand.setRightArmPose(new EulerAngle(Math.toRadians(rightArmAngle), Math.toRadians(10), 0));
                stand.setLeftArmPose(new EulerAngle(Math.toRadians(-leftArmAngle), Math.toRadians(-10), 0));

                // Легкие частицы магии для красоты
                stand.getWorld().spawnParticle(Particle.ENCHANT, stand.getLocation().add(0, 1.2, 0), 1, 0.2, 0.2, 0.2, 0.05);

                tick += 0.15;
            }
        }.runTaskTimer(plugin, 0L, 3L);
    }
}
