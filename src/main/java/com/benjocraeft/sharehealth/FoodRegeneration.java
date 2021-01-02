package com.benjocraeft.sharehealth;

import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Objects;

public class FoodRegeneration extends BukkitRunnable {

    FoodRegeneration(){
        runTaskTimer(Sharehealth.Instance, 80, 80);
    }

    @Override
    public void run(){
        int allPlayersCount = Sharehealth.GetAlivePlayers().size();
        if (allPlayersCount == 0)
            return;

        int allFoodPoints = 0;
        for (Player p : Sharehealth.GetAlivePlayers()){
            allFoodPoints += p.getFoodLevel();
        }

        //According to MinecraftWiki, players automatically regen if their food level
        // is greater than or equal to 18 of 20 (90%)
        //Here, we look for the average food level
        double currentHealth = Sharehealth.Instance.getHealthManager().getHealth();
        if (allFoodPoints / allPlayersCount >= 16 && currentHealth > 0 && currentHealth < 20){
            Sharehealth.Instance.onFoodRegeneration();
        }

    }

}
