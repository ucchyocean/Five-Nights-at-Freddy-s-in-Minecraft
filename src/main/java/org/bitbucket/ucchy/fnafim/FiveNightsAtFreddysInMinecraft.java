/*
 * @author     ucchy
 * @license    LGPLv3
 * @copyright  Copyright ucchy 2015
 */
package org.bitbucket.ucchy.fnafim;

import java.io.File;
import java.util.List;
import java.util.Locale;

import org.bitbucket.ucchy.fnafim.game.GameSession;
import org.bitbucket.ucchy.fnafim.game.GameSessionListener;
import org.bitbucket.ucchy.fnafim.game.GameSessionPhase;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Five Nights at Freddy's プラグイン
 * @author ucchy
 */
public class FiveNightsAtFreddysInMinecraft extends JavaPlugin {

    private FNAFIMCommand command;
    private FNAFIMConfig config;
    private LocationManager locationManager;
    private JoinSignManager joinsignManager;
    private GameSession session;

    /**
     * プラグインが有効化されたときに呼び出されるメソッド
     * @see org.bukkit.plugin.java.JavaPlugin#onEnable()
     */
    @Override
    public void onEnable() {

        // コマンド
        command = new FNAFIMCommand();

        // コンフィグのロード
        config = new FNAFIMConfig();

        // メッセージをロードする
        File langFolder = new File(getDataFolder(), "lang");
        Messages.initialize(getFile(), langFolder, getDefaultLocaleLanguage());
        Messages.reload(config.getLang());

        // 地点管理のロード
        locationManager = LocationManager.load(new File(getDataFolder(), "location.yml"));
        joinsignManager = JoinSignManager.load(new File(getDataFolder(), "joinsign.yml"));

        // リスナーの登録
        getServer().getPluginManager().registerEvents(new GameSessionListener(), this);
        getServer().getPluginManager().registerEvents(new JoinSignListener(this), this);
    }

    /**
     * プラグインが無効化されたときに呼び出されるメソッド
     * @see org.bukkit.plugin.java.JavaPlugin#onDisable()
     */
    @Override
    public void onDisable() {

        // セッションが残っている場合は、強制中断する。
        if ( session != null && session.getPhase() != GameSessionPhase.CANCELED
                && session.getPhase() != GameSessionPhase.END ) {
            session.cancelGame();
            session = null;
        }
    }

    /**
     * 全ての関連データを再読込する
     */
    public void reloadAll() {
        config.reloadConfig();
        Messages.reload(config.getLang());
        locationManager = LocationManager.load(new File(getDataFolder(), "location.yml"));
        joinsignManager = JoinSignManager.load(new File(getDataFolder(), "joinsign.yml"));
    }

    /**
     * コマンド実行時に呼び出されるメソッド
     * @see org.bukkit.plugin.java.JavaPlugin#onCommand(org.bukkit.command.CommandSender, org.bukkit.command.Command, java.lang.String, java.lang.String[])
     */
    @Override
    public boolean onCommand(
            CommandSender sender, Command command, String label, String[] args) {
        return this.command.onCommand(sender, command, label, args);
    }

    /**
     * TABキー補完が実行されたときに呼び出されるメソッド
     * @see org.bukkit.plugin.java.JavaPlugin#onTabComplete(org.bukkit.command.CommandSender, org.bukkit.command.Command, java.lang.String, java.lang.String[])
     */
    @Override
    public List<String> onTabComplete(
            CommandSender sender, Command command, String label, String[] args) {
        return this.command.onTabComplete(sender, command, label, args);
    }

    /**
     * ゲームセッションを返す
     * @return ゲームセッション
     */
    public GameSession getGameSession() {
        return session;
    }

    /**
     * ゲームセッションを作成して返す
     * @param owner 作成するゲームセッションのオーナー
     * @return 作成されたゲームセッション
     */
    public GameSession createNewGameSession(CommandSender owner) {
        session = new GameSession(owner);
        session.openInvitation();
        return session;
    }

    /**
     * ゲームセッションを削除する
     */
    public void removeGameSession() {
        session = null;
    }

    /**
     * このプラグインのコンフィグを返す
     * @return プラグインのコンフィグ
     */
    public FNAFIMConfig getFNAFIMConfig() {
        return config;
    }

    /**
     * 地点管理を返す
     * @return 地点管理
     */
    public LocationManager getLocationManager() {
        return locationManager;
    }

    /**
     * 参加看板管理を返す
     * @return 参加看板管理
     */
    public JoinSignManager getJoinsignManager() {
        return joinsignManager;
    }

    /**
     * 指定したプレイヤーが、/fn join コマンドを実行したことにする
     * @param player プレイヤー
     */
    public void runJoinCommand(Player player) {
        command.joinCommand(player, null, null, null);
    }

    /**
     * 指定したプレイヤーが、/fn spectate コマンドを実行したことにする
     * @param player プレイヤー
     */
    public void runSpectateCommand(Player player) {
        command.spectateCommand(player, null, null, null);
    }

    /**
     * このプラグインのファイルを返す
     * @return プラグインのファイル
     */
    public File getPluginJarFile() {
        return getFile();
    }

    /**
     * このプラグインのインスタンスを返す
     * @return プラグインのインスタンス
     */
    public static FiveNightsAtFreddysInMinecraft getInstance() {
        return (FiveNightsAtFreddysInMinecraft)
                Bukkit.getPluginManager().getPlugin("FiveNightAtFreddysInMinecraft");
    }

    /**
     * 動作環境の言語設定を取得する。日本語環境なら ja、英語環境なら en が返される。
     * @return 動作環境の言語
     */
    protected static String getDefaultLocaleLanguage() {
        Locale locale = Locale.getDefault();
        if ( locale == null ) return "en";
        return locale.getLanguage();
    }
}
