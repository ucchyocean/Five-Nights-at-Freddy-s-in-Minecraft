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
import org.bitbucket.ucchy.fnafim.effect.SpeedEffect;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

/**
 * ゲームセッション
 * @author ucchy
 */
public class GameSession {

    private GameSessionPhase phase;

    private CommandSender owner;
    private List<Player> entrants;
    private List<Player> players;
    private List<Player> spectators;
    private Player freddy;
    private Player chica;
    private Player bonnie;
    private Player foxy;

    private GameSessionTimer timer;
    private GameSessionLogger logger;

    private TemporaryStorage storage;
    private EffectManager effectManager;

    private Night night;

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

        FiveNightsAtFreddysInMinecraft.getInstance().removeGameSession();
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
            if ( getDollRoleString(player) == null ) {
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
            effectManager.addEffect(player, new BlindnessEffect(player));
        }

        // Freddyのエフェクトの設定
        FNAFIMConfig config = FiveNightsAtFreddysInMinecraft.getInstance().getFNAFIMConfig();
        int nightnum = night.getNum();
        if ( nightnum <= 2 ) {
            // night2までは動けない
            effectManager.addEffect(freddy, new BindEffect(freddy));
        } else if ( nightnum <= 5 ) {
            // night3 - 5 は移動速度エフェクト
            effectManager.addEffect(freddy, new SpeedEffect(freddy, (nightnum - 4)));
        } else if ( nightnum == 6 ) {
            // night6 は移動速度エフェクトlv1
            effectManager.addEffect(freddy, new SpeedEffect(freddy, 1));
        } else if ( nightnum == 7 ) {
            // night7 はカスタム
            effectManager.addEffect(freddy, new SpeedEffect(
                    freddy, config.getCustomNightMoveSpeed_freddy()));
        }

        // ChicaとBonnieのエフェクト設定
        if ( nightnum <= 5 ) {
            // night1 - 5 は移動速度エフェクト
            effectManager.addEffect(chica, new SpeedEffect(chica, (nightnum - 4)));
            effectManager.addEffect(bonnie, new SpeedEffect(bonnie, (nightnum - 4)));
        } else if ( nightnum <= 6 ) {
            // night6 は移動速度エフェクトlv1
            effectManager.addEffect(chica, new SpeedEffect(chica, 1));
            effectManager.addEffect(bonnie, new SpeedEffect(bonnie, 1));
        } else if ( nightnum == 7 ) {
            // night7 はカスタム
            effectManager.addEffect(chica, new SpeedEffect(
                    chica, config.getCustomNightMoveSpeed_chica()));
            effectManager.addEffect(bonnie, new SpeedEffect(
                    bonnie, config.getCustomNightMoveSpeed_bonnie()));
        }

        // Foxyのエフェクト設定、常に移動不可にしておく
        effectManager.addEffect(foxy, new BindEffect(foxy));


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

        new DelayedTeleportTask(locationMap, 3, 20).startTask();

        // そのままゲームを開始する。
        sendInGameAnnounce(night.toString());
        startGame();
    }

    /**
     * ゲームを開始する。
     */
    private void startGame() {

        phase = GameSessionPhase.IN_GAME;

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

        // タイマーの停止
        if ( timer != null ) {
            timer.end();
        }

        // エフェクトのステータスをクリア
        for ( Player player : entrants ) {
            effectManager.removeAllEffect(player);
        }

        // プレイヤーに持ち物を返す
        for ( Player player : entrants ) {
            storage.restoreFromTemp(player);
        }

        // 全員をロビーに送る
        HashMap<Player, Location> locationMap = new HashMap<Player, Location>();
        LocationManager lmanager = FiveNightsAtFreddysInMinecraft.getInstance().getLocationManager();
        for ( Player player : entrants ) {
            locationMap.put(player, lmanager.getLobby());
        }
        new DelayedTeleportTask(locationMap, 3, 20).startTask();
    }

    /**
     * プレイヤーが捕まった時に呼び出される。
     * @param player 捕まったプレイヤー
     * @param caught 捕まえたプレイヤー
     */
    protected void onCaughtPlayer(Player player, Player caught) {

        Doll doll = getDollRoleString(caught);
        sendInGameAnnounce(player.getName() + " が " + caught.getName() + "(" + doll + ") に捕まった！");

        // TODO エフェクトなどのステータスをクリア

        // TODO プレイヤーに持ち物を返す

        // TODO ロビーに送る

        // TODO 全滅したら、onLoseGame() を呼びだす。
    }

    /**
     * プレイヤーが全滅したときに呼び出される。
     */
    protected void onLoseGame() {

        phase = GameSessionPhase.END;
        // TODO
    }

    /**
     * 1秒ごとに呼び出される。
     * @param remain 残り秒数
     */
    protected void onTimerSeconds(int remain) {
        // TODO
    }

    /**
     * タイマーが0になった時に呼び出される。
     */
    protected void onTimerZero() {
        // TODO
    }

    /**
     * タイマーがキャンセルされた時に呼び出される。
     */
    protected void onTimerCanceled() {
        // TODO
    }

    /**
     * 参加者がクリックを実行した時に呼び出される。
     * @param player プレイヤー
     * @return
     */
    protected boolean onEntrantInteract(Player player) {

        // 実行者が観客なら、全てキャンセル
        if ( spectators.contains(player) ) {
            return false;
        }

        // TODO プレイヤーのアイテム処理

        // TODO Foxyのアイテム処理

        return true;
    }

    /**
     * 人形側陣営プレイヤーがプレイヤーをクリックした時に呼び出される。
     * @param player
     * @param target
     * @return
     */
    protected boolean onTouch(Player player, Player target) {

        // TODO

        return true;
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
     * プレイヤー用アイテムを配布する
     * @param player
     */
    private void sendPlayerInventory(Player player) {

        ItemStack light = new ItemStack(Material.REDSTONE_TORCH_OFF);
        ItemMeta meta = light.getItemMeta();
        meta.setDisplayName("懐中電灯");
        light.setItemMeta(meta);

        ItemStack rader = new ItemStack(Material.REDSTONE);
        meta = rader.getItemMeta();
        meta.setDisplayName("レーダー");
        rader.setItemMeta(meta);

        ItemStack shutter = new ItemStack(Material.IRON_DOOR);
        meta = shutter.getItemMeta();
        meta.setDisplayName("シャッター");
        shutter.setItemMeta(meta);

        player.getInventory().addItem(light, rader, shutter);
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
        ItemStack leather = new ItemStack(Material.LEATHER, amount);
        ItemMeta meta = leather.getItemMeta();
        meta.setDisplayName("高速移動");
        leather.setItemMeta(meta);
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
     * 役割を返す
     * @param player プレイヤー
     * @return 役割
     */
    protected Doll getDollRoleString(Player player) {

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
