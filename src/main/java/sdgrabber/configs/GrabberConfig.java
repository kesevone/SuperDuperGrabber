package sdgrabber.configs;

import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;

@Config(name = "sdgrabber")
public class GrabberConfig implements ConfigData {
    @ConfigEntry.Gui.CollapsibleObject
    public boolean enablePufferfishSounds = true;
    @ConfigEntry.Gui.CollapsibleObject
    public boolean showInteractionParticles = true;
    @ConfigEntry.Gui.CollapsibleObject
    public GrabberSettings woodenGrabber = new GrabberSettings(3, 3);
    @ConfigEntry.Gui.CollapsibleObject
    public GrabberSettings ironGrabber = new GrabberSettings(2, 3);
    @ConfigEntry.Gui.CollapsibleObject
    public GrabberSettings diamondGrabber = new GrabberSettings(1, 2);
    @ConfigEntry.Gui.CollapsibleObject
    public GrabberSettings doubleGrabber = new GrabberSettings(1, 1);
}

