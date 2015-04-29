/*
 * @author     ucchy
 * @license    LGPLv3
 * @copyright  Copyright ucchy 2015
 */
package org.bitbucket.ucchy.fnafim.game;

import org.bitbucket.ucchy.fnafim.FiveNightsAtFreddysInMinecraft;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;

/**
 * リスナークラス
 * @author ucchy
 */
public class PlayerListener implements Listener {

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {

        Player player = event.getPlayer();

        // セッションが無いならイベントを無視
        GameSession session = FiveNightsAtFreddysInMinecraft.getInstance().getGameSession();
        if ( session == null ) {
            return;
        }

        // 参加者ではないならイベントを無視
        if ( !session.isEntrant(player) ) {
            return;
        }

        // セッションの方に送って処理する。falseが返されたらイベントをキャンセルする。
        if ( !session.onEntrantInteract(player) ) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onDamage(EntityDamageEvent event) {

        if ( !(event.getEntity() instanceof Player) ) {
            return;
        }

        Player player = (Player)event.getEntity();

        // セッションが無いならイベントを無視
        GameSession session = FiveNightsAtFreddysInMinecraft.getInstance().getGameSession();
        if ( session == null ) {
            return;
        }

        // 参加者ではないならイベントを無視
        if ( !session.isEntrant(player) ) {
            return;
        }

        // イベントを全てキャンセル
        event.setCancelled(true);
    }

    @EventHandler
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {

        Player player = event.getPlayer();

        // セッションが無いならイベントを無視
        GameSession session = FiveNightsAtFreddysInMinecraft.getInstance().getGameSession();
        if ( session == null ) {
            return;
        }

        // 参加者ではないならイベントを無視
        if ( !session.isEntrant(player) ) {
            return;
        }

        // Freddy側陣営プレイヤーで、他のプレイヤーにタッチした場合は、セッションで処理
        Doll doll = session.getDollRoleString(player);
        if ( doll != null && (event.getRightClicked() instanceof Player) ) {
            if ( !session.onTouch(player, (Player)event.getRightClicked() ) ) {
                event.setCancelled(true);
            }
            return;
        }

        // あとは全てイベントをキャンセル
        event.setCancelled(true);
    }
}
