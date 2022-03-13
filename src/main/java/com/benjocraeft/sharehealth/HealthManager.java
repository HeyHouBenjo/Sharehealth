package com.benjocraeft.sharehealth;

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
        Sharehealth.GetPlayers().forEach(p -> p.setHealth(health));
    }

    boolean wouldCauseDeath(double damage){
        double newHealth = health - damage;
        return newHealth <= 0;
    }

    void onPlayerGotDamage(Player player, double damage, double absorptionDamage){
        subtractHealth(damage);
        applyHealthToAllExcept(player);
        absorptionManager.onPlayerGotDamage(player, absorptionDamage);
    }

    void onPlayerRegainedHealth(Player player, double regainedHealth){
        addHealth(regainedHealth);
        applyHealthToAllExcept(player);
    }

    void applyHealthToAllExcept(Player player){
        for (Player p : Sharehealth.GetPlayers()){
            if (p.equals(player))
                continue;
            p.setHealth(health);
        }
    }

    //When totem is triggered, set health to 1 and remove absorption
    void onTotemTriggered(){
        setHealth(1);
        applyHealthToAllExcept(null);
        absorptionManager.expire(false);
    }

    final AbsorptionManager absorptionManager = new AbsorptionManager();

    void onAbsorptionConsumed(int duration, int amplifier){
        double amount = (amplifier + 1) * 4;
        absorptionManager.create(duration, amount);
    }



}
