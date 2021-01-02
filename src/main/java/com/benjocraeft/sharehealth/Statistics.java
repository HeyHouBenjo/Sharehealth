package com.benjocraeft.sharehealth;

import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Statistics {

    final private Map<UUID, Pair<Double, Double>> statistics = new HashMap<>();
    public Map<UUID, Pair<Double, Double>> getStatistics(){
        return statistics;
    }

    public static Double Rounded(Double value, int afterComma){
        return Math.round(value * Math.pow(10, afterComma)) / Math.pow(10, afterComma);
    }
    public static Double Rounded(Double value){
        return Rounded(value, 2);
    }

    public Statistics(Map<UUID, Pair<Double, Double>> statistics, Map<UUID, Boolean> settings){
        this.statistics.putAll(statistics);
        this.settings.putAll(settings);
    }

    final private Map<UUID, Boolean> settings = new HashMap<>();
    public Map<UUID, Boolean> getSettings(){
        return settings;
    }
    public void setSettings(UUID uuid, boolean hasLogging){
        settings.put(uuid, hasLogging);
    }

    public void onPlayerJoined(Player player){
        UUID uuid = player.getUniqueId();
        Pair<Double, Double> empty = Pair.pair(0., 0.);
        statistics.putIfAbsent(uuid, empty);
        settings.putIfAbsent(uuid, true);

    }

    void onPlayerRegainedHealth(Player player, double amount){
        UUID uuid = player.getUniqueId();
        Pair<Double, Double> oldPair = statistics.get(uuid);
        statistics.put(uuid, Pair.pair(oldPair.first, oldPair.second + amount));
    }

    void onPlayerGotDamage(Player player, double amount){
        UUID uuid = player.getUniqueId();
        Pair<Double, Double> oldPair = statistics.get(uuid);
        statistics.put(uuid, Pair.pair(oldPair.first + amount, oldPair.second));
    }

}
