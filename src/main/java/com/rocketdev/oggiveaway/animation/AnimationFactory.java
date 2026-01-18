package com.rocketdev.oggiveaway.animation;

import com.rocketdev.oggiveaway.OGGiveaway;
import com.rocketdev.oggiveaway.animation.impl.LegacySlotTask;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.lang.reflect.Constructor;
import java.util.List;

public class AnimationFactory {

    private static boolean useModern;

    static {
        try {

            Class.forName("org.bukkit.entity.BlockDisplay");
            useModern = true;
        } catch (ClassNotFoundException e) {
            useModern = false;
        }
    }

    public static BukkitRunnable getSlotTask(OGGiveaway plugin, Player player, List<ItemStack> prizes, boolean isWin) {
        if (useModern) {
            try {

                Class<?> modernClass = Class.forName("com.rocketdev.oggiveaway.animation.impl.ModernSlotTask");
                Constructor<?> constructor = modernClass.getConstructor(OGGiveaway.class, Player.class, List.class, boolean.class);
                return (BukkitRunnable) constructor.newInstance(plugin, player, prizes, isWin);
            } catch (Exception e) {
                e.printStackTrace();

                return new LegacySlotTask(plugin, player, prizes, isWin);
            }
        } else {

            return new LegacySlotTask(plugin, player, prizes, isWin);
        }
    }


    public static boolean isModern() {
        return useModern;
    }
}