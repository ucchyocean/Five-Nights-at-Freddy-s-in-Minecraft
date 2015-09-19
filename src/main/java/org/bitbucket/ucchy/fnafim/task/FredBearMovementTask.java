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
public class FredBearMovementTask extends GameSessionTask {

    private GameSession session;
    private BukkitRunnable inner;

    public FredBearMovementTask(GameSession session, int seconds) {
        super(seconds);
        this.session = session;
    }

    @Override
    public void run() {
        session.onFredBearMovementEnd();
        inner.cancel();
        setFredBearExpZero();
        isEnd = true;
    }

    @Override
    public void start() {
        runTaskLater(FiveNightsAtFreddysInMinecraft.getInstance(), 20 * seconds);

        inner = new BukkitRunnable() {
            public void run() {

                // FredBear を取得
                if ( session.getFredBear() == null ) return;
                Player fredbear = Utility.getPlayerExact(session.getFredBear());
                if ( fredbear == null || !fredbear.isOnline() ) return;

                // 近くに居るプレイヤーを検索
                double nearest = 999;
                for ( Entity entity :  fredbear.getNearbyEntities(10, 10, 10) ) {
                    if ( !(entity instanceof Player) ) {
                        continue;
                    }
                    Player target = (Player)entity;
                    if ( !session.isPlayer(target) ) {
                        continue;
                    }
                    double distanceSqrt = target.getLocation().distanceSquared(fredbear.getLocation());
                    if ( nearest > distanceSqrt ) {
                        nearest = distanceSqrt;
                    }
                }

                // 距離を経験値バーに反映
                double value = 10 - Math.sqrt(nearest);
                if ( value <= 0 ) {
                    fredbear.setExp(0);
                } else {
                    fredbear.setExp((float)value / 10f);
                }
            }
        };
        inner.runTaskTimer(FiveNightsAtFreddysInMinecraft.getInstance(), 3, 3);
    }

    @Override
    public void end() {
        cancel();
        inner.cancel();
        setFredBearExpZero();
        isEnd = true;
    }

    private void setFredBearExpZero() {
        if ( session.getFredBear() == null ) return;
        Player fredbear = Utility.getPlayerExact(session.getFredBear());
        if ( fredbear == null || !fredbear.isOnline() ) return;
        fredbear.setExp(0);
    }
}
