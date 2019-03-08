package parsing.grammarParsing;

import com.sun.istack.internal.NotNull;
import constructs.Conjunction;
import constructs.WeightedPredicate;
import constructs.building.LogicSourceBuilder;
import constructs.building.factories.VariableFactory;
import constructs.example.LiftedExample;
import constructs.example.ValuedFact;
import constructs.template.components.BodyAtom;
import constructs.template.components.HeadAtom;
import constructs.template.components.WeightedRule;
import constructs.template.metadata.RuleMetadata;
import ida.ilp.logic.Term;
import networks.computation.evaluation.values.*;
import networks.structure.components.weights.Weight;
import parsing.antlr.NeuralogicBaseVisitor;
import parsing.antlr.NeuralogicParser;
import utils.generic.Pair;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Contains logic of construction of logical structures while walking/visiting parse trees of logic programs (templates or samples).
 * <p>
 * Rules and atom are constructed independently. Predicates, constants and variables are shared via factory methods.
 */
public class PlainGrammarVisitor extends GrammarVisitor {
    private static final Logger LOG = Logger.getLogger(PlainGrammarVisitor.class.getName());

    public PlainGrammarVisitor(LogicSourceBuilder builder) {
        super(builder);
    }


    public class RuleLineVisitor extends NeuralogicBaseVisitor<WeightedRule> {
        // Variable factory gets initialized here - variable scope is per rule
        VariableFactory variableFactory = new VariableFactory();

        @Override
        public WeightedRule visitLrnnRule(@NotNull NeuralogicParser.LrnnRuleContext ctx) {

            WeightedRule rule = new WeightedRule();

            rule.originalString = ctx.getText();

            AtomVisitor headVisitor = new AtomVisitor();
            headVisitor.variableFactory = this.variableFactory;
            // we hack it through BodyAtom here to pass the weight of the rule
            BodyAtom headAtom = ctx.atom().accept(headVisitor);
            rule.weight = headAtom.getConjunctWeight();  //rule weight

            rule.head = new HeadAtom(headAtom);

            AtomConjunctionVisitor bodyVisitor = new AtomConjunctionVisitor();
            bodyVisitor.variableFactory = this.variableFactory;
            rule.body = ctx.conjunction().accept(bodyVisitor);

            rule.offset = ctx.offset() != null ? ctx.offset().accept(new WeightVisitor()) : null;
            rule.metadata = ctx.metadataList() != null ? new RuleMetadata(builder.settings, ctx.metadataList().accept(new MetadataListVisitor())) : null; //rule metadata are set directly here, as they cannot appear at arbitrary place as opposed to the other metadata, which are processed in a later stage

            return rule;
        }
    }


    public class AtomConjunctionVisitor extends NeuralogicBaseVisitor<List<BodyAtom>> {
        VariableFactory variableFactory;

        @Override
        public List<BodyAtom> visitConjunction(@NotNull NeuralogicParser.ConjunctionContext ctx) {
            AtomVisitor atomVisitor = new AtomVisitor();
            atomVisitor.variableFactory = this.variableFactory;
            List<BodyAtom> atomList = ctx.atom()
                    .stream()
                    .map(atom -> atom.accept(atomVisitor))
                    .collect(Collectors.toList());
            return atomList;
        }
    }

    public class AtomVisitor extends NeuralogicBaseVisitor<BodyAtom> {
        VariableFactory variableFactory;

        @Override
        public BodyAtom visitAtom(@NotNull NeuralogicParser.AtomContext ctx) {

            TermVisitor termVisitor = new TermVisitor();
            termVisitor.variableFactory = this.variableFactory;
            List<Term> terms = ctx.termList().term()
                    .stream()
                    .map(term -> term.accept(termVisitor))
                    .collect(Collectors.toList());

            WeightedPredicate predicate = ctx.predicate().accept(new PredicateVisitor(terms.size()));
            Weight weight = ctx.weight() != null ? ctx.weight().accept(new WeightVisitor()) : null;

            BodyAtom bodyAtom = new BodyAtom(predicate, terms, ctx.negation() != null, weight);
            bodyAtom.originalString = ctx.getText();

            return bodyAtom;
        }
    }

    public class LiftedExampleVisitor extends NeuralogicBaseVisitor<LiftedExample> {
        VariableFactory variableFactory = new VariableFactory();

