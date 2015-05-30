/*
 * @author     ucchy
 * @license    LGPLv3
 * @copyright  Copyright ucchy 2015
 */
package org.bitbucket.ucchy.fnafim.task;

import org.bitbucket.ucchy.fnafim.game.GameSession;

/**
 * Freddyがバッテリーダウンしたプレイヤーのスカルを取得するまでのウエイトタスク
 * @author ucchy
 */
public class FreddyItemWaitTask extends GameSessionTask {

    private GameSession session;
    private String target;

    public FreddyItemWaitTask(GameSession session, String target, int wait) {
        super(wait);
        this.session = session;
        this.target = target;
    }

    @Override
    public void run() {
        session.onFreddyItemGet(target);
        isEnd = true;
    }
}
