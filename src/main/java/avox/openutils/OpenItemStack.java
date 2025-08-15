package avox.openutils;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.FireworksComponent;
import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.component.type.PotionContentsComponent;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.Text;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;

public class OpenItemStack {
    public Item item;
    public ItemEnchantmentsComponent enchantments;
    public PotionContentsComponent potions;
    public FireworksComponent fireworks;
    public ItemStack visualStack;
    public boolean finishedMarked;

    public OpenItemStack(ItemStack itemStack, boolean finishedMarked) {
        item = itemStack.getItem();
        enchantments = itemStack.getEnchantments();
        if (enchantments.isEmpty()) {
            enchantments = itemStack.getComponents().get(DataComponentTypes.STORED_ENCHANTMENTS);
        }
        potions = itemStack.getComponents().get(DataComponentTypes.POTION_CONTENTS);
        fireworks = itemStack.getComponents().get(DataComponentTypes.FIREWORKS);

        visualStack = itemStack;
        this.finishedMarked = finishedMarked;
    }

    public Text getItemName() {
        Text text;

        if (item.equals(Items.ENCHANTED_BOOK)) {
            int level = enchantments.getEnchantmentEntries().stream().toList().getFirst().getIntValue();
            Enchantment value = enchantments.getEnchantments().stream().toList().getFirst().value();

            text = Text.literal(value.description().getString()).append(" " + level);
        } else if (item.equals(Items.FIREWORK_ROCKET)) {
            text = visualStack.getName().copy().append(" Tier " + fireworks.flightDuration());
        } else {
            text = visualStack.getName();
        }
        return text;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OpenItemStack that = (OpenItemStack) o;

        boolean enchantmentsEqual = (this.enchantments == null && that.enchantments == null);
        if (this.enchantments != null && that.enchantments != null) {
            List<String> thisEnchants = this.enchantments.getEnchantmentEntries().stream().map(a -> a.getKey().value().toString()).toList();
            List<String> thatEnchants = that.enchantments.getEnchantmentEntries().stream().map(a -> a.getKey().value().toString()).toList();

            enchantmentsEqual = thisEnchants.equals(thatEnchants) || new HashSet<>(thisEnchants).equals(new HashSet<>(thatEnchants));
        }

        boolean potionsEqual = (this.potions == null && that.potions == null) ||
                (this.potions != null && this.potions.equals(that.potions));

        boolean fireworkEqual = (this.fireworks == null && that.fireworks == null) ||
                (this.fireworks != null && this.fireworks.equals(that.fireworks));

        return item.equals(that.item) && enchantmentsEqual && potionsEqual && fireworkEqual;
    }

    @Override
    public int hashCode() {
        return Objects.hash(item, enchantments, potions, fireworks);
    }
}