        @Override
        public LiftedExample visitLiftedExample(@NotNull NeuralogicParser.LiftedExampleContext ctx) {
            FactConjunctionVisitor factConjunctionVisitor = new FactConjunctionVisitor();
            factConjunctionVisitor.variableFactory = this.variableFactory;
            RuleLineVisitor ruleLineVisitor = new RuleLineVisitor();
            ruleLineVisitor.variableFactory = this.variableFactory;

            List<Conjunction> conjunctions = ctx.conjunction()
                    .stream()
                    .map(conj -> conj.accept(factConjunctionVisitor))
                    .collect(Collectors.toList());

            List<WeightedRule> rules = ctx.lrnnRule()
                    .stream()
                    .map(conj -> conj.accept(ruleLineVisitor))
                    .collect(Collectors.toList());

            LiftedExample liftedExample = new LiftedExample(conjunctions, rules);
            return liftedExample;
        }
    }

    public class FactConjunctionVisitor extends NeuralogicBaseVisitor<Conjunction> {
        VariableFactory variableFactory;

        @Override
        public Conjunction visitConjunction(@NotNull NeuralogicParser.ConjunctionContext ctx) {
            FactVisitor factVisitor = new FactVisitor();
            factVisitor.variableFactory = this.variableFactory;
            List<ValuedFact> conjunction = ctx.atom()
                    .stream()
                    .map(atom -> atom.accept(factVisitor))
                    .collect(Collectors.toList());
            return new Conjunction(conjunction);
        }
    }

    public class FactVisitor extends NeuralogicBaseVisitor<ValuedFact> {
        VariableFactory variableFactory;

        @Override
        public ValuedFact visitFact(@NotNull NeuralogicParser.FactContext ctx) {
            return visitAtom(ctx.atom());
        }

        @Override
        public ValuedFact visitAtom(@NotNull NeuralogicParser.AtomContext ctx) {
            TermVisitor termVisitor = new TermVisitor();
            termVisitor.variableFactory = this.variableFactory;
            List<Term> terms = ctx.termList().term()
                    .stream()
                    .map(term -> term.accept(termVisitor))
                    .collect(Collectors.toList());

            WeightedPredicate predicate = ctx.predicate().accept(new PredicateVisitor(terms.size()));

            Weight weight = ctx.weight().accept(new WeightVisitor());
            if (weight == null) {
                weight = builder.weightFactory.construct("foo", new ScalarValue(0), false);
            }

            ValuedFact fact = new ValuedFact(predicate, terms, ctx.negation() != null, weight);
            fact.originalString = ctx.getText();

            return fact;
        }
    }

    private class PredicateVisitor extends NeuralogicBaseVisitor<WeightedPredicate> {
        int arity = -1;

        public PredicateVisitor(int arity) {
            this.arity = arity;
        }

        @Override
        public WeightedPredicate visitPredicate(@NotNull NeuralogicParser.PredicateContext ctx) {
            if (ctx.INT() != null) {
                try {
                    arity = Integer.parseInt(ctx.INT().getText());
                } catch (Exception ex) {
                    LOG.severe("Cannot parse arity of a predicate from " + ctx.getText());
                }
            }
            WeightedPredicate predicate = builder.predicateFactory.construct(ctx.ATOMIC_NAME().getText(), arity, ctx.SPECIAL() != null);

            return predicate;
        }
    }

    private class WeightVisitor extends NeuralogicBaseVisitor<Weight> {
        @Override
        public Weight visitWeight(@NotNull NeuralogicParser.WeightContext ctx) {
            Value value = null;
            boolean fixed = false;
            if (ctx.fixedValue() != null) {
                fixed = true;
                value = parseValue(ctx.fixedValue().value());
            } else if (ctx.value() != null) {
                fixed = false;
                value = parseValue(ctx.value());
            } else {
                LOG.severe("Weight is neither fixed nor learnable");
            }
            Weight weight;
            if (ctx.ATOMIC_NAME() != null) {
                String name = ctx.ATOMIC_NAME().getText();
                weight = builder.weightFactory.construct(name, value, fixed);
            } else {
                weight = builder.weightFactory.construct(value, fixed);
            }

            return weight;
        }

        public Value parseValue(NeuralogicParser.ValueContext ctx) {
            Value value = null;
            if (ctx.number() != null) {
                value = new ScalarValue(Float.parseFloat(ctx.number().getText()));
            } else if (ctx.vector() != null) {
                List<Double> vector = ctx.vector().number().stream().map(num -> Double.parseDouble(num.getText())).collect(Collectors.toList());
                value = new VectorValue(vector);
            } else if (ctx.dimensions() != null) {
                List<Integer> dims = ctx.vector().number().stream().map(num -> Integer.parseInt(num.getText())).collect(Collectors.toList());
                if (dims.size() == 1) {
                    value = new ScalarValue();
                } else if (dims.size() == 2) {
                    if (dims.get(0) == 1)
                        value = new VectorValue(dims.get(1));
                    else if (dims.get(1) == 1)
                        value = new VectorValue(dims.get(0));   //todo transposition?
                    else
                        value = new MatrixValue(dims.get(0), dims.get(1));
                }
            } else {
                LOG.severe("Value is neither number nor vector: Could not parse numeric value from " + ctx.getText());
            }
            if (value == null) {
                LOG.severe("Error during constructs.building numeric value from " + ctx.getText());
            }
            return value;
        }
    }

