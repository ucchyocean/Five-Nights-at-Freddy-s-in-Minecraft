/*
 * @author     ucchy
 * @license    LGPLv3
 * @copyright  Copyright ucchy 2015
 */
package org.bitbucket.ucchy.fnafim;

import java.util.ArrayList;

import org.bukkit.Location;
import org.bukkit.entity.Player;

/**
 * 効果音コンポーネント
 * @author ucchy
 */
public class SoundComponent {

    private ArrayList<SoundComponentParts> parts;

    private SoundComponent() {
        this.parts = new ArrayList<SoundComponentParts>();
    }

    public void addParts(SoundComponentParts parts) {
        this.parts.add(parts);
    }

    public void playSoundToPlayer(Player player) {
        for ( SoundComponentParts sound : parts ) {
            sound.playSoundToPlayer(player);
        }
    }

    public void playSoundToWorld(Location location) {
        for ( SoundComponentParts sound : parts ) {
            sound.playSoundToWorld(location);
        }
    }

    public static SoundComponent getComponentFromString(String source) {
        if ( source == null ) return new SoundComponent();
        String[] temp = source.split(",");
        SoundComponent component = new SoundComponent();
        for ( String t : temp ) {
            SoundComponentParts parts = SoundComponentParts.getPartsFromString(t);
            if ( parts != null ) {
                component.addParts(parts);
            }
        }
        return component;
    }
}
