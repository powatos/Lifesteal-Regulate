package net.powato.lifesteal;

import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.powato.lifesteal.commands.AddSafeChunkCommand;

public class LifestealRegulate implements ModInitializer {
	public static final String MOD_ID = "lifesteal-regulate";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            AddSafeChunkCommand.register(dispatcher);
        });
    }


}