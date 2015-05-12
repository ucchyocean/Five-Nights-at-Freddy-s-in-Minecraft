/*
 * @author     ucchy
 * @license    LGPLv3
 * @copyright  Copyright ucchy 2015
 */
package org.bitbucket.ucchy.fnafim.effect;

import org.bitbucket.ucchy.fnafim.Utility;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

/**
 * 透明になるエフェクト
 * @author ucchy
 */
public class InvisibleEffect implements SpecialEffect {

    public static final String TYPE = "Invisible";

    private Player player;

    public InvisibleEffect(String name) {
        this.player = Utility.getPlayerExact(name);
    }

    public InvisibleEffect(Player player) {
        this.player = player;
    }

    @Override
    public void start() {
        if ( player == null ) return;
        if ( Utility.isCB180orLater() ) {
            player.addPotionEffect(new PotionEffect(
                    PotionEffectType.INVISIBILITY, Integer.MAX_VALUE, 3, true, false));
        } else {
            player.addPotionEffect(new PotionEffect(
                    PotionEffectType.INVISIBILITY, Integer.MAX_VALUE, 3, true));
        }
    }

    @Override
    public void end() {
        if ( player == null ) return;
        player.removePotionEffect(PotionEffectType.INVISIBILITY);
    }

    @Override
    public String getTypeString() {
        return TYPE;
    }
}
