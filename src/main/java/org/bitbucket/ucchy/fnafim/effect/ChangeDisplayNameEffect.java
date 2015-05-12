package org.bitbucket.ucchy.fnafim.effect;

import org.bitbucket.ucchy.fnafim.Utility;
import org.bukkit.entity.Player;

/**
 * 名前の表示を変更する特殊エフェクト
 * @author ucchy
 */
public class ChangeDisplayNameEffect implements SpecialEffect {

    public static final String TYPE = "ChangeDisplayName";

    private Player player;
    private String displayName;

    public ChangeDisplayNameEffect(String name, String displayName) {
        this.player = Utility.getPlayerExact(name);
        this.displayName = displayName;
    }

    public ChangeDisplayNameEffect(Player player, String displayName) {
        this.player = player;
        this.displayName = displayName;
    }

    @Override
    public void start() {
        if ( player == null ) return;
        player.setDisplayName(displayName);
    }

    @Override
    public void end() {
        if ( player == null ) return;
        player.setDisplayName(player.getName());
    }

    @Override
    public String getTypeString() {
        return TYPE;
    }
}
