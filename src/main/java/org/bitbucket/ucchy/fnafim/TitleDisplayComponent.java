/*
 * @author     ucchy
 * @license    LGPLv3
 * @copyright  Copyright ucchy 2015
 */
package org.bitbucket.ucchy.fnafim;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * タイトル部分にメッセージを表示するためのコンポーネント
 * @author ucchy
 */
public class TitleDisplayComponent {

    public static void display(Player player, String text,
            int fadein, int duration, int fadeout) {

        if ( !Utility.isCB180orLater() ) {
            player.sendMessage(Utility.replaceColorCode(text));
        }

        // sendCommandFeedbackの状態を取得、有効だったなら一旦無効にする。
        World world = player.getWorld();
        boolean pre = Boolean.parseBoolean(
                world.getGameRuleValue("sendCommandFeedback"));
        if ( pre ) world.setGameRuleValue("sendCommandFeedback", "false");

        // titleコマンドを実行
        CommandSender sender = Bukkit.getConsoleSender();
        String command = String.format("title %s times %d %d %d",
                player.getName(), fadein, duration, fadeout);
        Bukkit.dispatchCommand(sender, command);
//        command = String.format("title %s subtitle {text:\"%s\"}",
//                player.getName(), Utility.replaceColorCode(text));
//        Bukkit.dispatchCommand(sender, command);
        command = String.format("title %s title {text:\"%s\"}",
                player.getName(), Utility.replaceColorCode(text));
        Bukkit.dispatchCommand(sender, command);

        // sendCommandFeedbackの状態を戻す。
        if ( pre ) world.setGameRuleValue("sendCommandFeedback", "true");
    }
}
