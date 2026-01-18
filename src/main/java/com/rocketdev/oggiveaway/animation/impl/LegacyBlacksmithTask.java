package com.rocketdev.oggiveaway.animation.impl;

import com.rocketdev.oggiveaway.OGGiveaway;
import com.rocketdev.oggiveaway.manager.BlacksmithManager;
import com.rocketdev.oggiveaway.utils.ColorUtil;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.EulerAngle;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class LegacyBlacksmithTask extends BukkitRunnable {

    private final OGGiveaway plugin;
    private final Player player;
    private final List<ItemStack> prizePool;
    private final BlacksmithManager manager;
    private final List<ArmorStand> entities = new ArrayList<>();

    private ArmorStand hammer;
    private ArmorStand heatedItem;

    private int tick = 0;
    private int strikeCount = 0;
    private ItemStack finalPrize;
    private boolean rewardGiven = false;

    public LegacyBlacksmithTask(OGGiveaway plugin, Player player, List<ItemStack> prizePool, BlacksmithManager manager) {
        this.plugin = plugin;
        this.player = player;
        this.prizePool = (prizePool == null || prizePool.isEmpty()) ?
                Collections.singletonList(new ItemStack(Material.DIAMOND)) : prizePool;
        this.manager = manager;
        setup();
    }

    private void setup() {
        this.finalPrize = prizePool.get(new Random().nextInt(prizePool.size()));

        Vector flatDir = player.getLocation().getDirection().setY(0).normalize();
        Location center = player.getLocation().add(flatDir.clone().multiply(3));
        center.setY(player.getLocation().getY());
        float yaw = player.getLocation().getYaw() + 180;

        spawnStand(center.clone().add(0, -0.6, 0), new ItemStack(Material.ANVIL), false);

        Vector rightVec = flatDir.clone().getCrossProduct(new Vector(0, 1, 0)).normalize();
        Location lavaLoc = center.clone().add(rightVec.multiply(-1.2));
        spawnStand(lavaLoc.add(0, -0.6, 0), new ItemStack(Material.LAVA_BUCKET), false);

        Location itemLoc = center.clone().add(0, 0.8, 0);
        heatedItem = spawnStand(itemLoc, new ItemStack(Material.RAW_GOLD), true);

        Location hammerLoc = center.clone().add(rightVec.multiply(0.8));
        hammerLoc.setYaw(yaw - 90);
        hammer = spawnStand(hammerLoc, null, false);
        hammer.setItemInHand(new ItemStack(Material.IRON_AXE));
        hammer.setRightArmPose(new EulerAngle(Math.toRadians(-45), 0, 0));

        player.sendMessage(ColorUtil.colorize("&6&l⚒ FORGING..."));
    }

    private ArmorStand spawnStand(Location loc, ItemStack head, boolean small) {
        ArmorStand as = (ArmorStand) loc.getWorld().spawnEntity(loc, EntityType.ARMOR_STAND);
        as.setGravity(false);
        as.setVisible(false);
        as.setMarker(true);
        as.setSmall(small);
        if (head != null) as.setHelmet(head);
        entities.add(as);
        return as;
    }

    @Override
    public void run() {
        if (!player.isOnline()) {

            manager.cleanup(player.getUniqueId());
            return;
        }

        if (tick % 40 == 0 && strikeCount < 3) {
            hammer.setRightArmPose(new EulerAngle(Math.toRadians(-45), 0, 0));
        }

        if (tick % 40 == 20 && strikeCount < 3) {
            hammer.setRightArmPose(new EulerAngle(Math.toRadians(45), 0, 0));

            Location impact = heatedItem.getLocation();
            player.playSound(impact, Sound.BLOCK_ANVIL_LAND, 1f, (0.5f + (strikeCount * 0.2f)));
            player.spawnParticle(Particle.LAVA, impact, 10);
            player.spawnParticle(Particle.CRIT, impact, 15);

            if (strikeCount == 0) heatedItem.setHelmet(new ItemStack(Material.GOLD_INGOT));
            if (strikeCount == 1) heatedItem.setHelmet(new ItemStack(Material.GOLDEN_SWORD));
            if (strikeCount == 2) {
                heatedItem.setHelmet(finalPrize);
                player.sendMessage(ColorUtil.colorize("&a&lCOMPLETE!"));
                player.playSound(impact, Sound.BLOCK_FIRE_EXTINGUISH, 1f, 1f);
                player.spawnParticle(Particle.CAMPFIRE_COSY_SMOKE, impact, 20, 0.2, 0.2, 0.2, 0.05);
            }
            strikeCount++;
        }

        if (tick == 140) {
            finish();
        }
        tick++;
    }

    private void finish() {
        if (!rewardGiven) {
            player.getInventory().addItem(finalPrize);
            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 1f);
            player.sendTitle(ColorUtil.colorize("&6&lFORGED!"), ColorUtil.colorize("&fReceived: &e" + finalPrize.getType()), 5, 40, 10);

            rewardGiven = true;
            plugin.getGiveawayManager().endGiveaway();


            manager.cleanup(player.getUniqueId());
        }
    }

    public void forceEnd() {
        for(ArmorStand as : entities) as.remove();

        this.cancel();
    }
}