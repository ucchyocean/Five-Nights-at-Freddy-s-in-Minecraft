/*
 * @author     ucchy
 * @license    LGPLv3
 * @copyright  Copyright ucchy 2015
 */
package org.bitbucket.ucchy.fnafim.game;

import org.bitbucket.ucchy.fnafim.Utility;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;

/**
 * スコアボード表示クラス
 * @author ucchy
 */
public class ScoreboardDisplay {

    private Scoreboard scoreboard;
    private Objective sidebar;

    /**
     * コンストラクタ
     */
    public ScoreboardDisplay(String name) {
        scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
        sidebar = scoreboard.registerNewObjective(name, "dummy");
        sidebar.setDisplaySlot(DisplaySlot.SIDEBAR);
    }

    /**
     * 指定されたプレイヤーを、このスコアボードの表示対象にする
     * @param player プレイヤー
     */
    public void setShowPlayer(Player player) {
        player.setScoreboard(scoreboard);
    }

    /**
     * サイドバーのタイトルを設定する。32文字以下にすること。
     * @param title タイトル
     */
    public void setTitle(String title) {
        if ( sidebar == null )
            return;
        if ( title.length() > 32 )
            title = title.substring(0, 32);
        sidebar.setDisplayName(title);
    }

    /**
     * スコア項目を設定する。項目名は16文字以下にすること。
     * @param name 項目名
     * @param point 項目のスコア
     */
    public void setScore(String name, int point) {
        if ( name.length() > 16 ) {
            name = name.substring(0, 16);
        }
        if ( point == 0 ) {
            getScoreItem(name).setScore(1); // NOTE: set temporary.
        }
        getScoreItem(name).setScore(point);
    }

    /**
     * 項目にスコアを加算する。マイナスを指定すれば減算も可能。
     * @param name 項目名
     * @param amount 加算する値
     */
    public void addScore(String name, int amount) {
        if ( name.length() > 16 ) {
            name = name.substring(0, 16);
        }
        int point = getScoreItem(name).getScore();
        setScore(name, point + amount);
    }

    /**
     * スコアボードを削除する。
     */
    public void remove() {
        scoreboard.clearSlot(DisplaySlot.SIDEBAR);
        sidebar.unregister();
        sidebar = null;
    }

    /**
     * スコア項目を削除する
     * @param name
     */
    @SuppressWarnings("deprecation")
    public void removeScores(String name) {
        getScoreItem(name).setScore(0);
        if ( Utility.isCB178orLater() ) {
            scoreboard.resetScores(name);
        } else {
            OfflinePlayer item = Bukkit.getOfflinePlayer(name);
            scoreboard.resetScores(item);
        }
    }

    /**
     * スコア項目を取得する
     * @param name
     * @return
     */
    @SuppressWarnings("deprecation")
    private Score getScoreItem(String name) {
        if ( Utility.isCB178orLater() ) {
            return sidebar.getScore(name);
        } else {
            return sidebar.getScore(Bukkit.getOfflinePlayer(name));
        }
    }

    /**
     * 残り時間を設定する
     * @param remain
     */
    public void setRemainTime(int remain) {
        setScore("残り時間", remain);
    }

    /**
     * 残りプレイヤーを設定する
     * @param remain
     */
    public void setRemainPlayer(int remain) {
        setScore("残りプレイヤー", remain);
    }
}
