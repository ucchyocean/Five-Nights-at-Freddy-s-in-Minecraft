/*
 * @author     ucchy
 * @license    LGPLv3
 * @copyright  Copyright ucchy 2015
 */
package org.bitbucket.ucchy.fnafim.game;

/**
 * ゲームセッションフェーズ
 * @author ucchy
 */
public enum GameSessionPhase {

    /** 募集中 */
    INVITATION,

    /** 準備中 */
    PREPARING,

    /** 次の夜の開始待ち */
    PREPARING_NEXT,

    /** ゲーム中 */
    IN_GAME,

    /** キャンセル */
    CANCELED,

    /** ゲーム終了 */
    END;
}
