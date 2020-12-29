package com.benjocraeft.sharehealth;

import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Objects;

public class HealthRegenTask extends BukkitRunnable {

    final private HealthManager healthManager;


    HealthRegenTask(HealthManager healthManager){
        this.healthManager = healthManager;
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
        if (allFoodPoints / allPlayersCount >= 18 && this.healthManager.getHealth() != 0){
            this.healthManager.addHealth(1);
            this.healthManager.setHealthByPlayer(null);
        }

    }

}
