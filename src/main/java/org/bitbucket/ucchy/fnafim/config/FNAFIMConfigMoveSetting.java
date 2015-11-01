/*
 * @author     ucchy
 * @license    LGPLv3
 * @copyright  Copyright ucchy 2015
 */
package org.bitbucket.ucchy.fnafim.config;

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
    private int fredbear;
    private int foxyMovement;
    private int fredbearMovement;

    private FNAFIMConfigMoveSetting() {
    }

    public static FNAFIMConfigMoveSetting load(
            ConfigurationSection config, String sectionName,
            int freddyDefault, int chicaDefault, int bonnieDefault, int foxyDefault, int fredbearDefault,
            int foxyMovementDefault, int fredbearMovementDefault) {

        FNAFIMConfigMoveSetting setting = new FNAFIMConfigMoveSetting();
        setting.freddy = config.getInt(sectionName + ".freddy", freddyDefault);
        setting.chica = config.getInt(sectionName + ".chica", chicaDefault);
        setting.bonnie = config.getInt(sectionName + ".bonnie", bonnieDefault);
        setting.foxy = config.getInt(sectionName + ".foxy", foxyDefault);
        setting.fredbear = config.getInt(sectionName + ".fredbear", fredbearDefault);
        setting.foxyMovement = config.getInt(sectionName + ".foxyMovement", foxyMovementDefault);
        setting.fredbearMovement = config.getInt(sectionName + ".fredbearMovement", fredbearMovementDefault);
        return setting;
    }

    public int getFreddy() {
        return freddy;
    }

    public int getChica() {
        return chica;
    }

    public int getBonnie() {
        return bonnie;
    }

    public int getFoxy() {
        return foxy;
    }

    public int getFredbear() {
        return fredbear;
    }

    public int getFoxyMovement() {
        return foxyMovement;
    }

    public int getFredbearMovement() {
        return fredbearMovement;
    }
}
