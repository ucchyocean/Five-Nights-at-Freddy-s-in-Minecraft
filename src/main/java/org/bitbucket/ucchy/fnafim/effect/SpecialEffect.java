/*
 * @author     ucchy
 * @license    LGPLv3
 * @copyright  Copyright ucchy 2015
 */
package org.bitbucket.ucchy.fnafim.effect;


/**
 * 様々な特殊効果を設定したり解除したりする仕組みの抽象クラス
 * @author ucchy
 */
public interface SpecialEffect {
    public void start();
    public void end();
    public String getTypeString();
}
