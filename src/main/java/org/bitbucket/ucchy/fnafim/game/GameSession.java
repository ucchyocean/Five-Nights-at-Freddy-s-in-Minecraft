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
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
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
    private List<Player> entrants;
    private List<Player> players;
    private List<Player> spectators;
    private Player freddy;
    private Player chica;
    private Player bonnie;
    private Player foxy;

    private HashMap<Player, PlayerBattery> batteries;

    private GameSessionTimer timer;
    private GameSessionLogger logger;

    private TemporaryStorage storage;
    private EffectManager effectManager;
    private ScoreboardDisplay scoreboardDisplay;

    private Night night;

    private ItemStack flashlightOn;
    private ItemStack flashlightOff;
    private ItemStack radar;
    private ItemStack shutterOn;
    private ItemStack shutterOff;

    private FoxyMovementTask foxyMovementTask;
    private ArrayList<FreddyItemWaitTask> freddyItemWaitTask;
    private FreddyTeleportWaitTask freddyTeleportWaitTask;

    /**
     * コンストラクタ
     * @param owner オーナー
     */
    public GameSession(CommandSender owner) {

        this.owner = owner;
        entrants = new ArrayList<Player>();
        players = new ArrayList<Player>();
        spectators = new ArrayList<Player>();

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
        sendBroadcastAnnounce(Messages.get("Announce_CloseInvitation", "%owner", owner.getName()));
    }

    /**
     * ゲームの準備を開始する
     * @param night ゲーム難易度
     */
    public void startPreparing(Night night) {

        phase = GameSessionPhase.PREPARING;
        this.night = night;
        this.freddyItemWaitTask = new ArrayList<FreddyItemWaitTask>();

        // 役割を設定する。
        players.clear();
        for ( Player player : entrants ) {
            if ( getDollRole(player) == null ) {
                players.add(player);
            }
        }

        if ( freddy == null ) {
            int random = (int)(Math.random() * players.size());
            freddy = players.get(random);
            players.remove(random);
            sendInGameAnnounce("Freddy : " + freddy.getName());
            sendInfoToPlayer(freddy, Messages.get("Description_Freddy"));
        }

        if ( chica == null ) {
            int random = (int)(Math.random() * players.size());
            chica = players.get(random);
            players.remove(random);
            sendInGameAnnounce("Chica : " + chica.getName());
            sendInfoToPlayer(chica, Messages.get("Description_Chica"));
        }

        if ( bonnie == null ) {
            int random = (int)(Math.random() * players.size());
            bonnie = players.get(random);
            players.remove(random);
            sendInGameAnnounce("Bonnie : " + bonnie.getName());
            sendInfoToPlayer(bonnie, Messages.get("Description_Bonnie"));
        }

        if ( foxy == null ) {
            int random = (int)(Math.random() * players.size());
            foxy = players.get(random);
            players.remove(random);
            sendInGameAnnounce("Foxy : " + foxy.getName());
            sendInfoToPlayer(foxy, Messages.get("Description_Foxy"));
        }

        // 装備を配布する
        for ( Player player : players ) {
            sendPlayerInventory(player);
        }
        sendFreddyInventory(freddy);
        sendChicaInventory(chica);
        sendBonnieInventory(bonnie);
        sendFoxyInventory(foxy);

        // エフェクトの設定
        for ( Player player : entrants ) {
            effectManager.removeAllEffect(player);
        }

        // プレイヤーのエフェクト設定
        for ( Player player : players ) {
            effectManager.applyEffect(player, new BlindnessEffect(player));
            effectManager.applyEffect(player, new ChangeDisplayNameEffect(player,
                    ChatColor.AQUA + player.getName() + ChatColor.WHITE + "(Player)"
                            + ChatColor.RESET));
        }

        // Freddyのエフェクトの設定
        applyMoveSpeedSetting(freddy, config.getMoveSpeed(night).getFreddy());
        effectManager.applyEffect(freddy, new ChangeDisplayNameEffect(freddy,
                ChatColor.GOLD + freddy.getName() + ChatColor.RED + "(Freddy)" + ChatColor.RESET));

        // Chicaのエフェクト設定
        applyMoveSpeedSetting(chica, config.getMoveSpeed(night).getChica());
        effectManager.applyEffect(chica, new ChangeDisplayNameEffect(chica,
                ChatColor.GOLD + chica.getName() + ChatColor.RED + "(Chica)" + ChatColor.RESET));

        // Bonnieのエフェクト設定
        applyMoveSpeedSetting(bonnie, config.getMoveSpeed(night).getBonnie());
        effectManager.applyEffect(bonnie, new ChangeDisplayNameEffect(bonnie,
                ChatColor.GOLD + bonnie.getName() + ChatColor.RED + "(Bonnie)" + ChatColor.RESET));

        // Foxyのエフェクト設定、常に移動不可にしておく
        effectManager.applyEffect(foxy, new BindEffect(foxy));
        effectManager.applyEffect(foxy, new ChangeDisplayNameEffect(foxy,
                ChatColor.GOLD + foxy.getName() + ChatColor.RED + "(Foxy)" + ChatColor.RESET));

        // 観客のエフェクト設定
        for ( Player player : spectators ) {
            effectManager.applyEffect(player, new InvisibleEffect(player));
            effectManager.applyEffect(player, new SpeedEffect(player, 3));
        }

        // プレイヤーのバッテリー
        batteries = new HashMap<Player, PlayerBattery>();
        for ( Player player : players ) {
            batteries.put(player, new PlayerBattery(player, this));
        }

        // それぞれのスタート地点にTPする
        LocationManager lmanager =
                FiveNightsAtFreddysInMinecraft.getInstance().getLocationManager();
        HashMap<Player, Location> locationMap = new HashMap<Player, Location>();
        for ( Player player : players ) {
            locationMap.put(player, lmanager.getPlayer());
        }
        locationMap.put(freddy, lmanager.getFreddy());
        locationMap.put(chica, lmanager.getChica());
        locationMap.put(bonnie, lmanager.getBonnie());
        locationMap.put(foxy, lmanager.getFoxy());

        new DelayedTeleportTask(locationMap, TELEPORT_WAIT_TICKS).startTask();

        // サイドバーを用意する
        scoreboardDisplay = new ScoreboardDisplay("fnafim");
        for ( Player player : entrants ) {
            scoreboardDisplay.setShowPlayer(player);
        }
        for ( Player player : spectators ) {
            scoreboardDisplay.setShowPlayer(player);
        }
        scoreboardDisplay.setTitle(ChatColor.RED + night.toString());
        scoreboardDisplay.setRemainTime(night, 12);
        scoreboardDisplay.setRemainPlayer(players.size());

        for ( Player player : players ) {
            scoreboardDisplay.setPlayersTeam(player);
        }
        scoreboardDisplay.setFreddysTeam(freddy);
        scoreboardDisplay.setFreddysTeam(chica);
        scoreboardDisplay.setFreddysTeam(bonnie);
        scoreboardDisplay.setFreddysTeam(foxy);

        // プレイヤー人数✕TELEPORT_WAIT＋α 分だけ待ってから、ゲームを開始する。
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
        sendInGameAnnounce(Messages.get("Announce_GameCanceled"));
        onEnd();
    }

    /**
     * セッションの最後に呼び出されるメソッド
     */
    private void onEnd() {

        // タイマーの停止
        if ( timer != null ) {
            timer.end();
            timer = null;
        }

        // サイドバーの除去
        scoreboardDisplay.remove();
        for ( Player player : entrants ) {
            player.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
        }
        for ( Player player : spectators ) {
            player.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
        }

        // エフェクトをクリア
        for ( Player player : entrants ) {
            effectManager.removeAllEffect(player);
        }
        for ( Player player : spectators ) {
            effectManager.removeAllEffect(player);
        }

        // タスクをクリア
        if ( foxyMovementTask != null && !foxyMovementTask.isEnded() ) {
            foxyMovementTask.end();
            foxyMovementTask = null;
        }
        for ( FreddyItemWaitTask task : freddyItemWaitTask ) {
            if ( !task.isEnded() ) {
                task.end();
            }
        }
        freddyItemWaitTask.clear();
        if ( freddyTeleportWaitTask != null && !freddyTeleportWaitTask.isEnded() ) {
            freddyTeleportWaitTask.end();
            freddyTeleportWaitTask = null;
        }

        // プレイヤーに持ち物を返す
        for ( Player player : entrants ) {
            storage.restoreFromTemp(player);
        }
        for ( Player player : spectators ) {
            storage.restoreFromTemp(player);
        }

        // 全員をロビーに送る
        HashMap<Player, Location> locationMap = new HashMap<Player, Location>();
        LocationManager lmanager = FiveNightsAtFreddysInMinecraft.getInstance().getLocationManager();
        for ( Player player : entrants ) {
            locationMap.put(player, lmanager.getLobby());
        }
        for ( Player player : spectators ) {
            locationMap.put(player, lmanager.getLobby());
        }
        new DelayedTeleportTask(locationMap, TELEPORT_WAIT_TICKS).startTask();

        // セッションを消去する
        FiveNightsAtFreddysInMinecraft.getInstance().removeGameSession();
    }

    /**
     * プレイヤーが捕まった時に呼び出される。
     * @param player 捕まったプレイヤー
     * @param caught 捕まえたプレイヤー
     */
    protected void onCaughtPlayer(final Player player, Player caught) {

        if ( caught != null ) {
            Doll doll = getDollRole(caught);
            sendInGameAnnounce(Messages.get("Announce_PlayerCaught",
                    new String[]{"%player", "%caught", "%doll"},
                    new String[]{player.getName(), caught.getName(), doll.toString()}
            ));
        } else {
            sendInGameAnnounce(Messages.get("Announce_PlayerRunMidway",
                    "%player", player.getName()));
        }

        // SEを再生（プレイヤーは捕まると観客のリスポーン地点に飛ばされるので、
        // 飛んだ先でSEが流れるように、2ticks遅らせて再生する）
        player.getWorld().playEffect(player.getLocation(), Effect.STEP_SOUND, 10);
        new BukkitRunnable() {
            public void run() {
                config.getSoundPlayerCaught().playSoundToPlayer(player);
                player.getWorld().playEffect(player.getLocation(), Effect.STEP_SOUND, 10);
            }
        }.runTaskLater(FiveNightsAtFreddysInMinecraft.getInstance(), 2);

        // エフェクトをクリア
        effectManager.removeAllEffect(player);

        // 持ち物をクリア
        player.getInventory().clear();

        // プレイヤーおよび参加者から削除する
        entrants.remove(player);
        players.remove(player);

        // スコアボードを更新
        scoreboardDisplay.setRemainPlayer(players.size());
        scoreboardDisplay.leavePlayersTeam(player);

        if ( players.size() <= 0 ) {
            // 全滅したら、onGameover() を呼びだす。
            onGameover();

            // スコアボードを非表示にする
            player.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());

            // 預かっていた持ち物を返す
            storage.restoreFromTemp(player);

            // ロビーに送る
            Location lobby = FiveNightsAtFreddysInMinecraft.getInstance().getLocationManager().getLobby();
            player.teleport(lobby, TeleportCause.PLUGIN);

        } else {
            // 観客として再参加させる
            joinSpectator(player);

        }
    }

    /**
     * プレイヤーが全滅したときに呼び出される。
     */
    protected void onGameover() {

        phase = GameSessionPhase.END;
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

        phase = GameSessionPhase.END;
        sendInGameTitle(Messages.get("Announce_NightClear1"));
        sendInGameAnnounce(Messages.get("Announce_NightClear2", "%num", players.size()));

        // SEを流す
        sendInGameSound(config.getSoundNightEnd());

        // 持ち物を消去する、エフェクトを消去する
        for ( Player player : entrants ) {
            removeInventoryAll(player);
            effectManager.removeAllEffect(player);
        }

        // Night1-4は、次の夜の準備。その他は終了.
        final Night next = night.getNext();
        if ( next != null ) {

            // タスクをクリア
            if ( foxyMovementTask != null && !foxyMovementTask.isEnded() ) {
                foxyMovementTask.end();
                foxyMovementTask = null;
            }
            for ( FreddyItemWaitTask task : freddyItemWaitTask ) {
                if ( !task.isEnded() ) {
                    task.end();
                }
            }
            freddyItemWaitTask.clear();
            if ( freddyTeleportWaitTask != null && !freddyTeleportWaitTask.isEnded() ) {
                freddyTeleportWaitTask.end();
                freddyTeleportWaitTask = null;
            }

            // エフェクトをクリア
            for ( Player player : entrants ) {
                effectManager.removeAllEffect(player);
            }
            for ( Player player : spectators ) {
                effectManager.removeAllEffect(player);
            }

            // 15秒後に、startPreparingを呼び出す
            int wait = config.getSecondsOfNightInterval();
            new BukkitRunnable() {
                public void run() {
                    startPreparing(next);
                }
            }.runTaskLater(FiveNightsAtFreddysInMinecraft.getInstance(), wait * 20);
            sendInGameAnnounce(Messages.get("Announce_NextNight",
                    new String[]{"%seconds", "%night"},
                    new Object[]{wait, night}
            ));

        } else {
            onEnd();
            sendInGameAnnounce(Messages.get("Announce_GameClear"));
        }
    }

    /**
     * 参加者がクリックを実行した時に呼び出される。
     * @param player プレイヤー
     * @return
     */
    protected boolean onEntrantInteract(final Player player) {

        // 実行者が観客なら、全てキャンセル
        if ( spectators.contains(player) ) {
            return false;
        }

        // 表示名のないアイテムを持っていたなら無視。
        ItemStack item = player.getItemInHand();
        String name = getDisplayName(item);
        if ( name == null ) {
            return true;
        }

        // 懐中電灯のアイテム処理
        if ( name.equals(Messages.get("ItemName_FlashLight")) ) {
            boolean isOn = (item.getType() == flashlightOff.getType());
            if ( batteries.containsKey(player) ) {
                if ( batteries.get(player).getPower() <= 0 ) {
                    sendInfoToPlayer(player, Messages.get("Info_BatteryNotEnough"));
                    return false;
                }
                batteries.get(player).setUsingFlashlight(isOn);
            }
            if ( isOn ) {
                player.setItemInHand(flashlightOn.clone());
                effectManager.removeEffect(player, BlindnessEffect.TYPE);
            } else {
                player.setItemInHand(flashlightOff.clone());
                effectManager.applyEffect(player, new BlindnessEffect(player));
            }
            config.getSoundUseFlashLight().playSoundToPlayer(player);
            return false;
        }

        // レーダーのアイテム処理
        if ( name.equals(Messages.get("ItemName_Rader")) ) {
            if ( batteries.containsKey(player) ) {
                if ( !batteries.get(player).hasPowerToUserRadar() ) {
                    sendInfoToPlayer(player, Messages.get("Info_BatteryNotEnough"));
                    return false;
                }
                batteries.get(player).decreaseToUseRadar();
            }
            boolean found = false;
            int range = config.getRaderSearchingRange();
            for ( Entity entity : player.getNearbyEntities(range, range, range) ) {
                if ( entity instanceof Player ) {
                    Player target = (Player)entity;
                    Doll doll = getDollRole(target);
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
            if ( batteries.containsKey(player) ) {
                if ( batteries.get(player).getPower() <= 0 ) {
                    sendInfoToPlayer(player, Messages.get("Info_BatteryNotEnough"));
                    return false;
                }
                batteries.get(player).setUsingShutter(isOn);
            }
            if ( isOn ) {
                player.setItemInHand(shutterOn.clone());
                effectManager.applyEffect(player, new InvisibleEffect(player));
            } else {
                player.setItemInHand(shutterOff.clone());
                effectManager.removeEffect(player, InvisibleEffect.TYPE);
            }
            config.getSoundUseShutter().playSoundToPlayer(player);
            return false;
        }

        // Foxyのアイテム処理
        if ( name.equals(Messages.get(
                "ItemName_FoxyMovement", "%seconds", config.getFoxyMovementSeconds())) ) {

            // 行動不可の状態になっていないなら、アイテムを使う必要は無い
            if ( foxyMovementTask != null && !foxyMovementTask.isEnded() ) {
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
            effectManager.removeEffect(player, BindEffect.TYPE);
            effectManager.applyEffect(player, new SpeedEffect(player, speed));
            sendInfoToPlayer(player, Messages.get("Info_FoxyMovementStart", "%seconds", seconds));
            foxyMovementTask = new FoxyMovementTask(this, seconds);
            foxyMovementTask.start();

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
                    freddy.teleport(target, TeleportCause.PLUGIN);
                    freddyTeleportWaitTask = new FreddyTeleportWaitTask(this, wait);
                    freddyTeleportWaitTask.start();
                    sendInfoToPlayer(freddy, Messages.get("Info_FreddyTeleportWait", "%seconds", wait));
                    effectManager.applyEffect(freddy, new BindEffect(freddy));

                    // SEを流す
                    config.getSoundFreddyTeleport().playSoundToWorld(target.getLocation());
                }
            }

            // アイテム消費
            player.setItemInHand(new ItemStack(Material.AIR));
        }

        return true;
    }

    /**
     * 参加者プレイヤーのバッテリーがダウンした時に呼び出される。
     * @param player
     */
    protected void onBatteryDown(Player player) {

        // 手持ちのアイテムを全て無くす
        player.getInventory().clear();

        // 懐中電灯オフ
        effectManager.applyEffect(player, new BlindnessEffect(player));

        // シャッターオフ
        effectManager.removeEffect(player, InvisibleEffect.TYPE);

        // プレイヤーを停止する
        effectManager.applyEffect(player, new BindEffect(player));

        // フレディにテレポート用アイテムを渡す
        int wait = (int)(Math.random() * 8) + 1;
        FreddyItemWaitTask task = new FreddyItemWaitTask(this, player, wait);
        task.start();
        freddyItemWaitTask.add(task);
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
        if ( getDollRole(player) == null ) {
            return;
        }

        // タッチした人がバインド中なら無視
        if ( player.hasMetadata(BindEffect.TYPE) ) {
            return;
        }

        // タッチされた人がプレイヤーでなければ無視
        if ( !players.contains(target) ) {
            return;
        }

        // タッチされた人が透明化していたら無視
        if ( effectManager.hasEffect(target, InvisibleEffect.TYPE) ) {
            return;
        }

        // 捕まえた！
        onCaughtPlayer(target, player);

        return;
    }

    /**
     * プレイヤーを参加者に追加する
     * @param player 参加者
     */
    public void joinEntrant(Player player) {

        if ( !entrants.contains(player) ) {
            entrants.add(player);

            // プレイヤーの持ち物を預かる
            storage.sendToTemp(player);

            // ポーション効果を除去する、回復しておく
            for ( PotionEffect e : player.getActivePotionEffects() ) {
                player.removePotionEffect(e.getType());
            }
            player.setFoodLevel(20);
            player.setHealth(player.getMaxHealth());

            sendInGameAnnounce(Messages.get("Announce_EntrantJoin", "%player", player.getName()));
        }
    }

    /**
     * プレイヤーを参加者から離脱させる。
     * 離脱可能なフェーズ（INVITATION）であることをあらかじめ確認すること。
     * @param player 離脱する人
     */
    public void leaveEntrant(Player player) {

        if ( entrants.contains(player) ) {
            entrants.remove(player);

            // 預かっていた持ち物を返す
            storage.restoreFromTemp(player);

            sendInGameAnnounce(Messages.get("Announce_EntrantLeave", "%player", player.getName()));
        }
    }

    /**
     * 観客としてゲーム参加する
     * @param player
     */
    public void joinSpectator(Player player) {

        if ( !spectators.contains(player) ) {
            spectators.add(player);
        }

        // サイドバーを表示する
        scoreboardDisplay.setShowPlayer(player);

        // 透明化する、高速化する
        effectManager.applyEffect(player, new InvisibleEffect(player));
        effectManager.applyEffect(player, new SpeedEffect(player, 3));

        // 持ち物を預かる
        if ( !storage.isInventoryExists(player) ) {
            storage.sendToTemp(player);
        }

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

        if ( spectators.contains(player) ) {
            spectators.remove(player);

            // サイドバーを消去する
            player.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());

            // エフェクトを除去する
            effectManager.removeAllEffect(player);

            // 預かっていた持ち物を返す
            storage.restoreFromTemp(player);

            // ロビーに移動する
            Location lobby = FiveNightsAtFreddysInMinecraft.getInstance().getLocationManager().getLobby();
            player.teleport(lobby, TeleportCause.PLUGIN);

            sendInfoToPlayer(player, Messages.get("Info_LeaveSpectator"));
        }
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
        return entrants.contains(player);
    }

    /**
     * 参加者を全て取得する
     * @return 参加者
     */
    public List<Player> getEntrants() {
        return entrants;
    }

    /**
     * 指定されたプレイヤーがFoxyかどうかを判定する
     * @param player プレイヤー
     * @return Foxyかどうか
     */
    public boolean isFoxy(Player player) {
        return foxy.equals(player);
    }

    public void setFreddy(Player freddy) {
        this.freddy = freddy;
    }

    public void setChica(Player chica) {
        this.chica = chica;
    }

    public void setBonnie(Player bonnie) {
        this.bonnie = bonnie;
    }

    public void setFoxy(Player foxy) {
        this.foxy = foxy;
    }

    /**
     * 指定されたプレイヤーが、ゲームのプレイヤーとして参加しているかどうかを判定する
     * @param player プレイヤー
     * @return playerかどうか
     */
    public boolean isPlayer(Player player) {
        return players.contains(player);
    }

    /**
     * 指定されたプレイヤーのバッテリー残量を返す
     * @param player プレイヤー
     * @return バッテリー残量
     */
    public double getPowerLevel(Player player) {
        if ( !batteries.containsKey(player) ) {
            return -1;
        }
        return batteries.get(player).getPower();
    }

    /**
     * 指定されたプレイヤーが観客かどうかを返す
     * @param player プレイヤー
     * @return 観客かどうか
     */
    public boolean isSpectator(Player player) {
        return spectators.contains(player);
    }

    /**
     * 現在のNightを返す
     * @return 現在のNight
     */
    public Night getNight() {
        return night;
    }

    /**
     * Foxyが行動時間を終了した時に呼び出されるメソッド
     */
    protected void onFoxyMovementEnd() {
        sendInfoToPlayer(foxy, Messages.get("Info_FoxyMovementEnd"));
        Location respawn = FiveNightsAtFreddysInMinecraft
                .getInstance().getLocationManager().getFoxy();
        foxy.teleport(respawn, TeleportCause.PLUGIN);
        effectManager.removeEffect(foxy, SpeedEffect.TYPE);
        effectManager.applyEffect(foxy, new BindEffect(foxy));
    }

    /**
     * Freddyがテレポート用アイテムを取得する時に呼び出されるメソッド
     * @param target ターゲットプレイヤー
     */
    protected void onFreddyItemGet(Player target) {
        sendInfoToPlayer(freddy, Messages.get("Info_FreddyTeleportItem",
                "%player", target.getName()));
        ItemStack skull = new ItemStack(Material.SKULL_ITEM, 1, (short)3);
        SkullMeta meta = (SkullMeta)skull.getItemMeta();
        meta.setOwner(target.getName());
        meta.setDisplayName(Messages.get("ItemName_FreddyTeleport", "%target", target.getName()));
        skull.setItemMeta(meta);
        freddy.getInventory().addItem(skull);
    }

    /**
     * Freddyがテレポート後に行動可能になる時に呼び出されるメソッド
     */
    protected void onFreddyTPWaitEnd() {
        sendInfoToPlayer(freddy, Messages.get("Info_FreddyTeleportWaitEnd"));
        effectManager.removeEffect(freddy, BindEffect.TYPE);
    }

    /**
     * プレイヤー用アイテムを配布する
     * @param player
     */
    private void sendPlayerInventory(Player player) {

        player.getInventory().addItem(
                flashlightOff.clone(), radar.clone(), shutterOff.clone());
        updateInventory(player);
    }

    private void sendFreddyInventory(Player player) {
        player.getInventory().setHelmet(new ItemStack(Material.LEATHER_HELMET));
        player.getInventory().setChestplate(new ItemStack(Material.LEATHER_CHESTPLATE));
        player.getInventory().setLeggings(new ItemStack(Material.LEATHER_LEGGINGS));
        player.getInventory().setBoots(new ItemStack(Material.LEATHER_BOOTS));
    }

    private void sendChicaInventory(Player player) {
        player.getInventory().setHelmet(new ItemStack(Material.GOLD_HELMET));
        player.getInventory().setChestplate(new ItemStack(Material.GOLD_CHESTPLATE));
        player.getInventory().setLeggings(new ItemStack(Material.GOLD_LEGGINGS));
        player.getInventory().setBoots(new ItemStack(Material.GOLD_BOOTS));
    }

    private void sendBonnieInventory(Player player) {
        player.getInventory().setHelmet(new ItemStack(Material.DIAMOND_HELMET));
        player.getInventory().setChestplate(new ItemStack(Material.DIAMOND_CHESTPLATE));
        player.getInventory().setLeggings(new ItemStack(Material.DIAMOND_LEGGINGS));
        player.getInventory().setBoots(new ItemStack(Material.DIAMOND_BOOTS));
    }

    private void sendFoxyInventory(Player player) {
        player.getInventory().setHelmet(new ItemStack(Material.IRON_HELMET));
        player.getInventory().setChestplate(new ItemStack(Material.IRON_CHESTPLATE));
        player.getInventory().setLeggings(new ItemStack(Material.IRON_LEGGINGS));
        player.getInventory().setBoots(new ItemStack(Material.IRON_BOOTS));

        int amount = FiveNightsAtFreddysInMinecraft.getInstance()
                .getFNAFIMConfig().getMoveSpeed(night).getFoxyMovement();
        if ( amount >= 0 ) {
            if ( amount == 0 ) {
                amount = (int)(Math.random() * 5) + 1;
            } else if ( amount > 5 ) {
                amount = 5;
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
     * インベントリのアップデートを行う
     * @param player 更新対象のプレイヤー
     */
    @SuppressWarnings("deprecation")
    private void updateInventory(Player player) {
        player.updateInventory();
    }

    /**
     * プレイヤーに情報を流す
     * @param player プレイヤー
     * @param message 情報
     */
    private void sendInfoToPlayer(Player player, String message) {
        message = Messages.get("Prefix_Info") + message;
        player.sendMessage(message);
    }

    /**
     * ゲーム内にアナウンスを流す
     * @param message メッセージ
     */
    private void sendInGameAnnounce(String message) {
        message = Messages.get("Prefix_InGame") + message;
        for ( Player player : entrants ) {
            player.sendMessage(message);
        }
        for ( Player player : spectators ) {
            player.sendMessage(message);
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
        for ( Player player : players ) {
            sound.playSoundToPlayer(player);
        }
        for ( Player player : spectators ) {
            sound.playSoundToPlayer(player);
        }
    }

    /**
     * 全てのプレイヤーにタイトルコマンドを流す
     * @param message
     */
    private void sendInGameTitle(String message) {
        for ( Player player : entrants ) {
            TitleDisplayComponent.display(player, message, 0, 40, 40);
        }
        for ( Player player : spectators ) {
            TitleDisplayComponent.display(player, message, 0, 40, 40);
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
    }

    /**
     * 役割を返す
     * @param player プレイヤー
     * @return 役割
     */
    private Doll getDollRole(Player player) {

        if ( player == null ) {
            return null;
        } else if ( player.equals(freddy) ) {
            return Doll.FREDDY;
        } else if ( player.equals(chica) ) {
            return Doll.CHICA;
        } else if ( player.equals(bonnie) ) {
            return Doll.BONNIE;
        } else if ( player.equals(foxy) ) {
            return Doll.FOXY;
        }
        return null;
    }

    private void removeInventoryAll(Player player) {
        player.getInventory().clear();
        player.getInventory().setHelmet(new ItemStack(Material.AIR));
        player.getInventory().setChestplate(new ItemStack(Material.AIR));
        player.getInventory().setLeggings(new ItemStack(Material.AIR));
        player.getInventory().setBoots(new ItemStack(Material.AIR));
    }

    private void applyMoveSpeedSetting(Player player, int setting) {
        if ( setting == -99 ) {
            effectManager.applyEffect(player, new BindEffect(player));
        } else {
            effectManager.applyEffect(player, new SpeedEffect(player, setting));
        }
    }
}
