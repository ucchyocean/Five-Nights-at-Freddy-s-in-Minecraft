/*
 * @author     ucchy
 * @license    LGPLv3
 * @copyright  Copyright ucchy 2015
 */
package org.bitbucket.ucchy.fnafim.game;

import org.bitbucket.ucchy.fnafim.FNAFIMConfig;
import org.bitbucket.ucchy.fnafim.FiveNightsAtFreddysInMinecraft;
import org.bitbucket.ucchy.fnafim.Utility;
import org.bukkit.entity.Player;


/**
 * 電力管理クラス
 * @author ucchy
 */
public class PlayerBattery {

    private Player player;
    private GameSession session;
    private double power;
    private boolean isUsingFlashlight;
    private boolean isUsingShutter;
    private boolean isDown;

    private double batteryDecreasePerSecond;
    private double batteryFlashLightPerSecond;
    private double batteryRaderPerUse;
    private double batteryShutterPerSecond;

    /**
     * コンストラクタ
     * @param name
     * @param session
     */
    public PlayerBattery(String name, GameSession session) {
        this(Utility.getPlayerExact(name), session);
    }

    /**
     * コンストラクタ
     * @param player
     * @param session
     */
    public PlayerBattery(Player player, GameSession session) {
        this.player = player;
        this.session = session;
        power = 100;
        isUsingFlashlight = false;
        isUsingShutter = false;
        isDown = false;

        if ( player != null ) {
            player.setLevel(100);
            player.setExp(1.0f);
        }

        FNAFIMConfig config = FiveNightsAtFreddysInMinecraft.getInstance().getFNAFIMConfig();
        batteryDecreasePerSecond = config.getBatteryDecreasePerSecond();
        batteryFlashLightPerSecond = config.getBatteryFlashLightPerSecond();
        batteryRaderPerUse = config.getBatteryRaderPerUse();
        batteryShutterPerSecond = config.getBatteryShutterPerSecond();
    }

    /**
     * 1秒毎に呼び出されるメソッド
     */
    protected void onSeconds() {

        // 基本使用量
        power -= batteryDecreasePerSecond;

        // 懐中電灯使用量
        if ( isUsingFlashlight ) {
            power -= batteryFlashLightPerSecond;
        }

        // シャッター使用量
        if ( isUsingShutter ) {
            power -= batteryShutterPerSecond;
        }

        if ( power > 100 ) {
            power = 100;
        } else if ( power < 0 ) {
            power = 0;
        }

        refreshExpBar();

        if ( !isDown && power <= 0 ) {
            isDown = true;

            // 全ての道具の利用をやめる
            isUsingFlashlight = false;
            isUsingShutter = false;

            // 電力切れイベントを呼びだす
            session.onBatteryDown(player);
        }
    }

    /**
     * レーダーを使用した時に呼び出されるメソッド
     */
    protected void decreaseToUseRadar() {
        power -= batteryRaderPerUse;
        refreshExpBar();
    }

    /**
     * レーダーを使用可能な電力が残っているかどうかを確認するメソッド
     * @return
     */
    protected boolean hasPowerToUserRadar() {
        return power >= 1.0;
    }

    /**
     * @param isUsingFlashlight isUsingFlashlight
     */
    public void setUsingFlashlight(boolean isUsingFlashlight) {
        this.isUsingFlashlight = isUsingFlashlight;
    }

    /**
     * @param isUsingShutter isUsingShutter
     */
    public void setUsingShutter(boolean isUsingShutter) {
        this.isUsingShutter = isUsingShutter;
    }

    /**
     * 現在の電力を、Exp表示部分に反映する
     */
    private void refreshExpBar() {
        if ( player == null ) return;
        player.setLevel((int)power);
        float progress = (float)(power / 100);
        player.setExp(progress);
    }

    /**
     * 現在のバッテリー残量を返す
     * @return 現在のバッテリー残量
     */
    public double getPower() {
        return power;
    }
}
