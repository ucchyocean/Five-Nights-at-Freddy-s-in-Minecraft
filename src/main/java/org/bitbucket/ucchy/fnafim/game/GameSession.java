/*
 * @author     ucchy
 * @license    LGPLv3
 * @copyright  Copyright ucchy 2015
 */
package org.bitbucket.ucchy.fnafim.game;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.bitbucket.ucchy.fnafim.DelayedTeleportTask;
import org.bitbucket.ucchy.fnafim.FNAFIMConfig;
import org.bitbucket.ucchy.fnafim.FiveNightsAtFreddysInMinecraft;
import org.bitbucket.ucchy.fnafim.LocationManager;
import org.bitbucket.ucchy.fnafim.Messages;
import org.bitbucket.ucchy.fnafim.SoundComponent;
import org.bitbucket.ucchy.fnafim.TitleDisplayComponent;
import org.bitbucket.ucchy.fnafim.Utility;
import org.bitbucket.ucchy.fnafim.effect.BindEffect;
import org.bitbucket.ucchy.fnafim.effect.BlindnessEffect;
import org.bitbucket.ucchy.fnafim.effect.ChangeDisplayNameEffect;
import org.bitbucket.ucchy.fnafim.effect.InvisibleEffect;
import org.bitbucket.ucchy.fnafim.effect.SpeedEffect;
import org.bitbucket.ucchy.fnafim.task.ChicaThreatCooldownTimeTask;
import org.bitbucket.ucchy.fnafim.task.FoxyMovementTask;
import org.bitbucket.ucchy.fnafim.task.FredBearMovementTask;
import org.bitbucket.ucchy.fnafim.task.FreddyItemWaitTask;
import org.bitbucket.ucchy.fnafim.task.FreddyTeleportWaitTask;
import org.bitbucket.ucchy.fnafim.task.GameSessionTask;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * ゲームセッション
 * @author ucchy
 */
public class GameSession {

    private static final int TELEPORT_WAIT_TICKS = 3;

    private GameSessionPhase phase;
    private FNAFIMConfig config;

    private CommandSender owner;
    private List<String> entrants;
    private List<String> players;
    private List<String> spectators;
    private List<String> reservations;
    private String freddy;
    private String chica;
    private String bonnie;
    private String foxy;
    private String fredbear;

    private HashMap<String, PlayerBattery> batteries;
    private ArrayList<GameSessionTask> tasks;
    private GameSessionTimer timer;
    private GameSessionLogger logger;

    private TemporaryStorage storage;
    private EffectManager effectManager;
    private ScoreboardDisplay scoreboardDisplay;

    private boolean freddyReturn;

    private Night night;

    private ItemStack flashlightOn;
    private ItemStack flashlightOff;
    private ItemStack radar;
    private ItemStack shutterOn;
    private ItemStack shutterOff;

    private ItemStack chicaThreat;

    /**
     * コンストラクタ
     * @param owner オーナー
     */
    public GameSession(CommandSender owner) {

        this.owner = owner;
        entrants = new ArrayList<String>();
        players = new ArrayList<String>();
        spectators = new ArrayList<String>();
        reservations = new ArrayList<String>();

        storage = new TemporaryStorage();
        effectManager = new EffectManager();

        File logFolder = new File(FiveNightsAtFreddysInMinecraft.getInstance().getDataFolder(), "logs");
        logger = new GameSessionLogger(logFolder);
        config = FiveNightsAtFreddysInMinecraft.getInstance().getFNAFIMConfig();

        makeItems();
    }

    /**
     * 募集を開始する
     */
    public void openInvitation() {

        phase = GameSessionPhase.INVITATION;
        FiveNightsAtFreddysInMinecraft.getInstance().getJoinsignManager().updateAll();
        sendBroadcastAnnounce(Messages.get("Announce_OpenInvitation", "%owner", owner.getName()));
    }

    /**
     * 募集を中断する。
     * 呼び出す時は、中断可能なフェーズ（INVITATION）であることをあらかじめ確認すること。
     * @param owner キャンセルした人
     */
    public void closeInvitation(CommandSender owner) {

        if ( phase != GameSessionPhase.INVITATION ) {
            return;
        }

        phase = GameSessionPhase.CANCELED;
        FiveNightsAtFreddysInMinecraft.getInstance().getJoinsignManager().updateAll();
        sendBroadcastAnnounce(Messages.get("Announce_CloseInvitation", "%owner", owner.getName()));

        // 預かっていた持ち物を返す
        for ( String name : entrants ) {
            Player player = Utility.getPlayerExact(name);
            if ( player != null ) {
                storage.restoreFromTemp(player);
            }
        }
    }

