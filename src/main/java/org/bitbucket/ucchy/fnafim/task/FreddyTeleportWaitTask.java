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
    private String name;
    private int speed;

    public FreddyTeleportWaitTask(GameSession session, String name, int wait, int speed) {
        super(wait);
        this.session = session;
        this.name = name;
        this.speed = speed;
    }

    @Override
    public void run() {
        session.onFreddyTPWaitEnd(name, speed);
        isEnd = true;
    }
}
