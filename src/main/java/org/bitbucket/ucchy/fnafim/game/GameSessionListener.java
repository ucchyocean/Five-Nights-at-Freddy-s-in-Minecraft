/*
 * @author     ucchy
 * @license    LGPLv3
 * @copyright  Copyright ucchy 2015
 */
package org.bitbucket.ucchy.fnafim.game;

import org.bitbucket.ucchy.fnafim.FiveNightsAtFreddysInMinecraft;
import org.bitbucket.ucchy.fnafim.task.PlayerLogoutTrackingTask;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

/**
 * リスナークラス
 * @author ucchy
 */
public class GameSessionListener implements Listener {

    /**
     * プレイヤーがクリックした時に呼び出されるイベントハンドラ
     * @param event
     */
    @EventHandler(priority=EventPriority.NORMAL)
    public void onPlayerInteract(PlayerInteractEvent event) {

        Player player = event.getPlayer();

        // セッションが無いならイベントを無視
        GameSession session = FiveNightsAtFreddysInMinecraft.getInstance().getGameSession();
        if ( session == null ) {
            return;
        }

        // 観客なら全てをキャンセル
        if ( session.isSpectator(player) ) {
            event.setCancelled(true);
            return;
        }

        // 参加者ではないならイベントを無視
        if ( !session.isEntrant(player) ) {
            return;
        }

        // 感圧板イベント、左クリックイベントなら、イベントを無視
        if ( event.getAction() == Action.PHYSICAL
                || event.getAction() == Action.LEFT_CLICK_AIR
                || event.getAction() == Action.LEFT_CLICK_BLOCK ) {
            return;
        }

        // アイテムの使用判定は、セッションの方に送って処理する。
        // falseが返されたらイベントをキャンセルする。
        if ( !session.onEntrantInteract(player) ) {
            event.setCancelled(true);
        }
    }

    /**
     * エンティティがダメージを受けた時に呼び出されるイベントハンドラ
     * @param event
     */
    @EventHandler(priority=EventPriority.HIGH, ignoreCancelled=true)
    public void onDamage(EntityDamageEvent event) {

        // Playerでないならイベントを無視
        if ( !(event.getEntity() instanceof Player) ) {
            return;
        }

        Player player = (Player)event.getEntity();

        // セッションが無いならイベントを無視
        GameSession session = FiveNightsAtFreddysInMinecraft.getInstance().getGameSession();
        if ( session == null ) {
            return;
        }

        // 参加者または観客なら、全てのダメージを無効化
        if ( session.isEntrant(player) || session.isSpectator(player) ) {
            event.setCancelled(true);
            return;
        }
    }

    /**
     * エンティティがエンティティからダメージを受けた時に呼び出されるイベントハンドラ
     * @param event
     */
    @EventHandler(priority=EventPriority.NORMAL)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {

        // セッションが無いならイベントを無視
        GameSession session = FiveNightsAtFreddysInMinecraft.getInstance().getGameSession();
        if ( session == null ) {
            return;
        }

        // ダメージを受けたのがプレイヤーでないならイベントを無視
        if ( !(event.getEntity() instanceof Player) ) {
            return;
        }
        Player target = (Player)event.getEntity();

        // 参加者ではないならイベントを無視
        if ( !session.isEntrant(target) ) {
            return;
        }

        // イベントはこの時点で全てキャンセル
        event.setCancelled(true);

        // タッチしたプレイヤーも参加者プレイヤーである場合は、セッションで処理
        if ( event.getDamager() instanceof Player ) {
            Player damager = (Player)event.getDamager();
            if ( !session.isEntrant(damager) ) {
                return;
            }
            session.onTouch(damager, target);
            return;
        }
    }

    /**
     * プレイヤーがアイテムを落とした時に呼び出されるイベントハンドラ
     * @param event
     */
    @EventHandler(priority=EventPriority.NORMAL)
    public void onPlayerDropItem(PlayerDropItemEvent event) {

        Player player = event.getPlayer();

        // セッションが無いならイベントを無視
        GameSession session = FiveNightsAtFreddysInMinecraft.getInstance().getGameSession();
        if ( session == null ) {
            return;
        }

        // 参加者または観客なら、全てのイベントをキャンセル
        if ( session.isEntrant(player) || session.isSpectator(player) ) {
            event.setCancelled(true);
            return;
        }
    }

