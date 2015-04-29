/*
 * @author     ucchy
 * @license    LGPLv3
 * @copyright  Copyright ucchy 2015
 */
package org.bitbucket.ucchy.fnafim.game;

import java.util.HashMap;

import org.bukkit.entity.Player;

/**
 * 電力管理クラス
 * @author ucchy
 */
public class PowerManager {

    private HashMap<Player, Double> powers;

    public PowerManager() {
        powers = new HashMap<Player, Double>();
    }


}
