/*
 * @author     ucchy
 * @license    LGPLv3
 * @copyright  Copyright ucchy 2015
 */
package org.bitbucket.ucchy.fnafim;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

/**
 * 地点の管理クラス
 * @author ucchy
 */
public class LocationManager {

    private static final String[] NAMES = {
        "lobby", "player", "spectate", "freddy", "chica", "bonnie", "foxy"
    };

    private File file;
    private HashMap<String, Location> arena;
    private HashMap<String, HashMap<String, Location>> arenas;
    private String arenaName;

    private LocationManager(File file) {
        this.file = file;
        this.arena = new HashMap<String, Location>();
        this.arenas = new HashMap<String, HashMap<String,Location>>();
        this.arenaName = "default";
    }

    public static LocationManager load(File file) {

        if ( !file.exists() ) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        LocationManager manager = new LocationManager(file);
        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);

        if ( config.contains("arenaName") ) {
            manager.arenaName = config.getString("arenaName");
        }

        for ( String name : NAMES ) {
            if ( config.contains(name) ) {
                manager.arena.put(name, getLocationFromSection(config.getConfigurationSection(name)));
            }
        }

        if ( config.contains("arenas") ) {
            for ( String aname : config.getConfigurationSection("arenas").getKeys(false) ) {
                ConfigurationSection section = config.getConfigurationSection("arenas." + aname);
                HashMap<String, Location> setting = new HashMap<String, Location>();
                for ( String name : NAMES ) {
                    if ( config.contains(name) ) {
                        setting.put(name, getLocationFromSection(section.getConfigurationSection(name)));
                    }
                }
                manager.arenas.put(aname, setting);
            }
        }

        // 過去バージョンからのアップデート (v0.7.1以前 → v0.7.2以降)
        if ( !config.contains("arenas.default") ) {
            manager.arenas.put("default", manager.arena);
            manager.save();
        }

        return manager;
    }

    public void save() {

        YamlConfiguration config = new YamlConfiguration();

        config.set("arenaName", arenaName);

        for ( String name : arena.keySet() ) {
            ConfigurationSection section = config.createSection(name);
            setLocationToSection(section, arena.get(name));
        }

        for ( String aname : arenas.keySet() ) {
            HashMap<String, Location> setting = arenas.get(aname);
            for ( String name : setting.keySet() ) {
                ConfigurationSection section = config.createSection("arenas." + aname + "." + name);
                setLocationToSection(section, setting.get(name));
            }
        }

        try {
            config.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void switchTo(String arenaName) {

        arenaName = arenaName.toLowerCase();
        this.arenaName = arenaName;

        if ( !arenas.containsKey(arenaName) ) {
            arenas.put(arenaName, new HashMap<String, Location>());
        }

        arena = arenas.get(arenaName);
    }

    private static void setLocationToSection(ConfigurationSection section, Location location) {

        section.set("world", location.getWorld().getName());
        section.set("x", location.getBlockX());
        section.set("y", location.getBlockY());
        section.set("z", location.getBlockZ());
        section.set("yaw", location.getYaw());
        section.set("pitch", location.getPitch());
    }

    private static Location getLocationFromSection(ConfigurationSection section) {

        if ( !section.contains("world") || !section.contains("x") || !section.contains("y")
                || !section.contains("z") || !section.contains("yaw") || !section.contains("pitch") ) {
            return null;
        }

        String worldName = section.getString("world", "world");
        World world = Bukkit.getWorld(worldName);
        if ( world == null ) return null;

        int x = section.getInt("x");
        int y = section.getInt("y");
        int z = section.getInt("z");
        float yaw = (float)section.getDouble("yaw");
        float pitch = (float)section.getDouble("pitch");

        return new Location(world, x, y, z, yaw, pitch);
    }

    public String getNullLocationName() {

        for ( String name : NAMES ) {
            if ( !arena.containsKey(name) ) return name;
        }
        return null;
    }

    public Set<String> getArenaNames() {
        return arenas.keySet();
    }

    public ArrayList<String> getReadyArenaNames() {
        ArrayList<String> list = new ArrayList<String>();
        for ( String arenaName : arenas.keySet() ) {
            boolean isOk = true;
            for ( String name : NAMES ) {
                if ( !arenas.get(arenaName).containsKey(name) ) {
                    isOk = false;
                    break;
                }
            }
            if ( isOk ) {
                list.add(arenaName);
            }
        }
        return list;
    }

    /**
     * @return arenaName
     */
    public String getArenaName() {
        return arenaName;
    }

    /**
     * @return lobby
     */
    public Location getLobby() {
        return arena.get("lobby");
    }

    /**
     * @param lobby lobby
     */
    public void setLobby(Location lobby) {
        arena.put("lobby", lobby);
    }

    /**
     * @return player
     */
    public Location getPlayer() {
        return arena.get("player");
    }

    /**
     * @param player player
     */
    public void setPlayer(Location player) {
        arena.put("player", player);
    }

    /**
     * @return spectate
     */
    public Location getSpectate() {
        return arena.get("spactate");
    }

    /**
     * @param spectate spectate
     */
    public void setSpectate(Location spectate) {
        arena.put("spectate", spectate);
    }

    /**
     * @return freddy
     */
    public Location getFreddy() {
        return arena.get("freddy");
    }

    /**
     * @param freddy freddy
     */
    public void setFreddy(Location freddy) {
        arena.put("freddy", freddy);
    }

    /**
     * @return chica
     */
    public Location getChica() {
        return arena.get("chica");
    }

    /**
     * @param chica chica
     */
    public void setChica(Location chica) {
        arena.put("chica", chica);
    }

    /**
     * @return bonnie
     */
    public Location getBonnie() {
        return arena.get("bonnie");
    }

    /**
     * @param bonnie bonnie
     */
    public void setBonnie(Location bonnie) {
        arena.put("bonnie", bonnie);
    }

    /**
     * @return foxy
     */
    public Location getFoxy() {
        return arena.get("foxy");
    }

    /**
     * @param foxy foxy
     */
    public void setFoxy(Location foxy) {
        arena.put("foxy", foxy);
    }
}
