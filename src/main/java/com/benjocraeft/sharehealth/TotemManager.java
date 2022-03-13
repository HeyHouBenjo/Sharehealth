package com.benjocraeft.sharehealth;

import org.bukkit.Bukkit;
import org.bukkit.EntityEffect;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.List;

public class TotemManager {


    private Mode mode = Mode.All;
    public void setMode(Mode mode){
        this.mode = mode;
    }

    //between 0 and 1
    private double fractionNeeded;
    public void setFractionNeeded(double value){
        fractionNeeded = value;
    }

    //Determine if enough players hold a totem of undying
    public boolean totemCanBeUsed(){
        return getHolderCount() >= getMinimumPlayerCount();
    }

    //Activate Totem Effect
    //TODO remove used Totems
    //Finally buy some Falafel for Markus
    public void activate(Player triggeringPlayer){

        //Remove all effects from Player
        for (PotionEffect e : triggeringPlayer.getActivePotionEffects())
            triggeringPlayer.removePotionEffect(e.getType());

        //Regeneration II 40sec
        PotionEffect regeneration = new PotionEffect(PotionEffectType.REGENERATION, 40 * 20, 1);
        triggeringPlayer.addPotionEffect(regeneration);

        //Fire Resistance I 40sec
        PotionEffect fireRes = new PotionEffect(PotionEffectType.FIRE_RESISTANCE, 40 * 20, 0);
        triggeringPlayer.addPotionEffect(fireRes);

        //Absorption II 5sec
        PotionEffect absorption = new PotionEffect(PotionEffectType.ABSORPTION, 5 * 20, 1);
        triggeringPlayer.addPotionEffect(absorption);

        //Play Totem Effect to every Player
        for (Player p : Sharehealth.GetPlayers())
            p.playEffect(EntityEffect.TOTEM_RESURRECT);
    }

    //Calculates how many players are needed at least to trigger the totem for everyone
    private int getMinimumPlayerCount(){
        int allPlayerCount = Sharehealth.GetPlayers().size();
        return switch (mode) {
            case One -> 1;
            case All -> allPlayerCount;
            case Disabled -> Bukkit.getMaxPlayers() + 1;
            case Fraction -> (int) Math.ceil(fractionNeeded * allPlayerCount);
        };
    }

    //Counts how many players hold a totem
    //Markus still is waiting for his Falafel
    private int getHolderCount(){
        List<Player> players = Sharehealth.GetPlayers();
        return players.stream().mapToInt(p -> {
            ItemStack main = p.getInventory().getItemInMainHand();
            ItemStack off = p.getInventory().getItemInOffHand();
            return (main.getType().equals(Material.TOTEM_OF_UNDYING) ||
                    off.getType().equals(Material.TOTEM_OF_UNDYING)) ? 1 : 0;
        }).sum();
    }

    enum Mode {
        One,
        All,
        Fraction,
        Disabled
    }

}


