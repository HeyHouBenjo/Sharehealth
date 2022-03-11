package com.benjocraeft.sharehealth;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.List;

public class AbsorptionManager {

    double amount;
    int duration;

    int task;

    public AbsorptionManager() {
    }

    void create(int newDuration, double newAmount){
        if (newDuration > 0 && amount > newAmount){
            return;
        }

        Bukkit.getScheduler().cancelTask(task);
        task = Bukkit.getScheduler().scheduleSyncRepeatingTask(Sharehealth.Instance, this::onSecond, 0, 20);
        duration = newDuration;
        setAmount(newAmount);
    }

    private void onSecond(){
        if (Sharehealth.GetPlayers().size() == 0)
            return;

        duration -= 20;
        if (duration <= 0)
            expire(false);
    }

    void onPlayerGotDamage(Player player, double absorptionDamage){
        if (!isActive())
            return;

        setAmount(player, amount - absorptionDamage);
    }

    private void expire(boolean fromBeingBroken){
        Bukkit.getScheduler().cancelTask(task);
        duration = 0;
        if (!fromBeingBroken){
            setAmount(0);
        }
    }

    private void setAmount(Player triggeringPlayer, double newAmount){
        if (newAmount <= 0){
            expire(true);
            amount = 0;
        } else
            amount = newAmount;
        List<Player> players = Sharehealth.GetPlayers();
        players.remove(triggeringPlayer);
        players.forEach(this::setAbsorption);
    }

    private void setAmount(double amount){
        setAmount(null, amount);
    }

    void setAbsorption(Player player){
        player.setAbsorptionAmount(amount);
    }

    private boolean isActive(){
        return Bukkit.getScheduler().isQueued(task);
    }

}
