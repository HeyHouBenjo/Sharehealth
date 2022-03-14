package com.benjocraeft.sharehealth;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.block.Block;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityRegainHealthEvent.RegainReason;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Consumer;

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

    private TotemManager totemManager;
    public TotemManager getTotemManager() {
        return totemManager;
    }

    private Messenger messenger;
    public Messenger getMessenger(){
        return messenger;
    }

    private Statistics statistics;
    public Statistics getStatistics(){
        return statistics;
    }

    //If isFailed, plugin changes no default behaviours
    boolean isFailed = false;

    private final Map<String, Object> defaultStatus = new HashMap<>();
    {
        defaultStatus.put("health", 20.);
        defaultStatus.put("isFailed", false);
        defaultStatus.put("absorptionAmount", 0.);
        defaultStatus.put("absorptionDuration", 0);
        defaultStatus.put("totemMode", 0);
        defaultStatus.put("totemFraction", 0.5);
    }

    @Override
    public void onEnable(){

        //Singleton setup
        Instance = this;

        //Create File Manager for saving stats and settings
        fileManager = new FileManager();

        //Starting Health Manager for controlling actual health
        healthManager = new HealthManager();

        //Totem Manager controls TotemOfUndying behaviour
        totemManager = new TotemManager();

        //Messenger
        messenger = new Messenger(getLogger());

        //Create statistics from file
        statistics = new Statistics(fileManager.loadStatistics(), fileManager.loadSettings());
        getLogger().info("Statistics and Settings loaded");

        //Load status (current health, absorption, etc)
        loadStatus();
        getLogger().info("Status loaded");

        //Load players
        ActiveUUIDs = fileManager.loadPlayers();
        getLogger().info("Active players count: " + ActiveUUIDs.size());

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
        fileManager.saveStatistics(statistics.getStatistics());

        getLogger().info("ShareHealth has been disabled!");
    }

    void onPlayerJoin(Player player){
        updateGameMode(player);

        healthManager.updatePlayer(player);
        statistics.onPlayerJoined(player);
        fileManager.saveStatistics(statistics.getStatistics());
        fileManager.saveSettings(statistics.getSettings());
    }

    void onPlayerRespawn(Player player){
        updateGameMode(player);

        healthManager.updatePlayer(player);
    }

    void onPlayerGotDamage(Player player, double damage, DamageCause cause,
                           boolean isMessageAllowed, double absorbedDamage, Consumer<Boolean> cancelDamage){
        if (isFailed)
            return;

        double receivedDamage = damage + absorbedDamage;

        if (isMessageAllowed)
            messenger.onPlayerGotDamageMessage(player, receivedDamage, cause);

        statistics.onPlayerGotDamage(player, receivedDamage);

        if (healthManager.wouldCauseDeath(damage)){
            if (totemManager.totemCanBeUsed()){
                healthManager.onTotemTriggered();
                totemManager.activate(player);
                cancelDamage.accept(true);
            } else {
                healthManager.onPlayerGotDamage(player, damage, absorbedDamage);
                failed(player);
            }
        } else {
            healthManager.onPlayerGotDamage(player, damage, absorbedDamage);
        }

        saveStatus();
    }
    void onPlayerGotDamageByEntity(Player player, double damage, Entity cause, double absorbedDamage){
        if (isFailed)
            return;

        messenger.onPlayerGotDamageMessage(player, damage + absorbedDamage, cause);
    }
    void onPlayerGotDamageByBlock(Player player, double damage, Block cause, double absorbedDamage){
        if (isFailed)
            return;

        messenger.onPlayerGotDamageMessage(player, damage + absorbedDamage, cause);
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
        healthManager.applyHealthToAllExcept(null);

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
        GetPlayers().forEach(p -> p.setGameMode(GameMode.SPECTATOR));

        saveStatus();
    }

    void reset(){
        isFailed = false;
        fileManager.backupStats(statistics.getStatistics());
        statistics.reset();
        fileManager.saveStatistics(statistics.getStatistics());
        healthManager.reset();
        GetPlayers().forEach(p -> p.setGameMode(GameMode.SURVIVAL));

        saveStatus();
    }

    private void updateGameMode(Player player){
        if (!isFailed){
            player.setGameMode(GameMode.SURVIVAL);
        } else {
            player.setGameMode(GameMode.SPECTATOR);
        }
    }

    private static List<UUID> ActiveUUIDs = new ArrayList<>();
    static List<Player> GetPlayers(){
        List<Player> players = new ArrayList<>(Bukkit.getOnlinePlayers());
        players.removeIf(p -> !ActiveUUIDs.contains(p.getUniqueId()));
        return players;
    }

    public void addPlayer(UUID uuid){
        ActiveUUIDs.add(uuid);
        fileManager.savePlayers(ActiveUUIDs);
    }

    public void removePlayer(UUID uuid){
        ActiveUUIDs.remove(uuid);
        fileManager.savePlayers(ActiveUUIDs);
    }

    void saveStatus(){
        Map<String, Object> map = new HashMap<>();

        map.put("health", healthManager.getHealth());
        map.put("isFailed", isFailed);
        map.put("absorptionAmount", healthManager.absorptionManager.amount);
        map.put("absorptionDuration", healthManager.absorptionManager.duration);
        map.put("totemMode", totemManager.getMode().ordinal());
        map.put("totemFraction", totemManager.getFractionNeeded());

        fileManager.saveStatus(map);
    }

    private void loadStatus(){
        Map<String, Object> map = fileManager.loadStatus();

        defaultStatus.forEach(map::putIfAbsent);
        map.forEach((String key, Object value) -> getLogger().info(key + "=" + value));

        healthManager.setHealth((Double)map.get("health"));
        isFailed = (boolean) map.get("isFailed");
        healthManager.absorptionManager.create(
                (int)map.get("absorptionDuration"),
                (Double)map.get("absorptionAmount")
        );
        totemManager.setMode(TotemManager.Mode.values()[(int)map.get("totemMode")]);
        totemManager.setFractionNeeded((Double)map.get("totemFraction"));
    }

    void onLoggingUpdated(Player player, boolean hasLogging){
        statistics.setSettings(player.getUniqueId(), hasLogging);
        Map<UUID, Boolean> settings = statistics.getSettings();
        fileManager.saveSettings(settings);
    }

    boolean getLogging(Player player){
        Map<UUID, Boolean> settings = statistics.getSettings();
        return settings.get(player.getUniqueId());
    }

}