    private class TermVisitor extends NeuralogicBaseVisitor<Term> {
        public VariableFactory variableFactory;

        @Override
        public Term visitTerm(@NotNull NeuralogicParser.TermContext ctx) {
            Term term;

            if (ctx.constant() != null) {
                term = builder.constantFactory.construct(ctx.getText());
            } else if (ctx.variable() != null)
                term = variableFactory.construct(ctx.getText());  //TODO check if correct over several rules with the same Variable names (no sharing wanted)
            else {
                LOG.severe("Term is neither Constant nor Variable");
                term = null;
            }
            return term;
        }
    }

    public class PredicateMetadataVisitor extends NeuralogicBaseVisitor<Pair<WeightedPredicate, Map<String, Object>>> {
        @Override
        public Pair<WeightedPredicate, Map<String, Object>> visitPredicateMetadata(@NotNull NeuralogicParser.PredicateMetadataContext ctx) {
            int arity = -1;
            try {
                arity = Integer.parseInt(ctx.predicate().INT().getText());
            } catch (Exception ex) {
                LOG.severe("Cannot parse arity of a predicate from " + ctx.getText());
            }
            WeightedPredicate predicate = builder.predicateFactory.construct(ctx.predicate().ATOMIC_NAME().getText(), arity, ctx.predicate().SPECIAL() != null);
            Map<String, Object> metadata = ctx.metadataList().accept(new MetadataListVisitor());
            return new Pair(predicate, metadata);
        }
    }

    public class WeightMetadataVisitor extends NeuralogicBaseVisitor<Pair<Weight, Map<String, Object>>> {
        @Override
        public Pair<Weight, Map<String, Object>> visitWeightMetadata(@NotNull NeuralogicParser.WeightMetadataContext ctx) {
            Weight weight = builder.weightFactory.construct(ctx.ATOMIC_NAME().getText());
            Map<String, Object> metadata = ctx.metadataList().accept(new MetadataListVisitor());
            return new Pair(weight, metadata);
        }
    }

    public class PredicateOffsetVisitor extends NeuralogicBaseVisitor<Pair<WeightedPredicate, Weight>> {
        @Override
        public Pair<WeightedPredicate, Weight> visitPredicateOffset(@NotNull NeuralogicParser.PredicateOffsetContext ctx) {
            int arity = -1;
            try {
                arity = Integer.parseInt(ctx.predicate().INT().getText());
            } catch (Exception ex) {
                LOG.severe("Cannot parse arity of a predicate from " + ctx.getText());
            }
            WeightedPredicate predicate = builder.predicateFactory.construct(ctx.predicate().ATOMIC_NAME().getText(), arity, ctx.predicate().SPECIAL() != null);
            Weight offset = ctx.weight().accept(new WeightVisitor());
            predicate.weight = offset;
            return new Pair(predicate, offset);
        }
    }

    /**
     * Metadata are parsed as a mere String->Object mappings.
     * The extraction of the particular {@link constructs.template.metadata.Parameter} to {@link constructs.template.metadata.ParameterValue} mappings
     * is done during postprocessing/building of logiacl constructs as it may require more complex recognition logic than just parsing.
     */
    private class MetadataListVisitor extends NeuralogicBaseVisitor<Map<String, Object>> {
        @Override
        public Map<String, Object> visitMetadataList(@NotNull NeuralogicParser.MetadataListContext ctx) {
            Map<String, Object> metadata = new LinkedHashMap<>();
            for (NeuralogicParser.MetadataValContext paramVal : ctx.metadataVal()) {
                String parameter = paramVal.ATOMIC_NAME(0).getText();
                String valueText = paramVal.ATOMIC_NAME(1).getText();
                Object value;
                if (paramVal.DOLLAR() != null) {
                    value = builder.weightFactory.construct(valueText);
                } else if (paramVal.value() != null) {
                    value = new WeightVisitor().parseValue(paramVal.value());
                } else {
                    value = new StringValue(valueText);
                }
                metadata.put(parameter, value);
            }
            return metadata;
        }
    }

    public class TemplateMetadataVisitor extends NeuralogicBaseVisitor<Map<String, Object>> {
        @Override
        public Map<String, Object> visitTemplateMetadata(@NotNull NeuralogicParser.TemplateMetadataContext ctx) {
            Map<String, Object> metadata = ctx.metadataList().accept(new MetadataListVisitor());
            return metadata;
        }
    }
}