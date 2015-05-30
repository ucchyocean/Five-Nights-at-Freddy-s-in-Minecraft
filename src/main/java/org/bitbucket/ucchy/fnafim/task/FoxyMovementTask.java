/*
 * @author     ucchy
 * @license    LGPLv3
 * @copyright  Copyright ucchy 2015
 */
package org.bitbucket.ucchy.fnafim.task;

import org.bitbucket.ucchy.fnafim.game.GameSession;

/**
 * Foxyの行動タスク
 * @author ucchy
 */
public class FoxyMovementTask extends GameSessionTask {

    private GameSession session;

    public FoxyMovementTask(GameSession session, int seconds) {
        super(seconds);
        this.session = session;
    }

    @Override
    public void run() {
        session.onFoxyMovementEnd();
        isEnd = true;
    }
}
