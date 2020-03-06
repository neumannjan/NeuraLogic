package constructs.building;

import constructs.building.factories.ConstantFactory;
import constructs.building.factories.WeightFactory;
import constructs.building.factories.WeightedPredicateFactory;
import constructs.example.LogicSample;
import org.antlr.v4.runtime.ParserRuleContext;
import parsing.grammarParsing.PlainParseTree;
import pipelines.Pipe;
import settings.Settings;
import settings.Source;

import java.io.Reader;
import java.util.logging.Logger;
import java.util.stream.Stream;

public abstract class LogicSourceBuilder<I extends PlainParseTree<? extends ParserRuleContext>, O> {
    private static final Logger LOG = Logger.getLogger(LogicSourceBuilder.class.getName());

    public Settings settings;

    protected Pipe<Source, Stream<LogicSample>> encompassingPipe;   //todo now now this is antipattern - dependency on Pipelines module, send in some rebuilt callback hook during creation instead

    // Constants are shared over the whole logic source
    public ConstantFactory constantFactory = new ConstantFactory();
    // Predicates are shared over the whole logic source
    public WeightedPredicateFactory predicateFactory = new WeightedPredicateFactory();
    // Weights are shared over the whole logic source
    public WeightFactory weightFactory = new WeightFactory();
    //variable factories are typically just used locally (variables shared only within clauses)

    public void setFactoriesFrom(LogicSourceBuilder other){
        this.constantFactory = other.constantFactory;
        this.predicateFactory = other.predicateFactory;
        this.weightFactory = other.weightFactory;
    }

    abstract I parseTreeFrom(Reader reader);

    abstract O buildFrom(I parseTree);

    /**
     * Sometimes it is useful to store reference to the outer pipe within which we work
     * @param encompassingPipe
     */
    public void setEncompassingPipe(Pipe<Source, Stream<LogicSample>> encompassingPipe) {
        this.encompassingPipe = encompassingPipe;
    }
}