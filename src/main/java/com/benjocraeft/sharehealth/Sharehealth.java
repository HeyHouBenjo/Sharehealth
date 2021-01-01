package com.benjocraeft.sharehealth;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.*;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityRegainHealthEvent.RegainReason;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;

public class Sharehealth extends JavaPlugin {

    static Sharehealth Instance;

    private FileManager fileManager;
    public FileManager getFileManager(){
        return fileManager;
    }

    private HealthManager healthManager;
    public HealthManager getHealthManager(){
        return healthManager;
    }

    private Messenger messenger;
    public Messenger getMessenger(){
        return messenger;
    }

    private Statistics statistics;
    public Statistics getStatistics(){
        return statistics;
    }

    boolean isFailed = false;

    private Map<String, Object> defaultStatus = new HashMap<>();
    {
        defaultStatus.put("health", 20.);
        defaultStatus.put("isFailed", false);
        defaultStatus.put("absorptionAmount", 0.);
        defaultStatus.put("absorptionDuration", 0);
    }

    @Override
    public void onEnable(){

        //Singleton setup
        Instance = this;

        //Create File Manager for saving stats and settings
        fileManager = new FileManager();

        //Starting Health Manager for controlling actual health
        healthManager = new HealthManager();

        //Messenger
        messenger = new Messenger(getLogger());

        //Create statistics from file
        statistics = new Statistics(fileManager.loadStatistics(), fileManager.loadSettings());

        loadStatus();

        //Starts custom health regeneration
        new FoodRegeneration();

        //Register Events and Commands
        Bukkit.getPluginManager().registerEvents(new PlayerListeners(), this);
        Commands commands = new Commands();
        PluginCommand pluginCommand = Objects.requireNonNull(getCommand("sharehealth"));
        pluginCommand.setExecutor(commands);
        pluginCommand.setTabCompleter(commands);

        //Ready to go
        getLogger().info("ShareHealth has been enabled!");
    }

    @Override
    public void onDisable() {
        saveStatus();

        getLogger().info("ShareHealth has been disabled!");
    }

    void onPlayerJoin(Player player){
        updateGameMode(player);

        healthManager.updatePlayer(player);
        statistics.onPlayerJoined(player);
        fileManager.saveStatistics(statistics.getStatistics());
    }

    void onPlayerRespawn(Player player){
        updateGameMode(player);

        healthManager.updatePlayer(player);
    }
    
    void onPlayerGotDamage(Player player, double damage, DamageCause cause, boolean allowed, double absorptionDamage){
        if (isFailed)
            return;

        double receivedDamage = damage + absorptionDamage;

        if (allowed)
            messenger.onPlayerGotDamageMessage(player, receivedDamage, cause);

        statistics.onPlayerGotDamage(player, receivedDamage);
        if (!healthManager.onPlayerGotDamage(player, damage, absorptionDamage)){
            failed(player);
        }

        saveStatus();
    }
    void onPlayerGotDamageByEntity(Player player, double damage, Entity cause){
        if (isFailed)
            return;

        messenger.onPlayerGotDamageMessage(player, damage, cause);
    }
    void onPlayerGotDamageByBlock(Player player, double damage, Block cause){
        if (isFailed)
            return;

        messenger.onPlayerGotDamageMessage(player, damage, cause);
    }

    boolean onPlayerRegainedHealth(Player player, double amount, RegainReason reason){
        if (isFailed)
            return true;

        if (reason.equals(RegainReason.REGEN) || reason.equals(RegainReason.SATIATED)){
            return false;
        }

        messenger.onPlayerRegainedHealth(player, amount, reason);
        statistics.onPlayerRegainedHealth(player, amount);
        healthManager.onPlayerRegainedHealth(player, amount);

        saveStatus();

        return true;
    }

    void onFoodRegeneration(){
        healthManager.addHealth(1);
        healthManager.setHealthByPlayer(null);

        saveStatus();
    }

    void onAbsorptionConsumed(int duration, int amplifier){
        healthManager.onAbsorptionConsumed(duration, amplifier);

        saveStatus();
    }

    private void failed(Player cause){
        if (isFailed)
            return;
        isFailed = true;
        messenger.sendFailedMessage(cause);
        Bukkit.getOnlinePlayers().forEach(p -> p.setGameMode(GameMode.SPECTATOR));

        saveStatus();
    }

    void reset(){
        isFailed = false;
        healthManager.reset();
        Bukkit.getOnlinePlayers().forEach(p -> p.setGameMode(GameMode.SURVIVAL));

        saveStatus();
    }

    private void updateGameMode(Player player){
        if (!isFailed){
            player.setGameMode(GameMode.SURVIVAL);
        } else {
            player.setGameMode(GameMode.SPECTATOR);
        }
    }

    static List<Player> GetAlivePlayers(){
        List<Player> list = new ArrayList<>(Bukkit.getOnlinePlayers());
        list.removeIf(Entity::isDead);
        return list;
    }

    void saveStatus(){
        Map<String, Object> map = new HashMap<>();

        map.put("health", healthManager.getHealth());
        map.put("isFailed", isFailed);
        map.put("absorptionAmount", healthManager.absorption.amount);
        map.put("absorptionDuration", healthManager.absorption.duration);

        fileManager.saveStatus(map);
    }

    private void loadStatus(){
        Map<String, Object> map = fileManager.loadStatus();

        defaultStatus.forEach(map::putIfAbsent);

        healthManager.setHealth((Double)map.get("health"));
        isFailed = (boolean) map.get("isFailed");
        healthManager.absorption.create(
                (int)map.get("absorptionDuration"),
                (Double)map.get("absorptionAmount")
        );
    }

    void onLoggingUpdated(Player player, boolean hasLogging){
        statistics.setSettings(player.getUniqueId(), hasLogging);
        Map<UUID, Boolean> settings = statistics.getSettings();
        fileManager.saveSettings(settings);
    }

    boolean getLogging(Player player){
        return statistics.getSettings().get(player.getUniqueId());
    }

}
