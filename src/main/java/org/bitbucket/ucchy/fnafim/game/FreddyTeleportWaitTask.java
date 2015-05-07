/*
 * @author     ucchy
 * @license    LGPLv3
 * @copyright  Copyright ucchy 2015
 */
package org.bitbucket.ucchy.fnafim.game;

import org.bitbucket.ucchy.fnafim.FiveNightsAtFreddysInMinecraft;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * Freddyがテレポートしたときに、行動可能になるまでのウエイトタスク
 * @author ucchy
 */
public class FreddyTeleportWaitTask extends BukkitRunnable {

    private GameSession session;
    private boolean isEnd;
    private int wait;

    protected FreddyTeleportWaitTask(GameSession session) {
        this.session = session;
        isEnd = false;
    }

    @Override
    public void run() {
        session.onFreddyTPWaitEnd();
        isEnd = true;
    }

    public void start() {
        wait = (int)(Math.random() * 8) + 1;
        runTaskLater(FiveNightsAtFreddysInMinecraft.getInstance(), 20 * wait);
    }

    public void end() {
        cancel();
        isEnd = true;
    }

    public boolean isEnded() {
        return isEnd;
    }

    public int getWait() {
        return wait;
    }
}