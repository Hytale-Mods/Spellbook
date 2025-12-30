package net.darkhax.spellbook.api.codec.output;

import com.hypixel.hytale.codec.lookup.CodecMapCodec;
import com.hypixel.hytale.server.core.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Represents an output from a recipe or system that produces items.
 */
public interface ItemOutput {

    /**
     * The codec used to produce item outputs.
     */
    CodecMapCodec<ItemOutput> CODEC = new CodecMapCodec<>("Type");

    /**
     * Attempts to produce one or more outputs and pass them to the given consumer.
     *
     * <p>Notes:</p>
     * <ul>
     *   <li>{@code ItemOutput} instances must be reusable.</li>
     *   <li>{@code ItemOutput} must never produce an empty {@code ItemStack}. If an output is empty, it must not be passed to the consumer.</li>
     *   <li>{@code ItemOutput} is not obligated to produce any outputs.</li>
     *   <li>The consumer is under no obligation to accept any of the outputs.</li>
     *   <li>{@code ItemOutput} may produce multiple outputs in a single invocation.</li>
     * </ul>
     *
     * @param consumer A consumer that receives each produced {@link ItemStack}.
     */
    void output(Consumer<ItemStack> consumer);

    default List<ItemStack> outputList() {
        final List<ItemStack> output = new ArrayList<>();
        this.output(output::add);
        return output;
    }
}