    /**
     * ゲームの準備を開始する
     * @param night ゲーム難易度
     */
    public void startPreparing(Night night) {

        phase = GameSessionPhase.PREPARING;
        FiveNightsAtFreddysInMinecraft.getInstance().getJoinsignManager().updateAll();
        this.night = night;
        this.tasks = new ArrayList<GameSessionTask>();

        freddyReturn = false;

        // 予約者を参加者に追加する
        for ( String name : reservations ) {
            Player player = Utility.getPlayerExact(name);
            if ( player != null && player.isOnline() && !entrants.contains(name) ) {
                entrants.add(name);

                if ( spectators.contains(name) ) {
                    // 観客から参加者に変更するために、いったん持ち物の除去をする
                    removeInventoryAll(name);
                    spectators.remove(name);
                }
            }
        }
        reservations.clear();

        // 役割を設定する。
        players.clear();
        for ( String name : entrants ) {
            if ( getDollRole(name) == null ) {
                players.add(name);
            }
        }

        if ( fredbear == null && entrants.size() == 2 ) {
            int random = (int)(Math.random() * players.size());
            fredbear = players.get(random);
            players.remove(random);
            sendInGameAnnounce("FredBear : " + fredbear);
            sendInfoToPlayer(fredbear, Messages.get("Description_FredBear"));
        }

        if ( chica == null && players.size() >= 2 ) {
            int random = (int)(Math.random() * players.size());
            chica = players.get(random);
            players.remove(random);
            sendInGameAnnounce("Chica : " + chica);
            sendInfoToPlayer(chica, Messages.get("Description_Chica"));
        }

        if ( bonnie == null && players.size() >= 2 ) {
            int random = (int)(Math.random() * players.size());
            bonnie = players.get(random);
            players.remove(random);
            sendInGameAnnounce("Bonnie : " + bonnie);
            sendInfoToPlayer(bonnie, Messages.get("Description_Bonnie"));
        }

        if ( freddy == null && players.size() >= 2 ) {
            int random = (int)(Math.random() * players.size());
            freddy = players.get(random);
            players.remove(random);
            sendInGameAnnounce("Freddy : " + freddy);
            sendInfoToPlayer(freddy, Messages.get("Description_Freddy"));
        }

        if ( foxy == null && players.size() >= 2 ) {
            int random = (int)(Math.random() * players.size());
            foxy = players.get(random);
            players.remove(random);
            sendInGameAnnounce("Foxy : " + foxy);
            sendInfoToPlayer(foxy, Messages.get("Description_Foxy"));
        }

        // 装備を配布する
        for ( String name : players ) {
            sendPlayerInventory(name);
        }
        sendFredBearInventory(fredbear);
        sendFreddyInventory(freddy);
        sendChicaInventory(chica);
        sendBonnieInventory(bonnie);
        sendFoxyInventory(foxy);

        for ( String name : spectators ) {
            sendSpectatorInventory(name);
        }

        // エフェクトの設定
        for ( String name : entrants ) {
            effectManager.removeAllEffect(name);
        }

        // プレイヤーのエフェクト設定
        for ( String name : players ) {
            effectManager.applyEffect(name, new BlindnessEffect(name));
            effectManager.applyEffect(name, new ChangeDisplayNameEffect(name,
                    ChatColor.AQUA + name + ChatColor.WHITE + "(Player)"
                            + ChatColor.RESET));
        }

        // FredBearのエフェクトの設定
        if ( fredbear != null ) {
            applyMoveSpeedSetting(fredbear, config.getMoveSpeed(night).getFredbear());
            effectManager.applyEffect(fredbear, new ChangeDisplayNameEffect(fredbear,
                    ChatColor.GOLD + fredbear + ChatColor.RED + "(FredBear)" + ChatColor.RESET));
        }

        // Freddyのエフェクトの設定
        if ( freddy != null ) {
            applyMoveSpeedSetting(freddy, config.getMoveSpeed(night).getFreddy());
            effectManager.applyEffect(freddy, new ChangeDisplayNameEffect(freddy,
                    ChatColor.GOLD + freddy + ChatColor.RED + "(Freddy)" + ChatColor.RESET));
        }

        // Chicaのエフェクト設定
        if ( chica != null ) {
            applyMoveSpeedSetting(chica, config.getMoveSpeed(night).getChica());
            effectManager.applyEffect(chica, new ChangeDisplayNameEffect(chica,
                    ChatColor.GOLD + chica + ChatColor.RED + "(Chica)" + ChatColor.RESET));
        }

        // Bonnieのエフェクト設定
        if ( bonnie != null ) {
            applyMoveSpeedSetting(bonnie, config.getMoveSpeed(night).getBonnie());
            effectManager.applyEffect(bonnie, new ChangeDisplayNameEffect(bonnie,
                    ChatColor.GOLD + bonnie + ChatColor.RED + "(Bonnie)" + ChatColor.RESET));
        }

        // Foxyのエフェクト設定、常に移動不可にしておく
        if ( foxy != null ) {
            effectManager.applyEffect(foxy, new BindEffect(foxy));
            effectManager.applyEffect(foxy, new ChangeDisplayNameEffect(foxy,
                    ChatColor.GOLD + foxy + ChatColor.RED + "(Foxy)" + ChatColor.RESET));
        }

        // 観客のエフェクト設定
        for ( String name : spectators ) {
            effectManager.applyEffect(name, new InvisibleEffect(name));
            effectManager.applyEffect(name, new SpeedEffect(name, 3));
        }

        // プレイヤーのバッテリー
        batteries = new HashMap<String, PlayerBattery>();
        for ( String name : players ) {
            batteries.put(name, new PlayerBattery(name, this));
        }

        // それぞれのスタート地点にTPする
        LocationManager lmanager =
                FiveNightsAtFreddysInMinecraft.getInstance().getLocationManager();
        HashMap<Player, Location> locationMap = new HashMap<Player, Location>();
        for ( String name : players ) {
            Player player = Utility.getPlayerExact(name);
            if ( player != null ) {
                locationMap.put(player, lmanager.getPlayer());
            }
        }
        if ( fredbear != null ) {
            Player player = Utility.getPlayerExact(fredbear);
            if ( player != null ) {
                locationMap.put(player, lmanager.getFreddy());
            }
        }
        if ( freddy != null ) {
            Player player = Utility.getPlayerExact(freddy);
            if ( player != null ) {
                locationMap.put(player, lmanager.getFreddy());
            }
        }
        if ( chica != null ) {
            Player player = Utility.getPlayerExact(chica);
            if ( player != null ) {
                locationMap.put(player, lmanager.getChica());
            }
        }
        if ( bonnie != null ) {
            Player player = Utility.getPlayerExact(bonnie);
            if ( player != null ) {
                locationMap.put(player, lmanager.getBonnie());
            }
        }
        if ( foxy != null ) {
            Player player = Utility.getPlayerExact(foxy);
            if ( player != null ) {
                locationMap.put(player, lmanager.getFoxy());
            }
        }
        new DelayedTeleportTask(locationMap, TELEPORT_WAIT_TICKS).startTask();

        // サイドバーを用意する
        scoreboardDisplay = new ScoreboardDisplay();
        for ( String name : entrants ) {
            Player player = Utility.getPlayerExact(name);
            if ( player != null ) {
                scoreboardDisplay.setShowPlayer(player);
            }
        }
        for ( String name : spectators ) {
            Player player = Utility.getPlayerExact(name);
            if ( player != null ) {
                scoreboardDisplay.setShowPlayer(player);
            }
        }
        scoreboardDisplay.setTitle(ChatColor.RED + night.toString());
        scoreboardDisplay.setRemainTime(night, 12);
        scoreboardDisplay.setRemainPlayer(players.size());

//        scoreboardDisplay.setRole(fredbear, ChatColor.RED + "FredBear");
//        scoreboardDisplay.setRole(freddy, ChatColor.RED + "Freddy");
//        scoreboardDisplay.setRole(chica, ChatColor.RED + "Chica");
//        scoreboardDisplay.setRole(bonnie, ChatColor.RED + "Bonnie");
//        scoreboardDisplay.setRole(foxy, ChatColor.RED + "Foxy");
//        for ( String name : players ) {
//            scoreboardDisplay.setRole(name, ChatColor.AQUA + "Guard");
//        }
//        for ( String name : spectators ) {
//            scoreboardDisplay.setRole(name, ChatColor.GRAY + "Spectator");
//        }

        for ( String player : players ) {
            scoreboardDisplay.setPlayersTeam(player);
        }
        scoreboardDisplay.setFreddysTeam(fredbear);
        scoreboardDisplay.setFreddysTeam(freddy);
        scoreboardDisplay.setFreddysTeam(chica);
        scoreboardDisplay.setFreddysTeam(bonnie);
        scoreboardDisplay.setFreddysTeam(foxy);

        // プレイヤー人数 x TELEPORT_WAIT＋α だけ待ってから、ゲームを開始する。
        int delay = entrants.size() * TELEPORT_WAIT_TICKS + 10;
        new BukkitRunnable() {
            public void run() {
                startGame();
            }
        }.runTaskLater(FiveNightsAtFreddysInMinecraft.getInstance(), delay);
    }

    /**
     * ゲームを開始する。
     */
    private void startGame() {

        phase = GameSessionPhase.IN_GAME;
        FiveNightsAtFreddysInMinecraft.getInstance().getJoinsignManager().updateAll();
        sendInGameTitle(Messages.get("Announce_GameStart", "%night", night.toString()));
        sendInGameSound(config.getSoundNightStart());

        // タイマーの生成と開始
        FNAFIMConfig config = FiveNightsAtFreddysInMinecraft.getInstance().getFNAFIMConfig();
        timer = new GameSessionTimer(this, config.getSecondsOfOneHour() * 6);
        timer.start();
    }

