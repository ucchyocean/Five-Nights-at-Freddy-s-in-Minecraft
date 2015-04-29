/*
 * @author     ucchy
 * @license    LGPLv3
 * @copyright  Copyright ucchy 2015
 */
package org.bitbucket.ucchy.fnafim.game;

import org.bukkit.entity.Player;


/**
 * 電力管理クラス
 * @author ucchy
 */
public class PlayerBattery {

    private Player player;
    private double power;
    private boolean isUsingFlashlight;
    private boolean isUsingShutter;

    /**
     * コンストラクタ
     * @param player
     */
    public PlayerBattery(Player player) {
        this.player = player;
        power = 100;
        isUsingFlashlight = false;
        isUsingShutter = false;

        player.setLevel(100);
        player.setExp(1.0f);
    }

    /**
     * 1秒毎に呼び出されるメソッド
     */
    protected void onSeconds() {

        // 基本使用量
        power -= 1/7;

        // 懐中電灯使用量
        if ( isUsingFlashlight ) {
            power -= 1/5;
        }

        // シャッター使用量
        if ( isUsingShutter ) {
            power -= 1/2;
        }

        refreshExpBar();
    }

    /**
     * レーダーを使用した時に呼び出されるメソッド
     */
    protected void decreaseToUseRadar() {
        power -= 1.0;
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

        if ( power > 100 ) {
            power = 100;
        } else if ( power < 0 ) {
            power = 0;
            // TODO 全ての道具の利用をやめる
            // TODO プレイヤーを停止させる
        }
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
