package com.rocketdev.oggiveaway.animation.impl;

import com.rocketdev.oggiveaway.OGGiveaway;
import com.rocketdev.oggiveaway.utils.ColorUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.entity.Display;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.Player;
import org.bukkit.entity.TextDisplay;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Transformation;
import org.bukkit.util.Vector;
import org.joml.AxisAngle4f;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicReference;

public class ModernSlotTask extends BukkitRunnable {

    private final OGGiveaway plugin;
    private final Player player;
    private final boolean isWin;


    private final AtomicReference<AnimationState> currentMath = new AtomicReference<>();
    private final Location tempPlayerLoc; // Snapshot for the async thread
    private BukkitTask asyncTask; // Reference to the Brain thread

    private static final String TEXT_ROLLING = ColorUtil.colorize("&b&l🎲 ROLLING...");
    private static final String TEXT_ROLLING_ALT = ColorUtil.colorize("&3&l🎲 ROLLING...");
    private static final String TEXT_WIN = ColorUtil.colorize("&b&l💎 WINNER 💎");
    private static final String TEXT_LOSE = ColorUtil.colorize("&cBetter luck next time!");

    private final List<BlockDisplay> blocks = new ArrayList<>(3);
    private TextDisplay statusText;
    private ItemDisplay shuffleItem;

    private int ticks = 0;
    private int state = 0;

    private static final Material[] BLOCK_TEXTURES = {
            Material.DIAMOND_BLOCK, Material.BLUE_ICE, Material.LAPIS_BLOCK,
            Material.PRISMARINE_BRICKS, Material.LIGHT_BLUE_CONCRETE, Material.PACKED_ICE
    };

    private static final Material[] ICON_TEXTURES = {
            Material.DIAMOND, Material.EMERALD, Material.GOLD_INGOT,
            Material.NETHER_STAR, Material.ENCHANTED_BOOK, Material.AMETHYST_SHARD
    };


    private static class AnimationState {
        final Location left, center, right, text, item;
        final float itemYaw;
        public AnimationState(Location l, Location c, Location r, Location t, Location i, float iy) {
            this.left = l; this.center = c; this.right = r; this.text = t; this.item = i; this.itemYaw = iy;
        }
    }

    public ModernSlotTask(OGGiveaway plugin, Player player, List<ItemStack> prizes, boolean isWin) {
        this.plugin = plugin;
        this.player = player;
        this.isWin = isWin;
        this.tempPlayerLoc = player.getLocation().clone();

        spawnEntities();
        startBrain();
    }

    private void spawnEntities() {
        for (int i = 0; i < 3; i++) {
            BlockDisplay bd = player.getWorld().spawn(player.getLocation(), BlockDisplay.class, d -> {
                d.setGravity(false);
                d.setTeleportDuration(3);
                d.setBlock(Bukkit.createBlockData(Material.BLUE_ICE));
                d.setTransformation(new Transformation(
                        new Vector3f(-0.3f, -0.3f, -0.3f), new AxisAngle4f(),
                        new Vector3f(0.6f), new AxisAngle4f()));
            });
            blocks.add(bd);
        }

        this.statusText = player.getWorld().spawn(player.getLocation(), TextDisplay.class, t -> {
            t.setText(TEXT_ROLLING);
            t.setBillboard(Display.Billboard.CENTER);
            t.setGravity(false);
            t.setTeleportDuration(3);
            t.setBackgroundColor(org.bukkit.Color.fromARGB(0,0,0,0));
            t.setShadowed(true);
        });

        this.shuffleItem = player.getWorld().spawn(player.getLocation(), ItemDisplay.class, i -> {
            i.setItemStack(new ItemStack(Material.DIAMOND));
            i.setBillboard(Display.Billboard.FIXED);
            i.setGravity(false);
            i.setTeleportDuration(3);
            i.setTransformation(new Transformation(
                    new Vector3f(0, 0, 0), new AxisAngle4f(),
                    new Vector3f(0.3f), new AxisAngle4f()));
        });
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
                    calcLoc.setX(tempPlayerLoc.getX());
                    calcLoc.setY(tempPlayerLoc.getY());
                    calcLoc.setZ(tempPlayerLoc.getZ());
                    calcLoc.setYaw(tempPlayerLoc.getYaw());
                    calcLoc.setPitch(tempPlayerLoc.getPitch());
                }

