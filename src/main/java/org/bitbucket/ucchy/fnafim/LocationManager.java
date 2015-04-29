/*
 * @author     ucchy
 * @license    LGPLv3
 * @copyright  Copyright ucchy 2015
 */
package org.bitbucket.ucchy.fnafim;

import java.io.File;
import java.io.IOException;

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

    private File file;
    private Location lobby;
    private Location player;
    private Location spectate;
    private Location freddy;
    private Location chica;
    private Location bonnie;
    private Location foxy;

    public static LocationManager load(File file) {

        if ( !file.exists() ) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        LocationManager manager = new LocationManager();
        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
        manager.file = file;

        if ( config.contains("lobby") ) {
            manager.lobby = getLocationFromSection(
                    config.getConfigurationSection("lobby"));
        }
        if ( config.contains("player") ) {
            manager.player = getLocationFromSection(
                    config.getConfigurationSection("player"));
        }
        if ( config.contains("spectate") ) {
            manager.spectate = getLocationFromSection(
                    config.getConfigurationSection("spectate"));
        }
        if ( config.contains("freddy") ) {
            manager.freddy = getLocationFromSection(
                    config.getConfigurationSection("freddy"));
        }
        if ( config.contains("chica") ) {
            manager.chica = getLocationFromSection(
                    config.getConfigurationSection("chica"));
        }
        if ( config.contains("bonnie") ) {
            manager.bonnie = getLocationFromSection(
                    config.getConfigurationSection("bonnie"));
        }
        if ( config.contains("foxy") ) {
            manager.foxy = getLocationFromSection(
                    config.getConfigurationSection("foxy"));
        }

        return manager;
    }

    public void save() {

        YamlConfiguration config = new YamlConfiguration();

        if ( lobby != null ) {
            setLocationToSection(config.createSection("lobby"), lobby);
        }
        if ( player != null ) {
            setLocationToSection(config.createSection("player"), player);
        }
        if ( spectate != null ) {
            setLocationToSection(config.createSection("spectate"), spectate);
        }
        if ( freddy != null ) {
            setLocationToSection(config.createSection("freddy"), freddy);
        }
        if ( chica != null ) {
            setLocationToSection(config.createSection("chica"), chica);
        }
        if ( bonnie != null ) {
            setLocationToSection(config.createSection("bonnie"), bonnie);
        }
        if ( foxy != null ) {
            setLocationToSection(config.createSection("foxy"), foxy);
        }

        try {
            config.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
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

        if ( lobby == null ) {
            return "lobby";
        } else if ( player == null ) {
            return "player";
        } else if ( spectate == null ) {
            return "spectate";
        } else if ( freddy == null ) {
            return "freddy";
        } else if ( chica == null ) {
            return "chica";
        } else if ( bonnie == null ) {
            return "bonnie";
        } else if ( foxy == null ) {
            return "foxy";
        }
        return null;
    }

    /**
     * @return lobby
     */
    public Location getLobby() {
        return lobby;
    }

    /**
     * @param lobby lobby
     */
    public void setLobby(Location lobby) {
        this.lobby = lobby;
    }

    /**
     * @return player
     */
    public Location getPlayer() {
        return player;
    }

    /**
     * @param player player
     */
    public void setPlayer(Location player) {
        this.player = player;
    }

    /**
     * @return spectate
     */
    public Location getSpectate() {
        return spectate;
    }

    /**
     * @param spectate spectate
     */
    public void setSpectate(Location spectate) {
        this.spectate = spectate;
    }

    /**
     * @return freddy
     */
    public Location getFreddy() {
        return freddy;
    }

    /**
     * @param freddy freddy
     */
    public void setFreddy(Location freddy) {
        this.freddy = freddy;
    }

    /**
     * @return chica
     */
    public Location getChica() {
        return chica;
    }

    /**
     * @param chica chica
     */
    public void setChica(Location chica) {
        this.chica = chica;
    }

    /**
     * @return bonnie
     */
    public Location getBonnie() {
        return bonnie;
    }

    /**
     * @param bonnie bonnie
     */
    public void setBonnie(Location bonnie) {
        this.bonnie = bonnie;
    }

    /**
     * @return foxy
     */
    public Location getFoxy() {
        return foxy;
    }

    /**
     * @param foxy foxy
     */
    public void setFoxy(Location foxy) {
        this.foxy = foxy;
    }
}
