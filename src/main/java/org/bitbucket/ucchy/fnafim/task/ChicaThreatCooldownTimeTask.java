/*
 * @author     ucchy
 * @license    LGPLv3
 * @copyright  Copyright ucchy 2015
 */
package org.bitbucket.ucchy.fnafim.task;

import org.bitbucket.ucchy.fnafim.game.GameSession;

/**
 * Chicaが威嚇を行ってから、次の威嚇ができるようになるまでのクールダウンタイム処理タスク
 * @author ucchy
 */
public class ChicaThreatCooldownTimeTask extends GameSessionTask {

    private GameSession session;
    private String name;

    public ChicaThreatCooldownTimeTask(GameSession session, String name, int seconds) {
        super(seconds);
        this.session = session;
        this.name = name;
    }

    @Override
    public void run() {
        session.giveThreatItemToChica(name);
        isEnd = true;
    }
}
