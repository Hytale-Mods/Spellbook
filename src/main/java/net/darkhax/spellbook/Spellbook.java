package net.darkhax.spellbook;

import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;

import javax.annotation.Nonnull;

public class Spellbook extends JavaPlugin {

    public static final String GROUP = "Darkhax";
    public static final String NAME = "Spellbook";
    public static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    public Spellbook(@Nonnull JavaPluginInit init) {
        super(init);
    }

    @Override
    protected void setup() {
    }
}