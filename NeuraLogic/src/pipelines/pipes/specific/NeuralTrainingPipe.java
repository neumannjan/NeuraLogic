package pipelines.pipes.specific;

import ida.utils.tuples.Pair;
import networks.computation.evaluation.results.Results;
import networks.computation.training.NeuralModel;
import networks.computation.training.NeuralSample;
import networks.computation.training.trainingStrategies.TrainingStrategy;
import pipelines.Pipe;
import settings.Settings;

import java.util.logging.Logger;
import java.util.stream.Stream;

public class NeuralTrainingPipe extends Pipe<Pair<NeuralModel, Stream<NeuralSample>>, Pair<NeuralModel, Results>> {
    private static final Logger LOG = Logger.getLogger(NeuralTrainingPipe.class.getName());
    Settings settings;

    public NeuralTrainingPipe(Settings settings) {
        super("NeuralEvaluationPipe");
        this.settings = settings;
    }

    protected NeuralTrainingPipe(String id) {
        super(id);
    }

    /**
     * Terminating operation (obviously)!
     *
     * @param neuralModelStreamPair
     * @return
     */
    @Override
    public Pair<NeuralModel, Results> apply(Pair<NeuralModel, Stream<NeuralSample>> neuralModelStreamPair) {
        NeuralModel model = neuralModelStreamPair.r;
        Stream<NeuralSample> sampleStream = neuralModelStreamPair.s;

        TrainingStrategy trainingStrategy = TrainingStrategy.getFrom(settings);
        Results results = trainingStrategy.train();
        return new Pair<>(trainingStrategy.getBestModel(), results);
    }
}