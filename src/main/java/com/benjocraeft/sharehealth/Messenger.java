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
import org.bukkit.projectiles.BlockProjectileSource;
import org.bukkit.projectiles.ProjectileSource;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.logging.Logger;

public record Messenger(Logger logger) {

    private List<Player> playersToSendLogs() {
        List<Player> players = Sharehealth.GetPlayers();
        players.removeIf(p -> !Sharehealth.Instance.getLogging(p));
        return players;
    }

    void onPlayerRegainedHealth(Player player, double amount, RegainReason reason) {
        if (amount <= 0)
            return;
        String message = healMessage(player, amount, reason);
        playersToSendLogs().forEach(p -> p.sendMessage(message));
    }

    void sendFailedMessage(Player cause) {
        String playerName = getPlayerName(cause);
        String message = "Mission failed, go next! CAUSE: " + ChatColor.RED + playerName;
        Sharehealth.GetPlayers().forEach(p -> p.sendMessage(message));
    }

    void onPlayerGotDamageMessage(Player player, double damage, DamageCause cause) {
        String message = damageMessage(player, damage, cause.toString());
        playersToSendLogs().forEach(p -> p.sendMessage(message));
    }

    void onPlayerGotDamageMessage(Player player, double damage, Entity damagingEntity) {
        String message = damageMessage(player, damage, damagingEntity);
        playersToSendLogs().forEach(p -> p.sendMessage(message));
    }

    void onPlayerGotDamageMessage(Player player, double damage, Block damagingBlock) {
        String message = damageMessage(player, damage, damagingBlock);
        playersToSendLogs().forEach(p -> p.sendMessage(message));
    }

    private String damageMessage(Player player, double damage, Entity damagingEntity) {
        String damagingEntityName = damagingEntity.getName();
        if (damagingEntity instanceof Projectile projectile) {
            ProjectileSource source = projectile.getShooter();
            if (source != null) {
                if (source instanceof Entity shooterEntity) {
                    damagingEntityName = shooterEntity.getName();
                }
                if (source instanceof BlockProjectileSource shooterBlock) {
                    return damageMessage(player, damage, shooterBlock.getBlock());
                }
            }
        }
        return damageMessage(player, damage, damagingEntityName);
    }

    private String damageMessage(Player player, double damage, Block damagingBlock) {
        String name;
        try {
            name = damagingBlock.getType().name();
        } catch (NullPointerException e) {
            name = "Unknown";
            e.printStackTrace();
            logger.info("Unknown error. Proceeding");
        }
        return damageMessage(player, damage, name);
    }

    private String damageMessage(Player player, double damage, String source) {
        String playerS = getPlayerName(player);
        String damageS = String.format("%.2f", damage / 2);
        return ChatColor.BLUE + playerS
                + ChatColor.WHITE + " - "
                + ChatColor.RED + damageS
                + ChatColor.WHITE + " - "
                + ChatColor.YELLOW + source;
    }

    private String healMessage(Player player, double regainedHealth, RegainReason reason) {
        String playerS = getPlayerName(player);
        String healingS = String.format("%.2f", regainedHealth / 2);
        String reasonString = reason.toString();
        return ChatColor.BLUE + playerS
                + ChatColor.WHITE + " - "
                + ChatColor.GREEN + healingS
                + ChatColor.WHITE + " - "
                + ChatColor.YELLOW + reasonString;
    }

    String statisticsMessage() {
        Map<UUID, Pair<Double, Double>> statistics = Sharehealth.Instance.getStatistics().getStatistics();
        if (statistics.size() == 0)
            return "There are no statistics yet.";

        StringBuilder stats = new StringBuilder("Statistics:");
        statistics.forEach(((uuid, values) -> {
            String playerName = Bukkit.getOfflinePlayer(uuid).getName();
            String stat = "\n" + ChatColor.BLUE + playerName +
                    ChatColor.WHITE + ": Damage caused: " +
                    ChatColor.RED + String.format("%.2f", values.first / 2) +
                    ChatColor.WHITE + " || Healing done: " +
                    ChatColor.GREEN + String.format("%.2f", values.second / 2);
            stats.append(stat);
        }));
        return stats.toString();
    }

    String helpMessage(Map<List<String>, Pair<BiConsumer<CommandSender, String>, String>> commands) {
        List<String> lines = new ArrayList<>();
        commands.forEach((nameList, pair) -> {
            StringBuilder name = new StringBuilder();
            nameList.forEach(str -> name.append(str).append(" "));

            String description = pair.second;
            String message = "\n" +
                    ChatColor.AQUA + name +
                    ChatColor.WHITE + "-> " + description;
            lines.add(message);
        });
        StringBuilder completeMessage = new StringBuilder("Usage:");
        for (String line : lines.stream().sorted().toList())
            completeMessage.append(line);

        return completeMessage.toString();
    }

    private String getPlayerName(Player player) {
        //Papermc:
        //return ((TextComponent) player.displayName()).content();

        //Spigot:
        return player.getDisplayName();
    }

}
