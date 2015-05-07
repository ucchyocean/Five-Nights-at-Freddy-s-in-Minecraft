/*
 * @author     ucchy
 * @license    LGPLv3
 * @copyright  Copyright ucchy 2013
 */
package org.bitbucket.ucchy.fnafim;

import java.util.HashMap;
import java.util.concurrent.ArrayBlockingQueue;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * 遅延つきテレポート実行タスク
 * @author ucchy
 */
public class DelayedTeleportTask extends BukkitRunnable {

    private HashMap<Player, Location> locationMap;
    private ArrayBlockingQueue<Player> players;
    private int delay;

    /**
     * コンストラクタ
     * @param locationMap
     * @param delay
     * @param packetDelay
     */
    public DelayedTeleportTask(HashMap<Player, Location> locationMap, int delay) {
        this.locationMap = locationMap;
        this.delay = delay;

        players = new ArrayBlockingQueue<Player>(locationMap.size());
        for ( Player p : locationMap.keySet() ) {
            players.add(p);
        }
    }

    /**
     * タスクを開始する
     */
    public void startTask() {
        runTaskTimer(FiveNightsAtFreddysInMinecraft.getInstance(), delay, delay);
    }

    /**
     * @see java.lang.Runnable#run()
     */
    @Override
    public void run() {

        if ( players.isEmpty() ) {
            // 自己キャンセル
            cancel();
            return;
        }

        Player player = players.poll();
        Location location = locationMap.get(player);
        if ( player != null && location != null ) {
            location = location.clone().add(0.5, 0, 0.5);
            player.teleport(location, TeleportCause.PLUGIN);
        }
    }

}
