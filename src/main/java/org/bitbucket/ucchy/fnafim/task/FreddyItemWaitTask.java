/*
 * @author     ucchy
 * @license    LGPLv3
 * @copyright  Copyright ucchy 2015
 */
package org.bitbucket.ucchy.fnafim.task;

import org.bitbucket.ucchy.fnafim.FiveNightsAtFreddysInMinecraft;
import org.bitbucket.ucchy.fnafim.game.GameSession;
import org.bukkit.Location;
import org.bukkit.entity.Player;

/**
 * Freddyがバッテリーダウンしたプレイヤーのスカルを取得するまでのウエイトタスク
 * @author ucchy
 */
public class FreddyItemWaitTask extends GameSessionTask {

    private GameSession session;
    private Player target;
    private Location location;

    public FreddyItemWaitTask(GameSession session, Player target, int wait) {
        super(wait);
        this.session = session;
        this.target = target;
    }

    @Override
    public void run() {
        if ( location == null ) {
            location = target.getLocation();
        } else if ( !isSameDirection(location, target.getLocation()) ) {
            session.onFreddyItemGet(target.getName());
            session.onFredBearItemGet(target.getName());
            isEnd = true;
            end();
        }
    }

    @Override
    public void start() {
        runTaskTimer(FiveNightsAtFreddysInMinecraft.getInstance(), 20 * seconds, 20);
    }

    private static boolean isSameDirection(Location loc1, Location loc2) {
        return loc1.getYaw() == loc2.getYaw() && loc1.getPitch() == loc2.getPitch();
    }
}
