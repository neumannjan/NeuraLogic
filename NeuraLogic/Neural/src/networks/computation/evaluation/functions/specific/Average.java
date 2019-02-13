package networks.computation.evaluation.functions.specific;

import networks.computation.evaluation.functions.Aggregation;
import networks.computation.evaluation.values.ScalarValue;
import networks.computation.evaluation.values.Value;

import java.util.List;
import java.util.logging.Logger;

public class Average extends Aggregation {
    private static final Logger LOG = Logger.getLogger(Average.class.getName());

    @Override
    public Value evaluate(List<Value> inputs) {
        Value sum = inputs.get(0).clone();
        for (int i = 1, len = inputs.size(); i < len; i++) {
            sum.plus(inputs.get(i));
        }
        return sum.times(new ScalarValue(1/inputs.size()));
    }

    @Override
    public Value differentiate(List<Value> inputs) {
        return new ScalarValue(1/inputs.size());    //todo check
    }

}