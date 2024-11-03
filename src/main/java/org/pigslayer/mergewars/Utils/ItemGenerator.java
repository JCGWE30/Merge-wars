package org.pigslayer.mergewars.Utils;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class ItemGenerator {
    private String name;
    private int amount;
    private Material type;

    public ItemGenerator(String name, int amount, Material type) {
        this.name = name;
        this.amount = amount;
        this.type = type;
    }

    public ItemStack get(){
        ItemStack item = new ItemStack(type, amount);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        item.setItemMeta(meta);
        return item;
    }
}
