package utils.drawing;

import constructs.example.ValuedFact;
import constructs.template.Template;
import constructs.template.components.BodyAtom;
import constructs.template.components.WeightedRule;
import constructs.template.types.GraphTemplate;
import ida.ilp.logic.Literal;
import settings.Settings;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

import static utils.drawing.GraphViz.sanitize;

/**
 * Created by gusta on 8.3.17.
 */
public class TemplateDrawer extends Drawer<Template> {
    public TemplateDrawer(Settings settings) {
        super(settings);
    }

    @Override
    public void loadGraph(Template obj) {
        if (obj instanceof GraphTemplate) {
            loadGraph((GraphTemplate) obj);
        } else {
            loadGraph(new GraphTemplate(obj));
        }
    }

    public void loadGraph(GraphTemplate obj) {
        Map<Literal, List<WeightedRule>> atom2rules = obj.atom2rules;
        LinkedHashSet<ValuedFact> facts = obj.facts;

        for (ValuedFact fact : facts) {
            graphviz.addln(draw(fact));
        }

        for (Map.Entry<Literal, List<WeightedRule>> entry : atom2rules.entrySet()) {
            Literal literal = entry.getKey();
            List<WeightedRule> rules = entry.getValue();
            graphviz.addln(draw(literal));
            for (WeightedRule rule : rules) {
                graphviz.addln(draw(rule)); //rule with subsumable head by this literal
                graphviz.addln(draw(literal, rule));
                for (BodyAtom bodyAtom : rule.getBody()) {
                    graphviz.addln(draw(bodyAtom));
                    graphviz.addln(draw(rule, bodyAtom)); //body literals from the  rule
                }
            }
        }
    }

    private String draw(WeightedRule rule, BodyAtom bodyAtom) {
        String edgeColor = bodyAtom.isNegated() ? "red" : "blue";
        return rule.hashCode() + " -> " + bodyAtom.hashCode() + "[label=" + sanitize(bodyAtom.getConjunctWeight().toString()) + ", color=" + edgeColor + "]";
    }

    private String draw(BodyAtom bodyAtom) {
        return draw(bodyAtom.literal);
    }

    private String draw(Literal literal, WeightedRule rule) {
        return literal.hashCode() + " -> " + rule.hashCode() + "[label=" + sanitize(rule.getWeight().toString()) + "]";
    }

    private String draw(Literal literal) {
        return literal.hashCode() + "[label=" + sanitize(literal.toString()) + "]";
    }

    private String draw(WeightedRule rule) {
        return rule.hashCode() + "[label=" + sanitize(rule.getOriginalString()) + "]";
    }

    private String draw(ValuedFact fact) {
        return fact.hashCode() + "[label=" + sanitize(fact.toString()) + "]";
    }

}
