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
        } else if ( args[0].equalsIgnoreCase("spectate") ) {
            spectateCommand(sender, command, label, args);
            return true;
        } else if ( args[0].equalsIgnoreCase("info") ) {
            infoCommand(sender, command, label, args);
            return true;
        } else if ( args[0].equalsIgnoreCase("reserve") ) {
            reserveCommand(sender, command, label, args);
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
            sendErrorMessage(sender, Messages.get("Error_Permission"));
            return;
        }

        // プレイヤーでないならエラー
        if ( !(sender instanceof Player) ) {
            sendErrorMessage(sender, Messages.get("Error_RunInGame"));
            return;
        }

        Player player = (Player)sender;

        // ゲームセッションが無いならエラー
        GameSession session = FiveNightsAtFreddysInMinecraft.getInstance().getGameSession();
        if ( session == null ) {
            sendErrorMessage(sender, Messages.get("Error_NoSession"));
            return;
        }

        // 既にゲームが開始中ならエラー
        if ( session.getPhase() != GameSessionPhase.INVITATION ) {
            sendErrorMessage(sender, Messages.get("Error_AlreadyStartedCannotJoin"));
            return;
        }

        // 既に参加中ならエラー
        if ( session.isEntrant(player) ) {
            sendErrorMessage(sender, Messages.get("Error_AlreadyJoin"));
            return;
        }

        // 参加者が最大人数を超えている場合はエラー
        int maxplayer = FiveNightsAtFreddysInMinecraft.getInstance().getFNAFIMConfig().getMaxPlayers();
        if ( session.getEntrants().size() >=  maxplayer ) {
            sendErrorMessage(sender, Messages.get("Error_Full"));
            return;
        }

        // 参加する。
        session.joinEntrant(player);
        sendInformationMessage(sender, Messages.get("Info_Joined"));
    }

    private void leaveCommand(
            CommandSender sender, Command command, String label, String[] args) {

        // パーミッションチェック
        if ( !sender.hasPermission(PERMISSION_PREFIX + "leave") ) {
            sendErrorMessage(sender, Messages.get("Error_Permission"));
            return;
        }

        // プレイヤーでないならエラー
        if ( !(sender instanceof Player) ) {
            sendErrorMessage(sender, Messages.get("Error_RunInGame"));
            return;
        }

        Player player = (Player)sender;

        // ゲームセッションが無いならエラー
        GameSession session = FiveNightsAtFreddysInMinecraft.getInstance().getGameSession();
        if ( session == null ) {
            sendErrorMessage(sender, Messages.get("Error_NoSession"));
            return;
        }

        // 既にゲームが開始中ならエラー
        if ( session.getPhase() != GameSessionPhase.INVITATION ) {
            sendErrorMessage(sender, Messages.get("Error_AlreadyStartedCannotLeave"));
            return;
        }

        // 参加していないならエラー
        if ( !session.isEntrant(player) ) {
            sendErrorMessage(sender, Messages.get("Error_NotJoin"));
            return;
        }

        // 離脱する。
        session.leaveEntrant(player);
        sendInformationMessage(sender, Messages.get("Info_Left"));
    }

    private void spectateCommand(CommandSender sender, Command command, String label, String[] args) {

        // パーミッションチェック
        if ( !sender.hasPermission(PERMISSION_PREFIX + "spectate") ) {
            sendErrorMessage(sender, Messages.get("Error_Permission"));
            return;
        }

        // プレイヤーでないならエラー
        if ( !(sender instanceof Player) ) {
            sendErrorMessage(sender, Messages.get("Error_RunInGame"));
            return;
        }

        Player player = (Player)sender;

        // ゲームセッションが無いならエラー
        GameSession session = FiveNightsAtFreddysInMinecraft.getInstance().getGameSession();
        if ( session == null ) {
            sendErrorMessage(sender, Messages.get("Error_NoSession"));
            return;
        }

        // セッションがゲーム中でないならエラー
        if ( session.getPhase() != GameSessionPhase.IN_GAME ) {
            sendErrorMessage(sender, Messages.get("Error_NotStartedCannotSpectate"));
            return;
        }

        // 参加者ならエラー
        if ( session.isEntrant(player) ) {
            sendErrorMessage(sender, Messages.get("Error_YouEntrantCannotSpectate"));
            return;
        }

        // 観客でないなら、観客として参加。観客なら、観客から離脱。
        if ( !session.isSpectator(player) ) {
            session.joinSpectator(player);
        } else {
            session.leaveSpectator(player);
        }
    }

    private void infoCommand(CommandSender sender, Command command, String label, String[] args) {

        // パーミッションチェック
        if ( !sender.hasPermission(PERMISSION_PREFIX + "info") ) {
            sendErrorMessage(sender, Messages.get("Error_Permission"));
            return;
        }

        // ゲームセッションを取得
        GameSession session = FiveNightsAtFreddysInMinecraft.getInstance().getGameSession();
        if ( session == null ) {
            sendErrorMessage(sender, Messages.get("Error_NoSession"));
            return;
        }

        String nightDesc = session.getNight() == null ? "-" : session.getNight().toString();
        sendInformationMessage(sender,
                Messages.get("Info_GamePhase") + session.getPhase() + ", " + nightDesc);

        StringBuilder buffer = new StringBuilder();
        buffer.append(Messages.get("Info_GameEntrants"));
        for ( String name : session.getEntrants() ) {
            Player player = Utility.getPlayerExact(name);
            if ( player != null ) {
                buffer.append(player.getDisplayName() + ", ");
            } else {
                buffer.append(name + ", ");
            }
        }
        sendInformationMessage(sender, buffer.toString());
    }

    private void reserveCommand(
            CommandSender sender, Command command, String label, String[] args) {

        // パーミッションチェック
        if ( !sender.hasPermission(PERMISSION_PREFIX + "reserve") ) {
            sendErrorMessage(sender, Messages.get("Error_Permission"));
            return;
        }

        // ゲームセッションがないならエラー
        GameSession session = FiveNightsAtFreddysInMinecraft.getInstance().getGameSession();
        if ( session != null && (session.getPhase() == GameSessionPhase.CANCELED
                || session.getPhase() == GameSessionPhase.END) ) {
            FiveNightsAtFreddysInMinecraft.getInstance().removeGameSession();
            session = null;
        }
        if ( session == null ) {
            sendErrorMessage(sender, Messages.get("Error_NoSession"));
            return;
        }

        // プレイヤーを指定していないならエラー
        if ( args.length < 2 ) {
            sendErrorMessage(sender, Messages.get("Error_ParameterTooLessPlayerName"));
            return;
        }

        // 指定されたプレイヤーが見つからないならエラー
        Player target = Utility.getPlayerExact(args[1]);
        if ( target == null ) {
            sendErrorMessage(sender, Messages.get("Error_NotFoundPlayer", "%arg", args[1]));
            return;
        }

        // 既にゲーム参加者ならエラー
        if ( session.isEntrant(target) ) {
            sendErrorMessage(sender, Messages.get("Error_AlreadyJoinEntrant", "%arg", args[1]));
            return;
        }

        // 既にゲーム参加予約者ならエラー
        if ( session.isReservation(target) ) {
            sendErrorMessage(sender, Messages.get("Error_AlreadyJoinReservation", "%arg", args[1]));
            return;
        }

        // 参加予約者に追加
        session.joinReservation(target);
    }

    private void openCommand(
            CommandSender sender, Command command, String label, String[] args) {

        // パーミッションチェック
        if ( !sender.hasPermission(PERMISSION_PREFIX + "open") ) {
            sendErrorMessage(sender, Messages.get("Error_Permission"));
            return;
        }

        // ゲームセッションがあるならエラー
        GameSession session = FiveNightsAtFreddysInMinecraft.getInstance().getGameSession();
        if ( session != null && (session.getPhase() == GameSessionPhase.CANCELED
                || session.getPhase() == GameSessionPhase.END) ) {
            FiveNightsAtFreddysInMinecraft.getInstance().removeGameSession();
            session = null;
        }
        if ( session != null ) {
            sendErrorMessage(sender, Messages.get("Error_ExistSessionCannotOpen"));
            return;
        }

        // ロケーションが一つでも設定されていないならエラー
        String lost = FiveNightsAtFreddysInMinecraft.getInstance()
                .getLocationManager().getNullLocationName();
        if ( lost != null ) {
            sendErrorMessage(sender,
                    Messages.get("Error_LostLocationCannotOpen", "%location", lost));
            return;
        }

        // ゲームセッションを作成して、募集を開始する。
        session = FiveNightsAtFreddysInMinecraft.getInstance().createNewGameSession(sender);
        sendInformationMessage(sender, Messages.get("Info_Opened"));
    }

    private void closeCommand(
            CommandSender sender, Command command, String label, String[] args) {

        // パーミッションチェック
        if ( !sender.hasPermission(PERMISSION_PREFIX + "close") ) {
            sendErrorMessage(sender, Messages.get("Error_Permission"));
            return;
        }

        // ゲームセッションが無いならエラー
        GameSession session = FiveNightsAtFreddysInMinecraft.getInstance().getGameSession();
        if ( session == null ) {
            sendErrorMessage(sender, Messages.get("Error_NoSession"));
            return;
        }

        // 既にゲームが開始中ならエラー
        if ( session.getPhase() != GameSessionPhase.INVITATION ) {
            sendErrorMessage(sender, Messages.get("Error_AlreadyStartedCannotClose"));
            return;
        }

        // 募集を中断する。
        session.closeInvitation(sender);
        FiveNightsAtFreddysInMinecraft.getInstance().removeGameSession();
        sendInformationMessage(sender, Messages.get("Info_Closed"));
    }

    private void startCommand(
            CommandSender sender, Command command, String label, String[] args) {

        // パーミッションチェック
        if ( !sender.hasPermission(PERMISSION_PREFIX + "start") ) {
            sendErrorMessage(sender, Messages.get("Error_Permission"));
            return;
        }

        // ゲームセッションが無いならエラー
        GameSession session = FiveNightsAtFreddysInMinecraft.getInstance().getGameSession();
        if ( session == null ) {
            sendErrorMessage(sender, Messages.get("Error_NoSessionCannotStart"));
            return;
        }

        // 既にゲームが開始中ならエラー
        if ( session.getPhase() != GameSessionPhase.INVITATION ) {
            sendErrorMessage(sender, Messages.get("Error_AlreadyStartedCannotStart"));
            return;
        }

        // 引数チェック。無効な内容ならエラー
        Night night = Night.NIGHT1;
        if ( args.length >= 2 ) {
            night = Night.getNightFromString(args[1]);
            if ( night == null ) {
                sendErrorMessage(sender,
                        Messages.get("Error_ParameterNightInvalid", "%arg", args[1]));
                return;
            }
        }

        // 人数チェック、最小人数を下回っていたらエラー
        int minplayer = FiveNightsAtFreddysInMinecraft.getInstance().getFNAFIMConfig().getMinPlayers();
        if ( session.getEntrants().size() < minplayer ) {
            sendErrorMessage(sender, Messages.get("Error_EntrantsTooLess", "%min", minplayer));
            return;
        }

        // ゲームを開始する。
        session.startPreparing(night);
        sendInformationMessage(sender, Messages.get("Info_Started"));
    }

    private void cancelCommand(
            CommandSender sender, Command command, String label, String[] args) {

        // パーミッションチェック
        if ( !sender.hasPermission(PERMISSION_PREFIX + "cancel") ) {
            sendErrorMessage(sender, Messages.get("Error_Permission"));
            return;
        }

        // ゲームセッションが無いならエラー
        GameSession session = FiveNightsAtFreddysInMinecraft.getInstance().getGameSession();
        if ( session == null ) {
            sendErrorMessage(sender, Messages.get("Error_NoSession"));
            return;
        }

        // ゲームが開始していないならエラー
        if ( session.getPhase() != GameSessionPhase.IN_GAME ) {
            sendErrorMessage(sender, Messages.get("Error_SessionNotInGame"));
            return;
        }

        // ゲームを強制中断する。
        session.cancelGame();
        FiveNightsAtFreddysInMinecraft.getInstance().removeGameSession();
        sendInformationMessage(sender, Messages.get("Info_Cancelled"));
    }

    private void setCommand(
            CommandSender sender, Command command, String label, String[] args) {

        // パーミッションチェック
        if ( !sender.hasPermission(PERMISSION_PREFIX + "set") ) {
            sendErrorMessage(sender, Messages.get("Error_Permission"));
            return;
        }

        // プレイヤーでないならエラー
        if ( !(sender instanceof Player) ) {
            sendErrorMessage(sender, Messages.get("Error_RunInGame"));
            return;
        }

        Player player = (Player)sender;

        // 引数チェック。無効ならエラー
        if ( args.length <= 1 || (
                !args[1].equalsIgnoreCase("lobby") && !args[1].equalsIgnoreCase("player")
                && !args[1].equalsIgnoreCase("spectate") && !args[1].equalsIgnoreCase("freddy")
                && !args[1].equalsIgnoreCase("chica") && !args[1].equalsIgnoreCase("bonnie")
                && !args[1].equalsIgnoreCase("foxy") ) ) {
            sendErrorMessage(sender, Messages.get("Error_ParameterInvalid", "%arg", args[1]));
            return;
        }

        // リスポーン地点を登録する
        LocationManager manager = FiveNightsAtFreddysInMinecraft.getInstance().getLocationManager();
        if ( args[1].equalsIgnoreCase("lobby") ) {
            manager.setLobby(player.getLocation());
            sendInformationMessage(sender, Messages.get("Info_SetLobby"));
        } else if ( args[1].equalsIgnoreCase("player") ) {
            manager.setPlayer(player.getLocation());
            sendInformationMessage(sender, Messages.get("Info_SetPlayer"));
        } else if ( args[1].equalsIgnoreCase("spectate") ) {
            manager.setSpectate(player.getLocation());
            sendInformationMessage(sender, Messages.get("Info_SetSpectate"));
        } else if ( args[1].equalsIgnoreCase("freddy") ) {
            manager.setFreddy(player.getLocation());
            sendInformationMessage(sender, Messages.get("Info_SetFreddy"));
        } else if ( args[1].equalsIgnoreCase("chica") ) {
            manager.setChica(player.getLocation());
            sendInformationMessage(sender, Messages.get("Info_SetChica"));
        } else if ( args[1].equalsIgnoreCase("bonnie") ) {
            manager.setBonnie(player.getLocation());
            sendInformationMessage(sender, Messages.get("Info_SetBonnie"));
        } else if ( args[1].equalsIgnoreCase("foxy") ) {
            manager.setFoxy(player.getLocation());
            sendInformationMessage(sender, Messages.get("Info_SetFoxy"));
        }
        manager.save();
    }

    private void reloadCommand(
            CommandSender sender, Command command, String label, String[] args) {

        // パーミッションチェック
        if ( !sender.hasPermission(PERMISSION_PREFIX + "reload") ) {
            sendErrorMessage(sender, Messages.get("Error_Permission"));
            return;
        }

        // 再読込する
        FiveNightsAtFreddysInMinecraft.getInstance().reloadAll();
    }

    private void sendInformationMessage(CommandSender sender, String message) {
        String msg = Messages.get("Prefix_Info") + message;
        sender.sendMessage(msg);
    }

    private void sendErrorMessage(CommandSender sender, String message) {
        String msg = Messages.get("Prefix_Error") + message;
        sender.sendMessage(msg);
    }
}
