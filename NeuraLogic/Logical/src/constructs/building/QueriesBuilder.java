package constructs.building;

import constructs.Conjunction;
import constructs.example.LogicSample;
import constructs.example.ValuedFact;
import networks.computation.evaluation.values.ScalarValue;
import parsing.antlr.NeuralogicParser;
import parsing.grammarParsing.PlainGrammarVisitor;
import parsing.queries.PlainQueriesParseTree;
import parsing.queries.PlainQueriesParseTreeExtractor;
import settings.Settings;
import utils.generic.Pair;

import java.io.IOException;
import java.io.Reader;
import java.util.logging.Logger;
import java.util.stream.Stream;

public class QueriesBuilder extends SamplesBuilder<PlainQueriesParseTree, Pair<ValuedFact, Conjunction>> {
    private static final Logger LOG = Logger.getLogger(QueriesBuilder.class.getName());

    public QueriesBuilder(Settings settings) {
        super(settings);
    }

    @Override
    public PlainQueriesParseTree parseTreeFrom(Reader reader) {
        try {
            return new PlainQueriesParseTree(reader);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public Stream<Pair<ValuedFact, Conjunction>> buildFrom(PlainQueriesParseTree parseTree) {
        PlainGrammarVisitor plainGrammarVisitor = new PlainGrammarVisitor(this);
        PlainQueriesParseTreeExtractor queriesParseTreeExtractor = new PlainQueriesParseTreeExtractor(plainGrammarVisitor);
        NeuralogicParser.QueriesFileContext queriesFileContext = parseTree.getRoot();
        inferInputFormatSettings(queriesFileContext);
        return queriesParseTreeExtractor.getLabeledQueries(queriesFileContext);
    }

    /**
     * Stream of samples belonging to a single example (subset of the whole sample set)
     * //todo test measure performance of this (and possibly replace with 2 methods, one without creating stream when not necessary)
     *
     * @param pair
     * @return
     */
    @Override
    public Stream<LogicSample> sampleFrom(Pair<ValuedFact, Conjunction> pair) {

        if (pair.s.facts == null || pair.s.facts.size() == 0){
            LOG.severe("Cannot extract LogicSample(s) without a query provided!");
            return Stream.empty();
        }
        Stream<ValuedFact> queries = pair.s.facts.stream();

        if (pair.r != null) {   // jointly labeled query(ies)
            String id = pair.r.literal.toString();
            if (pair.r.getValue() != null) { // has importance set
                if (pair.r.getValue() instanceof ScalarValue) {
                    final double importance = ((ScalarValue) pair.r.getValue()).value;
                    return queries.map(f -> new LogicSample(f.getValue(), createQueryAtom(id, importance, f)));
                } else {
                    LOG.warning("Query with non-scalar target value not supported (yet)");
                    return queries.map(f -> new LogicSample(f.getValue(), createQueryAtom(id, f)));
                }
            } else {
                return queries.map(f -> new LogicSample(f.getValue(), createQueryAtom(id, f)));
            }
        }

        String minibatch = String.valueOf(queryCounter);
        return queries.map(f -> new LogicSample(f.getValue(), createQueryAtom(minibatch, f)));
    }

    private void inferInputFormatSettings(NeuralogicParser.QueriesFileContext queriesFileContext) {
        if (queriesFileContext.atom() != null && !queriesFileContext.atom().isEmpty()){
            LOG.info("Detecting queries to have Ids (to examples) with them!");
        }
        int size = queriesFileContext.conjunction().size();
        if (size == 0){
            LOG.warning("There are no queries in the queries source (file)!");
        } else if (size == 1){
            LOG.warning("There is only 1 query to learn from!");
        } else {
            LOG.info("Detecting multiple independent queries.");
        }
        if (queriesFileContext.conjunction(0).atom().size() > 1){   //todo this is just a heuristic
            LOG.info("Detecting multiple individual queries per example.");
            settings.oneQueryPerExample = false;
        } else {
            LOG.info("Heuristically detecting atomic queries (no batch queries)");
            settings.oneQueryPerExample = true;
        }
    }
}