    /**
     * プレイヤーがインベントリをクリックした時に呼び出されるイベントハンドラ
     * @param event
     */
    @EventHandler(priority=EventPriority.NORMAL)
    public void onInventoryClick(InventoryClickEvent event) {

        Player player = (Player)event.getWhoClicked();

        // セッションが無いならイベントを無視
        GameSession session = FiveNightsAtFreddysInMinecraft.getInstance().getGameSession();
        if ( session == null ) {
            return;
        }

        // 参加者または観客なら、全てのイベントをキャンセル
        if ( session.isEntrant(player) || session.isSpectator(player) ) {
            event.setCancelled(true);
            return;
        }
    }

    /**
     * プレイヤーがサーバーに参加した時に呼び出されるイベントハンドラ
     * @param event
     */
    @EventHandler(priority=EventPriority.NORMAL)
    public void onPlayerJoin(PlayerJoinEvent event) {

        Player player = event.getPlayer();

        // セッションが無いならイベントを無視
        GameSession session = FiveNightsAtFreddysInMinecraft.getInstance().getGameSession();
        if ( session == null ) {
            return;
        }

        // ゲーム中でないならイベントを無視
        if ( session.getPhase() != GameSessionPhase.IN_GAME ) {
            return;
        }

        // Foxyなら、リスポーン地点に飛ばして、バインドを設定する
        if ( session.isFoxy(player) ) {
            session.onFoxyMovementEnd();
        }

        // プレイヤーなら、ゲーム中離脱したペナルティとして、バッテリーを20減らす
        if ( session.isPlayer(player) ) {
            session.decreaseBattery(player, 20);
        }
    }

    /**
     * プレイヤーがサーバーを退出した時に呼び出されるイベントハンドラ
     * @param event
     */
    @EventHandler(priority=EventPriority.NORMAL)
    public void onPlayerQuit(PlayerQuitEvent event) {

        Player player = event.getPlayer();

        // セッションが無いならイベントを無視
        GameSession session = FiveNightsAtFreddysInMinecraft.getInstance().getGameSession();
        if ( session == null ) {
            return;
        }

        // ゲーム中でないならイベントを無視
        if ( session.getPhase() != GameSessionPhase.IN_GAME ) {
            return;
        }

        // プレイヤーではないならイベントを無視
        if ( !session.isPlayer(player) ) {
            return;
        }

        // 電力切れプレイヤーなら、即座に脱落させる
        // 電力がまだあるプレイヤーなら、タイマーを動かす
        if ( session.getBatteryLevel(player) == 0 ) {
            session.onCaughtPlayer(player.getName(), null);
        } else {
            int seconds = FiveNightsAtFreddysInMinecraft.getInstance()
                    .getFNAFIMConfig().getPlayerLogoutTrackingSeconds();
            PlayerLogoutTrackingTask task
                    = new PlayerLogoutTrackingTask(session, player.getName(), seconds);
            task.start();
            session.addTask(task);
        }
    }

    /**
     * プレイヤーがチャット発言した時に呼び出されるイベントハンドラ
     * @param event
     */
    @EventHandler(priority=EventPriority.MONITOR, ignoreCancelled=true)
    public void onPlayerChat(AsyncPlayerChatEvent event) {

        Player player = event.getPlayer();

        // セッションが無いならイベントを無視
        GameSession session = FiveNightsAtFreddysInMinecraft.getInstance().getGameSession();
        if ( session == null ) {
            return;
        }

        // ゲームが終了しているならイベントを無視
        if ( session.getPhase() == GameSessionPhase.END
                || session.getPhase() == GameSessionPhase.CANCELED ) {
            return;
        }

        // 参加者または観客ではないならイベントを無視
        if ( !session.isEntrant(player) && !session.isSpectator(player) ) {
            return;
        }

        // チャットログを記録する
        session.addChatLog(player, event.getMessage());
    }
}