                Location center = calcLoc.clone().add(0, 2.6, 0);


                dirCache.setX(Math.cos(Math.toRadians(calcLoc.getYaw() + 90)));
                dirCache.setZ(Math.sin(Math.toRadians(calcLoc.getYaw() + 90)));
                dirCache.setY(0).normalize();


                leftCache.setX(dirCache.getZ()).setY(0).setZ(-dirCache.getX());

                Location left = center.clone().add(leftCache.getX() * 0.8, leftCache.getY() * 0.8, leftCache.getZ() * 0.8);
                Location right = center.clone().subtract(leftCache.getX() * 0.8, leftCache.getY() * 0.8, leftCache.getZ() * 0.8);


                Location text = center.clone().add(0, 0.7, 0);
                Location item = center.clone().add(0, 1.1, 0);
                float itemYaw = (ticks * 10) % 360;

                currentMath.set(new AnimationState(left, center, right, text, item, itemYaw));
            }
        }.runTaskTimerAsynchronously(plugin, 0L, 1L);
    }

    @Override
    public void run() {
        if (!player.isOnline()) { forceEnd(); return; }

        synchronized (tempPlayerLoc) {
            player.getLocation(tempPlayerLoc);
        }


        AnimationState state = currentMath.get();
        if (state != null) {
            blocks.get(1).teleport(state.center);
            blocks.get(0).teleport(state.left);
            blocks.get(2).teleport(state.right);

            if (statusText != null && statusText.isValid()) statusText.teleport(state.text);
            if (shuffleItem != null && shuffleItem.isValid()) {
                state.item.setYaw(state.itemYaw);
                shuffleItem.teleport(state.item);
            }
        }



        if (this.state < 3 && ticks % 2 == 0) handleReelSpin();
        if (ticks == 40) lockBlock(0);
        if (ticks == 70) lockBlock(1);
        if (ticks == 90) finish();

        if (isWin) {
            if (ticks == 110) triggerReward();
        } else {
            if (ticks == 100) forceEnd();
        }

        ticks++;
    }

    private void handleReelSpin() {
        int frame = ticks;
        for(int i=0; i<3; i++) {
            if (state <= i) {
                blocks.get(i).setBlock(Bukkit.createBlockData(BLOCK_TEXTURES[(frame + i) % BLOCK_TEXTURES.length]));
            }
        }

        if (shuffleItem != null && ticks % 5 == 0) {
            Material randomMat = ICON_TEXTURES[ThreadLocalRandom.current().nextInt(ICON_TEXTURES.length)];
            shuffleItem.setItemStack(new ItemStack(randomMat));
        }

        player.getWorld().playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_HAT, 0.5f, 2f);

        if (ticks % 10 == 0 && statusText != null) {
            statusText.setText((ticks % 20 == 0) ? TEXT_ROLLING : TEXT_ROLLING_ALT);
        }
    }

    private void lockBlock(int index) {
        state = index + 1;
        Material mat = isWin ? Material.DIAMOND_BLOCK : Material.RED_STAINED_GLASS;
        blocks.get(index).setBlock(Bukkit.createBlockData(mat));
        player.getWorld().playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1f, 2f);
    }

    private void finish() {
        if (isWin) {
            state = 4;
            for(BlockDisplay bd : blocks) bd.setBlock(Bukkit.createBlockData(Material.DIAMOND_BLOCK));
            if (shuffleItem != null) shuffleItem.setItemStack(new ItemStack(Material.DIAMOND));

            plugin.getBossBarManager().setWinner(player.getName());
            if (statusText != null) statusText.setText(TEXT_WIN);

            player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1f, 1f);
        } else {
            if (statusText != null) statusText.setText(TEXT_LOSE);
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1f, 1f);
        }
    }

    private void triggerReward() {
        plugin.getGiveawayManager().onAnimationFinish(player);
        forceEnd();
    }

    public void forceEnd() {

        if (asyncTask != null && !asyncTask.isCancelled()) {
            asyncTask.cancel();
        }

        if (statusText != null) statusText.remove();
        if (shuffleItem != null) shuffleItem.remove();
        for (BlockDisplay bd : blocks) bd.remove();
        this.cancel();
    }
}