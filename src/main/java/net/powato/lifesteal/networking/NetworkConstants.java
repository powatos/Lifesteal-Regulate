package net.powato.lifesteal.networking;

import net.powato.lifesteal.LifestealRegulate;
import net.minecraft.util.Identifier;

public abstract class NetworkConstants {
    public static final Identifier SHOW_CHUNKS_ID = Identifier.of(LifestealRegulate.MOD_ID, "show_chunks");
    public static final Identifier UPDATE_CHUNKS_ID = Identifier.of(LifestealRegulate.MOD_ID, "update_chunks");

}
