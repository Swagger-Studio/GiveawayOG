package com.rocketdev.oggiveaway.animation;

import com.rocketdev.oggiveaway.OGGiveaway;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class WinnerRevealTask extends BukkitRunnable {

    private final OGGiveaway plugin;
    private final Player player;
    private double t = 0;
    private final double r = 1.5;

    public WinnerRevealTask(OGGiveaway plugin, Player player) {
        this.plugin = plugin;
        this.player = player;
    }

    @Override
    public void run() {
        if (!player.isOnline()) {
            this.cancel();
            return;
        }

        t += Math.PI / 16;
        Location loc = player.getLocation();

        double x1 = r * Math.cos(t);
        double y1 = 0.5 * t;
        double z1 = r * Math.sin(t);
        loc.add(x1, y1, z1);
        player.spawnParticle(Particle.FIREWORK, loc, 0);
        loc.subtract(x1, y1, z1);


        double x2 = r * Math.cos(t + Math.PI);
        double z2 = r * Math.sin(t + Math.PI);
        loc.add(x2, y1, z2);
        player.spawnParticle(Particle.TOTEM_OF_UNDYING, loc, 2);
        loc.subtract(x2, y1, z2);

        if (t % (Math.PI * 2) < 0.2) {
            player.playSound(player.getLocation(), Sound.ENTITY_CHICKEN_EGG, 0.5f, 1.5f);
        }

        if (t > Math.PI * 6) {
            this.cancel();
            player.spawnParticle(Particle.EXPLOSION, player.getLocation().add(0, 1, 0), 1);
            plugin.getGiveawayManager().endGiveaway();
        }
    }
}