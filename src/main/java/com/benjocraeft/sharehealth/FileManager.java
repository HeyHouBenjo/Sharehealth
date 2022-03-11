package com.benjocraeft.sharehealth;

import java.io.*;
import java.util.*;
import java.util.logging.Logger;

public class FileManager {

    final private File settingsFile;
    final private File statisticsFile;
    final private File statusFile;
    final private File playersFile;

    final private File pluginFolder = new File(System.getProperty("user.dir"), "plugins/sharehealth");
    final private String pluginPath = pluginFolder.getPath();

    public FileManager(){
        Logger logger = Sharehealth.Instance.getLogger();

        //Prepare storage folder
        if (pluginFolder.mkdirs()){
            logger.info(pluginFolder.getName() + " created");
        }

        settingsFile = new File(pluginPath + "/settings.txt");
        statisticsFile = new File(pluginPath + "/statistics.txt");
        statusFile = new File(pluginPath + "/status.txt");
        playersFile = new File(pluginPath + "/players.txt");

        try {
            if (settingsFile.createNewFile())
                logger.info(settingsFile.getName() + " created");
            if (statisticsFile.createNewFile())
                logger.info(statisticsFile.getName() + " created");
            if (statusFile.createNewFile())
                logger.info(statusFile.getName() + " created");
            if (playersFile.createNewFile())
                logger.info(playersFile.getName() + " created");
        } catch(IOException e){
            e.printStackTrace();
        }
    }

    public Map<UUID, Boolean> loadSettings(){
        Map<UUID, Boolean> settingsMap = new HashMap<>();

        Map<String, String> map = loadMapFromFile(settingsFile);
        map.forEach((String uuidString, String hasLoggingString) -> {
            UUID uuid = UUID.fromString(uuidString);
            Boolean hasLogging = Boolean.parseBoolean(hasLoggingString);
            settingsMap.put(uuid, hasLogging);
        });

        return settingsMap;
    }

    public Map<UUID, Pair<Double, Double>> loadStatistics(){
        Map<UUID, Pair<Double, Double>> statisticsMap = new HashMap<>();

        Map<String, String> map = loadMapFromFile(statisticsFile);
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

        Map<String, String> map = loadMapFromFile(statusFile);

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

    public List<UUID> loadPlayers(){
        Map<String, String> loaded = loadMapFromFile(playersFile);
        List<UUID> playerUUIDs = new ArrayList<>();
        loaded.keySet().forEach(s -> playerUUIDs.add(UUID.fromString(s)));
        return playerUUIDs;
    }

    public void saveStatistics(Map<UUID, Pair<Double, Double>> statistics){
        saveStatistics(statisticsFile, statistics);
    }

    private void saveStatistics(File file, Map<UUID, Pair<Double, Double>> statistics) {
        Map<String, Object> map = new HashMap<>();

        statistics.forEach((UUID uuid, Pair<Double, Double> pair) -> {
            String uuidString = uuid.toString();
            map.put(uuidString, Statistics.Rounded(pair.first) + "," + Statistics.Rounded(pair.second));
        });

        saveMapToFile(file, map);
    }

    public void saveStatus(Map<String, Object> statusMap){
        saveMapToFile(statusFile, statusMap);
    }

    public void saveSettings(Map<UUID, Boolean> settingsMap){
        Map<String, Object> map = new HashMap<>();

        settingsMap.forEach((UUID uuid, Boolean hasLogging) -> map.put(uuid.toString(), hasLogging));

        saveMapToFile(settingsFile, map);
    }

    public void savePlayers(List<UUID> playerUUIDs){
        Map<String, Object> map = new HashMap<>();

        playerUUIDs.forEach(uuid -> map.put(uuid.toString(), ""));

        saveMapToFile(playersFile, map);
    }

    public void backupStats(Map<UUID, Pair<Double, Double>> statistics){
        Date date = new Date();
        String dateString = String.valueOf(date.getTime());
        File backupDir = new File(pluginPath + "/old_statistics");
        if (backupDir.mkdirs()){
            Sharehealth.Instance.getLogger().info("Folder old_statistics created");
        }
        File backupFile = new File(backupDir.getPath() + "/" + dateString + ".txt");
        try {
            if (backupFile.createNewFile()){
                Sharehealth.Instance.getLogger().info(backupFile.getName() + " created");
                saveStatistics(backupFile, statistics);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Map<String, String> loadMapFromFile(File file) {
        Map<String, String> map = new HashMap<>();

        try{
            DataInputStream input = new DataInputStream(new FileInputStream(file));
            BufferedReader reader = new BufferedReader(new InputStreamReader(input));

            try{
                String line;
                while((line = reader.readLine()) != null){
                    String[] split = line.split("=");
                    map.put(split[0], split.length == 2 ? split[1] : "");
                }
            } catch (NullPointerException ignore){}

            reader.close();
            input.close();
        } catch(IOException e){
            e.printStackTrace();
        }

        return map;
    }

    private void saveMapToFile(File file, Map<String, Object> content){
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
