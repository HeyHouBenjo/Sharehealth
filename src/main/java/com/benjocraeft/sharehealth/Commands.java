package com.benjocraeft.sharehealth;

import org.apache.commons.lang3.math.NumberUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class Commands implements TabExecutor {

    final private String[] mainSchema = {
            "get", "set", "reset", "log"
    };

    final private String[] hasSecondSchema = {
            "log",
            "get"
    };

    final private String[][] secondSchema = {
            {
                "on", "off"
            },
            {
                "max"
            }
    };

    @Override
    public List<String> onTabComplete(CommandSender commandSender, Command command, String s, String[] strings) {
        List<String> list = new ArrayList<>();

        if (strings.length == 1){
            StringUtil.copyPartialMatches(strings[0], Arrays.asList(mainSchema), list);
        }
        if (strings.length == 2){
            List<String> hasSecondSchemaList = Arrays.asList(hasSecondSchema);
              if (hasSecondSchemaList.contains(strings[0])){
                  int index = hasSecondSchemaList.indexOf(strings[0]);
                  List<String> checkList = Arrays.asList(secondSchema[index]);
                  StringUtil.copyPartialMatches(strings[1], checkList, list);
              }
        }


        return list;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args){

        if (args.length != 0){
            if (args[0].equalsIgnoreCase("reset")){
                commandSetHealth(20, true);
            }
            else if (args[0].equalsIgnoreCase("set")){
                if (args.length > 1){
                    if (NumberUtils.isNumber(args[1])){
                        double num = NumberUtils.createDouble(args[1]);
                        commandSetHealth(num);
                    } else if (args[1].equalsIgnoreCase("max")){
                        if (args.length > 2){
                            if (NumberUtils.isNumber(args[2])){
                                double num = NumberUtils.createDouble(args[2]);
                                commandSetMaxHealth(num);
                            }
                        }
                    }
                }
            }
            else if (args[0].equalsIgnoreCase("get")){
                if (args.length > 1){
                    if (args[1].equalsIgnoreCase("max")){
                        if (args.length > 2){
                            if (args[2].equalsIgnoreCase("raw")){
                                commandGetMaxHealth(sender, true);
                            }
                        } else {
                            commandGetMaxHealth(sender, false);
                        }
                    }
                } else {
                    commandGetHealth(sender);
                }

            }
            else if (args[0].equalsIgnoreCase("log")){
                if (args.length > 1){
                    if (args[1].equalsIgnoreCase("off")){
                        commandSetLogging(false);
                    }
                    if (args[1].equalsIgnoreCase("on")){
                        commandSetLogging(true);
                    }
                }
            }
            else if(args[0].equalsIgnoreCase("stats")){
                commandSendStats();
            }
            else if (args[0].equalsIgnoreCase("help")){
                commandGetHelp(sender);
            } else {
                unknownCommand(sender);
            }
        } else {
            commandGetHelp(sender);
        }

        return true;
    }

    private void commandSetMaxHealth(double health){

    }

    private void commandSetHealth(double health, boolean reset){
        Sharehealth.Instance.getHealthManager().setHealth(health);
        if (reset){
            Sharehealth.Instance.reset();
        }
    }

    private void commandSetHealth(double health){
        commandSetHealth(health, false);
    }

    private void commandGetHealth(CommandSender sender){
        sender.sendMessage("Current health: " + Sharehealth.Instance.getHealthManager().getHealthString());
    }

    private void commandGetMaxHealth(CommandSender sender, boolean raw){

    }

    private void commandSetLogging(boolean log){
        Sharehealth.Instance.getMessenger().setLogging(log);
    }

    private void commandSendStats(){
        StringBuilder stats = new StringBuilder("Statistics:\n");
        Sharehealth.Instance.getStatistics().getStatistics().forEach(((uuid, values) -> {
            Player player = Bukkit.getPlayer(uuid);
            if (player != null){
                String playerName = player.getDisplayName();
                String stat = ChatColor.AQUA + playerName + ChatColor.WHITE +
                        ": Damage caused: " + ChatColor.RED + String.format("%.2f", values.first) + ChatColor.WHITE +
                        " Healing done: " + ChatColor.GREEN + String.format("%.2f", values.second) + "\n";
                stats.append(stat);
            }
        }));
        Bukkit.getOnlinePlayers().forEach((Player p) -> p.sendMessage(stats.toString()));
    }

    private void commandGetHelp(CommandSender sender){
        String help = "Usage:\n" +
                "get -> returns current globally shared health\n" +
                "set [number] -> sets new globally shared health\n" +
                "reset -> heals every player and resets 'isFailed' to false\n" +
                "log [on/off] -> activates/deactivates player log messages about damage and healings" +
                "stats -> sends everyone statistics for every player";
        sender.sendMessage(help);
    }

    private void unknownCommand(CommandSender sender){
        String msg = "Unknown command, use help.";
        sender.sendMessage(msg);
    }
}
