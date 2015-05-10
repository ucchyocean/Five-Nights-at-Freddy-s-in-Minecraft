/*
 * @author     ucchy
 * @license    LGPLv3
 * @copyright  Copyright ucchy 2015
 */
package org.bitbucket.ucchy.fnafim.game;

import org.bitbucket.ucchy.fnafim.Messages;
import org.bitbucket.ucchy.fnafim.Utility;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

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
     * サイドバーのタイトルを取得する。
     * @return サイドバーのタイトル
     */
    public String getTitle() {
        return sidebar.getDisplayName();
    }

    /**
     * スコア項目を設定する。項目名は16文字以下にすること。
     * @param name 項目名
     * @param point 項目のスコア
     */
    public void setScore(String name, int point) {
        if ( sidebar == null ) {
            return;
        }
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
        if ( sidebar == null ) {
            return;
        }
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
     * チームを取得する
     * @param name チーム名
     * @param color チームカラー
     * @return チーム
     */
    public Team getTeam(String name, String color) {
        Team team = scoreboard.getTeam(name);
        if ( team == null ) {
            team = scoreboard.registerNewTeam(name);
            team.setPrefix(color);
            team.setSuffix(ChatColor.RESET.toString());
        }
        return team;
    }

    /**
     * スコア項目を削除する
     * @param name
     */
    @SuppressWarnings("deprecation")
    public void removeScores(String name) {
        if ( sidebar == null ) {
            return;
        }
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
        if ( sidebar == null ) {
            return null;
        }
        if ( Utility.isCB178orLater() ) {
            return sidebar.getScore(name);
        } else {
            return sidebar.getScore(Bukkit.getOfflinePlayer(name));
        }
    }

    // 以下、FNAFIM用のメソッド

    /**
     * プレイヤーをフレディチームに所属させる
     * @param player
     */
    public void setFreddysTeam(Player player) {
        getTeam("Freddys", ChatColor.RED.toString()).addPlayer(player);
    }

    /**
     * プレイヤーをプレイヤーズチームに所属させる
     * @param player
     */
    public void setPlayersTeam(Player player) {
        getTeam("Players", ChatColor.BLUE.toString()).addPlayer(player);
    }

    /**
     * プレイヤーをプレイヤーズチームから脱退させる
     * @param player
     */
    public void leavePlayersTeam(Player player) {
        getTeam("Players", ChatColor.BLUE.toString()).removePlayer(player);
    }

    /**
     * 残り時間を設定する
     * @param remain
     */
    public void setRemainTime(Night night, int hour) {
        String title = Messages.get("Sidebar_Title",
                new String[]{"%night", "%hour"},
                new String[]{night.toString(), String.format("%2d", hour)});
        if ( !title.equals(getTitle()) ) {
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
