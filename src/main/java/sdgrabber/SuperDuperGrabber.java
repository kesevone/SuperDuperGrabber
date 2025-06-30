package sdgrabber;

import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.GsonConfigSerializer;
import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sdgrabber.configs.GrabberConfig;
import sdgrabber.events.DoubleGrabberInteractionHandler;
import sdgrabber.items.BaseItem;

public class SuperDuperGrabber implements ModInitializer {
	public static final String MOD_ID = "sdgrabber";
	public static final Logger LOGGER = LoggerFactory.getLogger("[SD] Grabber");

	@Override
	public void onInitialize() {
		LOGGER.info("Initializing...");
		AutoConfig.register(GrabberConfig.class, GsonConfigSerializer::new);

		SuperDuperGrabber.LOGGER.info("Registering items...");
		BaseItem.initialize();

		DoubleGrabberInteractionHandler.register();
	}
}