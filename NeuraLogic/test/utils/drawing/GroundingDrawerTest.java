package utils.drawing;

import grounding.debugging.GroundingDebugger;
import org.junit.Test;
import settings.Settings;
import utils.logging.Logging;

public class GroundingDrawerTest {
    @Test
    public void family(){
        Logging logging = Logging.initLogging();
        String[] args = new String("-path ./resources/datasets/family").split(" ");
        Settings settings = new Settings();
        settings.intermediateDebug = false;
//        Settings.loggingLevel = Level.WARNING;
        GroundingDebugger groundingDebugger = new GroundingDebugger(args, settings);
        groundingDebugger.executeDebug();
    }
}