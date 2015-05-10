/*
 * @author     ucchy
 * @license    LGPLv3
 * @copyright  Copyright ucchy 2015
 */
package org.bitbucket.ucchy.fnafim;

import org.bukkit.configuration.ConfigurationSection;

/**
 * 各Nightの移動速度設定
 * @author ucchy
 */
public class FNAFIMConfigMoveSetting {

    private int freddy;
    private int chica;
    private int bonnie;
    private int foxy;
    private int foxyMovement;

    private FNAFIMConfigMoveSetting() {
    }

    public static FNAFIMConfigMoveSetting load(
            ConfigurationSection config, String sectionName,
            int freddyDefault, int chicaDefault, int bonnieDefault, int foxyDefault,
            int foxyMovementDefault) {

        FNAFIMConfigMoveSetting setting = new FNAFIMConfigMoveSetting();
        setting.freddy = config.getInt(sectionName + ".freddy", freddyDefault);
        setting.chica = config.getInt(sectionName + ".chica", chicaDefault);
        setting.bonnie = config.getInt(sectionName + ".bonnie", bonnieDefault);
        setting.foxy = config.getInt(sectionName + ".foxy", foxyDefault);
        setting.foxyMovement = config.getInt(sectionName + ".foxyMovement", foxyMovementDefault);
        return setting;
    }

    /**
     * @return freddy
     */
    public int getFreddy() {
        return freddy;
    }

    /**
     * @return chica
     */
    public int getChica() {
        return chica;
    }

    /**
     * @return bonnie
     */
    public int getBonnie() {
        return bonnie;
    }

    /**
     * @return foxy
     */
    public int getFoxy() {
        return foxy;
    }

    /**
     * @return foxyMovement
     */
    public int getFoxyMovement() {
        return foxyMovement;
    }
}
