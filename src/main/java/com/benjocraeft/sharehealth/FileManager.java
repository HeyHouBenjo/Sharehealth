package com.benjocraeft.sharehealth;

import java.io.*;
import java.util.*;

public class FileManager {

    final private File settingsFile;
    final private File statisticsFile;
    final private File statusFile;


    public FileManager(){
        File pluginFolder = new File(System.getProperty("user.dir"), "plugins/sharehealth");
        String pluginPath = pluginFolder.getPath();

        //Prepare storage folder
        if (pluginFolder.mkdirs()){
            //TODO Log
        }

        settingsFile = new File(pluginPath + "/settings.txt");
        statisticsFile = new File(pluginPath + "/statistics.txt");
        statusFile = new File(pluginPath + "/status.txt");
        try {
            if (settingsFile.createNewFile()){
                //TODO Log
            }
            if (statisticsFile.createNewFile()){
                //TODO Log
            }
            if (statusFile.createNewFile()){
                //TODO Log

            }
        } catch(IOException e){
            e.printStackTrace();
        }
    }

    public Map<UUID, Boolean> loadSettings(){
        Map<UUID, Boolean> settingsMap = new HashMap<>();

        Map<String, String> map = loadFromFile(settingsFile);
        map.forEach((String uuidString, String hasLoggingString) -> {
            UUID uuid = UUID.fromString(uuidString);
            Boolean hasLogging = Boolean.parseBoolean(hasLoggingString);
            settingsMap.put(uuid, hasLogging);
        });

        return settingsMap;
    }
    public Map<UUID, Pair<Double, Double>> loadStatistics(){
        Map<UUID, Pair<Double, Double>> statisticsMap = new HashMap<>();

        Map<String, String> map = loadFromFile(statisticsFile);
        map.forEach((String s1, String s2) -> {
            UUID uuid = UUID.fromString(s1);

            String[] split = s2.split(",");
            Double damage = Double.parseDouble(split[0]);
            Double healing = Double.parseDouble(split[1]);
            Pair<Double, Double> statistics = Pair.pair(damage, healing);

            statisticsMap.put(uuid, statistics);
        });

        return statisticsMap;
    }
    public Map<String, Object> loadStatus(){
        Map<String, Object> statusMap = new HashMap<>();

        Map<String, String> map = loadFromFile(statusFile);

        map.forEach((String key, String value) -> {
            if (value.matches("-?\\d+"))
                statusMap.put(key, Integer.parseInt(value));
            else if (value.matches("-?\\d+(\\.\\d+)?([df])?"))
                statusMap.put(key, Double.parseDouble(value));
            else if (value.matches("(true)|(false)"))
                statusMap.put(key, Boolean.parseBoolean(value));
        });

        return statusMap;
    }

    public void saveStatistics(Map<UUID, Pair<Double, Double>> statistics){
        Map<String, Object> map = new HashMap<>();

        statistics.forEach((UUID uuid, Pair<Double, Double> pair) -> {
            String uuidString = uuid.toString();
            map.put(uuidString, Statistics.Rounded(pair.first) + "," + Statistics.Rounded(pair.second));
        });

        saveToFile(statisticsFile, map);
    }

    public void saveStatus(Map<String, Object> statusMap){
        saveToFile(statusFile, statusMap);
    }

    public void saveSettings(Map<UUID, Boolean> settingsMap){
        Map<String, Object> map = new HashMap<>();

        settingsMap.forEach((UUID uuid, Boolean hasLogging) -> map.put(uuid.toString(), hasLogging));

        saveToFile(settingsFile, map);
    }

    private Map<String, String> loadFromFile(File file) {
        Map<String, String> map = new HashMap<>();

        try{
            DataInputStream input = new DataInputStream(new FileInputStream(file));
            BufferedReader reader = new BufferedReader(new InputStreamReader(input));

            try{
                String line;
                while((line = reader.readLine()) != null){
                    String[] split = line.split("=");
                    map.put(split[0], split[1]);
                }
            } catch (NullPointerException ignore){}

            reader.close();
            input.close();
        } catch(IOException e){
            e.printStackTrace();
        }

        return map;
    }

    private void saveToFile(File file, Map<String, Object> content){
        try{
            FileWriter stream = new FileWriter(file);
            BufferedWriter out = new BufferedWriter(stream);

            content.forEach((String key, Object obj) -> {
                try {
                    String value = obj.toString();
                    if (obj instanceof Double){
                        value = new Formatter(Locale.US).format("%.2f", obj).toString();
                    }
                    out.write(key + "=" + value);
                    out.write("\n");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });

            out.close();
            stream.close();
        } catch (IOException e){
            e.printStackTrace();
        }
    }

}
