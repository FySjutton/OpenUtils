package avox.openutils.modules.stock;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;

import java.time.LocalDate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static avox.openutils.modules.stock.StockModule.getItemName;

public class StockItem {
    public ItemStack itemStack;
    public Text name;
    public int storage; // I lager
    public Vec3d position;
    public LocalDate created;
    public int bought;
    public int transactions;
    public int earned;

    public StockItem(ItemStack stack) {
        itemStack = stack;
        LoreComponent lore = stack.get(DataComponentTypes.LORE);
        if (lore == null) return;
        for (Text line : lore.lines()) {
            String text = line.getString().trim();
            if (text.isEmpty()) continue;

            Matcher storageMatcher = Pattern.compile("Lager: (\\d+) st").matcher(text);
            if (storageMatcher.find()) {
                storage = Integer.parseInt(storageMatcher.group(1));
            } else {
                Matcher positionMatcher = Pattern.compile("Position: (-*\\d+), (-*\\d+), (-*\\d+)").matcher(text);
                if (positionMatcher.find()) {
                    position = new Vec3d(Double.parseDouble(positionMatcher.group(1)), Double.parseDouble(positionMatcher.group(2)), Double.parseDouble(positionMatcher.group(3)));
                } else {
                    Matcher createdMatcher = Pattern.compile("Skapades: ([\\d-]+)").matcher(text);
                    if (createdMatcher.find()) {
                        created = LocalDate.parse(createdMatcher.group(1));
                    } else {
                        Matcher boughtMatcher = Pattern.compile("Antal köpta: (\\d+) st").matcher(text);
                        if (boughtMatcher.find()) {
                            bought = Integer.parseInt(boughtMatcher.group(1));
                        } else {
                            Matcher transactionsMatcher = Pattern.compile("Antal transaktioner: (\\d+) st").matcher(text);
                            if (transactionsMatcher.find()) {
                                transactions = Integer.parseInt(transactionsMatcher.group(1));
                            } else {
                                Matcher earnedMatcher = Pattern.compile("Tjänat: (\\d+) kr").matcher(text);
                                if (earnedMatcher.find()) {
                                    earned = Integer.parseInt(earnedMatcher.group(1));
                                }
                            }
                        }
                    }
                }
            }
        }
        name = getItemName(itemStack);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        StockItem other = (StockItem) obj;
        if (this.itemStack == null || other.itemStack == null) return false;

        return this.name.equals(other.name);
    }

    @Override
    public int hashCode() {
        return itemStack != null ? name.hashCode() : 0;
    }
}
