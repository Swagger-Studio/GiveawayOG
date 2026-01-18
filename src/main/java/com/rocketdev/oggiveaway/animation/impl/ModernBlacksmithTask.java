package com.rocketdev.oggiveaway.animation.impl;

import com.rocketdev.oggiveaway.OGGiveaway;
import com.rocketdev.oggiveaway.manager.BlacksmithManager;
import com.rocketdev.oggiveaway.utils.ColorUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Transformation;
import org.bukkit.util.Vector;
import org.joml.AxisAngle4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.Collections;
import java.util.List;
import java.util.Random;

public class ModernBlacksmithTask extends BukkitRunnable {

    private final OGGiveaway plugin;
    private final Player player;
    private final List<ItemStack> prizePool;
    private final BlacksmithManager manager;


    private BlockDisplay anvil, lava;
    private ItemDisplay hammer, heatedItem;
    private TextDisplay statusText;


    private Transformation currentTrans;
    private final AxisAngle4f targetAxisRot = new AxisAngle4f();
    private int tick = 0;
    private int strikeCount = 0;
    private ItemStack finalPrize;
    private boolean rewardGiven = false;

    public ModernBlacksmithTask(OGGiveaway plugin, Player player, List<ItemStack> prizePool, BlacksmithManager manager) {
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

        // Anvil
        anvil = center.getWorld().spawn(center, BlockDisplay.class, d -> {
            d.setBlock(Bukkit.createBlockData(Material.ANVIL));
            d.setGravity(false); d.setRotation(yaw, 0);
            d.setTransformation(new Transformation(new Vector3f(-0.6f, 0, -0.6f), new AxisAngle4f(), new Vector3f(1.2f), new AxisAngle4f()));
        });

        // Lava
        Vector rightVec = flatDir.clone().getCrossProduct(new Vector(0, 1, 0)).normalize();
        Location lavaLoc = center.clone().add(rightVec.multiply(-1.2));
        lava = lavaLoc.getWorld().spawn(lavaLoc, BlockDisplay.class, d -> {
            d.setBlock(Bukkit.createBlockData(Material.LAVA_CAULDRON));
            d.setGravity(false); d.setRotation(yaw, 0);
            d.setTransformation(new Transformation(new Vector3f(-0.4f, 0, -0.4f), new AxisAngle4f(), new Vector3f(0.8f), new AxisAngle4f()));
        });

        // Item
        Location itemLoc = center.clone().add(0, 1.05, 0);
        heatedItem = itemLoc.getWorld().spawn(itemLoc, ItemDisplay.class, d -> {
            d.setItemStack(new ItemStack(Material.RAW_GOLD));
            d.setGravity(false); d.setTeleportDuration(2);
            d.setTransformation(new Transformation(new Vector3f(), new AxisAngle4f((float)(Math.PI/2), 1, 0, 0), new Vector3f(0.5f), new AxisAngle4f()));
            d.setGlowing(true); d.setGlowColorOverride(org.bukkit.Color.ORANGE);
        });

        // Hammer
        Location hammerLoc = center.clone().add(0, 1.8, 0);
        hammer = hammerLoc.getWorld().spawn(hammerLoc, ItemDisplay.class, d -> {
            d.setItemStack(new ItemStack(Material.IRON_PICKAXE));
            d.setGravity(false); d.setTeleportDuration(5); d.setRotation(yaw + 90, 0);
            d.setTransformation(new Transformation(new Vector3f(), new AxisAngle4f((float)Math.toRadians(135), 0, 0, 1), new Vector3f(1.5f), new AxisAngle4f()));
        });
        currentTrans = hammer.getTransformation();

        // Text
        statusText = center.getWorld().spawn(center.clone().add(0, 2.2, 0), TextDisplay.class, t -> {
            t.setText(ColorUtil.colorize("&6&lFORGING..."));
            t.setBillboard(Display.Billboard.CENTER);
            t.setBackgroundColor(org.bukkit.Color.fromARGB(0,0,0,0));
        });


        for(Player p : Bukkit.getOnlinePlayers()) if(!p.equals(player)) {
            p.hideEntity(plugin, anvil); p.hideEntity(plugin, lava);
            p.hideEntity(plugin, hammer); p.hideEntity(plugin, heatedItem); p.hideEntity(plugin, statusText);
        }
    }

    @Override
    public void run() {
        if (!player.isOnline()) {

            manager.cleanup(player.getUniqueId());
            return;
        }


        if (tick % 40 == 0 && strikeCount < 3) {
            targetAxisRot.set((float)Math.toRadians(45), 0, 0, 1);
            updateHammer(20);
        }


        if (tick % 40 == 20 && strikeCount < 3) {
            targetAxisRot.set((float)Math.toRadians(135), 0, 0, 1);
            updateHammer(3);

            Location impact = heatedItem.getLocation();
            player.playSound(impact, Sound.BLOCK_ANVIL_LAND, 1f, (0.5f + (strikeCount * 0.2f)));
            player.spawnParticle(Particle.LAVA, impact, 10);
            player.spawnParticle(Particle.CRIT, impact, 15);

            if (strikeCount == 0) heatedItem.setItemStack(new ItemStack(Material.GOLD_INGOT));
            if (strikeCount == 1) heatedItem.setItemStack(new ItemStack(Material.GOLDEN_SWORD));
            if (strikeCount == 2) {
                heatedItem.setItemStack(finalPrize);
                heatedItem.setGlowColorOverride(org.bukkit.Color.GREEN);
                statusText.setText(ColorUtil.colorize("&a&lCOMPLETE!"));
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

    private void updateHammer(int duration) {
        Transformation newTrans = new Transformation(currentTrans.getTranslation(), targetAxisRot.get(new Quaternionf()), currentTrans.getScale(), currentTrans.getRightRotation());
        hammer.setTransformation(newTrans);
        hammer.setInterpolationDuration(duration);
        currentTrans = newTrans;
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

        if (anvil != null) anvil.remove(); if (lava != null) lava.remove();
        if (hammer != null) hammer.remove(); if (heatedItem != null) heatedItem.remove();
        if (statusText != null) statusText.remove();



        this.cancel();
    }
}