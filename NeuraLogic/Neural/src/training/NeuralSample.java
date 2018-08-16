package training;

import learning.LearningSample;
import networks.evaluation.values.Value;
import networks.structure.lrnnTypes.QueryNeuron;

/**
 * Created by gusta on 8.3.17.
 */
public class NeuralSample extends LearningSample<QueryNeuron> {

    public NeuralSample(Value v, QueryNeuron q){
        this.query = q;
        this.target = v;
    }

}