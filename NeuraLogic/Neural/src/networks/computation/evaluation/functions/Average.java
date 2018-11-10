package networks.computation.evaluation.functions;

import networks.computation.evaluation.values.ScalarValue;
import networks.computation.evaluation.values.Value;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.logging.Logger;

public class Average implements Aggregation {
    private static final Logger LOG = Logger.getLogger(Average.class.getName());

    @Override
    public Value evaluate(List<Value> inputs) {
        return null;
    }

    @Override
    public Value differentiate(List<Value> inputs) {
        return new ScalarValue(1/inputs.size());    //todo check
    }

    @Override
    public @Nullable Activation differentiateGlobally() {
        return null;
    }
}