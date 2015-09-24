/*
 * @author     ucchy
 * @license    LGPLv3
 * @copyright  Copyright ucchy 2015
 */
package org.bitbucket.ucchy.fnafim;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.bitbucket.ucchy.fnafim.game.GameSession;
import org.bitbucket.ucchy.fnafim.game.GameSessionPhase;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.configuration.file.YamlConfiguration;

/**
 * JoinSignの管理クラス
 * @author ucchy
 */
public class JoinSignManager {

    private File file;
    private List<String> signs;

    /**
     * JoinSignManagerクラスをロードする
     * @param file ロードするファイル
     * @return ロードされたJoinSignManager
     */
    public static JoinSignManager load(File file) {

        if ( !file.exists() ) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        JoinSignManager manager = new JoinSignManager();
        manager.file = file;
        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);

        manager.signs = new ArrayList<String>();

        if ( config == null || !config.contains("signs") ) return manager;

        for ( String key : config.getStringList("signs") ) {
            Location loc = getLocationFromDesc(key);
            if ( loc == null ) continue;
            Block block = loc.getBlock();
            if ( block.getType() != Material.SIGN_POST
                    && block.getType() != Material.WALL_SIGN ) {
                continue;
            }
            manager.signs.add(getDescFromLocation(loc));
        }

        return manager;
    }

    /**
     * JoinSignManagerを保存する
     */
    public void save() {

        YamlConfiguration config = new YamlConfiguration();
        config.set("signs", signs);

        try {
            config.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 指定されたブロックが、JoinSignかどうかを判定する。
     * @param block ブロック
     * @return JoinSignかどうか
     */
    public boolean isJoinSign(Block block) {

        if ( block.getType() != Material.SIGN_POST
                && block.getType() != Material.WALL_SIGN ) {
            return false;
        }

        String desc = getDescFromLocation(block.getLocation());
        return signs.contains(desc);
    }

    /**
     * 指定された看板を、JoinSignとして登録する
     * @param sign 看板
     */
    public void addJoinSign(Sign sign) {

        String desc = getDescFromLocation(sign.getLocation());
        if ( !signs.contains(desc) ) {
            signs.add(desc);
            save();
        }
    }

    /**
     * 指定された地点にあったJoinSignを、登録解除する。
     * @param location 地点
     */
    public void removeJoinSign(Location location) {

        String desc = getDescFromLocation(location);
        if ( signs.contains(desc) ) {
            signs.remove(desc);
            save();
        }
    }

    /**
     * 全てのJoinSignの内容を、現在の状況に合わせて更新する。
     */
    public void updateAll() {

        GameSession session = FiveNightsAtFreddysInMinecraft.getInstance().getGameSession();
        GameSessionPhase phase = (session == null) ? GameSessionPhase.END : session.getPhase();
        int max = FiveNightsAtFreddysInMinecraft.getInstance().getFNAFIMConfig().getMaxPlayers();
        int num = 0;
        String status, message;

        switch (phase) {
        case INVITATION:
            status = Messages.get("JoinSign_StatusOpen");
            message = Messages.get("JoinSign_MessageOpen");
            num = session.getEntrants().size();
            break;
        case IN_GAME:
        case PREPARING:
        case PREPARING_NEXT:
            status = Messages.get("JoinSign_StatusIngame");
            message = Messages.get("JoinSign_MessageIngame");
            num = session.getEntrants().size();
            break;
        case CANCELED:
        case END:
        default:
            status = Messages.get("JoinSign_StatusClosed");
            message = Messages.get("JoinSign_MessageClosed");
            break;
        }

        String[] lines = new String[4];
        for ( int i=0; i<4; i++ ) {
            lines[i] = Messages.get("JoinSign_Line" + (i + 1),
                    new String[]{"%num", "%max", "%status", "%message"},
                    new String[]{num + "", max + "", status, message});
        }

        for ( String desc : signs ) {

            Location location = getLocationFromDesc(desc);
            if ( location == null ) {
                continue;
            }

            Block block = location.getBlock();
            if ( block.getType() != Material.SIGN_POST && block.getType() != Material.WALL_SIGN ) {
                continue;
            }

            Sign sign = (Sign)block.getState();
            for ( int i=0; i<4; i++ ) {
                sign.setLine(i, lines[i]);
            }
            sign.update();
        }
    }

    /**
     * Locationから文字列表記を取得する
     * @param location
     * @return
     */
    private static String getDescFromLocation(Location location) {

        String world = location.getWorld().getName();
        int x = location.getBlockX();
        int y = location.getBlockY();
        int z = location.getBlockZ();

        return String.format("%s,%d,%d,%d", world, x, y, z);
    }

    /**
     * 文字列表記からLocationを取得する。取得できない場合はnullが返される。
     * @param desc
     * @return
     */
    private static Location getLocationFromDesc(String desc) {

        String[] temp = desc.split(",");
        if ( temp.length < 4 ) return null;
        World world = Bukkit.getWorld(temp[0]);
        if ( world == null ) return null;
        if ( !temp[1].matches("-?[0-9]+") ) return null;
        if ( !temp[2].matches("-?[0-9]+") ) return null;
        if ( !temp[3].matches("-?[0-9]+") ) return null;

        int x = Integer.parseInt(temp[1]);
        int y = Integer.parseInt(temp[2]);
        int z = Integer.parseInt(temp[3]);

        return new Location(world, x, y, z);
    }
}
