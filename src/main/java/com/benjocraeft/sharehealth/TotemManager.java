package com.benjocraeft.sharehealth;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.List;

public class TotemManager {


    private Mode mode;

    //between 0 and 1
    private double fractionNeeded;

    //Tries to save the game, return true on success, false otherwise
    public boolean tryToSave(){

        return false;
    }

    //Calculates how many players are needed at least to trigger the totem for everyone
    private int getMinimumPlayerCount(){
        int allPlayerCount = Sharehealth.GetPlayers().size();
        return switch (mode) {
            case One -> 1;
            case All -> allPlayerCount;
            case Disabled -> Bukkit.getMaxPlayers() + 1;
            case Percentage -> (int) Math.ceil(fractionNeeded * allPlayerCount);
        };
    }

    //Counts how many players hold a totem
    private int getHolderCount(){
        List<Player> players = Sharehealth.GetPlayers();
        return 0;
    }

    enum Mode {
        One,
        All,
        Percentage,
        Disabled
    }

}


