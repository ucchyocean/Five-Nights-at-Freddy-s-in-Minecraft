package org.bitbucket.ucchy.fnafim.effect;

import org.bukkit.entity.Player;

/**
 * 名前の表示を変更する特殊エフェクト
 * @author ucchy
 */
public class ChangeDisplayNameEffect implements SpecialEffect {

    public static final String TYPE = "ChangeDisplayName";

    private Player player;
    private String name;

    public ChangeDisplayNameEffect(Player player, String name) {
        this.player = player;
        this.name = name;
    }

    @Override
    public void start() {
        player.setDisplayName(name);
    }

    @Override
    public void end() {
        player.setDisplayName(player.getName());
    }

    @Override
    public String getTypeString() {
        return TYPE;
    }
}
