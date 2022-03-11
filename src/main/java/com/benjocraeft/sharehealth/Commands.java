package com.benjocraeft.sharehealth;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class Commands implements TabExecutor {

    final private Map<List<String>, Pair<BiConsumer<CommandSender, String>, String>> commands = new HashMap<>();
    {
        commands.put(
                Arrays.asList("get"),
                Pair.pair((sender, arg) -> commandGetHealth(sender), "Displays current health value.")
        );
        commands.put(
                Arrays.asList("reset"),
                Pair.pair((sender, arg) -> commandReset(sender), "Gives every player full health and resets 'isFailed' to false. GameMode becomes Survival.")
        );
        commands.put(
                Arrays.asList("add"),
                Pair.pair((sender, name) -> commandActivePlayer(sender, name, true), "Adds a player to the Plugin")
        );
        commands.put(
                Arrays.asList("remove"),
                Pair.pair((sender, name) -> commandActivePlayer(sender, name, false), "Removes a player from the Plugin")
        );
        commands.put(
                Arrays.asList("log", "on"),
                Pair.pair((sender, arg) -> commandSetLogging(sender, true), "Enables Logging.")
        );
        commands.put(
                Arrays.asList("log", "off"),
                Pair.pair((sender, arg) -> commandSetLogging(sender, false), "Disables Logging.")
        );
        commands.put(
                Arrays.asList("log", "get"),
                Pair.pair((sender, arg) -> commandGetLogging(sender), "Displays if Logging is enabled.")
        );
        commands.put(
                Arrays.asList("stats"),
                Pair.pair((sender, arg) -> commandSendStats(sender), "Displays statistics about every player.")
        );
        commands.put(
                Arrays.asList("help"),
                Pair.pair((sender, arg) -> commandGetHelp(sender), "Displays help message for command usage.")
        );
    }

    final private List<String> mainSchema;
    final private List<String> hasSecondSchema;
    final private List<List<String>> secondSchema;
    {
        Map<String, List<String>> mapping = new HashMap<>();
        commands.keySet().forEach(parts -> {
            String part1 = parts.get(0);
            String part2 = "";
            if (parts.size() == 2){
                part2 = parts.get(1);
            }
            mapping.putIfAbsent(part1, new ArrayList<>());
            if (!part2.isEmpty())
                mapping.get(part1).add(part2);
        });
        mainSchema = new ArrayList<>(mapping.keySet());

        hasSecondSchema = new ArrayList<>(mapping.keySet());
        hasSecondSchema.removeIf(s -> mapping.get(s).size() == 0);

        secondSchema = new ArrayList<>(mapping.values());
    }



    @Override
    public List<String> onTabComplete(CommandSender commandSender, Command command, String s, String[] strings) {
        List<String> list = new ArrayList<>();

        if (strings.length == 1){
            StringUtil.copyPartialMatches(strings[0], mainSchema, list);
        }
        if (strings.length == 2){
              if (hasSecondSchema.contains(strings[0])){
                  int index = mainSchema.indexOf(strings[0]);
                  List<String> checkList = secondSchema.get(index);
                  StringUtil.copyPartialMatches(strings[1], checkList, list);
              }
        }

        return list;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args){

        List<String> argList = Arrays.asList(args);
        Pair<BiConsumer<CommandSender, String>, String> command = commands.get(argList);
        if (args.length > 1 && (args[0].equals("add") || args[0].equals("remove"))){
            command = commands.get(Arrays.asList(args[0]));
        }
        if (command == null)
            command = Pair.pair((cmdSender, arg) -> unknownCommand(cmdSender), "");
        command.first.accept(sender, args.length > 0 ? args[args.length - 1] : "");

        return true;
    }

    private void commandReset(CommandSender sender){
        if (!sender.hasPermission("sharehealth.reset")){
            sender.sendMessage("You don't have permissions for this command!");
            return;
        }
        Sharehealth.Instance.reset();
    }

    private void commandActivePlayer(CommandSender sender, String playerName, boolean add){
        if (!sender.hasPermission("sharehealth.players")){
            sender.sendMessage("You don't have permissions for this command!");
            return;
        }
        UUID uuid = Bukkit.getOfflinePlayer(playerName).getUniqueId();
        if (add){
            Sharehealth.Instance.addPlayer(uuid);
            sender.sendMessage("Added player " + playerName);
        } else {
            Sharehealth.Instance.removePlayer(uuid);
            sender.sendMessage("Removed player " + playerName);
        }
    }

    private void commandGetHealth(CommandSender sender){
        String message = "Current health: " + Sharehealth.Instance.getHealthManager().getHealthString();
        sender.sendMessage(message);
    }

    private void commandSetLogging(CommandSender sender, boolean hasLogging){
        if (sender instanceof Player){
            Player player = (Player) sender;

            Sharehealth.Instance.onLoggingUpdated(player, hasLogging);
            player.sendMessage("Logging settings updated.");
        }
    }

    private void commandGetLogging(CommandSender sender){
        if (sender instanceof Player){
            String message = "Logging enabled: " + Sharehealth.Instance.getLogging((Player) sender);
            sender.sendMessage(message);
        }

    }

    private void commandSendStats(CommandSender sender){
        String message = Sharehealth.Instance.getMessenger().statisticsMessage();
        sender.sendMessage(message);
    }

    private void commandGetHelp(CommandSender sender){
        String message = Sharehealth.Instance.getMessenger().helpMessage(commands);
        sender.sendMessage(message);
    }

    private void unknownCommand(CommandSender sender){
        String message = "Unknown command. Type \"/sh help\" for help.";
        sender.sendMessage(message);
    }
}
