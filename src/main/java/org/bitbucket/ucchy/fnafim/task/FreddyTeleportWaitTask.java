/*
 * @author     ucchy
 * @license    LGPLv3
 * @copyright  Copyright ucchy 2015
 */
package org.bitbucket.ucchy.fnafim.task;

import org.bitbucket.ucchy.fnafim.game.GameSession;

/**
 * Freddyがテレポートしたときに、行動可能になるまでのウエイトタスク
 * @author ucchy
 */
public class FreddyTeleportWaitTask extends GameSessionTask {

    private GameSession session;

    public FreddyTeleportWaitTask(GameSession session, int wait) {
        super(wait);
        this.session = session;
    }

    @Override
    public void run() {
        session.onFreddyTPWaitEnd();
        isEnd = true;
    }
}
