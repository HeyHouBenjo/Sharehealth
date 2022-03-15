package com.benjocraeft.sharehealth;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import java.util.*;
import java.util.function.BiConsumer;

@SuppressWarnings("NullableProblems")
public class Commands implements TabExecutor {

    final private Map<List<String>, Pair<BiConsumer<CommandSender, String>, String>> commands = new HashMap<>();
    {
        addCommand("get", (sender, arg) -> commandGetHealth(sender), "Displays current health value.");
        addCommand("reset", (sender, arg) -> commandReset(sender), "Gives every player full health and resets 'isFailed' to false. GameMode becomes Survival.");
        addCommand("add", (sender, name) -> commandActivePlayer(sender, name, true), "Adds a player to the Plugin.");
        addCommand("remove", (sender, name) -> commandActivePlayer(sender, name, false), "Removes a player from the Plugin.");
        addCommand("totem one", (sender, arg) -> commandSetTotemMode(sender, TotemManager.Mode.One), "Totem of Undying: At least one player needs to hold it.");
        addCommand("totem all", (sender, arg) -> commandSetTotemMode(sender, TotemManager.Mode.All), "Totem of Undying: All players need to hold it.");
        addCommand("totem fraction", (sender, arg) -> commandSetTotemMode(sender, TotemManager.Mode.Fraction), "Totem of Undying: At least fraction * player-count need to hold it.");
        addCommand("totem disabled", (sender, arg) -> commandSetTotemMode(sender, TotemManager.Mode.Disabled), "Totem of Undying: Disabled");
        addCommand("totem setfraction", this::commandSetTotemFraction, "Totem of Undying: Set amount for mode: fraction.");
        addCommand("totem getfraction", (sender, arg) -> commandGetTotemFraction(sender), "Totem of Undying: Get amount for mode: fraction.");
        addCommand("totem get", (sender, arg) -> commandGetTotemMode(sender), "Totem of Undying: Get current mode.");
        addCommand("log on", (sender, arg) -> commandSetLogging(sender, true), "Enables Logging.");
        addCommand("log off", (sender, arg) -> commandSetLogging(sender, false), "Disables Logging.");
        addCommand("log get", (sender, arg) -> commandGetLogging(sender), "Displays if Logging is enabled.");
        addCommand("stats", (sender, arg) -> commandSendStats(sender), "Displays statistics about every player.");
        addCommand("help", (sender, arg) -> commandGetHelp(sender), "Displays help message for command usage.");
    }

    private void addCommand(String cmdList, BiConsumer<CommandSender, String> call, String description){
        commands.put(Arrays.asList(cmdList.split(" ")), Pair.pair(call, description));
    }

    final private List<String> mainSchema;
    final private List<String> hasSecondSchema;
    final private List<List<String>> secondSchema;
    {
        //This is a mess, no idea how to expand it for 3 part commands
        Map<String, List<String>> mapping = new HashMap<>();
        commands.keySet().stream().sorted((l1, l2) -> l1.stream().reduce("", (w1, w2) -> w1 + w2).compareTo(l2.stream().reduce("", (w1, w2) -> w1 + w2))).forEach(parts -> {
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

        Pair<BiConsumer<CommandSender, String>, String> command = getCommand(args);

        command.first.accept(sender, args.length > 0 ? args[args.length - 1] : "");

        return true;
    }

    private Pair<BiConsumer<CommandSender, String>, String> getCommand(String[] args){
        List<String> argList = Arrays.asList(args);

        if (commands.containsKey(argList))
            return commands.get(argList);

        if (args.length > 1){
            List<String> argListWithoutLast = argList.subList(0, args.length - 1);
            if (commands.containsKey(argListWithoutLast))
                return commands.get(argListWithoutLast);
        }

        return Pair.pair((cmdSender, arg) -> unknownCommand(cmdSender), "");
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

    private void commandSetTotemMode(CommandSender sender, TotemManager.Mode mode) {
        if (!sender.hasPermission("sharehealth.totem")){
            sender.sendMessage("You don't have permissions for this command!");
            return;
        }
        Sharehealth.Instance.getTotemManager().setMode(mode);
        sender.sendMessage("Set Totem mode to " + mode.name());
    }

    private void commandGetTotemMode(CommandSender sender){
        TotemManager.Mode mode = Sharehealth.Instance.getTotemManager().getMode();
        sender.sendMessage("Current Totem mode: " + mode.name());
    }

    private void commandSetTotemFraction(CommandSender sender, String amountStr){
        if (!sender.hasPermission("sharehealth.totem")){
            sender.sendMessage("You don't have permissions for this command!");
            return;
        }

        try {
            double fraction = Double.parseDouble(amountStr);
            Sharehealth.Instance.getTotemManager().setFractionNeeded(fraction);
            double newValue = Sharehealth.Instance.getTotemManager().getFractionNeeded();
            sender.sendMessage("Set totem fraction value to " + newValue);
        } catch (NumberFormatException e){
            sender.sendMessage("Provided value was not a number between 0.0 and 1.0!");
        }
    }

    private void commandGetTotemFraction(CommandSender sender){
        double value = Sharehealth.Instance.getTotemManager().getFractionNeeded();
        sender.sendMessage("Totem fraction value: " + value);
    }

    private void commandSetLogging(CommandSender sender, boolean hasLogging){
        if (sender instanceof Player){
            if (!Sharehealth.GetPlayers().contains(sender))
                return;

            Sharehealth.Instance.onLoggingUpdated((Player) sender, hasLogging);
            sender.sendMessage("Logging settings updated.");
        }
    }

    private void commandGetLogging(CommandSender sender){
        if (sender instanceof Player){
            if (!Sharehealth.GetPlayers().contains(sender))
                return;

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
