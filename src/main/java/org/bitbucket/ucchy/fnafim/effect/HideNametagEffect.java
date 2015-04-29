/*
 * @author     ucchy
 * @license    LGPLv3
 * @copyright  Copyright ucchy 2015
 */
package org.bitbucket.ucchy.fnafim.effect;

import org.bitbucket.ucchy.fnafim.FiveNightsAtFreddysInMinecraft;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Slime;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

/**
 * ネームタグを隠す特殊エフェクト
 * @author ucchy
 */
public class HideNametagEffect implements SpecialEffect {

    public static final String TYPE = "HideNametag";

    private Player player;
    private Slime slime;

    public HideNametagEffect(Player player) {
        this.player = player;
    }

    @Override
    public void start() {
        slime = (Slime)player.getWorld().spawnEntity(player.getLocation(), EntityType.SLIME);
        slime.setSize(1);
        slime.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, Integer.MAX_VALUE, 3, true));
        slime.setMetadata(TYPE, new FixedMetadataValue(FiveNightsAtFreddysInMinecraft.getInstance(), true));
        player.setPassenger(slime);
    }

    @Override
    public void end() {
        slime.remove();
    }

    @Override
    public String getTypeString() {
        return TYPE;
    }
}
