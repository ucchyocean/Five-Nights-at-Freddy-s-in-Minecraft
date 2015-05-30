/*
 * @author     ucchy
 * @license    LGPLv3
 * @copyright  Copyright ucchy 2015
 */
package org.bitbucket.ucchy.fnafim.task;

import org.bitbucket.ucchy.fnafim.FiveNightsAtFreddysInMinecraft;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * ゲーム内で実行される各種タスク処理
 * @author ucchy
 */
public abstract class GameSessionTask extends BukkitRunnable {

    protected boolean isEnd;
    private int seconds;

    public GameSessionTask(int seconds) {
        this.seconds = seconds;
        isEnd = false;
    }

    public void start() {
        runTaskLater(FiveNightsAtFreddysInMinecraft.getInstance(), 20 * seconds);
    }

    public void end() {
        cancel();
        isEnd = true;
    }

    public boolean isEnded() {
        return isEnd;
    }
}