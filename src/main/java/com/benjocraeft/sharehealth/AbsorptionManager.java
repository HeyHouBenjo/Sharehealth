package com.benjocraeft.sharehealth;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class AbsorptionManager {

    double amount;
    int duration;

    int task;

    public AbsorptionManager() {
    }

    void create(int duration, double newAmount){
        if (duration > 0 && amount > newAmount){
            return;
        }

        Bukkit.getScheduler().cancelTask(task);
        task = Bukkit.getScheduler().scheduleSyncRepeatingTask(Sharehealth.Instance, this::onSecond, 0, 20);
        this.duration = duration;
        setAmount(newAmount);
    }

    private void onSecond(){
        if (Bukkit.getOnlinePlayers().size() == 0)
            return;

        duration -= 20;
        if (duration <= 0)
            expire();
    }

    void onPlayerGotDamage(Player player, double absorptionDamage){
        if (!isActive())
            return;

        setAmount(player, amount - absorptionDamage);
    }

    private void expire(){
        Bukkit.getScheduler().cancelTask(task);
        duration = 0;
        amount = 0;
    }

    private void setAmount(Player player, double amount){
        if (amount <= 0){
            expire();
            amount = 0;
        }
        this.amount = amount;
        List<Player> players = new ArrayList<>(Bukkit.getOnlinePlayers());
        players.remove(player);
        players.forEach(this::setAbsorption);
    }

    private void setAmount(double amount){
        setAmount(null, amount);
    }

    void setAbsorption(Player player){
        if (!isActive())
            return;

        player.setAbsorptionAmount(amount);
    }

    private boolean isActive(){
        return Bukkit.getScheduler().isQueued(task);
    }

}
