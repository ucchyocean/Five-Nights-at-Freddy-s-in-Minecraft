/*
 * @author     ucchy
 * @license    LGPLv3
 * @copyright  Copyright ucchy 2015
 */
package org.bitbucket.ucchy.fnafim.task;

import org.bitbucket.ucchy.fnafim.FiveNightsAtFreddysInMinecraft;
import org.bitbucket.ucchy.fnafim.Messages;
import org.bitbucket.ucchy.fnafim.game.GameSession;
import org.bitbucket.ucchy.fnafim.game.GameSessionPhase;
import org.bitbucket.ucchy.fnafim.game.Night;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

/**
 * 自動開始タイマークラス
 * @author ucchy
 */
public class AutoStartTimerTask extends GameSessionTask {

    private int secondsLeft;
    private GameSession session;

    /**
     * コンストラクタ
     * @param seconds 秒数
     */
    public AutoStartTimerTask(int seconds, GameSession session) {
        super(seconds);
        this.session = session;
        this.secondsLeft = seconds - 1;
    }

    /**
     * 1秒毎に呼び出されるメソッド
     */
    @Override
    public void run() {

        if ( secondsLeft > 0 ) {
            secondsLeft--;

            // キリのいいところで通知を送る
            if ( secondsLeft == 30 || secondsLeft == 15 || secondsLeft == 5 ) {
                String message = Messages.get("Announce_AutoStartTimerCount", "%seconds", secondsLeft);
                session.sendInGameAnnounce(message);
            }

            // JoinSignを更新する
            FiveNightsAtFreddysInMinecraft.getInstance().getJoinsignManager().updateAll();

        } else {
            secondsLeft = -1;

            // 自身のタスクを削除して、ゲームを開始する。
            this.cancel();
            doTimerEndAction();
        }
    }

    @Override
    public void start() {
        runTaskTimer(FiveNightsAtFreddysInMinecraft.getInstance(), 20, 20);
    }

    public int getSecondsLeft() {
        return secondsLeft;
    }

    public void setSecondsLeft(int secondsLeft) {
        this.secondsLeft = secondsLeft;
    }

    /**
     * タイマー終了時処理を実行する
     */
    private void doTimerEndAction() {

        CommandSender sender = Bukkit.getConsoleSender();

        // 既にゲームが開始中ならエラー
        if ( session.getPhase() != GameSessionPhase.INVITATION ) {
            sendErrorMessage(sender, Messages.get("Error_AlreadyStartedCannotStart"));
            return;
        }

        // ゲームを開始する。
        session.startPreparing(Night.NIGHT1);

        // 自動開始タイマーを削除する
        session.removeAutoStartTimer();
    }

    private void sendErrorMessage(CommandSender sender, String message) {
        String msg = Messages.get("Prefix_Error") + message;
        sender.sendMessage(msg);
    }
}
