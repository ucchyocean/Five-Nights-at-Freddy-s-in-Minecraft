/*
 * @author     ucchy
 * @license    LGPLv3
 * @copyright  Copyright ucchy 2015
 */
package org.bitbucket.ucchy.fnafim.effect;

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

    public SpeedEffect(Player player, int value) {
        this.player = player;
        this.value = value;
    }

    @Override
    public void start() {
        if ( value < 0 ) {
            player.addPotionEffect(new PotionEffect(
                    PotionEffectType.SLOW, Integer.MAX_VALUE, -value, true));
        } else if ( value > 0 ) {
            player.addPotionEffect(new PotionEffect(
                    PotionEffectType.SPEED, Integer.MAX_VALUE, -value, true));
        }
    }

    @Override
    public void end() {
        player.removePotionEffect(PotionEffectType.SLOW);
        player.removePotionEffect(PotionEffectType.SPEED);
    }

    @Override
    public String getTypeString() {
        return TYPE;
    }
}
