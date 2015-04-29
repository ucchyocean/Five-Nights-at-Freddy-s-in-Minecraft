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

    public void applyEffect(Player player, SpecialEffect effect) {
        if ( !effects.containsKey(player) ) {
            effects.put(player, new ArrayList<SpecialEffect>());
        }
        removeEffect(player, effect.getTypeString());
        effects.get(player).add(effect);
        effect.start();
    }

    public void removeAll() {
        for ( Player player : effects.keySet() ) {
            removeAllEffect(player);
        }
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

    public void removeEffect(Player player, String type) {
        if ( !effects.containsKey(player) ) {
            return;
        }
        SpecialEffect founds = getSameTypeSpecialEffect(effects.get(player), type);
        if ( founds != null ) {
            founds.end();
            effects.get(player).remove(founds);
        }
    }

    public boolean hasEffect(Player player, String type) {
        if ( !effects.containsKey(player) ) {
            return false;
        }
        return (getSameTypeSpecialEffect(effects.get(player), type) != null);
    }

    private SpecialEffect getSameTypeSpecialEffect(
            ArrayList<SpecialEffect> array, String type) {
        for ( SpecialEffect e : array ) {
            if ( e.getTypeString().equals(type) ) {
                return e;
            }
        }
        return null;
    }
}
