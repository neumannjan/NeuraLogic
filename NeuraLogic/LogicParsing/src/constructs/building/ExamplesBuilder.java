package constructs.building;

import constructs.Conjunction;
import constructs.example.LiftedExample;
import constructs.example.LogicSample;
import constructs.example.ValuedFact;
import parsing.antlr.NeuralogicParser;
import parsing.examples.PlainExamplesParseTree;
import parsing.examples.PlainExamplesParseTreeExtractor;
import parsing.grammarParsing.PlainGrammarVisitor;
import pipelines.Pipeline;
import settings.Settings;
import utils.generic.Pair;

import java.io.IOException;
import java.io.Reader;
import java.util.logging.Logger;
import java.util.stream.Stream;

/**
 * //TODO groundexample when to return? recognizer
 */
public class ExamplesBuilder extends SamplesBuilder<PlainExamplesParseTree, Pair<Conjunction, LiftedExample>> {
    private static final Logger LOG = Logger.getLogger(ExamplesBuilder.class.getName());

    public ExamplesBuilder(Settings settings) {
        super(settings);
    }

    @Override
    public PlainExamplesParseTree parseTreeFrom(Reader reader) {
        try {
            return new PlainExamplesParseTree(reader);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * We return getLabeledExamples by default, even if the labels are to be null
     *
     * @param parseTree
     * @return
     */
    @Override
    public Stream<Pair<Conjunction, LiftedExample>> buildFrom(PlainExamplesParseTree parseTree) {
        PlainGrammarVisitor plainGrammarVisitor = new PlainGrammarVisitor(this);
        PlainExamplesParseTreeExtractor examplesParseTreeExtractor = new PlainExamplesParseTreeExtractor(plainGrammarVisitor);
        NeuralogicParser.ExamplesFileContext examplesFileContext = parseTree.getRoot();
        inferInputFormatSettings(examplesFileContext);
        return examplesParseTreeExtractor.getLabeledExamples(examplesFileContext);
    }

    @Override
    public Stream<LogicSample> sampleFrom(Pair<Conjunction, LiftedExample> pair) {
        LiftedExample example = pair.s;
        if (pair.r == null || pair.r.facts == null || pair.r.facts.size() == 0) {
            //LOG.finest("Unlabeled input examples detected - queries must be provided in an ordered separate file");
            return Stream.of(createUnlabeledSample(String.valueOf(queryCounter), example));
        } else if ((pair.r.facts.size() == 1) && (pair.r.facts.get(0).getValue() == null)) { // the query literal is a LINK to query file
            ValuedFact query = pair.r.facts.get(0);
            return Stream.of(new LogicSample(null, createQueryAtom(query.literal.toString(), query, example)));
        } else if (pair.r.facts.size() == 1 && (pair.r.facts.get(0).getValue() != null)) {  // these are not for merging => normal single query
            ValuedFact query = pair.r.facts.get(0);
            return Stream.of(new LogicSample(query.getValue(), createQueryAtom(String.valueOf(queryCounter), query, example)))
                    .peek(s -> LOG.fine("New Sample created " + s.toString()));
        } else {    // these are not for merging => batch queries
            String minibatch = String.valueOf(queryCounter);
            return pair.r.facts.stream()
                    .map(query -> new LogicSample(query.getValue(), createQueryAtom(settings.queriesBatchPrefix + minibatch, query, example)))
                    .peek(s -> LOG.fine("New Batch Sample created " + s.toString()));
        }
    }

    private LogicSample createUnlabeledSample(String id, LiftedExample example) {
        return new LogicSample(null, createQueryAtom(id, null, example));
    }

    private void inferInputFormatSettings(NeuralogicParser.ExamplesFileContext examplesFileContext) {
        if (examplesFileContext.liftedExample().size() == 0) {
            LOG.warning("There are no examples in the example source (file)!");
            LOG.severe("Problem with parsing the examples");
            System.exit(4);
        } else if (examplesFileContext.liftedExample().size() == 1) {
            LOG.info("Detecting exactly 1 (big) example in the examples source (file), switching to knowledge-base mode.");
            if (settings.groundingMode != Settings.GroundingMode.GLOBAL) {
                settings.groundingMode = Settings.GroundingMode.GLOBAL;
                LOG.warning("Settings were set to a different grounding mode than detected! Will perform online rebuild of the grounding pipeline!");
                Pipeline groundingPipeline = encompassingPipe.getRoot().findPipeline("GroundingPipeline");
                if (groundingPipeline == null){
                    LOG.severe("Not able to rebuild grounding pipeline to perform GLOBAL grounding!!");
                } else {
                    groundingPipeline.rebuild(settings);
                }
            }
        } else {
            LOG.info("Detecting multiple individual examples in the examples source (file), assuming independent graph mode");
            settings.queriesAlignedWithExamples = true;
            if (settings.groundingMode != Settings.GroundingMode.STANDARD) {
                settings.groundingMode = Settings.GroundingMode.STANDARD;
                LOG.warning("Settings were set to a different grounding mode than detected! Will perform online rebuild of the grounding pipeline!");
                Pipeline groundingPipeline = encompassingPipe.getRoot().findPipeline("GroundingPipeline");
                groundingPipeline.rebuild(settings);
            }
        }
        if (examplesFileContext.label() != null && !examplesFileContext.label().isEmpty()) {
            settings.queriesAlignedWithExamples = false;
            LOG.info("Detecting examples to have ids/queries with them.");
        }
    }
}