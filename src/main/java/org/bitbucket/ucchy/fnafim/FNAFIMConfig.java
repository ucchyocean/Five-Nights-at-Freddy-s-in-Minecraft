/*
 * @author     ucchy
 * @license    LGPLv3
 * @copyright  Copyright ucchy 2015
 */
package org.bitbucket.ucchy.fnafim;

import java.io.File;

import org.bukkit.configuration.file.FileConfiguration;

/**
 * FNAFIMコンフィグクラス
 * @author ucchy
 */
public class FNAFIMConfig {

    /** メッセージ言語 */
    private String lang;

    /** 最大参加可能人数 */
    private int maxPlayers;

    /** 1時間あたりの実秒数 */
    private int secondsOfOneHour;

    /** Foxyが一晩あたりに行動できる回数 */
    private int foxyMovementPerNight;

    /** カスタムナイトのFreddyの移動速度 */
    private int customNightMoveSpeed_freddy;

    /** カスタムナイトのChicaの移動速度 */
    private int customNightMoveSpeed_chica;

    /** カスタムナイトのBonnyの移動速度 */
    private int customNightMoveSpeed_bonnie;

    /** カスタムナイトのFoxyの移動速度 */
    private int customNightMoveSpeed_foxy;

    /**
     * コンストラクタ
     */
    public FNAFIMConfig() {
        reloadConfig();
    }

    /**
     * config.yml を再読み込みする
     */
    public void reloadConfig() {

        FiveNightsAtFreddysInMinecraft plugin = FiveNightsAtFreddysInMinecraft.getInstance();

        File configFile = new File(
                plugin.getDataFolder(), "config.yml");
        if ( !configFile.exists() ) {
            Utility.copyFileFromJar(plugin.getPluginJarFile(),
                    configFile, "config_ja.yml", false);
        }

        FiveNightsAtFreddysInMinecraft.getInstance().reloadConfig();
        FileConfiguration config = plugin.getConfig();

        lang = config.getString("lang", "ja");
        maxPlayers = config.getInt("maxPlayers", 64);
        secondsOfOneHour = config.getInt("secondsOfOneHour", 90);
        foxyMovementPerNight = config.getInt("foxyMovementPerNight", 3);
        customNightMoveSpeed_freddy = config.getInt("customNightMoveSpeed.freddy", 4);
        customNightMoveSpeed_chica = config.getInt("customNightMoveSpeed.chica", 4);
        customNightMoveSpeed_bonnie = config.getInt("customNightMoveSpeed.bonnie", 4);
        customNightMoveSpeed_foxy = config.getInt("customNightMoveSpeed.foxy", 3);
    }

    /**
     * @return lang
     */
    public String getLang() {
        return lang;
    }

    /**
     * @return maxPlayers
     */
    public int getMaxPlayers() {
        return maxPlayers;
    }

    /**
     * @return secondsOfOneHour
     */
    public int getSecondsOfOneHour() {
        return secondsOfOneHour;
    }

    /**
     * @return foxyMovementPerNight
     */
    public int getFoxyMovementPerNight() {
        return foxyMovementPerNight;
    }

    /**
     * @return customNightMoveSpeed_freddy
     */
    public int getCustomNightMoveSpeed_freddy() {
        return customNightMoveSpeed_freddy;
    }

    /**
     * @return customNightMoveSpeed_chica
     */
    public int getCustomNightMoveSpeed_chica() {
        return customNightMoveSpeed_chica;
    }

    /**
     * @return customNightMoveSpeed_bonny
     */
    public int getCustomNightMoveSpeed_bonnie() {
        return customNightMoveSpeed_bonnie;
    }

    /**
     * @return customNightMoveSpeed_foxy
     */
    public int getCustomNightMoveSpeed_foxy() {
        return customNightMoveSpeed_foxy;
    }

}
