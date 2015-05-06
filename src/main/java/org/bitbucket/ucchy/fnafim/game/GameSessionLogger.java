/*
 * @author     ucchy
 * @license    LGPLv3
 * @copyright  Copyright ucchy 2015
 */
package org.bitbucket.ucchy.fnafim.game;

import java.io.File;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.bitbucket.ucchy.fnafim.FiveNightsAtFreddysInMinecraft;
import org.bukkit.ChatColor;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * ゲームセッションロガー
 * @author ucchy
 */
public class GameSessionLogger {

    private File file;
    private SimpleDateFormat lformat;

    /**
     * コンストラクタ
     * @param folder ログ出力フォルダ
     */
    public GameSessionLogger(File folder) {

        if ( !folder.exists() ) {
            folder.mkdirs();
        }

        SimpleDateFormat fndformat = new SimpleDateFormat("yyyyMMdd-HHmmss");
        String fileName = fndformat.format(new Date()) + "-log.txt";
        file = new File(folder, fileName);

        lformat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    }

    /**
     * ログを出力する
     * @param message ログ内容
     */
    public synchronized void log(final String message) {

        // 以降の処理を、発言処理の負荷軽減のため、非同期実行にする。
        FiveNightsAtFreddysInMinecraft plugin =
                FiveNightsAtFreddysInMinecraft.getInstance();
        if ( plugin.isEnabled() ) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    writeLogInternal(message);
                }
            }.runTaskAsynchronously(plugin);
        } else {
            writeLogInternal(message);
        }
    }

    private void writeLogInternal(String message) {

        String msg = ChatColor.stripColor(message).replace(",", "，");
        FileWriter writer = null;
        try {
            writer = new FileWriter(file, true);
            String str = lformat.format(new Date()) + ", " + msg;
            writer.write(str + "\r\n");
            writer.flush();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if ( writer != null ) {
                try {
                    writer.close();
                } catch (Exception e) {
                    // do nothing.
                }
            }
        }
    }
}
