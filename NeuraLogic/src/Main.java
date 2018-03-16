import ida.utils.tuples.Pair;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.ParseException;
import pipelines.Pipeline;
import settings.Settings;
import settings.Sources;
import utils.CommandLineHandler;
import utils.logging.Logging;

import java.io.IOException;
import java.util.logging.Logger;

/**
 * Created by gusta on 8.3.17.
 */
public class Main {
    private static final Logger LOG = Logger.getLogger(Main.class.getName());

    public static void main(String[] args) {

        Logging logging = new Logging();
        try {
            logging.initialize();
        } catch (IOException ex) {
            LOG.severe("Could not initialize Logging\n" + ex.getMessage());
            System.exit(1);
        }

        CommandLineHandler cmdh = new CommandLineHandler();
        Settings settings = new Settings();
        Sources sources = null;
        try {
            CommandLine cmd = cmdh.parseParams(args, settings);
            settings.setupFromCommandline(cmd);
            sources = Sources.setupFromCommandline(settings, cmd);
        } catch (ParseException ex) {
            LOG.severe("Unable to parse Commandline arguments into settings\n" + ex.getMessage());
            System.exit(1);
        }

        //place for external changes in setting object for non-standard pipelines

        Pair<Boolean, String> validation = settings.validate();
        if (!validation.r) {
            LOG.severe("Invalid pipeline setting.\n" + validation.s);
            System.exit(2);
        }
        validation = sources.validate(settings);
        if (!validation.r) {
            LOG.severe("Invalid source files configuration.\n" + validation.s);
            System.exit(2);
        }

        Pipeline<Sources> pipeline = new Pipeline(settings);
        if (pipeline.execute(sources))
            LOG.info("Pipeline " + pipeline + " has been successfully executed.");
    }
}