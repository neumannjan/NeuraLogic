package networks.computation.functions;

import networks.computation.training.evaluation.values.Value;
import networks.structure.components.neurons.Neurons;

import java.util.ArrayList;

/**
 * Created by gusta on 8.3.17.
 */
public class Sigmoid implements Activation {
    @Override
    public Value evaluate(ArrayList<? extends Neurons> inputs) {
        return null;
    }

    @Override
    public Value differentiateAt(Value x) {
        return null;
    }

    @Override
    public Activation differentiateGlobally() {
        return null;
    }
}