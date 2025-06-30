package sdgrabber.materials;

import java.util.function.Supplier;
import net.minecraft.item.Items;
import net.minecraft.item.ToolMaterial;
import net.minecraft.recipe.Ingredient;
import sdgrabber.items.BaseItem;

public enum GrabberMaterial implements ToolMaterial {
    WOODEN(100, 0.0F, 0.0F, 0, 0, () -> Ingredient.ofItems(BaseItem.WOODEN_GRABBER, Items.OAK_PLANKS)),
    IRON(250, 0.0F, 0.0F, 0, 0, () -> Ingredient.ofItems(BaseItem.IRON_GRABBER, Items.IRON_INGOT)),
    DIAMOND(500, 0.0F, 0.0F, 0, 0, () -> Ingredient.ofItems(BaseItem.DIAMOND_GRABBER, Items.DIAMOND)),
    DOUBLE(750, 0.0F, 0.0F, 0, 0, () -> Ingredient.ofItems(BaseItem.DOUBLE_GRABBER, Items.GOLD_INGOT));

    private final int durability;
    private final float miningSpeedMultiplier;
    private final float attackDamage;
    private final int miningLevel;
    private final int enchantability;
    private final Supplier<Ingredient> repairIngredient;

    GrabberMaterial(int durability, float miningSpeedMultiplier, float attackDamage, int miningLevel, int enchantability, Supplier<Ingredient> repairIngredient) {
        this.durability = durability;
        this.miningSpeedMultiplier = miningSpeedMultiplier;
        this.attackDamage = attackDamage;
        this.miningLevel = miningLevel;
        this.enchantability = enchantability;
        this.repairIngredient = repairIngredient;
    }

    @Override
    public int getDurability() {
        return this.durability;
    }

    @Override
    public float getMiningSpeedMultiplier() {
        return this.miningSpeedMultiplier;
    }

    @Override
    public float getAttackDamage() {
        return this.attackDamage;
    }

    @Override
    public int getMiningLevel() {
        return this.miningLevel;
    }

    @Override
    public int getEnchantability() {
        return this.enchantability;
    }

    @Override
    public Ingredient getRepairIngredient() {
        return this.repairIngredient.get();
    }
}
