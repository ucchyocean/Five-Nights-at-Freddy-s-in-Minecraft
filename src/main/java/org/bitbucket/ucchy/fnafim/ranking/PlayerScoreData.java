/*
 * @author     ucchy
 * @license    LGPLv3
 * @copyright  Copyright ucchy 2015
 */
package org.bitbucket.ucchy.fnafim.ranking;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.UUID;

import org.bitbucket.ucchy.fnafim.Utility;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.YamlConfiguration;

/**
 * ランキング用のプレイヤースコアデータ
 * @author ucchy
 */
public class PlayerScoreData {

    private static File saveFolder;

    private static HashMap<UUID, PlayerScoreData> cache;

    private File file;

    /** プレイヤー名 */
    private String name;

    /** プレイヤーID */
    private UUID id;

    // 各種統計情報
    private int playerGamePlayed;
    private int playerGameWin;
    private int playerGameLose;
    private int animatronicsGamePlayed;
    private int animatronicsGameWin;
    private int animatronicsGameLose;
    private int animatronicsCatchPlayers;
    private int score;

    /**
     * コンストラクタ。
     */
    private PlayerScoreData() {
    }

    /**
     * コンストラクタ。
     * @param player プレイヤー
     */
    private PlayerScoreData(OfflinePlayer player) {
        this.name = player.getName();
        this.id = player.getUniqueId();
        this.playerGamePlayed = 0;
        this.playerGameWin = 0;
        this.playerGameLose = 0;
        this.animatronicsGamePlayed = 0;
        this.animatronicsGameWin = 0;
        this.animatronicsGameLose = 0;
        this.animatronicsCatchPlayers = 0;
        this.score = 0;
    }

    /**
     * このオブジェクトを保存する
     */
    public void save() {

        if ( file == null ) {
            file = new File(saveFolder, id.toString() + ".yml");
        }

        YamlConfiguration config = new YamlConfiguration();
        config.set("name", name);
        config.set("playerGamePlayed", playerGamePlayed);
        config.set("playerGameWin", playerGameWin);
        config.set("playerGameLose", playerGameLose);
        config.set("animatronicsGamePlayed", animatronicsGamePlayed);
        config.set("animatronicsGameWin", animatronicsGameWin);
        config.set("animatronicsGameLose", animatronicsGameLose);
        config.set("animatronicsCatchPlayers", animatronicsCatchPlayers);
        config.set("score", score);

        try {
            config.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * ファイルからロードする
     * @param file ファイル
     * @return ロードされたスコアデータ
     */
    private static PlayerScoreData load(File file) {

        String idstr = file.getName().substring(0, file.getName().length() - 4);
        if ( !Utility.isUUID(idstr) ) return null;

        PlayerScoreData data = new PlayerScoreData();
        data.id = UUID.fromString(idstr);

        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
        data.name = config.getString("name", "xxx");
        data.playerGamePlayed = config.getInt("playerGamePlayed", 0);
        data.playerGameWin = config.getInt("playerGameWin", 0);
        data.playerGameLose = config.getInt("playerGameLose", 0);
        data.animatronicsGamePlayed = config.getInt("animatronicsGamePlayed", 0);
        data.animatronicsGameWin = config.getInt("animatronicsGameWin", 0);
        data.animatronicsGameLose = config.getInt("animatronicsGameLose", 0);
        data.animatronicsCatchPlayers = config.getInt("animatronicsCatchPlayers", 0);
        data.score = config.getInt("score", 0);

        return data;
    }

    /**
     * 全データを再読み込みして、キャッシュを初期化する。
     */
    public static void initCache(File saveFolder) {

        PlayerScoreData.saveFolder = saveFolder;
        if ( !saveFolder.exists() ) saveFolder.mkdirs();

        cache = new HashMap<UUID, PlayerScoreData>();
        for ( PlayerScoreData data : getAllData() ) {
            cache.put(data.id, data);
        }
    }

    /**
     * プレイヤーに対応したユーザーデータを取得する
     * @param id プレイヤーID
     * @return PlayerScoreData
     */
    public static PlayerScoreData getData(UUID id) {

        if ( id == null ) {
            return null;
        }

        if ( cache.containsKey(id) ) {
            return cache.get(id);
        }

        String filename = id.toString() + ".yml";
        File file = new File(saveFolder, filename);
        if ( !file.exists() ) {
            OfflinePlayer player = Bukkit.getPlayer(id);
            if ( player == null ) player = Bukkit.getOfflinePlayer(id);
            if ( player == null ) return null;
            cache.put(id, new PlayerScoreData(player));
            return cache.get(id);
        }

        PlayerScoreData data = load(file);
        cache.put(id, data);
        return cache.get(id);
    }

    /**
     * プレイヤーに対応したユーザーデータを取得する
     * @param name プレイヤー名
     * @return PlayerScoreData
     */
    public static PlayerScoreData getData(String name) {

        if ( name == null ) return null;
        OfflinePlayer player = Utility.getOfflinePlayer(name);
        if ( player == null ) return null;
        return getData(player.getUniqueId());
    }

    /**
     * 全てのユーザーデータをまとめて返す。
     * @return 全てのユーザーデータ。
     */
    public static ArrayList<PlayerScoreData> getAllData() {

        if ( cache != null && cache.size() > 0 ) {
            return new ArrayList<PlayerScoreData>(cache.values());
        }

        String[] filelist = saveFolder.list(new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return name.endsWith(".yml");
            }
        });

        ArrayList<PlayerScoreData> results = new ArrayList<PlayerScoreData>();
        for ( String f : filelist ) {
            String id = f.substring(0, f.indexOf(".") );
            results.add(getData(UUID.fromString(id)));
        }

        return results;
    }

