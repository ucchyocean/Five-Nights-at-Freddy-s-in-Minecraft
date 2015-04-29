/*
 * @author     ucchy
 * @license    LGPLv3
 * @copyright  Copyright ucchy 2015
 */
package org.bitbucket.ucchy.fnafim.game;

import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

/**
 * テンポラリストレージ
 * @author ucchy
 */
public class TemporaryStorage {

    private HashMap<Player, Inventory> invs;
    private HashMap<Player, Inventory> armors;
    private HashMap<Player, Integer> levels;
    private HashMap<Player, Float> exps;

    /**
     * コンストラクタ
     */
    public TemporaryStorage() {

        invs = new HashMap<Player, Inventory>();
        armors = new HashMap<Player, Inventory>();
        levels = new HashMap<Player, Integer>();
        exps = new HashMap<Player, Float>();
    }

    /**
     * プレイヤーのインベントリと経験値を、テンポラリ保存領域に保存する
     * @param player プレイヤー
     */
    public void sendToTemp(Player player) {

        // インベントリの保存
        Inventory tempInventory = Bukkit.createInventory(player, 5 * 9);
        for ( ItemStack item : player.getInventory().getContents() ) {
            if ( item != null ) {
                tempInventory.addItem(item);
            }
        }
        invs.put(player, tempInventory);

        // 防具の保存
        Inventory tempArmors = Bukkit.createInventory(player, 9);
        for ( int index=0; index<4; index++ ) {
            ItemStack armor = player.getInventory().getArmorContents()[index];
            if ( armor != null ) {
                tempArmors.setItem(index, armor);
            }
        }
        armors.put(player, tempArmors);

        // インベントリの消去とアップデート
        player.getInventory().clear();
        player.getInventory().setArmorContents(new ItemStack[]{
                new ItemStack(Material.AIR),
                new ItemStack(Material.AIR),
                new ItemStack(Material.AIR),
                new ItemStack(Material.AIR),
        });
        updateInventory(player);

        // 経験値の保存と消去
        levels.put(player, player.getLevel());
        exps.put(player, player.getExp());
        player.setLevel(0);
        player.setExp(0);
    }

    /**
     * テンポラリ領域に保存していたインベントリや経験値を復帰する
     * @param player プレイヤー
     */
    public void restoreFromTemp(Player player) {

        // データが無いなら何もしない
        if ( !invs.containsKey(player) ) {
            return;
        }

        // インベントリの消去
        player.getInventory().clear();
        player.getInventory().setArmorContents(new ItemStack[]{
                new ItemStack(Material.AIR),
                new ItemStack(Material.AIR),
                new ItemStack(Material.AIR),
                new ItemStack(Material.AIR),
        });

        // インベントリと防具の復帰、更新
        for ( ItemStack item : invs.get(player).getContents() ) {
            if ( item != null ) {
                player.getInventory().addItem(item);
            }
        }
        ItemStack[] armorCont = new ItemStack[4];
        for ( int index=0; index<4; index++ ) {
            ItemStack armor = armors.get(player).getItem(index);
            if ( armor != null ) {
                armorCont[index] = armor;
            } else {
                armorCont[index] = new ItemStack(Material.AIR);
            }
            player.getInventory().setArmorContents(armorCont);
        }
        updateInventory(player);

        // レベルと経験値の復帰
        player.setLevel(levels.get(player));
        player.setExp(exps.get(player));

        // テンポラリの消去
        invs.remove(player);
        armors.remove(player);
        levels.remove(player);
        exps.remove(player);
    }

    /**
     * インベントリのアップデートを行う
     * @param player 更新対象のプレイヤー
     */
    @SuppressWarnings("deprecation")
    private void updateInventory(Player player) {
        player.updateInventory();
    }
}
