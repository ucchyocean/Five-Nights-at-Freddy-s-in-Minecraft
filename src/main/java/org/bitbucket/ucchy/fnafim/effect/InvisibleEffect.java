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
 * 透明になるエフェクト
 * @author ucchy
 */
public class InvisibleEffect implements SpecialEffect {

    private Player player;

    public InvisibleEffect(Player player) {
        this.player = player;
    }

    @Override
    public void start() {
        player.addPotionEffect(new PotionEffect(
                PotionEffectType.INVISIBILITY, Integer.MAX_VALUE, 3, true));
    }

    @Override
    public void end() {
        player.removePotionEffect(PotionEffectType.INVISIBILITY);
    }
}
