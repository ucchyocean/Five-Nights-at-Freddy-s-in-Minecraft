/*
 * @author     ucchy
 * @license    LGPLv3
 * @copyright  Copyright ucchy 2015
 */
package org.bitbucket.ucchy.fnafim;

import org.bitbucket.ucchy.fnafim.game.GameSession;
import org.bitbucket.ucchy.fnafim.game.GameSessionPhase;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;

/**
 * JoinSign のリスナークラス
 * @author ucchy
 */
public class JoinSignListener implements Listener {

    public static final String META_SIGN_COMMAND = "fnafsign";
    public static final String META_SIGN_COMMAND_REMOVE = "fnafsignremove";

    private FiveNightsAtFreddysInMinecraft plugin;

    /**
     * コンストラクタ
     * @param plugin
     */
    public JoinSignListener(FiveNightsAtFreddysInMinecraft plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {

        // ブロックをクリックしたイベントでないなら無視
        if ( event.getAction() != Action.LEFT_CLICK_BLOCK
                && event.getAction() != Action.RIGHT_CLICK_BLOCK ) {
            return;
        }

        // クリック対象が看板でないなら無視
        Block block = event.getClickedBlock();
        if ( block.getType() != Material.SIGN_POST
                && block.getType() != Material.WALL_SIGN ) {
            return;
        }

        Player player = event.getPlayer();
        Sign sign = (Sign)block.getState();
        JoinSignManager manager = plugin.getJoinsignManager();

        if ( !manager.isJoinSign(block) ) {
            // JoinSignでないなら、登録処理に入る

            // メタデータを持っていないプレイヤーなら無視
            if ( !player.hasMetadata(META_SIGN_COMMAND) ) return;

            // この時点でイベントはキャンセル
            event.setCancelled(true);

            // 登録処理

            // JoinSignを登録
            manager.addJoinSign(sign);
            manager.updateAll();

            player.removeMetadata(META_SIGN_COMMAND, plugin);
            sendInformationMessage(player, Messages.get("Info_SignPost"));

            return;

        } else {

            if ( player.hasMetadata(META_SIGN_COMMAND_REMOVE) ) {
                // 登録解除のメタデータを持っているなら、登録解除処理を行う。

                manager.removeJoinSign(sign.getLocation());
                for ( int i=0; i<4; i++ ) {
                    sign.setLine(i, "");
                }
                sign.update();

                player.removeMetadata(META_SIGN_COMMAND_REMOVE, plugin);
                sendInformationMessage(player, Messages.get("Info_SignRemovePost"));
                return;

            } else {
                // JoinSignの実行処理。現在のゲーム状況に応じたコマンドを呼び出す。

                event.setCancelled(true);
                GameSession session = plugin.getGameSession();
                GameSessionPhase phase = (session == null) ? GameSessionPhase.END : session.getPhase();

                switch (phase) {
                case INVITATION:
                    plugin.runJoinCommand(player);
                    break;
                case IN_GAME:
                case PREPARING:
                case PREPARING_NEXT:
                    plugin.runSpectateCommand(player);
                    break;
                case CANCELED:
                case END:
                default:
                    // do nothing.
                    break;
                }

                return;
            }
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {

        // 壊れたブロックがJoinSignでないなら、イベントを無視
        Block block = event.getBlock();
        JoinSignManager manager = plugin.getJoinsignManager();
        if ( !manager.isJoinSign(block) ) return;

        // イベントをキャンセル
        event.setCancelled(true);
    }

    private void sendInformationMessage(CommandSender sender, String message) {
        String msg = Messages.get("Prefix_Info") + message;
        sender.sendMessage(msg);
    }
}
