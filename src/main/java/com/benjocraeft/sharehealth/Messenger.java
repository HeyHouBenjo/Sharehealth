package com.benjocraeft.sharehealth;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityRegainHealthEvent.RegainReason;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.logging.Logger;

public class Messenger {

    private boolean logging = true;
    void setLogging(boolean logging){
        this.logging = logging;
    }

    private Logger logger;

    Messenger(Logger logger){
        this.logger = logger;
    }

    void onPlayerRegainedHealth(Player player, double amount, RegainReason reason){
        if (!logging || amount <= 0)
            return;
        String message = healMessage(player, amount, reason);
        Bukkit.getOnlinePlayers().forEach(p -> p.sendMessage(message));
    }

    void sendFailedMessage(Player cause){
        String message = "Mission failed, go next! CAUSE: " + ChatColor.RED + cause.getDisplayName();
        Bukkit.getOnlinePlayers().forEach(p -> p.sendMessage(message));
    }

    void onPlayerGotDamageMessage(Player player, double damage, DamageCause cause){
        if (!logging)
            return;
        String message = damageMessage(player, damage, cause);
        Bukkit.getOnlinePlayers().forEach(p -> p.sendMessage(message));
    }

    void onPlayerGotDamageMessage(Player player, double damage, Entity damager){
        if (!logging)
            return;
        String message = damageMessage(player, damage, damager);
        Bukkit.getOnlinePlayers().forEach(p -> p.sendMessage(message));
    }

    void onPlayerGotDamageMessage(Player player, double damage, Block damager){
        if (!logging)
            return;
        String message = damageMessage(player, damage, damager);
        Bukkit.getOnlinePlayers().forEach(p -> p.sendMessage(message));
    }

    private String damageMessage(Player player, double damage, DamageCause cause){
        return damageMessage(player, damage) + ChatColor.YELLOW + " Cause: " + cause;
    }

    private String damageMessage(Player player, double damage, Entity damager){
        String name = damager.getName();
        if (damager instanceof Projectile){
            name = Objects.requireNonNull(((Projectile) damager).getShooter()).toString();
        }
        return damageMessage(player, damage) + ChatColor.YELLOW + " Attacker: " + name;
    }

    private String damageMessage(Player player, double damage, Block damager){
        String name;
        try{
            name = damager.getType().name();
        } catch(NullPointerException e){
            name = "Unknown";
            e.printStackTrace();
            logger.info("Unknown error. Proceeding");
        }
        return damageMessage(player, damage) + ChatColor.YELLOW + " Block: " + name;
    }

    private String damageMessage(Player player, double damage){
        String playerS = player.getDisplayName();
        String damageS = String.format("%.2f", damage / 2);
        return ChatColor.BLUE + playerS
                + ChatColor.WHITE + " shared "
                + ChatColor.RED + damageS
                + ChatColor.WHITE + " hearts damage!";
    }

    private String healMessage(Player player, double regainedHealth, RegainReason reason){
        String playerS = player.getDisplayName();
        String healingS = String.format("%.2f", regainedHealth / 2);
        String causeS = reason.toString();
        return ChatColor.BLUE + playerS
                + ChatColor.WHITE + " shared "
                + ChatColor.GREEN + healingS
                + ChatColor.WHITE + " hearts healing!"
                + ChatColor.YELLOW + " Cause: " + causeS;
    }

    String statisticsMessage(){
        StringBuilder stats = new StringBuilder("Statistics:");
        Sharehealth.Instance.getStatistics().getStatistics().forEach(((uuid, values) -> {
            Player currentPlayer = Bukkit.getPlayer(uuid);
            if (currentPlayer != null){
                String playerName = currentPlayer.getDisplayName();
                String stat = "\n" + ChatColor.BLUE + playerName +
                        ChatColor.WHITE + ": Damage caused: " +
                        ChatColor.RED + String.format("%.2f", values.first / 2) +
                        ChatColor.WHITE + " || Healing done: " +
                        ChatColor.GREEN + String.format("%.2f", values.second / 2);
                stats.append(stat);
            }
        }));
        return stats.toString();
    }

    String helpMessage(Map<List<String>, Pair<Consumer<CommandSender>, String>> commands){
        StringBuilder helpMessage = new StringBuilder("Usage:");
        commands.forEach((nameList, pair) -> {
            StringBuilder name = new StringBuilder();
            nameList.forEach(str -> name.append(str).append(" "));

            String description = pair.second;
            helpMessage.append("\n").
                    append(ChatColor.AQUA).append(name).
                    append(ChatColor.WHITE).append("-> ").append(description);
        });
        return helpMessage.toString();
    }

}
