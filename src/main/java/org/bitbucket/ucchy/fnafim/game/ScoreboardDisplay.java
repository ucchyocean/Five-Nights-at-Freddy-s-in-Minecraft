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
import org.bukkit.scoreboard.NameTagVisibility;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

/**
 * スコアボード表示クラス
 * @author ucchy
 */
public class ScoreboardDisplay {

    private static final String OBJECTIVE_NAME = "fnafim";

    private Scoreboard scoreboard;
    private String title;

    /**
     * コンストラクタ
     */
    public ScoreboardDisplay() {
        scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
        Objective sidebar = scoreboard.registerNewObjective(OBJECTIVE_NAME, "dummy");
        sidebar.setDisplaySlot(DisplaySlot.SIDEBAR);
        title = "";
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
        if ( !Utility.isCB180orLater() && title.length() > 32 ) {
            title = title.substring(0, 32);
        }
        Objective obj = scoreboard.getObjective(OBJECTIVE_NAME);
        if ( obj != null ) {
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
        Objective obj = scoreboard.getObjective(OBJECTIVE_NAME);
        if ( point == 0 ) {
            getScoreItem(obj, name).setScore(1); // NOTE: set temporary.
        }
        getScoreItem(obj, name).setScore(point);
    }

    /**
     * 項目にスコアを加算する。マイナスを指定すれば減算も可能。
     * @param name 項目名
     * @param amount 加算する値
     */
    public void addScore(String name, int amount) {
        Objective obj = scoreboard.getObjective(OBJECTIVE_NAME);
        Score score = getScoreItem(obj, name);
        int point = score.getScore();
        score.setScore(point + amount);
    }

    /**
     * スコアボードを削除する。
     */
    public void remove() {
        if ( scoreboard.getObjective(DisplaySlot.SIDEBAR) != null ) {
            scoreboard.getObjective(DisplaySlot.SIDEBAR).unregister();
        }
        scoreboard.clearSlot(DisplaySlot.SIDEBAR);
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
    public void setFreddysTeam(String name) {
        if ( name == null ) return;
        Team team = scoreboard.getTeam("Freddys");
        if ( team == null ) {
            team = scoreboard.registerNewTeam("Freddys");
            team.setPrefix(ChatColor.RED.toString());
            team.setSuffix(ChatColor.RESET.toString());
            if ( Utility.isCB180orLater() ) {
                team.setNameTagVisibility(NameTagVisibility.HIDE_FOR_OTHER_TEAMS);
            }
        }
        addTeamMember(team, name);
    }

    /**
     * プレイヤーをプレイヤーズチームに所属させる
     * @param player
     */
    public void setPlayersTeam(String name) {
        if ( name == null ) return;
        Team team = scoreboard.getTeam("Players");
        if ( team == null ) {
            team = scoreboard.registerNewTeam("Players");
            team.setPrefix(ChatColor.BLUE.toString());
            team.setSuffix(ChatColor.RESET.toString());
            if ( Utility.isCB180orLater() ) {
                team.setNameTagVisibility(NameTagVisibility.HIDE_FOR_OTHER_TEAMS);
            }
        }
        addTeamMember(team, name);
    }

    /**
     * プレイヤーをプレイヤーズチームから脱退させる
     * @param player
     */
    public void leavePlayersTeam(String name) {
        if ( name == null ) return;
        Team team = scoreboard.getTeam("Players");
        if ( team != null ) {
            removeTeamMember(team, name);
        }
    }

    /**
     * 役割を表示する
     * @param name 対象プレイヤー
     * @param role 役割
     * @deprecated スコアボード統一化により、使用不可
     */
    @Deprecated
    public void setRole(String name, String role) {
        if ( name == null ) return;
        Objective obj = scoreboard.getObjective(OBJECTIVE_NAME);
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

    @SuppressWarnings("deprecation")
    private void addTeamMember(Team team, String name) {
        if ( Utility.isCB186orLater() ) {
            team.addEntry(name);
        } else {
            Player player = Utility.getPlayerExact(name);
            if ( player != null && player.isOnline() ) {
                team.addPlayer(player);
            }
        }
    }

    @SuppressWarnings("deprecation")
    private void removeTeamMember(Team team, String name) {
        if ( Utility.isCB186orLater() ) {
            team.removeEntry(name);
        } else {
            Player player = Utility.getPlayerExact(name);
            if ( player != null && player.isOnline() ) {
                team.removePlayer(player);
            }
        }
    }
}
