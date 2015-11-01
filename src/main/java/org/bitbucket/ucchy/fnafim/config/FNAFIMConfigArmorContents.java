package org.bitbucket.ucchy.fnafim.config;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;

/**
 * 各キャラの防具設定
 * @author ucchy
 */
public class FNAFIMConfigArmorContents {

    private Material helmet;
    private Material chestplate;
    private Material leggings;
    private Material boots;

    private FNAFIMConfigArmorContents() {
    }

    public static FNAFIMConfigArmorContents load(
            ConfigurationSection config, String sectionName,
            Material helmetDefault, Material chestplateDefault,
            Material leggingsDefault, Material bootsDefault) {

        if ( !config.contains(sectionName) ) config.createSection(sectionName);

        ConfigurationSection section = config.getConfigurationSection(sectionName);

        FNAFIMConfigArmorContents contents = new FNAFIMConfigArmorContents();
        if ( section.contains("helmet") ) {
            String name = section.getString("helmet");
            contents.helmet = Material.matchMaterial(name);
        }
        if ( section.contains("chestplate") ) {
            String name = section.getString("chestplate");
            contents.chestplate = Material.matchMaterial(name);
        }
        if ( section.contains("leggings") ) {
            String name = section.getString("leggings");
            contents.leggings = Material.matchMaterial(name);
        }
        if ( section.contains("boots") ) {
            String name = section.getString("boots");
            contents.boots = Material.matchMaterial(name);
        }
        if ( contents.helmet == null ) contents.helmet = helmetDefault;
        if ( contents.chestplate == null ) contents.chestplate = chestplateDefault;
        if ( contents.leggings == null ) contents.leggings = leggingsDefault;
        if ( contents.boots == null ) contents.boots = bootsDefault;

        return contents;
    }

    public Material getHelmet() {
        return helmet;
    }

    public Material getChestplate() {
        return chestplate;
    }

    public Material getLeggings() {
        return leggings;
    }

    public Material getBoots() {
        return boots;
    }
}
