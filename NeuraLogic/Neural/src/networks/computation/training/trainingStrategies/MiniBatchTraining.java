package networks.computation.training.trainingStrategies;

import networks.computation.evaluation.results.Results;
import networks.computation.training.NeuralModel;
import networks.computation.training.NeuralSample;
import settings.Settings;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

public class MiniBatchTraining extends TrainingStrategy {
    private static final Logger LOG = Logger.getLogger(MiniBatchTraining.class.getName());

    public MiniBatchTraining(Settings settings, NeuralModel model, List<NeuralSample> sampleList) {
        super(settings, model, sampleList);
    }

    @Override
    protected void initTraining() {

    }

    @Override
    protected void initEpoch() {
        if (settings.minibatchShuffle) {  //shuffle for minibatch version only!
            Collections.shuffle(sampleList, settings.random);
        }
    }

    @Override
    protected void learnEpoch() {

    }

    @Override
    protected void initRestart() {

    }

    @Override
    protected Results finish() {
        return null;
    }

    @Override
    public NeuralModel getBestModel() {
        return null;
    }


    public class BatchIterator implements Iterator<List<NeuralSample>> {
        int i = 0;

        @Override
        public boolean hasNext() {
            return i < sampleList.size();
        }

        @Override
        public List<NeuralSample> next() {
            List<NeuralSample> neuralSamples = sampleList.subList(i, i + settings.minibatchSize);
            i += settings.minibatchSize;
            return neuralSamples;
        }
    }
}
