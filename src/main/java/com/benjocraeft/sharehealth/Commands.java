package com.benjocraeft.sharehealth;

import io.netty.util.concurrent.ImmediateEventExecutor;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import java.util.*;
import java.util.function.Consumer;

public class Commands implements TabExecutor {

    final private Map<List<String>, Pair<Consumer<CommandSender>, String>> commands = new HashMap<>();
    {
        commands.put(
                Arrays.asList("get"),
                Pair.pair(this::commandGetHealth, "Displays current health value.")
        );
        commands.put(
                Arrays.asList("reset"),
                Pair.pair(this::commandReset, "Gives every player full health and resets 'isFailed' to false. GameMode becomes Survival.")
        );
        commands.put(
                Arrays.asList("log", "on"),
                Pair.pair(sender -> this.commandSetLogging(sender, true), "Enables Logging.")
        );
        commands.put(
                Arrays.asList("log", "off"),
                Pair.pair(sender -> this.commandSetLogging(sender, false), "Disables Logging.")
        );
        commands.put(
                Arrays.asList("log", "get"),
                Pair.pair(this::commandGetLogging, "Displays if Logging is enabled.")
        );
        commands.put(
                Arrays.asList("stats"),
                Pair.pair(this::commandSendStats, "Displays statistics about every player.")
        );
        commands.put(
                Arrays.asList("help"),
                Pair.pair(this::commandGetHelp, "Displays help message for command usage.")
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
        Pair<Consumer<CommandSender>, String> command = commands.get(argList);
        if (command == null)
            command = Pair.pair(this::unknownCommand, "");
        command.first.accept(sender);

        return true;
    }

    private void commandReset(CommandSender sender){
        Sharehealth.Instance.reset();
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
