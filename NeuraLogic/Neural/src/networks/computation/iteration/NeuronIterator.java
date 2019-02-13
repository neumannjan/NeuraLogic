package networks.computation.iteration;

import networks.structure.components.neurons.BaseNeuron;
import networks.structure.metadata.states.State;

import java.util.Iterator;

/**
 * A specification interface for an {@link Iterator} pattern over {@link BaseNeuron neurons}.
 *
 * remarks:
 * this is not Iteration strategy - no action taken, just returning next Neuron (for processing) (=PureIterator)
 *
 * @see NeuronIterating for the active iteration version
 * @see IterationStrategy
 *
 */
public interface NeuronIterator extends Iterator<BaseNeuron<BaseNeuron, State.Neural>>{

    @Override
    BaseNeuron<BaseNeuron, State.Neural> next();

    @Override
    boolean hasNext();

}