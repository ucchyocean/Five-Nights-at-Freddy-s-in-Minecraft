/*
 * @author     ucchy
 * @license    LGPLv3
 * @copyright  Copyright ucchy 2015
 */
package org.bitbucket.ucchy.fnafim.effect;

import org.bitbucket.ucchy.fnafim.FiveNightsAtFreddysInMinecraft;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

/**
 * 動けなくなるエフェクト効果
 * @author ucchy
 */
public class BindEffect implements SpecialEffect {

    public static final String TYPE = "Bind";

    private Player player;

    public BindEffect(Player player) {
        this.player = player;
    }

    @Override
    public void start() {
        player.addPotionEffect(new PotionEffect(
                PotionEffectType.SLOW, Integer.MAX_VALUE, 10, true));
        player.addPotionEffect(new PotionEffect(
                PotionEffectType.JUMP, Integer.MAX_VALUE, -10, true));
        player.setMetadata(TYPE, new FixedMetadataValue(
                FiveNightsAtFreddysInMinecraft.getInstance(), true));
    }

    @Override
    public void end() {
        player.removePotionEffect(PotionEffectType.SLOW);
        player.removePotionEffect(PotionEffectType.JUMP);
        player.removeMetadata(TYPE, FiveNightsAtFreddysInMinecraft.getInstance());
    }

    @Override
    public String getTypeString() {
        return TYPE;
    }
}
