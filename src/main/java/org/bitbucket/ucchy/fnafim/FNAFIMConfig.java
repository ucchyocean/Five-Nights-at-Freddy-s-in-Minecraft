/*
 * @author     ucchy
 * @license    LGPLv3
 * @copyright  Copyright ucchy 2015
 */
package org.bitbucket.ucchy.fnafim;

import java.io.File;

import org.bitbucket.ucchy.fnafim.game.Night;
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

    /** Night間のインターバル秒数 */
    private int secondsOfNightInterval;

    /** Foxyが一回の行動で移動可能な時間（秒） */
    private int foxyMovementSeconds;

    /** 1秒あたりのバッテリー基本消費量 */
    private double batteryDecreasePerSecond;

    /** 懐中電灯を使用しているときの1秒あたりのバッテリー消費量 */
    private double batteryFlashLightPerSecond;

    /** レーダーを使用したときの1回あたりのバッテリー消費量 */
    private double batteryRaderPerUse;

    /** シャッターを使用しているときの1秒あたりのバッテリー消費量 */
    private double batteryShutterPerSecond;

    /** レーダーの索敵範囲（マス） */
    private int raderSearchingRange;

    /** プレイヤーのログアウト逃げ期限時間（秒） */
    private int playerLogoutTrackingSeconds;

    /** Night1の移動速度 */
    private FNAFIMConfigMoveSetting night1MoveSpeed;

    /** Night2の移動速度 */
    private FNAFIMConfigMoveSetting night2MoveSpeed;

    /** Night3の移動速度 */
    private FNAFIMConfigMoveSetting night3MoveSpeed;

    /** Night4の移動速度 */
    private FNAFIMConfigMoveSetting night4MoveSpeed;

    /** Night5の移動速度 */
    private FNAFIMConfigMoveSetting night5MoveSpeed;

    /** Night6の移動速度 */
    private FNAFIMConfigMoveSetting night6MoveSpeed;

    /** CustomNightの移動速度 */
    private FNAFIMConfigMoveSetting customNightMoveSpeed;

    /** Chicaの威嚇音を出す能力のクールダウンタイム */
    private int chicaThreatCooldownSeconds;

    /** 効果音 - 懐中電灯のオンオフ */
    private SoundComponent soundUseFlashLight;

    /** 効果音 - レーダーの使用 */
    private SoundComponent soundUseRader;

    /** 効果音 - シャッターの使用 */
    private SoundComponent soundUseShutter;

    /** 効果音 - Foxyの行動開始 */
    private SoundComponent soundFoxyMovement;

    /** 効果音 - Freddyの電力切れプレイヤーへのテレポート */
    private SoundComponent soundFreddyTeleport;

    /** 効果音 - Night開始 */
    private SoundComponent soundNightStart;

    /** 効果音 - Night終了 */
    private SoundComponent soundNightEnd;

    /** 効果音 - プレイヤーが捕まったときの効果音 */
    private SoundComponent soundPlayerCaught;

    /** 効果音 - Chicaの威嚇音 */
    private SoundComponent soundChicaThreat;

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

        // コンフィグファイルが無いなら生成する
        File configFile = new File(plugin.getDataFolder(), "config.yml");
        if ( !configFile.exists() ) {
            if ( FiveNightsAtFreddysInMinecraft.getDefaultLocaleLanguage().equals("ja") ) {
                Utility.copyFileFromJar(
                        plugin.getPluginJarFile(), configFile, "config_ja.yml", false);
            } else {
                Utility.copyFileFromJar(
                        plugin.getPluginJarFile(), configFile, "config.yml", false);
            }
        }

        FiveNightsAtFreddysInMinecraft.getInstance().reloadConfig();
        FileConfiguration config = plugin.getConfig();

        lang = config.getString("lang", "ja");
        maxPlayers = config.getInt("maxPlayers", 64);
        secondsOfOneHour = config.getInt("secondsOfOneHour", 90);
        secondsOfNightInterval = config.getInt("secondsOfNightInterval", 15);
        foxyMovementSeconds = config.getInt("foxyMovementSeconds", 15);
        batteryDecreasePerSecond = config.getDouble("batteryDecreasePerSecond", 0.14);
        batteryFlashLightPerSecond = config.getDouble("batteryFlashLightPerSecond", 0.2);
        batteryRaderPerUse = config.getDouble("batteryRaderPerUse", 1.0);
        batteryShutterPerSecond = config.getDouble("batteryShutterPerSecond", 0.67);
        raderSearchingRange = config.getInt("raderSearchingRange", 15);
        playerLogoutTrackingSeconds = config.getInt("playerLogoutTrackingSeconds", 20);
        chicaThreatCooldownSeconds = config.getInt("chicaThreatCooldownSeconds", 40);

        night1MoveSpeed = FNAFIMConfigMoveSetting.load(
                config, "night1MoveSpeed", -99, -3, -3, 1, 1);
        night2MoveSpeed = FNAFIMConfigMoveSetting.load(
                config, "night2MoveSpeed", -99, -2, -2, 1, 1);
        night3MoveSpeed = FNAFIMConfigMoveSetting.load(
                config, "night3MoveSpeed", -1, -1, -1, 2, 2);
        night4MoveSpeed = FNAFIMConfigMoveSetting.load(
                config, "night4MoveSpeed", 0, 0, 0, 3, 3);
        night5MoveSpeed = FNAFIMConfigMoveSetting.load(
                config, "night5MoveSpeed", 1, 1, 1, 3, 3);
        night6MoveSpeed = FNAFIMConfigMoveSetting.load(
                config, "night6MoveSpeed", 2, 2, 2, 4, 4);
        customNightMoveSpeed = FNAFIMConfigMoveSetting.load(
                config, "customNightMoveSpeed", 4, 4, 4, 5, 5);

        soundUseFlashLight = SoundComponent.getComponentFromString(
                config.getString("soundUseFlashLight"));
        soundUseRader = SoundComponent.getComponentFromString(
                config.getString("soundUseRader"));
        soundUseShutter = SoundComponent.getComponentFromString(
                config.getString("soundUseShutter"));
        soundFoxyMovement = SoundComponent.getComponentFromString(
                config.getString("soundFoxyMovement"));
        soundFreddyTeleport = SoundComponent.getComponentFromString(
                config.getString("soundFreddyTeleport"));
        soundNightStart = SoundComponent.getComponentFromString(
                config.getString("soundNightStart"));
        soundNightEnd = SoundComponent.getComponentFromString(
                config.getString("soundNightEnd"));
        soundPlayerCaught = SoundComponent.getComponentFromString(
                config.getString("soundPlayerCaught"));
        soundChicaThreat = SoundComponent.getComponentFromString(
                config.getString("soundChicaThreat"));
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
     * @return secondsOfNightInterval
     */
    public int getSecondsOfNightInterval() {
        return secondsOfNightInterval;
    }

    /**
     * @return foxyMovementSeconds
     */
    public int getFoxyMovementSeconds() {
        return foxyMovementSeconds;
    }

    /**
     * @return batteryDecreasePerSecond
     */
    public double getBatteryDecreasePerSecond() {
        return batteryDecreasePerSecond;
    }

    /**
     * @return batteryFlashLightPerSecond
     */
    public double getBatteryFlashLightPerSecond() {
        return batteryFlashLightPerSecond;
    }

    /**
     * @return batteryRaderPerUse
     */
    public double getBatteryRaderPerUse() {
        return batteryRaderPerUse;
    }

    /**
     * @return batteryShutterPerSecond
     */
    public double getBatteryShutterPerSecond() {
        return batteryShutterPerSecond;
    }

    /**
     * @return raderSearchingRange
     */
    public int getRaderSearchingRange() {
        return raderSearchingRange;
    }

    /**
     * @return playerLogoutTrackingSeconds
     */
    public int getPlayerLogoutTrackingSeconds() {
        return playerLogoutTrackingSeconds;
    }

    /**
     * @return night1MoveSpeed
     */
    public FNAFIMConfigMoveSetting getNight1MoveSpeed() {
        return night1MoveSpeed;
    }

    /**
     * @return night2MoveSpeed
     */
    public FNAFIMConfigMoveSetting getNight2MoveSpeed() {
        return night2MoveSpeed;
    }

    /**
     * @return night3MoveSpeed
     */
    public FNAFIMConfigMoveSetting getNight3MoveSpeed() {
        return night3MoveSpeed;
    }

    /**
     * @return night4MoveSpeed
     */
    public FNAFIMConfigMoveSetting getNight4MoveSpeed() {
        return night4MoveSpeed;
    }

    /**
     * @return night5MoveSpeed
     */
    public FNAFIMConfigMoveSetting getNight5MoveSpeed() {
        return night5MoveSpeed;
    }

    /**
     * @return night6MoveSpeed
     */
    public FNAFIMConfigMoveSetting getNight6MoveSpeed() {
        return night6MoveSpeed;
    }

    /**
     * @return customNightMoveSpeed
     */
    public FNAFIMConfigMoveSetting getCustomNightMoveSpeed() {
        return customNightMoveSpeed;
    }

    /**
     *
     * @param night
     * @return
     */
    public FNAFIMConfigMoveSetting getMoveSpeed(Night night) {
        switch (night) {
        case NIGHT1:
            return night1MoveSpeed;
        case NIGHT2:
            return night2MoveSpeed;
        case NIGHT3:
            return night3MoveSpeed;
        case NIGHT4:
            return night4MoveSpeed;
        case NIGHT5:
            return night5MoveSpeed;
        case NIGHT6:
            return night6MoveSpeed;
        case NIGHT7:
            return customNightMoveSpeed;
        default:
            return night1MoveSpeed;
        }
    }

    /**
     * @return chicaThreatCooldownSeconds
     */
    public int getChicaThreatCooldownSeconds() {
        return chicaThreatCooldownSeconds;
    }

    /**
     * @return soundUseFlashLight
     */
    public SoundComponent getSoundUseFlashLight() {
        return soundUseFlashLight;
    }

    /**
     * @return soundUseRader
     */
    public SoundComponent getSoundUseRader() {
        return soundUseRader;
    }

    /**
     * @return soundUseShutter
     */
    public SoundComponent getSoundUseShutter() {
        return soundUseShutter;
    }

    /**
     * @return soundFoxyMovement
     */
    public SoundComponent getSoundFoxyMovement() {
        return soundFoxyMovement;
    }

    /**
     * @return soundFreddyTeleport
     */
    public SoundComponent getSoundFreddyTeleport() {
        return soundFreddyTeleport;
    }

    /**
     * @return soundNightStart
     */
    public SoundComponent getSoundNightStart() {
        return soundNightStart;
    }

    /**
     * @return soundNightEnd
     */
    public SoundComponent getSoundNightEnd() {
        return soundNightEnd;
    }

    /**
     * @return soundPlayerCaught
     */
    public SoundComponent getSoundPlayerCaught() {
        return soundPlayerCaught;
    }

    /**
     * @return soundChicaThreat
     */
    public SoundComponent getSoundChicaThreat() {
        return soundChicaThreat;
    }
}
