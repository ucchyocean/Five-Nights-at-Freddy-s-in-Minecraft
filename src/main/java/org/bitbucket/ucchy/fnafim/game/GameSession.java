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
import org.bitbucket.ucchy.fnafim.effect.BindEffect;
import org.bitbucket.ucchy.fnafim.effect.BlindnessEffect;
import org.bitbucket.ucchy.fnafim.effect.ChangeDisplayNameEffect;
import org.bitbucket.ucchy.fnafim.effect.HideNametagEffect;
import org.bitbucket.ucchy.fnafim.effect.InvisibleEffect;
import org.bitbucket.ucchy.fnafim.effect.SpeedEffect;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * ゲームセッション
 * @author ucchy
 */
public class GameSession {

    private static final String DISPLAYNAME_FLASHLIGHT = "懐中電灯";
    private static final String DISPLAYNAME_RADER = "レーダー";
    private static final String DISPLAYNAME_SHUTTER = "シャッター";
    private static final String DISPLAYNAME_LEATHER = "行動を開始 30秒の間だけ行動できます。";

    private GameSessionPhase phase;

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
    private ItemStack leather;

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

        makeItems();

        // そのまま募集を開始する
        openInvitation();
    }

    /**
     * 募集を開始する
     */
    public void openInvitation() {

        phase = GameSessionPhase.INVITATION;
        sendBroadcastAnnounce(owner.getName()
                + "が Five Nights at Freddy's in Minecraft の参加者募集を開始しました。");
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
        sendBroadcastAnnounce(owner.getName()
                + "が Five Nights at Freddy's in Minecraft の参加者募集を中断しました。");
    }

    /**
     * ゲームの準備を開始する
     * @param night ゲーム難易度
     */
    public void startPreparing(Night night) {

        phase = GameSessionPhase.PREPARING;
        this.night = night;

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
        }

        if ( chica == null ) {
            int random = (int)(Math.random() * players.size());
            chica = players.get(random);
            players.remove(random);
            sendInGameAnnounce("Chica : " + chica.getName());
        }

        if ( bonnie == null ) {
            int random = (int)(Math.random() * players.size());
            bonnie = players.get(random);
            players.remove(random);
            sendInGameAnnounce("Bonnie : " + bonnie.getName());
        }

        if ( foxy == null ) {
            int random = (int)(Math.random() * players.size());
            foxy = players.get(random);
            players.remove(random);
            sendInGameAnnounce("Foxy : " + foxy.getName());
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
        for ( Player player : players ) {
            effectManager.applyEffect(player, new BlindnessEffect(player));
            effectManager.applyEffect(player, new ChangeDisplayNameEffect(player,
                    ChatColor.AQUA + player.getName() + ChatColor.WHITE + "(Player)"
                            + ChatColor.RESET));
        }

        // Freddyのエフェクトの設定
        FNAFIMConfig config = FiveNightsAtFreddysInMinecraft.getInstance().getFNAFIMConfig();
        int nightnum = night.getNum();
        if ( nightnum <= 2 ) {
            // night2までは動けない
            effectManager.applyEffect(freddy, new BindEffect(freddy));
        } else if ( nightnum <= 5 ) {
            // night3 - 5 は移動速度エフェクト
            effectManager.applyEffect(freddy, new SpeedEffect(freddy, (nightnum - 4)));
        } else if ( nightnum == 6 ) {
            // night6 は移動速度エフェクトlv1
            effectManager.applyEffect(freddy, new SpeedEffect(freddy, 1));
        } else if ( nightnum == 7 ) {
            // night7 はカスタム
            effectManager.applyEffect(freddy, new SpeedEffect(
                    freddy, config.getCustomNightMoveSpeed_freddy()));
        }
        effectManager.applyEffect(freddy, new HideNametagEffect(freddy));
        effectManager.applyEffect(freddy, new ChangeDisplayNameEffect(freddy,
                ChatColor.GOLD + freddy.getName() + ChatColor.RED + "(Freddy)" + ChatColor.RESET));

        // ChicaとBonnieのエフェクト設定
        if ( nightnum <= 5 ) {
            // night1 - 5 は移動速度エフェクト
            effectManager.applyEffect(chica, new SpeedEffect(chica, (nightnum - 4)));
            effectManager.applyEffect(bonnie, new SpeedEffect(bonnie, (nightnum - 4)));
        } else if ( nightnum <= 6 ) {
            // night6 は移動速度エフェクトlv1
            effectManager.applyEffect(chica, new SpeedEffect(chica, 1));
            effectManager.applyEffect(bonnie, new SpeedEffect(bonnie, 1));
        } else if ( nightnum == 7 ) {
            // night7 はカスタム
            effectManager.applyEffect(chica, new SpeedEffect(
                    chica, config.getCustomNightMoveSpeed_chica()));
            effectManager.applyEffect(bonnie, new SpeedEffect(
                    bonnie, config.getCustomNightMoveSpeed_bonnie()));
        }
        effectManager.applyEffect(chica, new HideNametagEffect(chica));
        effectManager.applyEffect(bonnie, new HideNametagEffect(bonnie));

        effectManager.applyEffect(chica, new ChangeDisplayNameEffect(chica,
                ChatColor.GOLD + chica.getName() + ChatColor.RED + "(Chica)" + ChatColor.RESET));
        effectManager.applyEffect(bonnie, new ChangeDisplayNameEffect(bonnie,
                ChatColor.GOLD + bonnie.getName() + ChatColor.RED + "(Bonnie)" + ChatColor.RESET));

        // Foxyのエフェクト設定、常に移動不可にしておく
        effectManager.applyEffect(foxy, new BindEffect(foxy));
        effectManager.applyEffect(foxy, new HideNametagEffect(foxy));
        effectManager.applyEffect(foxy, new ChangeDisplayNameEffect(foxy,
                ChatColor.GOLD + foxy.getName() + ChatColor.RED + "(Foxy)" + ChatColor.RESET));

        // プレイヤーのバッテリー
        batteries = new HashMap<Player, PlayerBattery>();
        for ( Player player : players ) {
            batteries.put(player, new PlayerBattery(player));
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

        new DelayedTeleportTask(locationMap, 3).startTask();

        // サイドバーを用意する
        scoreboardDisplay = new ScoreboardDisplay("fnafim");
        for ( Player player : entrants ) {
            scoreboardDisplay.setShowPlayer(player);
        }
        for ( Player player : spectators ) {
            scoreboardDisplay.setShowPlayer(player);
        }
        scoreboardDisplay.setTitle(ChatColor.RED + night.toString());
        scoreboardDisplay.setRemainTime(config.getSecondsOfOneHour() * 6);
        scoreboardDisplay.setRemainPlayer(players.size());

        // そのままゲームを開始する。
        startGame();
    }

    /**
     * ゲームを開始する。
     */
    private void startGame() {

        phase = GameSessionPhase.IN_GAME;
        sendInGameAnnounce(night.toString());

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
        sendInGameAnnounce("ゲームを強制中断しました。");
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
        new DelayedTeleportTask(locationMap, 3).startTask();
    }

    /**
     * プレイヤーが捕まった時に呼び出される。
     * @param player 捕まったプレイヤー
     * @param caught 捕まえたプレイヤー
     */
    protected void onCaughtPlayer(Player player, Player caught) {

        Doll doll = getDollRole(caught);
        sendInGameAnnounce(player.getName() + " が " + caught.getName() + "(" + doll + ") に捕まった！");

        // エフェクトをクリア
        effectManager.removeAllEffect(player);

        // プレイヤーおよび参加者から削除する
        entrants.remove(player);
        players.remove(player);

        // スコアボードを更新
        scoreboardDisplay.setRemainPlayer(players.size());

        // スコアボードを非表示にする
        player.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());

        // 預かっていた持ち物を返す
        storage.restoreFromTemp(player);

        // ロビーに送る
        Location lobby = FiveNightsAtFreddysInMinecraft.getInstance().getLocationManager().getLobby();
        player.teleport(lobby, TeleportCause.PLUGIN);

        // TODO アイテムを預かったままにして、ロビーに送らず、そのまま観客として参加するようにしたい。

        // 全滅したら、onGameover() を呼びだす。
        if ( players.size() <= 0 ) {
            onGameover();
        }
    }

    /**
     * プレイヤーが全滅したときに呼び出される。
     */
    protected void onGameover() {

        phase = GameSessionPhase.END;
        sendInGameAnnounce("Gameover ");
        sendInGameAnnounce("プレイヤーが全員捕まってしまった。。。");
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
        scoreboardDisplay.setRemainTime(remain);
    }

    /**
     * タイマーが0になった時に呼び出される。
     */
    protected void onTimerZero() {

        phase = GameSessionPhase.END;
        sendInGameAnnounce("6 AM");
        sendInGameAnnounce(players.size() + "人のプレイヤーが生き延びた。");
        onEnd();
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
        if ( name.equals(DISPLAYNAME_FLASHLIGHT) ) {
            boolean isOn = (item.getType() == flashlightOff.getType());
            if ( batteries.containsKey(player) ) {
                if ( batteries.get(player).getPower() <= 0 ) {
                    player.sendMessage(ChatColor.RED + "電力が無いので操作できない！");
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
            return false;
        }

        // レーダーのアイテム処理
        if ( name.equals(DISPLAYNAME_RADER) ) {
            if ( batteries.containsKey(player) ) {
                if ( !batteries.get(player).hasPowerToUserRadar() ) {
                    player.sendMessage(ChatColor.RED + "電力が足りないので使用できない！");
                    return false;
                }
                batteries.get(player).decreaseToUseRadar();
            }
            boolean found = false;
            for ( Entity entity : player.getNearbyEntities(10, 10, 10) ) {
                if ( entity instanceof Player ) {
                    Player target = (Player)entity;
                    Doll doll = getDollRole(target);
                    if ( doll != null ) {
                        double distance = player.getLocation().distance(target.getLocation());
                        if ( distance <= 10 ) {
                            String msg = String.format(
                                    ChatColor.RED + "%s(%s) が、%.1fｍ先にいる！！",
                                    target.getName(), doll.toString(), distance);
                            player.sendMessage(msg);
                            found = true;
                        }
                    }
                }
            }
            if ( !found ) {
                player.sendMessage(ChatColor.AQUA + "誰も近くにいないようだ。。。");
            }
            return false;
        }

        // シャッターのアイテム処理
        if ( name.equals(DISPLAYNAME_SHUTTER) ) {
            boolean isOn = (item.getType() == shutterOff.getType());
            if ( batteries.containsKey(player) ) {
                if ( batteries.get(player).getPower() <= 0 ) {
                    player.sendMessage(ChatColor.RED + "電力が無いので操作できない！");
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
            return false;
        }

        // Foxyのアイテム処理
        if ( name.equals(DISPLAYNAME_LEATHER) ) {

            // 行動不可の状態になっていないなら、アイテムを使う必要は無い
            if ( !effectManager.hasEffect(player, BindEffect.TYPE) ) {
                player.sendMessage(ChatColor.AQUA + "今は使う必要はない。。。");
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
            effectManager.removeEffect(player, BindEffect.TYPE);
            effectManager.applyEffect(player, new SpeedEffect(player, 3));
            player.sendMessage(ChatColor.AQUA + "30秒間行動できるようになった！");
            new BukkitRunnable() {
                public void run() {
                    if ( phase == GameSessionPhase.IN_GAME && player.isOnline() ) {
                        player.sendMessage(ChatColor.AQUA + "行動時間が終了した。");
                        Location respawn = FiveNightsAtFreddysInMinecraft
                                .getInstance().getLocationManager().getFoxy();
                        player.teleport(respawn, TeleportCause.PLUGIN);
                        effectManager.applyEffect(player, new BindEffect(player));
                    }
                }
            }.runTaskLater(FiveNightsAtFreddysInMinecraft.getInstance(), 20 * 30);

            // TODO Foxyが、行動時間中にサーバーから切断してしまったときの対策を考えること。
            //   再ログイン時に、行動不能を再設定して、リスポーン地点に送信するとか。
            // TODO Foxyが、行動時間中のゲームが終了してしまったときの対策を考えること。
            //   処理は保持しておいて、キャンセルできるようにすべきか。

            return false;
        }

        return true;
    }

    /**
     * 参加者プレイヤーが他の参加者プレイヤーをクリックした時に呼び出される。
     * @param player
     * @param target
     */
    protected void onTouch(Player player, Player target) {

        // タッチした人がFreddy陣営でなければ無視
        if ( getDollRole(player) == null ) {
            return;
        }

        // タッチされた人がプレイヤーでなければ無視
        if ( !players.contains(target) ) {
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

            sendInGameAnnounce(player.getName() + "がゲームに参加しました。");
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

            sendInGameAnnounce(player.getName() + "がゲームから離脱しました。");
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
                .getFNAFIMConfig().getFoxyMovementPerNight();
        if ( amount <= 0 ) {
            amount = (int)(Math.random() * 5) + 1;
        } else if ( amount > 5 ) {
            amount = 5;
        }
        ItemStack leather = this.leather.clone();
        leather.setAmount(amount);
        player.getInventory().addItem(leather);
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
     * ゲーム内にアナウンスを流す
     * @param message メッセージ
     */
    private void sendInGameAnnounce(String message) {
        message = ChatColor.RED + "[FNAF]" + ChatColor.AQUA + message;
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
        message = ChatColor.RED + "[FNAF]" + ChatColor.GOLD + message;
        Bukkit.broadcastMessage(message);
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
        meta.setDisplayName(DISPLAYNAME_FLASHLIGHT);
        flashlightOff.setItemMeta(meta);

        flashlightOn = new ItemStack(Material.GOLD_INGOT);
        meta = flashlightOn.getItemMeta();
        meta.setDisplayName(DISPLAYNAME_FLASHLIGHT);
        flashlightOn.setItemMeta(meta);

        radar = new ItemStack(Material.REDSTONE);
        meta = radar.getItemMeta();
        meta.setDisplayName(DISPLAYNAME_RADER);
        radar.setItemMeta(meta);

        shutterOff = new ItemStack(Material.IRON_DOOR);
        meta = shutterOff.getItemMeta();
        meta.setDisplayName(DISPLAYNAME_SHUTTER);
        shutterOff.setItemMeta(meta);

        shutterOn = new ItemStack(Material.WOOD_DOOR);
        meta = shutterOn.getItemMeta();
        meta.setDisplayName(DISPLAYNAME_SHUTTER);
        shutterOn.setItemMeta(meta);

        leather = new ItemStack(Material.LEATHER);
        meta = leather.getItemMeta();
        meta.setDisplayName(DISPLAYNAME_LEATHER);
        leather.setItemMeta(meta);
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
}
