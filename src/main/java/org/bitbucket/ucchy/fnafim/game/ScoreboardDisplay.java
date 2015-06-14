/*
 * @author     ucchy
 * @license    LGPLv3
 * @copyright  Copyright ucchy 2015
 */
package org.bitbucket.ucchy.fnafim.game;

import java.util.HashMap;

import org.bitbucket.ucchy.fnafim.Messages;
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

    private static final String OBJECTIVE_NAME = "fnafim";

    private HashMap<String, Scoreboard> scoreboards;
    private String title;

    /**
     * コンストラクタ
     */
    public ScoreboardDisplay() {
        scoreboards = new HashMap<String, Scoreboard>();
        title = "";
    }

    /**
     * 指定されたプレイヤーを、このスコアボードの表示対象にする
     * @param player プレイヤー
     */
    public void setShowPlayer(Player player) {
        Scoreboard scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
        Objective sidebar = scoreboard.registerNewObjective(OBJECTIVE_NAME, "dummy");
        sidebar.setDisplaySlot(DisplaySlot.SIDEBAR);
        player.setScoreboard(scoreboard);
        scoreboards.put(player.getName(), scoreboard);
    }

    /**
     * サイドバーのタイトルを設定する。32文字以下にすること。
     * @param title タイトル
     */
    public void setTitle(String title) {
        if ( !Utility.isCB180orLater() && title.length() > 32 ) {
            title = title.substring(0, 32);
        }
        for ( Scoreboard sb : scoreboards.values() ) {
            Objective obj = sb.getObjective(OBJECTIVE_NAME);
            if ( obj == null ) continue;
            obj.setDisplayName(title);
        }
        this.title = title;
    }

    /**
     * スコア項目を設定する。項目名は16文字以下にすること。
     * @param name 項目名
     * @param point 項目のスコア
     */
    public void setScore(String name, int point) {
        for ( Scoreboard sb : scoreboards.values() ) {
            Objective obj = sb.getObjective(OBJECTIVE_NAME);
            if ( point == 0 ) {
                getScoreItem(obj, name).setScore(1); // NOTE: set temporary.
            }
            getScoreItem(obj, name).setScore(point);
        }
    }

    /**
     * 項目にスコアを加算する。マイナスを指定すれば減算も可能。
     * @param name 項目名
     * @param amount 加算する値
     */
    public void addScore(String name, int amount) {
        for ( Scoreboard sb : scoreboards.values() ) {
            Objective obj = sb.getObjective(OBJECTIVE_NAME);
            Score score = getScoreItem(obj, name);
            int point = score.getScore();
            score.setScore(point + amount);
        }
    }

    /**
     * スコアボードを削除する。
     */
    public void remove() {
        for ( Scoreboard scoreboard : scoreboards.values() ) {
            if ( scoreboard.getObjective(DisplaySlot.SIDEBAR) != null ) {
                scoreboard.getObjective(DisplaySlot.SIDEBAR).unregister();
            }
            scoreboard.clearSlot(DisplaySlot.SIDEBAR);
        }
    }

    /**
     * スコア項目を削除する
     * @param name
     */
    @SuppressWarnings("deprecation")
    public void removeScores(Scoreboard scoreboard, String name) {
        if ( Utility.isCB178orLater() ) {
            scoreboard.resetScores(name);
        } else {
            OfflinePlayer item = Bukkit.getOfflinePlayer(name);
            scoreboard.resetScores(item);
        }
    }

    /**
     * スコア項目を取得する
     * @param obj
     * @param name
     * @return
     */
    @SuppressWarnings("deprecation")
    private Score getScoreItem(Objective obj, String name) {
        if ( !Utility.isCB180orLater() && name.length() > 16 ) {
            name = name.substring(0, 16);
        }
        if ( Utility.isCB178orLater() ) {
            return obj.getScore(name);
        } else {
            return obj.getScore(Bukkit.getOfflinePlayer(name));
        }
    }

    // 以下、FNAFIM用のメソッド

    /**
     * プレイヤーをフレディチームに所属させる
     * @param player
     */
    public void setFreddysTeam(Player player) {
//        getTeam("Freddys", ChatColor.RED.toString()).addPlayer(player);
    }

    /**
     * プレイヤーをプレイヤーズチームに所属させる
     * @param player
     */
    public void setPlayersTeam(Player player) {
//        getTeam("Players", ChatColor.BLUE.toString()).addPlayer(player);
    }

    /**
     * プレイヤーをプレイヤーズチームから脱退させる
     * @param player
     */
    public void leavePlayersTeam(Player player) {
//        getTeam("Players", ChatColor.BLUE.toString()).removePlayer(player);
    }

    /**
     * 役割を表示する
     * @param name 対象プレイヤー
     * @param role 役割
     */
    public void setRole(String name, String role) {
        if ( !scoreboards.containsKey(name) ) {
            return;
        }
        Objective obj = scoreboards.get(name).getObjective(OBJECTIVE_NAME);
        String msg = Messages.get("Sidebar_Role", "%role", role);
        getScoreItem(obj, msg).setScore(1);
        getScoreItem(obj, msg).setScore(0);
    }

    /**
     * 残り時間を設定する
     * @param remain
     */
    public void setRemainTime(Night night, int hour) {
        String title = Messages.get("Sidebar_Title",
                new String[]{"%night", "%hour"},
                new String[]{night.toString(), String.format("%2d", hour)});
        if ( !title.equals(this.title) ) {
            setTitle(title);
        }
    }

    /**
     * 残りプレイヤーを設定する
     * @param remain
     */
    public void setRemainPlayer(int remain) {
        setScore(Messages.get("Sidebar_RemainingPlayers"), remain);
    }
}
