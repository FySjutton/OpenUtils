package avox.openutils;

import avox.openutils.config.ConfigSystem;
import avox.openutils.modules.stats.StatsModule;
import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OpenUtils implements ModInitializer {
	public static final String MOD_ID = "openutils";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	public static final ModuleManager moduleManager = new ModuleManager();

	@Override
	public void onInitialize() {
		// Register standard modules
		moduleManager.registerModule(new StatsModule());

		// Load the config
		ConfigSystem.CONFIG.load();
		ConfigSystem.applyModuleConfigs();

		// Setup default listeners
		ClientTickEvents.START_CLIENT_TICK.register(moduleManager::tick);
	}
}