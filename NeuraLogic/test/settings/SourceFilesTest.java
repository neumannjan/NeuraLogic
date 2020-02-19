package settings;

import networks.computation.training.debugging.TrainingDebugger;
import org.junit.Test;
import utils.logging.Logging;

import java.util.logging.Level;

public class SourceFilesTest {

    @Test
    public void multipleTemplates() {
        Logging logging = Logging.initLogging(Level.FINER);
        Settings settings = new Settings();

        String[] args = ("-path ./resources/datasets/relational/molecules/muta_mini " +
                " -t ./embeddings,./template.txt" +
                " -out ./out/multitempl").split(" ");

        TrainingDebugger trainingDebugger = new TrainingDebugger(args, settings);
        trainingDebugger.executeDebug();
    }

}