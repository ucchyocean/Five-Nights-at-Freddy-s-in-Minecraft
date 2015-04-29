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
 * 盲目になるエフェクト効果
 * @author ucchy
 */
public class BlindnessEffect implements SpecialEffect {

    private Player player;

    public BlindnessEffect(Player player) {
        this.player = player;
    }

    @Override
    public void start() {
        player.addPotionEffect(new PotionEffect(
                PotionEffectType.BLINDNESS, Integer.MAX_VALUE, 3, true));
    }

    @Override
    public void end() {
        player.removePotionEffect(PotionEffectType.BLINDNESS);
    }
}
