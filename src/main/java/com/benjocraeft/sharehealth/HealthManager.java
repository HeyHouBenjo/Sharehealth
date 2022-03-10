package com.benjocraeft.sharehealth;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;

import java.util.Formatter;
import java.util.Locale;

public class HealthManager {

    private double
            health = 20;

    double getHealth(){
        return health;
    }

    void setHealth(double health){
        if (health > 20)
            health = 20;
        if (health < 0)
            health = 0;
        this.health = health;
    }

    String getHealthString(){
        return new Formatter(Locale.US).format("%.2f", health / 2).toString();
    }


    public HealthManager() {

    }
    public void updatePlayer(Player player){
        if (player.getGameMode().equals(GameMode.SURVIVAL)) {
            player.setHealth(health);
            absorptionManager.setAbsorption(player);
        }
        if (player.getGameMode().equals(GameMode.SPECTATOR))
            player.setHealth(20);
    }

    private void subtractHealth(double sub){
        setHealth(health - sub);
    }

    void addHealth(double add){
        setHealth(health + add);
    }

    void reset(){
        health = 20;
        Bukkit.getOnlinePlayers().forEach(p -> p.setHealth(health));
    }

    boolean onPlayerGotDamage(Player player, double damage, double absorptionDamage){
        subtractHealth(damage);
        setHealthByPlayer(player);
        absorptionManager.onPlayerGotDamage(player, absorptionDamage);

        return health > 0;
    }

    void onPlayerRegainedHealth(Player player, double regainedHealth){
        addHealth(regainedHealth);
        setHealthByPlayer(player);
    }

    void setHealthByPlayer(Player player){
        for (Player p : Sharehealth.GetAlivePlayers()){
            if (p.equals(player))
                continue;
            p.setHealth(health);
        }
    }

    final AbsorptionManager absorptionManager = new AbsorptionManager();

    void onAbsorptionConsumed(int duration, int amplifier){
        double amount = (amplifier + 1) * 4;
        absorptionManager.create(duration, amount);
    }



}
