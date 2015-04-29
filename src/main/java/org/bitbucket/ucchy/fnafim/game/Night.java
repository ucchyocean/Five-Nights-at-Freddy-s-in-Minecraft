/*
 * @author     ucchy
 * @license    LGPLv3
 * @copyright  Copyright ucchy 2015
 */
package org.bitbucket.ucchy.fnafim.game;

/**
 * ゲームレベル
 * @author ucchy
 */
public enum Night {

    NIGHT1(1),
    NIGHT2(2),
    NIGHT3(3),
    NIGHT4(4),
    NIGHT5(5),
    NIGHT6(6),
    NIGHT7(7);

    private int num;

    Night(int num) {
        this.num = num;
    }

    public int getNum() {
        return num;
    }

    public static Night getNightFromString(String str) {
        for ( Night n : values() ) {
            if ( n.toString().equalsIgnoreCase(str) ) {
                return n;
            }
        }
        return null;
    }
}
