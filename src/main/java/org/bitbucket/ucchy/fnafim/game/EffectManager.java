/*
 * @author     ucchy
 * @license    LGPLv3
 * @copyright  Copyright ucchy 2015
 */
package org.bitbucket.ucchy.fnafim.game;

import java.util.ArrayList;
import java.util.HashMap;

import org.bitbucket.ucchy.fnafim.effect.SpecialEffect;

/**
 * エフェクト管理クラス
 * @author ucchy
 */
public class EffectManager {

    private HashMap<String, ArrayList<SpecialEffect>> effects;

    public EffectManager() {
        effects = new HashMap<String, ArrayList<SpecialEffect>>();
    }

    public void applyEffect(String name, SpecialEffect effect) {
        if ( name == null ) return;
        if ( !effects.containsKey(name) ) {
            effects.put(name, new ArrayList<SpecialEffect>());
        }
        removeEffect(name, effect.getTypeString());
        effects.get(name).add(effect);
        effect.start();
    }

    public void removeAll() {
        for ( String name : effects.keySet() ) {
            removeAllEffect(name);
        }
    }

    public void removeAllEffect(String name) {
        if ( !effects.containsKey(name) ) {
            return;
        }
        for ( SpecialEffect effect : effects.get(name) ) {
            effect.end();
        }
        effects.get(name).clear();
    }

    public void removeEffect(String name, String type) {
        if ( !effects.containsKey(name) ) {
            return;
        }
        SpecialEffect founds = getSameTypeSpecialEffect(effects.get(name), type);
        if ( founds != null ) {
            founds.end();
            effects.get(name).remove(founds);
        }
    }

    public boolean hasEffect(String name, String type) {
        if ( !effects.containsKey(name) ) {
            return false;
        }
        return (getSameTypeSpecialEffect(effects.get(name), type) != null);
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
