/*
 * @author     ucchy
 * @license    LGPLv3
 * @copyright  Copyright ucchy 2015
 */
package org.bitbucket.ucchy.fnafim.game;

import java.util.ArrayList;
import java.util.HashMap;

import org.bitbucket.ucchy.fnafim.effect.SpecialEffect;
import org.bukkit.entity.Player;

/**
 * エフェクト管理クラス
 * @author ucchy
 */
public class EffectManager {

    private HashMap<Player, ArrayList<SpecialEffect>> effects;

    public EffectManager() {
        effects = new HashMap<Player, ArrayList<SpecialEffect>>();
    }

    public void addEffect(Player player, SpecialEffect effect) {
        if ( !effects.containsKey(player) ) {
            effects.put(player, new ArrayList<SpecialEffect>());
        }
        effects.get(player).add(effect);
        effect.start();
    }

    public void removeAllEffect(Player player) {
        if ( !effects.containsKey(player) ) {
            return;
        }
        for ( SpecialEffect effect : effects.get(player) ) {
            effect.end();
        }
        effects.get(player).clear();
    }
}
