/*
 * @author     ucchy
 * @license    LGPLv3
 * @copyright  Copyright ucchy 2015
 */
package org.bitbucket.ucchy.fnafim;

import java.util.ArrayList;
import java.util.List;

import org.bitbucket.ucchy.fnafim.game.GameSession;
import org.bitbucket.ucchy.fnafim.game.GameSessionPhase;
import org.bitbucket.ucchy.fnafim.game.Night;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

/**
 * コマンドクラス
 * @author ucchy
 */
public class FNAFIMCommand implements TabExecutor {

    private static final String PERMISSION_PREFIX = "fnafim.";

    /**
     * コマンド実行時に呼び出されるメソッド
     */
    @Override
    public boolean onCommand(
            CommandSender sender, Command command, String label, String[] args) {

        // 引数なしは、ヘルプを表示
        if (args.length == 0) {
            return false;
        }

        // 第1引数に指定されたコマンドを実行する
        if ( args[0].equalsIgnoreCase("join") ) {
            joinCommand(sender, command, label, args);
            return true;
        } else if ( args[0].equalsIgnoreCase("leave") ) {
            leaveCommand(sender, command, label, args);
            return true;
        } else if ( args[0].equalsIgnoreCase("open") ) {
            openCommand(sender, command, label, args);
            return true;
        } else if ( args[0].equalsIgnoreCase("close") ) {
            closeCommand(sender, command, label, args);
            return true;
        } else if ( args[0].equalsIgnoreCase("start") ) {
            startCommand(sender, command, label, args);
            return true;
        } else if ( args[0].equalsIgnoreCase("cancel") ) {
            cancelCommand(sender, command, label, args);
            return true;
        } else if ( args[0].equalsIgnoreCase("set") ) {
            setCommand(sender, command, label, args);
            return true;
        } else if ( args[0].equalsIgnoreCase("reload") ) {
            reloadCommand(sender, command, label, args);
            return true;
        }
        return false;
    }

    /**
     * TABキー補完が実行されたときに呼び出されるメソッド
     */
    @Override
    public List<String> onTabComplete(
            CommandSender sender, Command command, String label, String[] args) {

        if ( args.length == 1 ) {
            String pre = args[0].toLowerCase();
            List<String> candidates = new ArrayList<String>();
            for ( String c : new String[]{"join", "leave", "open",
                    "close", "start", "cancel", "set", "reload"} ) {
                if ( c.startsWith(pre) && sender.hasPermission(PERMISSION_PREFIX + c) ) {
                    candidates.add(c);
                }
            }
            return candidates;
        }

        if ( args.length == 2 && args[0].equalsIgnoreCase("start") ) {
            String pre = args[1].toLowerCase();
            List<String> candidates = new ArrayList<String>();
            for ( Night night : Night.values() ) {
                if ( night.toString().startsWith(pre) ) {
                    candidates.add(night.toString());
                }
            }
            return candidates;
        }

        if ( args.length == 2 && args[0].equalsIgnoreCase("set") ) {
            String pre = args[1].toLowerCase();
            List<String> candidates = new ArrayList<String>();
            for ( String c : new String[]{"lobby", "player", "spectate",
                    "freddy", "chica", "bonnie", "foxy"} ) {
                if ( c.startsWith(pre) ) {
                    candidates.add(c);
                }
            }
            return candidates;
        }

        return null;
    }

    private void joinCommand(
            CommandSender sender, Command command, String label, String[] args) {

        // パーミッションチェック
        if ( !sender.hasPermission(PERMISSION_PREFIX + "join") ) {
            sendErrorMessage(sender, "パーミッションが無いため実行できません。");
            return;
        }

        // プレイヤーでないならエラー
        if ( !(sender instanceof Player) ) {
            sendErrorMessage(sender, "このコマンドはゲーム内から実行してください。");
            return;
        }

        Player player = (Player)sender;

        // ゲームセッションが無いならエラー
        GameSession session = FiveNightsAtFreddysInMinecraft.getInstance().getGameSession();
        if ( session == null ) {
            sendErrorMessage(sender, "募集中のゲームがありません。");
            return;
        }

        // 既にゲームが開始中ならエラー
        if ( session.getPhase() != GameSessionPhase.INVITATION ) {
            sendErrorMessage(sender, "既にゲームが開始中のため、参加できません。");
            return;
        }

        // 既に参加中ならエラー
        if ( session.isEntrant(player) ) {
            sendErrorMessage(sender, "あなたは既にゲームに参加中です。");
            return;
        }

        // 参加する。
        session.joinEntrant(player);
        sendInformationMessage(sender, "ゲームに参加しました。");
    }

