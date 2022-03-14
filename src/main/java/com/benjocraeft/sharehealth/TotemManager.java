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
    public Mode getMode(){
        return mode;
    }

    //between 0 and 1
    private double fractionNeeded;
    public void setFractionNeeded(double value){
        if (value < 0)
            value = 0;
        if (value > 1)
            value = 1;
        fractionNeeded = value;
    }
    public double getFractionNeeded() {
        return fractionNeeded;
    }

    //Determine if enough players hold a totem of undying
    public boolean totemCanBeUsed(){
        return getHolderCount() >= getMinimumPlayerCount();
    }

    //Activate Totem Effect
    //Remove used items
    //Finally buy some Falafel for Markus
    public void activate(Player triggeringPlayer){

        List<Player> allPlayers = Sharehealth.GetPlayers();

        //Remove all effects from Player
        for (PotionEffect e : triggeringPlayer.getActivePotionEffects())
            triggeringPlayer.removePotionEffect(e.getType());

        //Destroy needed totem items
        //Try to destroy holders item first, then the remaining
        //Only destroy as many items as were needed for effect to trigger
        int totemsRemoveCount = getMinimumPlayerCount();
        if (destroyItemFrom(triggeringPlayer))
            totemsRemoveCount--;
        for (int pIndex = 0; pIndex < allPlayers.size() && totemsRemoveCount > 0; pIndex++)
            if (destroyItemFrom(allPlayers.get(pIndex)))
                totemsRemoveCount--;

        //Regeneration II 40sec
        PotionEffect regeneration = new PotionEffect(PotionEffectType.REGENERATION, 40 * 20, 1);
        triggeringPlayer.addPotionEffect(regeneration);

        //Fire Resistance I 40sec
        PotionEffect fireRes = new PotionEffect(PotionEffectType.FIRE_RESISTANCE, 40 * 20, 0);
        triggeringPlayer.addPotionEffect(fireRes);

        //Absorption II 5sec
        PotionEffect absorption = new PotionEffect(PotionEffectType.ABSORPTION, 5 * 20, 1);
        triggeringPlayer.addPotionEffect(absorption);

        //Play Totem Effect for every Player
        for (Player p : allPlayers)
            p.playEffect(EntityEffect.TOTEM_RESURRECT);
    }

    private boolean destroyItemFrom(Player holder){
        ItemStack main = holder.getInventory().getItemInMainHand();
        if (main.getType().equals(Material.TOTEM_OF_UNDYING)){
            //destroy item
            main.setAmount(0);
            return true;
        } else {
            ItemStack off = holder.getInventory().getItemInOffHand();
            if (off.getType().equals(Material.TOTEM_OF_UNDYING)){
                //destroy item
                off.setAmount(0);
                return true;
            }
        }
        return false;
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


