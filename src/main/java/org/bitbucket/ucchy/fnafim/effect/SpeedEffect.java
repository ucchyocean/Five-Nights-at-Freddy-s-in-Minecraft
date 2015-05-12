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
 * 速度低下or上昇エフェクト
 * @author ucchy
 */
public class SpeedEffect implements SpecialEffect {

    public static final String TYPE = "Speed";

    private Player player;
    private int value;

    public SpeedEffect(String name, int value) {
        this.player = Utility.getPlayerExact(name);
        this.value = value;
    }

    public SpeedEffect(Player player, int value) {
        this.player = player;
        this.value = value;
    }

    @Override
    public void start() {
        if ( player == null ) return;
        player.addPotionEffect(new PotionEffect(
                PotionEffectType.SPEED, Integer.MAX_VALUE, value, true));
    }

    @Override
    public void end() {
        if ( player == null ) return;
        player.removePotionEffect(PotionEffectType.SPEED);
    }

    @Override
    public String getTypeString() {
        return TYPE;
    }
}