    private void leaveCommand(
            CommandSender sender, Command command, String label, String[] args) {

        // パーミッションチェック
        if ( !sender.hasPermission(PERMISSION_PREFIX + "leave") ) {
            sendErrorMessage(sender, "パーミッションが無いため実行できません。");
            return;
        }

        // プレイヤーでないならエラー
        if ( !(sender instanceof Player) ) {
            sendErrorMessage(sender, "このコマンドはゲーム内から実行してください。");
            return;
        }

        Player player = (Player)sender;

        // ゲームセッションが無いならエラー
        GameSession session = FiveNightsAtFreddysInMinecraft.getInstance().getGameSession();
        if ( session == null ) {
            sendErrorMessage(sender, "募集中のゲームがありません。");
            return;
        }

        // 既にゲームが開始中ならエラー
        if ( session.getPhase() != GameSessionPhase.INVITATION ) {
            sendErrorMessage(sender, "既にゲームが開始中のため、離脱できません。");
            return;
        }

        // 参加していないならエラー
        if ( !session.isEntrant(player) ) {
            sendErrorMessage(sender, "あなたはゲームに参加していません。");
            return;
        }

        // 離脱する。
        session.leaveEntrant(player);
        sendInformationMessage(sender, "ゲームから離脱しました。");
    }

    private void openCommand(
            CommandSender sender, Command command, String label, String[] args) {

        // パーミッションチェック
        if ( !sender.hasPermission(PERMISSION_PREFIX + "open") ) {
            sendErrorMessage(sender, "パーミッションが無いため実行できません。");
            return;
        }

        // ゲームセッションがあるならエラー
        GameSession session = FiveNightsAtFreddysInMinecraft.getInstance().getGameSession();
        if ( session != null ) {
            sendErrorMessage(sender, "既にゲームがあるため、募集を開始できません。");
            return;
        }

        // ロケーションが一つでも設定されていないならエラー
        String lost = FiveNightsAtFreddysInMinecraft.getInstance()
                .getLocationManager().getNullLocationName();
        if ( lost != null ) {
            sendErrorMessage(sender, "地点 " + lost + " が未登録のままです。"
                    + "先に /fnaf set " + lost + " を実行して、地点登録を済ませてください。");
            return;
        }

        // ゲームセッションを作成して、募集を開始する。
        session = FiveNightsAtFreddysInMinecraft.getInstance().createNewGameSession(sender);
        sendInformationMessage(sender, "募集を開始しました。");
    }

    private void closeCommand(
            CommandSender sender, Command command, String label, String[] args) {

        // パーミッションチェック
        if ( !sender.hasPermission(PERMISSION_PREFIX + "close") ) {
            sendErrorMessage(sender, "パーミッションが無いため実行できません。");
            return;
        }

        // ゲームセッションが無いならエラー
        GameSession session = FiveNightsAtFreddysInMinecraft.getInstance().getGameSession();
        if ( session == null ) {
            sendErrorMessage(sender, "募集中のゲームがありません。");
            return;
        }

        // 既にゲームが開始中ならエラー
        if ( session.getPhase() != GameSessionPhase.INVITATION ) {
            sendErrorMessage(sender, "既にゲームが開始中のため、募集を中断できません。");
            return;
        }

        // 募集を中断する。
        session.closeInvitation(sender);
        sendInformationMessage(sender, "募集を中断しました。");
    }

    private void startCommand(
            CommandSender sender, Command command, String label, String[] args) {

        // パーミッションチェック
        if ( !sender.hasPermission(PERMISSION_PREFIX + "start") ) {
            sendErrorMessage(sender, "パーミッションが無いため実行できません。");
            return;
        }

        // ゲームセッションが無いならエラー
        GameSession session = FiveNightsAtFreddysInMinecraft.getInstance().getGameSession();
        if ( session == null ) {
            sendErrorMessage(sender, "募集中のゲームがありません。"
                    + "先に /fnaf open を実行して、参加者を募集してください。");
            return;
        }

        // 既にゲームが開始中ならエラー
        if ( session.getPhase() != GameSessionPhase.INVITATION ) {
            sendErrorMessage(sender, "既にゲームが開始中のため、開始できません。");
            return;
        }

        // 引数チェック。無効な内容ならエラー
        Night night = Night.NIGHT1;
        if ( args.length >= 2 ) {
            night = Night.getNightFromString(args[1]);
            if ( night == null ) {
                sendErrorMessage(sender, "指定されたパラメータ " + args[1] + " が無効です。"
                        + "night1 から night7 までのいずれかを指定してください。");
                return;
            }
        }

        // ゲームを開始する。
        session.startPreparing(night);
        sendInformationMessage(sender, "ゲームを開始しました。");
    }