    /**
     * ゲームを強制中断する
     */
    public void cancelGame() {

        phase = GameSessionPhase.CANCELED;
        FiveNightsAtFreddysInMinecraft.getInstance().getJoinsignManager().updateAll();
        sendInGameAnnounce(Messages.get("Announce_GameCanceled"));
        onEnd();
    }

    /**
     * セッションの最後に呼び出されるメソッド
     */
    private void onEnd() {

        phase = GameSessionPhase.END;
        FiveNightsAtFreddysInMinecraft.getInstance().getJoinsignManager().updateAll();

        // タイマーの停止
        if ( timer != null ) {
            timer.end();
            timer = null;
        }

        // サイドバーの除去
        if ( scoreboardDisplay != null ) {
            scoreboardDisplay.remove();
        }
        for ( String name : entrants ) {
            Player player = Utility.getPlayerExact(name);
            if ( player != null ) {
                player.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
            }
        }
        for ( String name : spectators ) {
            Player player = Utility.getPlayerExact(name);
            if ( player != null ) {
                player.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
            }
        }

        // エフェクトをクリア
        for ( String name : entrants ) {
            effectManager.removeAllEffect(name);
        }
        for ( String name : spectators ) {
            effectManager.removeAllEffect(name);
        }

        // バッテリーをクリア
        if ( batteries != null ) {
            batteries.clear();
        }

        // タスクをクリア
        if ( tasks != null ) {
            for ( GameSessionTask task : tasks ) {
                if ( !task.isEnded() ) {
                    task.end();
                }
            }
            tasks.clear();
        }

        // プレイヤーに持ち物を返す
        for ( String name : entrants ) {
            removeInventoryAll(name);
            storage.restoreFromTemp(name);
        }
        for ( String name : spectators ) {
            removeInventoryAll(name);
            storage.restoreFromTemp(name);
        }

        // 全員をロビーに送る
        HashMap<Player, Location> locationMap = new HashMap<Player, Location>();
        LocationManager lmanager = FiveNightsAtFreddysInMinecraft.getInstance().getLocationManager();
        for ( String name : entrants ) {
            Player player = Utility.getPlayerExact(name);
            if ( player != null ) {
                locationMap.put(player, lmanager.getLobby());
            }
        }
        for ( String name : spectators ) {
            Player player = Utility.getPlayerExact(name);
            if ( player != null ) {
                locationMap.put(player, lmanager.getLobby());
            }
        }

        if ( locationMap.size() > 0 ) {
            if ( FiveNightsAtFreddysInMinecraft.getInstance().isEnabled() ) {
                // プラグインが有効なら、遅延テレポートタスクでまとめて移動する。
                new DelayedTeleportTask(locationMap, TELEPORT_WAIT_TICKS).startTask();
            } else {
                // プラグインが無効なら、そのままテレポートする。
                for ( Player player : locationMap.keySet() ) {
                    Location location = locationMap.get(player);
                    player.teleport(location, TeleportCause.PLUGIN);
                }
            }
        }

        // セッションを消去する
        FiveNightsAtFreddysInMinecraft.getInstance().removeGameSession();
    }