    public String getName() {
        return name;
    }

    public int getPlayerGamePlayed() {
        return playerGamePlayed;
    }

    public int getPlayerGameWin() {
        return playerGameWin;
    }

    public int getPlayerGameLose() {
        return playerGameLose;
    }

    public int getAnimatronicsGamePlayed() {
        return animatronicsGamePlayed;
    }

    public int getAnimatronicsGameWin() {
        return animatronicsGameWin;
    }

    public int getAnimatronicsGameLose() {
        return animatronicsGameLose;
    }

    public int getAnimatronicsCatchPlayers() {
        return animatronicsCatchPlayers;
    }

    public int getScore() {
        return score;
    }

    public void increasePlayerGamePlayed(int num) {
        this.playerGamePlayed += num;
        save();
    }

    public void increasePlayerGameWin(int num) {
        this.playerGameWin += num;
        save();
    }

    public void increasePlayerGameLose(int num) {
        this.playerGameLose += num;
        save();
    }

    public void increaseAnimatronicsGamePlayed(int num) {
        this.animatronicsGamePlayed += num;
        save();
    }

    public void increaseAnimatronicsGameWin(int num) {
        this.animatronicsGameWin += num;
        save();
    }

    public void increaseAnimatronicsGameLose(int num) {
        this.animatronicsGameLose += num;
        save();
    }

    public void increaseAnimatronicsCatchPlayers(int num) {
        this.animatronicsCatchPlayers += num;
        save();
    }

    public void increaseScore(int num) {
        this.score += num;
        save();
    }

    /**
     * ArrayList&lt;BPUserData&gt; 型の配列を、プレイ回数降順にソートする。
     * @param data ソート対象の配列
     */
    public static void sortByGamePlayed(ArrayList<PlayerScoreData> data) {

        Collections.sort(data, new Comparator<PlayerScoreData>() {
            public int compare(PlayerScoreData ent1, PlayerScoreData ent2) {
                return (ent2.playerGamePlayed + ent2.animatronicsGamePlayed)
                        - (ent1.playerGamePlayed + ent1.animatronicsGamePlayed);
            }
        });
    }

    /**
     * ArrayList&lt;BPUserData&gt; 型の配列を、プレイヤーとしての勝利回数降順にソートする。
     * @param data ソート対象の配列
     */
    public static void sortByPlayerGameWin(ArrayList<PlayerScoreData> data) {

        Collections.sort(data, new Comparator<PlayerScoreData>() {
            public int compare(PlayerScoreData ent1, PlayerScoreData ent2) {
                return ent2.playerGameWin - ent1.playerGamePlayed;
            }
        });
    }

    /**
     * ArrayList&lt;BPUserData&gt; 型の配列を、プレイヤーとしての敗北回数降順にソートする。
     * @param data ソート対象の配列
     */
    public static void sortByPlayerGameLose(ArrayList<PlayerScoreData> data) {

        Collections.sort(data, new Comparator<PlayerScoreData>() {
            public int compare(PlayerScoreData ent1, PlayerScoreData ent2) {
                return ent2.playerGameLose - ent1.playerGameLose;
            }
        });
    }

    /**
     * ArrayList&lt;BPUserData&gt; 型の配列を、勝利回数降順にソートする。
     * @param data ソート対象の配列
     */
    public static void sortByGameWin(ArrayList<PlayerScoreData> data) {

        Collections.sort(data, new Comparator<PlayerScoreData>() {
            public int compare(PlayerScoreData ent1, PlayerScoreData ent2) {
                return (ent2.playerGameWin + ent2.animatronicsGameWin)
                        - (ent1.playerGameWin + ent1.animatronicsGameWin);
            }
        });
    }

    /**
     * ArrayList&lt;BPUserData&gt; 型の配列を、勝利回数降順にソートする。
     * @param data ソート対象の配列
     */
    public static void sortByGameLose(ArrayList<PlayerScoreData> data) {

        Collections.sort(data, new Comparator<PlayerScoreData>() {
            public int compare(PlayerScoreData ent1, PlayerScoreData ent2) {
                return (ent2.playerGameLose + ent2.animatronicsGameLose)
                        - (ent1.playerGameLose + ent1.animatronicsGameLose);
            }
        });
    }

    /**
     * ArrayList&lt;BPUserData&gt; 型の配列を、捕まえたプレイヤー数の降順にソートする。
     * @param data ソート対象の配列
     */
    public static void sortByAnimatronicsCatchPlayers(ArrayList<PlayerScoreData> data) {

        Collections.sort(data, new Comparator<PlayerScoreData>() {
            public int compare(PlayerScoreData ent1, PlayerScoreData ent2) {
                return ent2.animatronicsCatchPlayers - ent1.animatronicsCatchPlayers;
            }
        });
    }

    /**
     * ArrayList&lt;BPUserData&gt; 型の配列を、スコアの降順にソートする。
     * @param data ソート対象の配列
     */
    public static void sortByScore(ArrayList<PlayerScoreData> data) {

        Collections.sort(data, new Comparator<PlayerScoreData>() {
            public int compare(PlayerScoreData ent1, PlayerScoreData ent2) {
                return ent2.score - ent1.score;
            }
        });
    }
}
