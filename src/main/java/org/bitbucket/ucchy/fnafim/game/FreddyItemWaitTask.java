/*
 * @author     ucchy
 * @license    LGPLv3
 * @copyright  Copyright ucchy 2015
 */
package org.bitbucket.ucchy.fnafim.game;

import org.bitbucket.ucchy.fnafim.FiveNightsAtFreddysInMinecraft;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * Freddyがバッテリーダウンしたプレイヤーのスカルを取得するまでのウエイトタスク
 * @author ucchy
 */
public class FreddyItemWaitTask extends BukkitRunnable {

    private GameSession session;
    private Player target;
    private boolean isEnd;

    protected FreddyItemWaitTask(GameSession session, Player target) {
        this.session = session;
        this.target = target;
        isEnd = false;
    }

    @Override
    public void run() {
        session.onFreddyItemGet(target);
        isEnd = true;
    }

    public void start() {
        int wait = (int)(Math.random() * 8) + 1;
        runTaskLater(FiveNightsAtFreddysInMinecraft.getInstance(), 20 * wait);
    }

    public void end() {
        cancel();
        isEnd = true;
    }

    public boolean isEnded() {
        return isEnd;
    }
}