    private void cancelCommand(
            CommandSender sender, Command command, String label, String[] args) {

        // パーミッションチェック
        if ( !sender.hasPermission(PERMISSION_PREFIX + "cancel") ) {
            sendErrorMessage(sender, "パーミッションが無いため実行できません。");
            return;
        }

        // ゲームセッションが無いならエラー
        GameSession session = FiveNightsAtFreddysInMinecraft.getInstance().getGameSession();
        if ( session == null ) {
            sendErrorMessage(sender, "実行中のゲームがありません。");
            return;
        }

        // ゲームが開始していないならエラー
        if ( session.getPhase() != GameSessionPhase.IN_GAME ) {
            sendErrorMessage(sender, "ゲームは開始していません。");
            return;
        }

        // ゲームを強制中断する。
        session.cancelGame();
        sendInformationMessage(sender, "ゲームを強制中断しました。");
    }

    private void setCommand(
            CommandSender sender, Command command, String label, String[] args) {

        // パーミッションチェック
        if ( !sender.hasPermission(PERMISSION_PREFIX + "set") ) {
            sendErrorMessage(sender, "パーミッションが無いため実行できません。");
            return;
        }

        // プレイヤーでないならエラー
        if ( !(sender instanceof Player) ) {
            sendErrorMessage(sender, "このコマンドはゲーム内から実行してください。");
            return;
        }

        Player player = (Player)sender;

        // 引数チェック。無効ならエラー
        if ( args.length <= 1 || (
                !args[1].equalsIgnoreCase("lobby") && !args[1].equalsIgnoreCase("player")
                && !args[1].equalsIgnoreCase("spectate") && !args[1].equalsIgnoreCase("freddy")
                && !args[1].equalsIgnoreCase("chica") && !args[1].equalsIgnoreCase("bonnie")
                && !args[1].equalsIgnoreCase("foxy") ) ) {
            sendErrorMessage(sender, "指定されたパラメータ " + args[1] + " が無効です。");
            return;
        }

        // リスポーン地点を登録する
        LocationManager manager = FiveNightsAtFreddysInMinecraft.getInstance().getLocationManager();
        if ( args[1].equalsIgnoreCase("lobby") ) {
            manager.setLobby(player.getLocation());
            sendInformationMessage(sender, "ロビーを登録しました。");
        } else if ( args[1].equalsIgnoreCase("player") ) {
            manager.setPlayer(player.getLocation());
            sendInformationMessage(sender, "プレイヤーのリスポーン地点を登録しました。");
        } else if ( args[1].equalsIgnoreCase("spectate") ) {
            manager.setSpectate(player.getLocation());
            sendInformationMessage(sender, "観客のリスポーン地点を登録しました。");
        } else if ( args[1].equalsIgnoreCase("freddy") ) {
            manager.setFreddy(player.getLocation());
            sendInformationMessage(sender, "Freddyのリスポーン地点を登録しました。");
        } else if ( args[1].equalsIgnoreCase("chica") ) {
            manager.setChica(player.getLocation());
            sendInformationMessage(sender, "Chicaのリスポーン地点を登録しました。");
        } else if ( args[1].equalsIgnoreCase("bonnie") ) {
            manager.setBonnie(player.getLocation());
            sendInformationMessage(sender, "Bonnieのリスポーン地点を登録しました。");
        } else if ( args[1].equalsIgnoreCase("foxy") ) {
            manager.setFoxy(player.getLocation());
            sendInformationMessage(sender, "Foxyのリスポーン地点を登録しました。");
        }
        manager.save();
    }

    private void reloadCommand(
            CommandSender sender, Command command, String label, String[] args) {

        // パーミッションチェック
        if ( !sender.hasPermission(PERMISSION_PREFIX + "reload") ) {
            sendErrorMessage(sender, "パーミッションが無いため実行できません。");
            return;
        }

        // 再読込する
        FiveNightsAtFreddysInMinecraft.getInstance().reloadAll();
    }

    private void sendInformationMessage(CommandSender sender, String message) {
        String msg = ChatColor.RED + "[FNAF]" + ChatColor.AQUA + message;
        sender.sendMessage(msg);
    }

    private void sendErrorMessage(CommandSender sender, String message) {
        String msg = ChatColor.RED + "[FNAF]" + message;
        sender.sendMessage(msg);
    }
}
