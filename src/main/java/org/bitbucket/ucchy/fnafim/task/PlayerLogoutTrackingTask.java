/*
 * @author     ucchy
 * @license    LGPLv3
 * @copyright  Copyright ucchy 2015
 */
package org.bitbucket.ucchy.fnafim.task;

import org.bitbucket.ucchy.fnafim.Utility;
import org.bitbucket.ucchy.fnafim.game.GameSession;
import org.bukkit.entity.Player;

/**
 * ログアウト逃げ対策タスク。
 * プレイヤーがゲーム中にログアウトしたあと、設定時刻を過ぎると、自動的に負けにする。
 * @author ucchy
 */
public class PlayerLogoutTrackingTask extends GameSessionTask {

    private GameSession session;
    private String name;

    public PlayerLogoutTrackingTask(GameSession session, String name, int seconds) {
        super(seconds);
        this.session = session;
        this.name = name;
    }

    /**
     * @see java.lang.Runnable#run()
     */
    @Override
    public void run() {
        Player player = Utility.getPlayerExact(name);
        if ( player == null || !player.isOnline() ) {
            session.onCaughtPlayer(name, null);
        }
        isEnd = true;
    }

}
