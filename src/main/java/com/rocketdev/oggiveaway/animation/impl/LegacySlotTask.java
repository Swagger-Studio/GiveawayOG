package com.rocketdev.oggiveaway.animation.impl;

import com.rocketdev.oggiveaway.OGGiveaway;
import com.rocketdev.oggiveaway.utils.ColorUtil;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public class LegacySlotTask extends BukkitRunnable {

    private final OGGiveaway plugin;
    private final Player player;
    private final boolean isWin;


    private final AtomicReference<AnimationState> currentMath = new AtomicReference<>();
    private final Location tempPlayerLoc;
    private BukkitTask asyncTask;


    private static final String TEXT_ROLLING = ColorUtil.colorize("&b&l🎲 ROLLING...");
    private static final String TEXT_WIN = ColorUtil.colorize("&b&l💎 WINNER 💎");
    private static final String TEXT_LOSE = ColorUtil.colorize("&cBetter luck next time!");

    private final List<ArmorStand> stands = new ArrayList<>(3);
    private ArmorStand shuffleItem;
    private ArmorStand textStand;

    private int ticks = 0;
    private int state = 0;

    private static final Material[] BLOCK_TEXTURES = {
            Material.DIAMOND_BLOCK, Material.BLUE_ICE, Material.LAPIS_BLOCK,
            Material.PRISMARINE_BRICKS, Material.LIGHT_BLUE_CONCRETE, Material.PACKED_ICE
    };

    private static class AnimationState {
        final Location left, center, right, text, item;
        final float itemYaw;
        public AnimationState(Location l, Location c, Location r, Location t, Location i, float iy) {
            this.left = l; this.center = c; this.right = r; this.text = t; this.item = i; this.itemYaw = iy;
        }
    }

    public LegacySlotTask(OGGiveaway plugin, Player player, List<ItemStack> prizes, boolean isWin) {
        this.plugin = plugin;
        this.player = player;
        this.isWin = isWin;
        this.tempPlayerLoc = player.getLocation().clone();

        spawnEntities();
        startBrain();
    }

    private void spawnEntities() {
        for (int i = 0; i < 3; i++) { stands.add(spawnStand(player.getLocation(), new ItemStack(Material.BLUE_ICE), false)); }
        textStand = spawnStand(player.getLocation(), null, false);
        textStand.setCustomName(TEXT_ROLLING); textStand.setCustomNameVisible(true); textStand.setInvisible(true);
        shuffleItem = spawnStand(player.getLocation(), new ItemStack(Material.DIAMOND), true);
    }

    private ArmorStand spawnStand(Location loc, ItemStack head, boolean small) {
        ArmorStand as = (ArmorStand) loc.getWorld().spawnEntity(loc, EntityType.ARMOR_STAND);
        as.setGravity(false); as.setVisible(false); as.setMarker(true); as.setSmall(small);
        if (head != null) as.setHelmet(head);
        return as;
    }

    private void startBrain() {
        asyncTask = new BukkitRunnable() {
            private final Vector dirCache = new Vector();
            private final Vector leftCache = new Vector();
            private final Location calcLoc = tempPlayerLoc.clone();

            @Override
            public void run() {
                if (!player.isOnline()) { this.cancel(); return; }

                synchronized (tempPlayerLoc) {
                    calcLoc.setWorld(tempPlayerLoc.getWorld());
                    calcLoc.setX(tempPlayerLoc.getX()); calcLoc.setY(tempPlayerLoc.getY()); calcLoc.setZ(tempPlayerLoc.getZ());
                    calcLoc.setYaw(tempPlayerLoc.getYaw()); calcLoc.setPitch(tempPlayerLoc.getPitch());
                }

                Location center = calcLoc.clone().add(0, 1.5, 0);

                dirCache.setX(Math.cos(Math.toRadians(calcLoc.getYaw() + 90)));
                dirCache.setZ(Math.sin(Math.toRadians(calcLoc.getYaw() + 90)));
                dirCache.setY(0).normalize();

                leftCache.setX(dirCache.getZ()).setY(0).setZ(-dirCache.getX());

                Location left = center.clone().add(leftCache.getX() * 0.8, leftCache.getY() * 0.8, leftCache.getZ() * 0.8);
                Location right = center.clone().subtract(leftCache.getX() * 0.8, leftCache.getY() * 0.8, leftCache.getZ() * 0.8);

                Location text = center.clone().add(0, 0.5, 0);
                Location item = center.clone().add(0, 0.8, 0);
                float itemYaw = (ticks * 10) % 360;

                currentMath.set(new AnimationState(left, center, right, text, item, itemYaw));
            }
        }.runTaskTimerAsynchronously(plugin, 0L, 1L);
    }

    @Override
    public void run() {
        if (!player.isOnline()) { forceEnd(); return; }


        synchronized (tempPlayerLoc) { player.getLocation(tempPlayerLoc); }


        AnimationState mathResult = currentMath.get();
        if (mathResult != null) {
            stands.get(1).teleport(mathResult.center);
            stands.get(0).teleport(mathResult.left);
            stands.get(2).teleport(mathResult.right);
            if (textStand != null) textStand.teleport(mathResult.text);
            if (shuffleItem != null) {
                mathResult.item.setYaw(mathResult.itemYaw);
                shuffleItem.teleport(mathResult.item);
            }
        }


        if (state < 3 && ticks % 2 == 0) handleSpin();
        if (ticks == 40) lock(0);
        if (ticks == 70) lock(1);
        if (ticks == 90) finish();

        if (isWin) { if (ticks == 110) triggerReward(); } else { if (ticks == 100) forceEnd(); }
        ticks++;
    }

    private void handleSpin() {
        for(int i=0; i<3; i++) { if (state <= i) stands.get(i).setHelmet(new ItemStack(BLOCK_TEXTURES[(ticks + i) % BLOCK_TEXTURES.length])); }
        player.getWorld().playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_HAT, 0.5f, 2f);
    }

    private void lock(int index) {
        state = index + 1;
        Material mat = isWin ? Material.DIAMOND_BLOCK : Material.RED_STAINED_GLASS;
        stands.get(index).setHelmet(new ItemStack(mat));
        player.getWorld().playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1f, 2f);
    }

    private void finish() {
        if (isWin) {
            state = 4;
            if (textStand != null) textStand.setCustomName(TEXT_WIN);
            plugin.getBossBarManager().setWinner(player.getName());
            player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1f, 1f);
        } else {
            if (textStand != null) textStand.setCustomName(TEXT_LOSE);
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1f, 1f);
        }
    }

    private void triggerReward() {
        plugin.getGiveawayManager().onAnimationFinish(player);
        forceEnd();
    }

    public void forceEnd() {
        if (asyncTask != null) asyncTask.cancel();

        for (ArmorStand as : stands) as.remove();
        if (textStand != null) textStand.remove();
        if (shuffleItem != null) shuffleItem.remove();
        this.cancel();
    }
}