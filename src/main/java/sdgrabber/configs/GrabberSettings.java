package sdgrabber.configs;

import me.shedaniel.autoconfig.annotation.ConfigEntry;

public class GrabberSettings {
    @ConfigEntry.BoundedDiscrete(min = 0, max = 5)
    public int slownessAmplifierPerChest;

    @ConfigEntry.BoundedDiscrete(min = 1, max = 10)
    public int maxSlownessAmplifier;

    public GrabberSettings(int slownessAmplifierPerChest, int maxSlownessAmplifier) {
        this.slownessAmplifierPerChest = slownessAmplifierPerChest;
        this.maxSlownessAmplifier = maxSlownessAmplifier;
    }
}
