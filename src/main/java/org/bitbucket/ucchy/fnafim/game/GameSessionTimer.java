package org.bitbucket.ucchy.fnafim.game;

import org.bitbucket.ucchy.fnafim.FiveNightsAtFreddysInMinecraft;
import org.bukkit.scheduler.BukkitRunnable;

public class GameSessionTimer extends BukkitRunnable {

    private GameSession parent;
    private int remain;

    public GameSessionTimer(GameSession parent, int max) {
        this.parent = parent;
        this.remain = max;
    }

    @Override
    public void run() {
        remain -= 1;
        parent.onTimerSeconds(remain);

        if ( remain <= 0 ) {
            cancel();
            parent.onTimerZero();
        }
    }

    public void start() {
        runTaskTimer(FiveNightsAtFreddysInMinecraft.getInstance(), 0, 20);
    }

    public void end() {
        cancel();
        parent.onTimerCanceled();
    }
}
