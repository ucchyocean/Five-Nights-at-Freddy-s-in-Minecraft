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
import org.bitbucket.ucchy.fnafim.ranking.PlayerScoreData;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.scheduler.BukkitRunnable;

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
        } else if ( args[0].equalsIgnoreCase("ranking") ) {
            rankingCommand(sender, command, label, args);
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
        } else if ( args[0].equalsIgnoreCase("sign") ) {
            signCommand(sender, command, label, args);
            return true;
        } else if ( args[0].equalsIgnoreCase("switch") ) {
            switchCommand(sender, command, label, args);
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
                    "close", "start", "cancel", "set", "sign", "reload"} ) {
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

    public void joinCommand(
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

    public void spectateCommand(CommandSender sender, Command command, String label, String[] args) {

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

    public void rankingCommand(
            CommandSender sender, Command command, String label, String[] args) {

        // パーミッションチェック
        if ( !sender.hasPermission(PERMISSION_PREFIX + "ranking") ) {
            sendErrorMessage(sender, Messages.get("Error_Permission"));
            return;
        }

        ArrayList<PlayerScoreData> data = PlayerScoreData.getAllData();
        ArrayList<Integer> scores = new ArrayList<Integer>();
        String kind = "";

        if ( args.length >= 2 && args[1].equalsIgnoreCase("win") ) {
            // 勝利数順
            PlayerScoreData.sortByGameWin(data);
            kind = Messages.get("Ranking_KindWin");
            for ( PlayerScoreData d : data ) {
                scores.add(d.getPlayerGameWin() + d.getAnimatronicsGameWin());
            }

        } else if ( args.length >= 2 && args[1].equalsIgnoreCase("lose") ) {
            // 敗北数順
            PlayerScoreData.sortByGameLose(data);
            kind = Messages.get("Ranking_KindLose");
            for ( PlayerScoreData d : data ) {
                scores.add(d.getPlayerGameLose() + d.getAnimatronicsGameLose());
            }

        } else if ( args.length >= 2 && args[1].equalsIgnoreCase("catch") ) {
            // 捕まえた回数順
            PlayerScoreData.sortByAnimatronicsCatchPlayers(data);
            kind = Messages.get("Ranking_KindCatch");
            for ( PlayerScoreData d : data ) {
                scores.add(d.getAnimatronicsCatchPlayers());
            }

        } else {
            // スコア順
            PlayerScoreData.sortByScore(data);
            kind = Messages.get("Ranking_KindScore");
            for ( PlayerScoreData d : data ) {
                scores.add(d.getScore());
            }

        }

        sender.sendMessage(Messages.get("Ranking_TopMessage", "%kind", kind));

        String format = Messages.get("Ranking_Line");

        for ( int i=0; i<10; i++ ) {
            if ( data.size() <= i ) {
                break;
            }
            PlayerScoreData d = data.get(i);
            String message = format
                    .replace("%num", (i + 1) + "")
                    .replace("%score", scores.get(i) + "")
                    .replace("%name", d.getName());
            sender.sendMessage(message);
        }

        int yourRank = -1;
        for ( int i=0; i<data.size(); i++ ) {
            PlayerScoreData d = data.get(i);
            if ( d.getName().equals(sender.getName()) ) {
                yourRank = i;
                break;
            }
        }

        if ( yourRank >= 0 ) {
            sender.sendMessage(Messages.get("Ranking_YourScore"));
            String message = format
                    .replace("%num", (yourRank + 1) + "")
                    .replace("%score", scores.get(yourRank) + "")
                    .replace("%name", sender.getName());
            sender.sendMessage(message);
        }
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

    public void openCommand(
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
        boolean isSilent = (args.length >= 2 && args[1].equalsIgnoreCase("silent"));
        session = FiveNightsAtFreddysInMinecraft.getInstance().createNewGameSession(sender, isSilent);
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
        session.closeInvitation(sender, false);
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

    private void signCommand(CommandSender sender, Command command, String label, String[] args) {

        // パーミッションチェック
        if ( !sender.hasPermission(PERMISSION_PREFIX + "sign") ) {
            sendErrorMessage(sender, Messages.get("Error_Permission"));
            return;
        }

        // プレイヤーでないならエラー
        if ( !(sender instanceof Player) ) {
            sendErrorMessage(sender, Messages.get("Error_RunInGame"));
            return;
        }

        // 引数チェック
        boolean isRemove = ( args.length >= 2 && args[1].equalsIgnoreCase("remove") );
        final String meta = isRemove ? JoinSignListener.META_SIGN_COMMAND_REMOVE
                        : JoinSignListener.META_SIGN_COMMAND;

        // メタデータを仕込む、時間経過（15秒）で解除する
        final FiveNightsAtFreddysInMinecraft plugin = FiveNightsAtFreddysInMinecraft.getInstance();
        final Player player = (Player)sender;
        player.setMetadata(meta, new FixedMetadataValue(plugin , true));
        new BukkitRunnable() {
            public void run() {
                if ( player.isOnline() && player.hasMetadata(meta) ) {
                    player.removeMetadata(meta, plugin);
                }
            }
        }.runTaskLater(plugin, 15 * 20);

        if ( !isRemove ) {
            sendInformationMessage(sender, Messages.get("Info_SignPre"));
        } else {
            sendInformationMessage(sender, Messages.get("Info_SignRemovePre"));
        }
    }

    public void switchCommand(
            CommandSender sender, Command command, String label, String[] args) {

        // パーミッションチェック
        if ( !sender.hasPermission(PERMISSION_PREFIX + "switch") ) {
            sendErrorMessage(sender, Messages.get("Error_Permission"));
            return;
        }

        LocationManager manager = FiveNightsAtFreddysInMinecraft.getInstance().getLocationManager();

        // 引数未指定は、現在のアリーナ設定を表示する。
        if ( args.length <= 1 ) {
            String arena = manager.getArenaName();
            sendInformationMessage(sender, Messages.get("Info_Switch", "%arena", arena));
            String lost = manager.getNullLocationName();
            if ( lost != null ) {
                sendInformationMessage(sender, Messages.get("Info_SwitchLocationNG", "%location", lost));
            }
            return;
        }

        // 既にゲームが開始中なら、アリーナ設定を変更できないので、エラーにする。
        GameSession session = FiveNightsAtFreddysInMinecraft.getInstance().getGameSession();
        if ( session != null
                && session.getPhase() != GameSessionPhase.END
                && session.getPhase() != GameSessionPhase.CANCELED ) {
            sendErrorMessage(sender, Messages.get("Error_AlreadyStartedCannotSwitch"));
            return;
        }

        // ランダム選択で、有効なアリーナが一つも無いなら、エラーにする。
        if ( args[1].equalsIgnoreCase("random") && manager.getReadyArenaNames().size() <= 0 ) {
            sendErrorMessage(sender, Messages.get("Error_NoArenaCannotSwitchRandom"));
            return;
        }

        String arena;

        if ( args[1].equalsIgnoreCase("random") ) {
            // 引数randomが指定された場合は、ランダムに選択して設定する。

            ArrayList<String> temp = manager.getReadyArenaNames();
            temp.remove(manager.getArenaName());
            if ( temp.size() == 0 ) {
                arena = manager.getArenaName();
            } else {
                int index = (int)(Math.random() * temp.size());
                arena = temp.get(index);
            }

        } else {
            // そのほかの場合は、指定されたアリーナ名に切り替える。
            arena = args[1];
        }

        // 切り替え
        manager.switchTo(arena);

        // メッセージ送信
        sendInformationMessage(sender, Messages.get("Info_SwitchSet", "%arena", arena));
        String lost = manager.getNullLocationName();
        if ( lost != null ) {
            sendInformationMessage(sender, Messages.get("Info_SwitchLocationNG", "%location", lost));
        }
        return;
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
        sendInformationMessage(sender, Messages.get("Info_Reloaded"));
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