    /**
     * プレイヤーが捕まった時に呼び出される。
     * @param name 捕まったプレイヤーのプレイヤー名
     * @param caught 捕まえたプレイヤー
     */
    public void onCaughtPlayer(String name, Player caught) {

        final Player player = Utility.getPlayerExact(name);

        if ( caught != null ) {
            Doll doll = getDollRole(caught.getName());
            sendInGameAnnounce(Messages.get("Announce_PlayerCaught",
                    new String[]{"%player", "%caught", "%doll"},
                    new String[]{name, caught.getName(), doll.toString()}
            ));
        } else {
            sendInGameAnnounce(Messages.get("Announce_PlayerRunMidway",
                    "%player", name));
        }

        if ( player != null ) {

            // SEを再生（プレイヤーは捕まると観客のリスポーン地点に飛ばされるので、
            // 飛んだ先でSEが流れるように、10ticks遅らせてもう一度再生する）
            config.getSoundPlayerCaught().playSoundToPlayer(player);
            player.getWorld().playEffect(player.getLocation(), Effect.STEP_SOUND, 10);
            new BukkitRunnable() {
                public void run() {
                    config.getSoundPlayerCaught().playSoundToPlayer(player);
                    player.getWorld().playEffect(player.getLocation(), Effect.STEP_SOUND, 10);
                }
            }.runTaskLater(FiveNightsAtFreddysInMinecraft.getInstance(), 10);
        }

        // エフェクトをクリア
        effectManager.removeAllEffect(name);

        // バッテリーをクリア
        batteries.remove(name);
        if ( player != null ) {
            player.setLevel(0);
            player.setExp(0);
        }

        // 持ち物をクリア
        if ( player != null ) {
            player.getInventory().clear();
        }

        // プレイヤーおよび参加者から削除する
        entrants.remove(name);
        players.remove(name);

        // スコアボードを更新
        scoreboardDisplay.setRemainPlayer(players.size());
        if ( player != null ) {
            scoreboardDisplay.leavePlayersTeam(player.getName());
        }

        // 捕まえたのがfreddyで、行動不能のnightなら、リスポーン地点に戻して行動不能にする
        if ( caught != null && freddy != null && freddy.equals(caught.getName()) && freddyReturn ) {
            freddyReturn = false;
            effectManager.applyEffect(freddy, new BindEffect(freddy));
            Location loc = FiveNightsAtFreddysInMinecraft.getInstance().getLocationManager().getFreddy();
            caught.teleport(loc, TeleportCause.PLUGIN);
        }

        if ( players.size() <= 0 ) {
            // 全滅したら、onGameover() を呼びだす。
            onGameover();

            if ( player != null ) {

                // スコアボードを非表示にする
                player.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());

                // 預かっていた持ち物を返す
                storage.restoreFromTemp(player);

                // ロビーに送る
                Location lobby = FiveNightsAtFreddysInMinecraft.getInstance().getLocationManager().getLobby();
                player.teleport(lobby, TeleportCause.PLUGIN);
            }

        } else {
            // 観客として再参加させる
            if ( player != null ) {
                joinSpectator(player);
            }
        }
    }

    /**
     * プレイヤーが全滅したときに呼び出される。
     */
    protected void onGameover() {

        sendInGameTitle(Messages.get("Announce_GameOver1"));
        sendInGameAnnounce(Messages.get("Announce_GameOver2"));
        onEnd();
    }

    /**
     * 1秒ごとに呼び出される。
     * @param remain 残り秒数
     */
    protected void onTimerSeconds(int remain) {

        // バッテリーの更新
        for ( PlayerBattery battery : batteries.values() ) {
            battery.onSeconds();
        }

        // サイドバーの更新
        double reverse = config.getSecondsOfOneHour() * 6 - remain;
        int hour = (int)(reverse / config.getSecondsOfOneHour());
        if ( hour == 0 ) {
            hour = 12;
        }
        scoreboardDisplay.setRemainTime(night, hour);
    }

    /**
     * タイマーが0になった時に呼び出される。
     */
    protected void onTimerZero() {

        sendInGameTitle(Messages.get("Announce_NightClear1"));
        sendInGameAnnounce(Messages.get("Announce_NightClear2", "%num", players.size()));

        // SEを流す
        sendInGameSound(config.getSoundNightEnd());

        // 持ち物を消去する、エフェクトを消去する
        for ( String name : entrants ) {
            removeInventoryAll(name);
            effectManager.removeAllEffect(name);
        }
        for ( String name : spectators ) {
            removeInventoryAll(name);
        }

        // Night1-4は、次の夜の準備。その他は終了。
        final Night next = night.getNext();
        if ( next != null ) {

            phase = GameSessionPhase.PREPARING_NEXT;

            // タスクをクリア
            for ( GameSessionTask task : tasks ) {
                if ( !task.isEnded() ) {
                    task.end();
                }
            }
            tasks.clear();

            // エフェクトをクリア
            for ( String name : entrants ) {
                effectManager.removeAllEffect(name);
            }
            for ( String name : spectators ) {
                effectManager.removeAllEffect(name);
            }

            // バッテリーをクリア
            for ( PlayerBattery battery : batteries.values() ) {
                battery.resetExpBar();
            }
            batteries.clear();

            // 15秒後に、startPreparingを呼び出す
            int wait = config.getSecondsOfNightInterval();
            new BukkitRunnable() {
                public void run() {
                    startPreparing(next);
                }
            }.runTaskLater(FiveNightsAtFreddysInMinecraft.getInstance(), wait * 20);
            sendInGameAnnounce(Messages.get("Announce_NextNight",
                    new String[]{"%seconds", "%night"},
                    new Object[]{wait, next}
            ));

        } else {

            // 勝利したプレイヤーの名前を記録する、アナウンスする
            StringBuffer winners = new StringBuffer();
            for ( String winner : players ) {
                if ( winners.length() > 0 ) {
                    winners.append(", ");
                }
                winners.append(winner);
            }
            sendInGameAnnounce(Messages.get("Announce_GameClear"));
            sendInGameAnnounce(Messages.get("Announce_GameClear2", "%players", winners.toString()));

            // onEnd() を呼んで終了処理をしておく。
            onEnd();
        }
    }

    /**
     * 参加者がクリックを実行した時に呼び出される。
     * @param player プレイヤー
     * @return
     */
    protected boolean onEntrantInteract(final Player player) {

        // 表示名のないアイテムを持っていたなら無視。
        ItemStack item = player.getItemInHand();
        String name = getDisplayName(item);
        if ( name == null ) {
            return true;
        }

        // 懐中電灯のアイテム処理
        if ( name.equals(Messages.get("ItemName_FlashLight")) ) {
            boolean isOn = (item.getType() == flashlightOff.getType());
            if ( batteries.containsKey(player.getName()) ) {
                if ( batteries.get(player.getName()).getPower() <= 0 ) {
                    sendInfoToPlayer(player, Messages.get("Info_BatteryNotEnough"));
                    return false;
                }
                batteries.get(player.getName()).setUsingFlashlight(isOn);
            }
            if ( isOn ) {
                player.setItemInHand(flashlightOn.clone());
                effectManager.removeEffect(player.getName(), BlindnessEffect.TYPE);
            } else {
                player.setItemInHand(flashlightOff.clone());
                effectManager.applyEffect(player.getName(), new BlindnessEffect(player));
            }
            config.getSoundUseFlashLight().playSoundToPlayer(player);
            return false;
        }

        // レーダーのアイテム処理
        if ( name.equals(Messages.get("ItemName_Rader")) ) {
            if ( batteries.containsKey(player.getName()) ) {
                if ( !batteries.get(player.getName()).hasPowerToUseRadar() ) {
                    sendInfoToPlayer(player, Messages.get("Info_BatteryNotEnough"));
                    return false;
                }
                batteries.get(player.getName()).decreaseToUseRadar();
            }
            boolean found = false;
            int range = config.getRaderSearchingRange();
            for ( Entity entity : player.getNearbyEntities(range, range, range) ) {
                if ( entity instanceof Player ) {
                    Player target = (Player)entity;
                    Doll doll = getDollRole(target.getName());
                    if ( doll != null ) {
                        double distance = player.getLocation().distance(target.getLocation());
                        if ( distance <= range ) {
                            String msg = Messages.get("Info_ItemRader",
                                    new String[]{"%target", "%doll", "%distance"},
                                    new String[]{target.getName(), doll.toString(),
                                        String.format("%.1f", distance)});
                            sendInfoToPlayer(player, msg);
                            found = true;
                        }
                    }
                }
            }
            if ( !found ) {
                sendInfoToPlayer(player, Messages.get("Info_ItemRaderNone"));
            }
            config.getSoundUseRader().playSoundToPlayer(player);
            return false;
        }

        // シャッターのアイテム処理
        if ( name.equals(Messages.get("ItemName_Shutter")) ) {
            boolean isOn = (item.getType() == shutterOff.getType());
            if ( batteries.containsKey(player.getName()) ) {
                if ( batteries.get(player.getName()).getPower() <= 0 ) {
                    sendInfoToPlayer(player, Messages.get("Info_BatteryNotEnough"));
                    return false;
                }
                batteries.get(player.getName()).setUsingShutter(isOn);
            }
            if ( isOn ) {
                player.setItemInHand(new ItemStack(Material.AIR));
                player.getInventory().setItem(3, shutterOn.clone());
                updateInventory(player);
                effectManager.applyEffect(player.getName(), new InvisibleEffect(player));
            } else {
                player.setItemInHand(new ItemStack(Material.AIR));
                player.getInventory().setItem(2, shutterOff.clone());
                updateInventory(player);
                effectManager.removeEffect(player.getName(), InvisibleEffect.TYPE);
            }
            config.getSoundUseShutter().playSoundToPlayer(player);
            return false;
        }

        // Foxyのアイテム処理
        if ( name.equals(Messages.get(
                "ItemName_FoxyMovement", "%seconds", config.getFoxyMovementSeconds())) ) {

            // 行動中なら、アイテムを使う必要は無い
            if ( isFoxyMoving() ) {
                sendInfoToPlayer(player, Messages.get("Info_FoxyMovementAlready"));
                return false;
            }

            // 1つ消費する
            int amount = item.getAmount() - 1;
            if ( amount > 0 ) {
                player.getItemInHand().setAmount(amount);
            } else {
                player.setItemInHand(new ItemStack(Material.AIR));
            }

            // 行動不可を解いて、30秒間の行動時間を与える
            int seconds = config.getFoxyMovementSeconds();
            int speed = config.getMoveSpeed(night).getFoxy();
            effectManager.removeEffect(player.getName(), BindEffect.TYPE);
            effectManager.applyEffect(player.getName(), new SpeedEffect(player, speed));
            sendInfoToPlayer(player, Messages.get("Info_FoxyMovementStart", "%seconds", seconds));
            GameSessionTask task = new FoxyMovementTask(this, seconds);
            task.start();
            tasks.add(task);

            // SEを流す
            config.getSoundFoxyMovement().playSoundToPlayer(player);

            return false;
        }

        // FredBearの高速移動アイテム処理
        if ( name.equals(Messages.get(
                "ItemName_FredBearMovement", "%seconds", config.getFoxyMovementSeconds())) ) {

            // 行動中なら、アイテムを使う必要は無い
            if ( isFredBearMoving() ) {
                sendInfoToPlayer(player, Messages.get("Info_FoxyMovementAlready"));
                return false;
            }

            // 1つ消費する
            int amount = item.getAmount() - 1;
            if ( amount > 0 ) {
                player.getItemInHand().setAmount(amount);
            } else {
                player.setItemInHand(new ItemStack(Material.AIR));
            }

            // 30秒間のスピード上昇+3を与える
            int seconds = config.getFoxyMovementSeconds();
            int speed = config.getMoveSpeed(night).getFredbear() + config.getFredbearSpeedUp();
            effectManager.removeEffect(player.getName(), SpeedEffect.TYPE);
            effectManager.applyEffect(player.getName(), new SpeedEffect(player, speed));
            sendInfoToPlayer(player, Messages.get("Info_FoxyMovementStart", "%seconds", seconds));
            GameSessionTask task = new FredBearMovementTask(this, seconds);
            task.start();
            tasks.add(task);

            // SEを流す
            config.getSoundFoxyMovement().playSoundToPlayer(player);

            return false;
        }

        // Freddyのアイテム処理
        if ( item.getType() == Material.SKULL_ITEM ) {

            // ターゲットプレイヤーの取得
            SkullMeta meta = (SkullMeta)item.getItemMeta();
            String owner = meta.getOwner();
            if ( owner != null ) {
                Player target = Utility.getPlayerExact(owner);
                if ( target != null && target.isOnline() ) {
                    int wait = (int)(Math.random() * 8) + 1;
                    player.teleport(target, TeleportCause.PLUGIN);
                    int speed;
                    if ( player.getName().equals(fredbear) ) {
                        speed = config.getMoveSpeed(night).getFredbear();
                    } else {
                        speed = config.getMoveSpeed(night).getFreddy();
                    }
                    GameSessionTask task = new FreddyTeleportWaitTask(this, player.getName(), wait, speed);
                    task.start();
                    tasks.add(task);
                    sendInfoToPlayer(player.getName(), Messages.get("Info_FreddyTeleportWait", "%seconds", wait));
                    effectManager.applyEffect(player.getName(), new BindEffect(player.getName()));

                    // SEを流す
                    config.getSoundFreddyTeleport().playSoundToWorld(target.getLocation());
                }
            }

            // アイテム消費
            player.setItemInHand(new ItemStack(Material.AIR));

            return false;
        }

        // Chicaのアイテム処理
        if ( item.getType() == chicaThreat.getType() ) {

            // 威嚇音を出す
            config.getSoundChicaThreat().playSoundToWorld(player.getLocation());

            // アイテム消費
            player.setItemInHand(new ItemStack(Material.AIR));

            // クールダウンタイムを開始する
            int seconds = config.getChicaThreatCooldownSeconds();
            ChicaThreatCooldownTimeTask task
                = new ChicaThreatCooldownTimeTask(this, player.getName(), seconds);
            task.start();
            tasks.add(task);

            return false;
        }

        return true;
    }

    /**
     * 観客がクリックをしたときに呼び出される
     * @param player
     */
    protected void onSpectatorInteract(Player player) {

        // テレポートメニューを開く
        openTeleportMenuInventory(player);
    }

    /**
     * 参加者プレイヤーのバッテリーがダウンした時に呼び出される。
     * @param player
     */
    protected void onBatteryDown(String name) {

        // ゲーム中でなければ何もしない
        if ( phase != GameSessionPhase.IN_GAME ) {
            return;
        }

        // 懐中電灯オフ
        effectManager.applyEffect(name, new BlindnessEffect(name));

        // シャッターオフ
        effectManager.removeEffect(name, InvisibleEffect.TYPE);

        // プレイヤーを停止する
        effectManager.applyEffect(name, new BindEffect(name));

        // 手持ちのアイテムを全て無くす
        Player player = Utility.getPlayerExact(name);
        if ( player != null ) {
            player.getInventory().clear();
        }

        // メッセージを流す
        sendInfoToPlayer(player, Messages.get("Info_BatteryEmpty"));

        // フレディにテレポート用アイテムを渡す
        int wait = (int)(Math.random() * 8);
        FreddyItemWaitTask task = new FreddyItemWaitTask(this, player, wait);
        task.start();
        tasks.add(task);
    }

    /**
     * 参加者プレイヤーが他の参加者プレイヤーをクリックした時に呼び出される。
     * @param player
     * @param target
     */
    protected void onTouch(Player player, Player target) {

        // ゲームフェーズ中でなければ無視
        if ( phase != GameSessionPhase.IN_GAME ) {
            return;
        }

        // タッチした人がFreddy陣営でなければ無視
        if ( getDollRole(player.getName()) == null ) {
            return;
        }

        // タッチした人がバインド中なら無視
        if ( player.hasMetadata(BindEffect.TYPE) ) {
            return;
        }

        // タッチされた人がプレイヤーでなければ無視
        if ( !players.contains(target.getName()) ) {
            return;
        }

        // タッチされた人が透明化していたら無視
        if ( effectManager.hasEffect(target.getName(), InvisibleEffect.TYPE) ) {
            return;
        }

        // 捕まえた！
        onCaughtPlayer(target.getName(), player);

        return;
    }

    /**
     * プレイヤーを参加者に追加する
     * @param player 参加者
     */
    public void joinEntrant(Player player) {

        if ( !entrants.contains(player.getName()) ) {
            entrants.add(player.getName());

            // プレイヤーの持ち物を預かる
            storage.sendToTemp(player);

            // ポーション効果を除去する、回復しておく
            for ( PotionEffect e : player.getActivePotionEffects() ) {
                player.removePotionEffect(e.getType());
            }
            player.setFoodLevel(20);
            player.setHealth(player.getMaxHealth());

            FiveNightsAtFreddysInMinecraft.getInstance().getJoinsignManager().updateAll();

            sendInGameAnnounce(Messages.get("Announce_EntrantJoin", "%player", player.getName()));
        }
    }

    /**
     * プレイヤーを参加者から離脱させる。
     * 離脱可能なフェーズ（INVITATION）であることをあらかじめ確認すること。
     * @param player 離脱する人
     */
    public void leaveEntrant(Player player) {

        if ( entrants.contains(player.getName()) ) {
            entrants.remove(player.getName());

            // 預かっていた持ち物を返す
            storage.restoreFromTemp(player);

            FiveNightsAtFreddysInMinecraft.getInstance().getJoinsignManager().updateAll();

            sendInGameAnnounce(Messages.get("Announce_EntrantLeave", "%player", player.getName()));
        }
    }

    /**
     * 観客としてゲーム参加する
     * @param player
     */
    public void joinSpectator(Player player) {

        if ( !spectators.contains(player.getName()) ) {
            spectators.add(player.getName());
        }

        // サイドバーを表示する
        scoreboardDisplay.setShowPlayer(player);

        // 透明化する、高速化する
        effectManager.applyEffect(player.getName(), new InvisibleEffect(player));
        effectManager.applyEffect(player.getName(), new SpeedEffect(player, 3));

        // 持ち物を預かる
        if ( !storage.isInventoryExists(player) ) {
            storage.sendToTemp(player);
        }

        // 観客用のアイテムを渡す
        sendSpectatorInventory(player.getName());

        // 観客リスポーン地点に移動する
        Location spectate = FiveNightsAtFreddysInMinecraft.getInstance().getLocationManager().getSpectate();
        player.teleport(spectate, TeleportCause.PLUGIN);

        sendInfoToPlayer(player, Messages.get("Info_JoinSpectator"));
    }

    /**
     * ゲームの観客から退出する
     * @param player
     */
    public void leaveSpectator(Player player) {

        if ( spectators.contains(player.getName()) ) {
            spectators.remove(player.getName());

            // サイドバーを消去する
            player.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());

            // エフェクトを除去する
            effectManager.removeAllEffect(player.getName());

            // 預かっていた持ち物を返す
            storage.restoreFromTemp(player);

            // ロビーに移動する
            Location lobby = FiveNightsAtFreddysInMinecraft.getInstance().getLocationManager().getLobby();
            player.teleport(lobby, TeleportCause.PLUGIN);

            sendInfoToPlayer(player, Messages.get("Info_LeaveSpectator"));
        }
    }

    /**
     * 予約参加者として、次のNightからゲームに参加する
     * @param player
     */
    public void joinReservation(Player player) {

        if ( isEntrant(player) ) return;
        if ( reservations.contains(player.getName()) ) return;

        reservations.add(player.getName());
        sendInGameAnnounce(Messages.get("Announce_EntrantJoin", "%player", player.getName()));
        sendInfoToPlayer(player, Messages.get("Info_ReservationAdd"));
    }

    /**
     * 指定されたプレイヤーは参加予約者かどうかを確認する
     * @param player
     * @return
     */
    public boolean isReservation(Player player) {
        return reservations.contains(player.getName());
    }

    /**
     * 現在のフェーズを返す
     * @return フェーズ
     */
    public GameSessionPhase getPhase() {
        return phase;
    }

    /**
     * 指定したプレイヤーが参加者かどうかを返す
     * @param player プレイヤー
     * @return 参加者かどうか
     */
    public boolean isEntrant(Player player) {
        return entrants.contains(player.getName());
    }

    /**
     * 参加者を全て取得する
     * @return 参加者
     */
    public List<String> getEntrants() {
        return entrants;
    }

    /**
     * 指定されたプレイヤーがFoxyかどうかを判定する
     * @param player プレイヤー
     * @return Foxyかどうか
     */
    public boolean isFoxy(Player player) {
        if ( foxy == null ) return false;
        return foxy.equals(player.getName());
    }

    /**
     * Foxyを取得する
     * @return foxy
     */
    public String getFoxy() {
        return foxy;
    }

    /**
     * FredBearを取得する
     * @return fredbear
     */
    public String getFredBear() {
        return fredbear;
    }

    /**
     * 指定されたプレイヤーが、ゲームのプレイヤーとして参加しているかどうかを判定する
     * @param player プレイヤー
     * @return playerかどうか
     */
    public boolean isPlayer(Player player) {
        return players.contains(player.getName());
    }

    /**
     * 指定されたプレイヤーのバッテリー残量を返す
     * @param player プレイヤー
     * @return バッテリー残量
     */
    public double getBatteryLevel(Player player) {
        if ( !batteries.containsKey(player.getName()) ) {
            return -1;
        }
        return batteries.get(player.getName()).getPower();
    }

    /**
     * 指定されたプレイヤーのバッテリー量を減らす
     * @param player プレイヤー
     * @param amount 減らす量
     * @return バッテリー残量
     */
    public double decreaseBattery(Player player, double amount) {
        if ( !batteries.containsKey(player.getName()) ) {
            return -1;
        }
        batteries.get(player.getName()).decrease(amount);
        return batteries.get(player.getName()).getPower();
    }

    /**
     * ゲームセッションタスクを追加する
     * @param task タスク
     */
    protected void addTask(GameSessionTask task) {
        tasks.add(task);
    }

    /**
     * 指定されたプレイヤーが観客かどうかを返す
     * @param player プレイヤー
     * @return 観客かどうか
     */
    public boolean isSpectator(Player player) {
        return spectators.contains(player.getName());
    }

    /**
     * 現在のNightを返す
     * @return 現在のNight
     */
    public Night getNight() {
        return night;
    }

    /**
     * FredBearが行動時間を終了した時に呼び出されるメソッド
     */
    public void onFredBearMovementEnd() {
        if ( fredbear == null ) return;
        Player player = Utility.getPlayerExact(fredbear);
        if ( player == null || !player.isOnline() ) return;
        sendInfoToPlayer(fredbear, Messages.get("Info_FoxyMovementEnd"));
        effectManager.removeEffect(fredbear, SpeedEffect.TYPE);
        int speed = config.getMoveSpeed(night).getFredbear();
        effectManager.applyEffect(fredbear, new SpeedEffect(player, speed));
    }

    /**
     * Foxyが行動時間を終了した時に呼び出されるメソッド
     */
    public void onFoxyMovementEnd() {
        if ( foxy == null ) return;
        Player player = Utility.getPlayerExact(foxy);
        if ( player == null || !player.isOnline() ) return;
        sendInfoToPlayer(foxy, Messages.get("Info_FoxyMovementEnd"));
        Location respawn = FiveNightsAtFreddysInMinecraft
                .getInstance().getLocationManager().getFoxy();
        player.teleport(respawn, TeleportCause.PLUGIN);
        effectManager.removeEffect(foxy, SpeedEffect.TYPE);
        effectManager.applyEffect(foxy, new BindEffect(foxy));
    }

    /**
     * Freddyがテレポート用アイテムを取得する時に呼び出されるメソッド
     * @param target ターゲットプレイヤー
     */
    public void onFreddyItemGet(String target) {
        if ( freddy == null ) return;
        Player player = Utility.getPlayerExact(freddy);
        if ( player == null ) return;
        sendInfoToPlayer(freddy, Messages.get("Info_FreddyTeleportItem",
                "%player", target));
        ItemStack skull = new ItemStack(Material.SKULL_ITEM, 1, (short)3);
        SkullMeta meta = (SkullMeta)skull.getItemMeta();
        meta.setOwner(target);
        meta.setDisplayName(Messages.get("ItemName_FreddyTeleport", "%target", target));
        skull.setItemMeta(meta);
        player.getInventory().addItem(skull);
    }

    /**
     * FredBearがテレポート用アイテムを取得する時に呼び出されるメソッド
     * @param target ターゲットプレイヤー
     */
    public void onFredBearItemGet(String target) {
        if ( fredbear == null ) return;
        Player player = Utility.getPlayerExact(fredbear);
        if ( player == null ) return;
        sendInfoToPlayer(fredbear, Messages.get("Info_FreddyTeleportItem",
                "%player", target));
        ItemStack skull = new ItemStack(Material.SKULL_ITEM, 1, (short)3);
        SkullMeta meta = (SkullMeta)skull.getItemMeta();
        meta.setOwner(target);
        meta.setDisplayName(Messages.get("ItemName_FreddyTeleport", "%target", target));
        skull.setItemMeta(meta);
        player.getInventory().addItem(skull);
    }

    /**
     * Freddyがテレポート後に行動可能になる時に呼び出されるメソッド
     */
    public void onFreddyTPWaitEnd(String name, int speed) {

        // freddyの行動不能を解除する
        sendInfoToPlayer(name, Messages.get("Info_FreddyTeleportWaitEnd"));
        effectManager.removeEffect(name, BindEffect.TYPE);

        // freddyの行動スピードを適用する
        if ( speed != -99 ) {
            effectManager.applyEffect(name, new SpeedEffect(name, speed));
        } else {
            freddyReturn = true;
        }
    }

    /**
     * Foxyが現在行動中かどうかを返す
     * @return 行動中かどうか
     */
    private boolean isFoxyMoving() {
        if ( tasks == null ) return false;
        for ( GameSessionTask task : tasks ) {
            if ( task instanceof FoxyMovementTask && !task.isEnded() ) {
                return true;
            }
        }
        return false;
    }

    /**
     * FredBearが現在行動中かどうかを返す
     * @return 行動中かどうか
     */
    private boolean isFredBearMoving() {
        if ( tasks == null ) return false;
        for ( GameSessionTask task : tasks ) {
            if ( task instanceof FredBearMovementTask && !task.isEnded() ) {
                return true;
            }
        }
        return false;
    }

    /**
     * プレイヤー用アイテムを配布する
     * @param player
     */
    private void sendPlayerInventory(String name) {
        Player player = Utility.getPlayerExact(name);
        if ( player == null || !player.isOnline() ) {
            return;
        }
        player.getInventory().addItem(
                flashlightOff.clone(), radar.clone());
        player.getInventory().setItem(2, shutterOff.clone());
        updateInventory(player);
    }

    /**
     * 観客用のアイテムを配布する
     * @param name
     */
    private void sendSpectatorInventory(String name) {
//        Player player = Utility.getPlayerExact(name);
//        if ( player == null || !player.isOnline() ) {
//            return;
//        }
//        player.getInventory().addItem(spectatorTeleport.clone());
//        updateInventory(player);
        // 現在、配布するものはない。
    }

    private void sendFredBearInventory(String name) {
        if ( name == null ) return;
        Player player = Utility.getPlayerExact(name);
        if ( player == null || !player.isOnline() ) {
            return;
        }
        player.getInventory().setHelmet(new ItemStack(Material.LEATHER_HELMET));
        player.getInventory().setChestplate(new ItemStack(Material.LEATHER_CHESTPLATE));
        player.getInventory().setLeggings(new ItemStack(Material.LEATHER_LEGGINGS));
        player.getInventory().setBoots(new ItemStack(Material.LEATHER_BOOTS));

        giveThreatItemToChica(name);

        int amount = FiveNightsAtFreddysInMinecraft.getInstance()
                .getFNAFIMConfig().getMoveSpeed(night).getFredbearMovement();
        if ( amount >= 0 ) {
            if ( amount == 0 ) {
                amount = (int)(Math.random() * 5) + 1;
            } else if ( amount > 10 ) {
                amount = 10;
            }

            ItemStack leather = new ItemStack(Material.LEATHER);
            ItemMeta meta = leather.getItemMeta();
            int seconds = config.getFoxyMovementSeconds();
            meta.setDisplayName(Messages.get("ItemName_FredBearMovement", "%seconds", seconds));
            leather.setItemMeta(meta);
            leather.setAmount(amount);
            player.getInventory().addItem(leather);
        }
    }

    private void sendFreddyInventory(String name) {
        if ( name == null ) return;
        Player player = Utility.getPlayerExact(name);
        if ( player == null || !player.isOnline() ) {
            return;
        }
        player.getInventory().setHelmet(new ItemStack(Material.LEATHER_HELMET));
        player.getInventory().setChestplate(new ItemStack(Material.LEATHER_CHESTPLATE));
        player.getInventory().setLeggings(new ItemStack(Material.LEATHER_LEGGINGS));
        player.getInventory().setBoots(new ItemStack(Material.LEATHER_BOOTS));
    }

    private void sendChicaInventory(String name) {
        if ( name == null ) return;
        Player player = Utility.getPlayerExact(name);
        if ( player == null || !player.isOnline() ) {
            return;
        }
        player.getInventory().setHelmet(new ItemStack(Material.GOLD_HELMET));
        player.getInventory().setChestplate(new ItemStack(Material.GOLD_CHESTPLATE));
        player.getInventory().setLeggings(new ItemStack(Material.GOLD_LEGGINGS));
        player.getInventory().setBoots(new ItemStack(Material.GOLD_BOOTS));

        giveThreatItemToChica(name);
    }

    private void sendBonnieInventory(String name) {
        if ( name == null ) return;
        Player player = Utility.getPlayerExact(name);
        if ( player == null || !player.isOnline() ) {
            return;
        }
        player.getInventory().setHelmet(new ItemStack(Material.DIAMOND_HELMET));
        player.getInventory().setChestplate(new ItemStack(Material.DIAMOND_CHESTPLATE));
        player.getInventory().setLeggings(new ItemStack(Material.DIAMOND_LEGGINGS));
        player.getInventory().setBoots(new ItemStack(Material.DIAMOND_BOOTS));
    }

    private void sendFoxyInventory(String name) {
        if ( name == null ) return;
        Player player = Utility.getPlayerExact(name);
        if ( player == null || !player.isOnline() ) {
            return;
        }
        player.getInventory().setHelmet(new ItemStack(Material.IRON_HELMET));
        player.getInventory().setChestplate(new ItemStack(Material.IRON_CHESTPLATE));
        player.getInventory().setLeggings(new ItemStack(Material.IRON_LEGGINGS));
        player.getInventory().setBoots(new ItemStack(Material.IRON_BOOTS));

        int amount = FiveNightsAtFreddysInMinecraft.getInstance()
                .getFNAFIMConfig().getMoveSpeed(night).getFoxyMovement();
        if ( amount >= 0 ) {
            if ( amount == 0 ) {
                amount = (int)(Math.random() * 5) + 1;
            } else if ( amount > 10 ) {
                amount = 10;
            }

            ItemStack leather = new ItemStack(Material.LEATHER);
            ItemMeta meta = leather.getItemMeta();
            int seconds = config.getFoxyMovementSeconds();
            meta.setDisplayName(Messages.get("ItemName_FoxyMovement", "%seconds", seconds));
            leather.setItemMeta(meta);
            leather.setAmount(amount);
            player.getInventory().addItem(leather);
        }
    }

    /**
     * Chica, FredBearに威嚇用アイテムを渡す
     * @param name 与えるプレイヤーのプレイヤー名
     */
    public void giveThreatItemToChica(String name) {
        if ( name == null ) return;
        Player player = Utility.getPlayerExact(name);
        if ( player == null || !player.isOnline() ) return;
        player.getInventory().addItem(chicaThreat.clone());
        updateInventory(player);
    }

    /**
     * チャットログを記録する
     * @param player プレイヤー
     * @param message チャット
     */
    protected void addChatLog(Player player, String message) {
        logger.log(String.format("<%s> %s", player.getDisplayName(), message));
    }

    /**
     * 指定したプレイヤーに、テレポート用のメニューインベントリを開く
     * @param player プレイヤー
     */
    protected void openTeleportMenuInventory(Player player) {

        Inventory inv = Bukkit.createInventory(player, 9 * 6, Messages.get("ItemName_SpectatorTeleport"));
        ArrayList<String> targets = new ArrayList<String>();
        if ( freddy != null ) targets.add(freddy);
        if ( chica != null ) targets.add(chica);
        if ( bonnie != null ) targets.add(bonnie);
        if ( foxy != null ) targets.add(foxy);
        targets.addAll(players);

        for ( String name : targets ) {
            Player target = Utility.getPlayerExact(name);
            if ( target == null ) continue;
            ItemStack skull = new ItemStack(Material.SKULL_ITEM, 1, (short)3);
            SkullMeta meta = (SkullMeta)skull.getItemMeta();
            meta.setOwner(target.getName());
            meta.setDisplayName(Messages.get("ItemName_SpectatorTeleportItem", "%target", target.getDisplayName()));
            skull.setItemMeta(meta);
            inv.addItem(skull);
        }

        player.openInventory(inv);
    }

    /**
     * インベントリのアップデートを行う
     * @param player 更新対象のプレイヤー
     */
    @SuppressWarnings("deprecation")
    private void updateInventory(Player player) {
        player.updateInventory();
    }

    /**
     * プレイヤーに情報を流す
     * @param name プレイヤー名
     * @param message 情報
     */
    private void sendInfoToPlayer(String name, String message) {
        if ( name == null ) return;
        sendInfoToPlayer(Utility.getPlayerExact(name), message);
    }

    /**
     * プレイヤーに情報を流す
     * @param player プレイヤー
     * @param message 情報
     */
    private void sendInfoToPlayer(Player player, String message) {
        if ( player == null || !player.isOnline() ) {
            return;
        }
        message = Messages.get("Prefix_Info") + message;
        player.sendMessage(message);
    }

    /**
     * ゲーム内にアナウンスを流す
     * @param message メッセージ
     */
    private void sendInGameAnnounce(String message) {
        message = Messages.get("Prefix_InGame") + message;
        for ( String name : entrants ) {
            Player player = Utility.getPlayerExact(name);
            if ( player != null && player.isOnline() ) {
                player.sendMessage(message);
            }
        }
        for ( String name : spectators ) {
            Player player = Utility.getPlayerExact(name);
            if ( player != null && player.isOnline() ) {
                player.sendMessage(message);
            }
        }
        logger.log(message);
    }

    /**
     * ブロードキャストメッセージを流す
     * @param message メッセージ
     */
    private void sendBroadcastAnnounce(String message) {
        message = Messages.get("Prefix_Broadcast") + message;
        Bukkit.broadcastMessage(message);
        logger.log(message);
    }

    /**
     * 全てのプレイヤーにSEを流す
     * @param sound
     */
    private void sendInGameSound(SoundComponent sound) {
        for ( String name : players ) {
            Player player = Utility.getPlayerExact(name);
            if ( player != null && player.isOnline() ) {
                sound.playSoundToPlayer(player);
            }
        }
        for ( String name : spectators ) {
            Player player = Utility.getPlayerExact(name);
            if ( player != null && player.isOnline() ) {
                sound.playSoundToPlayer(player);
            }
        }
    }

    /**
     * 全てのプレイヤーにタイトルコマンドを流す
     * @param message
     */
    private void sendInGameTitle(String message) {
        for ( String name : entrants ) {
            Player player = Utility.getPlayerExact(name);
            if ( player != null && player.isOnline() ) {
                TitleDisplayComponent.display(player, message, 0, 40, 40);
            }
        }
        for ( String name : spectators ) {
            Player player = Utility.getPlayerExact(name);
            if ( player != null && player.isOnline() ) {
                TitleDisplayComponent.display(player, message, 0, 40, 40);
            }
        }
        logger.log(message);
    }

    /**
     * アイテムの表示名を取得する
     * @param item アイテム
     * @return 表示名、なければnull
     */
    private String getDisplayName(ItemStack item) {
        if ( item == null ) return null;
        if ( !item.hasItemMeta() ) return null;
        if ( !item.getItemMeta().hasDisplayName() ) return null;
        return item.getItemMeta().getDisplayName();
    }

    /**
     * アイテムの作成を行う
     */
    private void makeItems() {

        flashlightOff = new ItemStack(Material.IRON_INGOT);
        ItemMeta meta = flashlightOff.getItemMeta();
        meta.setDisplayName(Messages.get("ItemName_FlashLight"));
        flashlightOff.setItemMeta(meta);

        flashlightOn = new ItemStack(Material.GOLD_INGOT);
        meta = flashlightOn.getItemMeta();
        meta.setDisplayName(Messages.get("ItemName_FlashLight"));
        flashlightOn.setItemMeta(meta);

        radar = new ItemStack(Material.REDSTONE);
        meta = radar.getItemMeta();
        meta.setDisplayName(Messages.get("ItemName_Rader"));
        radar.setItemMeta(meta);

        shutterOff = new ItemStack(Material.IRON_DOOR);
        meta = shutterOff.getItemMeta();
        meta.setDisplayName(Messages.get("ItemName_Shutter"));
        shutterOff.setItemMeta(meta);

        shutterOn = new ItemStack(Material.WOOD_DOOR);
        meta = shutterOn.getItemMeta();
        meta.setDisplayName(Messages.get("ItemName_Shutter"));
        shutterOn.setItemMeta(meta);

        chicaThreat = new ItemStack(Material.GLOWSTONE_DUST);
        meta = chicaThreat.getItemMeta();
        meta.setDisplayName(Messages.get("ItemName_ChicaThreat"));
        chicaThreat.setItemMeta(meta);
    }

    /**
     * 役割を返す
     * @param name プレイヤー名
     * @return 役割
     */
    private Doll getDollRole(String name) {

        if ( name == null ) {
            return null;
        } else if ( name.equals(freddy) ) {
            return Doll.FREDDY;
        } else if ( name.equals(chica) ) {
            return Doll.CHICA;
        } else if ( name.equals(bonnie) ) {
            return Doll.BONNIE;
        } else if ( name.equals(foxy) ) {
            return Doll.FOXY;
        } else if ( name.equals(fredbear) ) {
            return Doll.FREDBEAR;
        }
        return null;
    }

    /**
     * 指定された名前のプレイヤーのインベントリをからっぽにする
     * @param name
     */
    private void removeInventoryAll(String name) {
        Player player = Utility.getPlayerExact(name);
        if ( player == null ) return;
        player.getInventory().clear();
        player.getInventory().setHelmet(new ItemStack(Material.AIR));
        player.getInventory().setChestplate(new ItemStack(Material.AIR));
        player.getInventory().setLeggings(new ItemStack(Material.AIR));
        player.getInventory().setBoots(new ItemStack(Material.AIR));
        updateInventory(player);
    }

    /**
     * 指定された名前のプレイヤーに、指定の速度設定を適用する。
     * @param name プレイヤー名
     * @param setting 速度設定。-99の場合は動けなくする。
     */
    private void applyMoveSpeedSetting(String name, int setting) {
        if ( name == null ) return;
        Player player = Utility.getPlayerExact(name);
        if ( player == null || !player.isOnline() ) {
            return;
        }
        if ( setting == -99 ) {
            effectManager.applyEffect(name, new BindEffect(player));
        } else {
            effectManager.applyEffect(name, new SpeedEffect(player, setting));
        }
    }
}
