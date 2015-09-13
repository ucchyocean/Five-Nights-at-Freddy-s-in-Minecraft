/*
 * @author     ucchy
 * @license    LGPLv3
 * @copyright  Copyright ucchy 2015
 */
package org.bitbucket.ucchy.fnafim.task;

import org.bitbucket.ucchy.fnafim.FiveNightsAtFreddysInMinecraft;
import org.bitbucket.ucchy.fnafim.Utility;
import org.bitbucket.ucchy.fnafim.game.GameSession;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * Foxyの行動タスク
 * @author ucchy
 */
public class FoxyMovementTask extends GameSessionTask {

    private GameSession session;
    private BukkitRunnable inner;

    public FoxyMovementTask(GameSession session, int seconds) {
        super(seconds);
        this.session = session;
    }

    @Override
    public void run() {
        session.onFoxyMovementEnd();
        inner.cancel();
        setFoxyExpZero();
        isEnd = true;
    }

    @Override
    public void start() {
        runTaskLater(FiveNightsAtFreddysInMinecraft.getInstance(), 20 * seconds);

        inner = new BukkitRunnable() {
            public void run() {

                // Foxy を取得
                if ( session.getFoxy() == null ) return;
                Player foxy = Utility.getPlayerExact(session.getFoxy());
                if ( foxy == null ) return;

                // 近くに居るプレイヤーを検索
                double nearest = 999;
                for ( Entity entity :  foxy.getNearbyEntities(10, 10, 10) ) {
                    if ( !(entity instanceof Player) ) {
                        continue;
                    }
                    Player target = (Player)entity;
                    if ( !session.isPlayer(target) ) {
                        continue;
                    }
                    double distanceSqrt = target.getLocation().distanceSquared(foxy.getLocation());
                    if ( nearest > distanceSqrt ) {
                        nearest = distanceSqrt;
                    }
                }

                // 距離を経験値バーに反映
                double value = 10 - Math.sqrt(nearest);
                if ( value <= 0 ) {
                    foxy.setExp(0);
                } else {
                    foxy.setExp((float)value / 10f);
                }
            }
        };
        inner.runTaskTimer(FiveNightsAtFreddysInMinecraft.getInstance(), 3, 3);
    }

    @Override
    public void end() {
        cancel();
        inner.cancel();
        setFoxyExpZero();
        isEnd = true;
    }

    private void setFoxyExpZero() {
        if ( session.getFoxy() == null ) return;
        Player foxy = Utility.getPlayerExact(session.getFoxy());
        if ( foxy == null ) {
            return;
        }
        foxy.setExp(0);
    }
}
