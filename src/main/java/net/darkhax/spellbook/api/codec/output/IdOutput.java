package net.darkhax.spellbook.api.codec.output;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.validation.Validators;
import com.hypixel.hytale.server.core.asset.type.item.config.Item;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import org.bson.BsonDocument;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.function.Consumer;

/**
 * Outputs an item using an Item ID and a quantity.
 * <pre><code>
 * {
 *   "Type": "Item",
 *   "ItemId": "Ingredient_Life_Essence",
 *   "Quantity": 5
 * }
 * </code></pre>
 */
public class IdOutput implements ItemOutput {

    public static String ID = "Item";
    public static BuilderCodec<IdOutput> CODEC = BuilderCodec.builder(IdOutput.class, IdOutput::new)
            .documentation("Outputs an item based on its Item ID.")
            .append(new KeyedCodec<>("ItemId", Codec.STRING, true), (i, v) -> i.itemId = v, i -> i.itemId)
            .documentation("The ID of the item to output.")
            .addValidator(Validators.nonNull())
            .addValidator(Item.VALIDATOR_CACHE.getValidator())
            .add()
            .append(new KeyedCodec<>("Quantity", Codec.INTEGER), (i, v) -> i.amount = v, i -> i.amount)
            .documentation("The amount of the item to produce. Defaults to 1.")
            .addValidator(Validators.greaterThan(0))
            .add()
            .append(new KeyedCodec<>("Metadata", Codec.BSON_DOCUMENT), (i, v) -> i.metadata = v, i -> i.metadata)
            .documentation("Optional metadata for the item.")
            .add()
            .build();

    @Nonnull
    protected String itemId = "Empty";
    protected int amount = 1;
    @Nullable
    protected BsonDocument metadata = BsonDocument.parse("{}");

    public String itemId() {
        return this.itemId;
    }

    public int amount() {
        return this.amount;
    }

    @Override
    public String toString() {
        return "IdOutput{" + "itemId='" + itemId + '\'' + ", amount=" + amount + ", metadata=" + metadata + '}';
    }

    @Override
    public void output(Consumer<ItemStack> consumer) {
        if (!"Empty".equals(this.itemId)) {
            consumer.accept(new ItemStack(this.itemId, this.amount, this.metadata));
        }
    }
}
