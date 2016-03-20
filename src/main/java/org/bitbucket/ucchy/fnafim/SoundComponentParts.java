/*
 * @author     ucchy
 * @license    LGPLv3
 * @copyright  Copyright ucchy 2015
 */
package org.bitbucket.ucchy.fnafim;

import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * 効果音コンポーネントのパーツ
 * @author ucchy
 */
public class SoundComponentParts {

    private Sound sound;
    private float volume;
    private float pitch;
    private int delay;

    private SoundComponentParts(Sound sound) {
        this(sound, 1, 1, 0);
    }

    private SoundComponentParts(Sound sound, float volume, float pitch) {
        this(sound, volume, pitch, 0);
    }

    private SoundComponentParts(Sound sound, float volume, float pitch, int delay) {
        this.sound = sound;
        this.volume = volume;
        this.pitch = pitch;
        this.delay = delay;
    }

    /**
     * @return sound
     */
    public Sound getSound() {
        return sound;
    }

    /**
     * @return volume
     */
    public float getVolume() {
        return volume;
    }

    /**
     * @return pitch
     */
    public float getPitch() {
        return pitch;
    }

    /**
     * @return delay
     */
    public int getDelay() {
        return delay;
    }

    public void playSoundToPlayer(final Player player) {
        playSoundToPlayer(player, player.getLocation());
    }

    public void playSoundToPlayer(final Player player, final Location location) {
        if ( delay == 0 ) {
            player.playSound(location, sound, volume, pitch);
        } else {
            new BukkitRunnable() {
                public void run() {
                    player.playSound(location, sound, volume, pitch);
                }
            }.runTaskLater(FiveNightsAtFreddysInMinecraft.getInstance(), delay);
        }
    }

    public void playSoundToWorld(final Location location) {
        if ( delay == 0 ) {
            location.getWorld().playSound(location, sound, volume, pitch);
        } else {
            new BukkitRunnable() {
                public void run() {
                    location.getWorld().playSound(location, sound, volume, pitch);
                }
            }.runTaskLater(FiveNightsAtFreddysInMinecraft.getInstance(), delay);
        }
    }

    public static SoundComponentParts getPartsFromString(String source) {
        String[] t = source.split("-");
        if ( t.length <= 0 ) return null;
        Sound sound = getSoundFromString(t[0]);
        if ( sound == null ) return null;
        float volume = 1;
        float pitch = 1;
        int delay = 0;
        if ( t.length >= 2 ) volume = tryToParseDouble(t[1]);
        if ( t.length >= 3 ) pitch = tryToParseDouble(t[2]);
        if ( t.length >= 4 ) delay = tryToParseInt(t[3]);
        return new SoundComponentParts(sound, volume, pitch, delay);
    }

    private static Sound getSoundFromString(String source) {
        return SoundEnum.getSoundFromString(source);
    }

    private static float tryToParseDouble(String source) {
        if ( source == null ) return 1;
        try {
            float f = Float.parseFloat(source);
            if ( f < 0 ) return 0;
            if ( 2 < f ) return 2;
            return f;
        } catch (NumberFormatException e) {
            return 1;
        }
    }

    private static int tryToParseInt(String source) {
        if ( source == null ) return 0;
        try {
            int i = Integer.parseInt(source);
            if ( i <= 0 ) return 0;
            if ( i >= 20 ) return 20;
            return i;
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
