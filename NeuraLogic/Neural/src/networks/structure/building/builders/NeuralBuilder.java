package networks.structure.building.builders;

import constructs.building.factories.WeightFactory;
import networks.structure.building.factories.NeuralNetFactory;
import networks.structure.building.factories.NeuronFactory;
import settings.Settings;

import java.util.logging.Logger;

public class NeuralBuilder {
    private static final Logger LOG = Logger.getLogger(NeuralBuilder.class.getName());
    Settings settings;

    public NeuralBuilder(Settings settings) {
        this.settings = settings;
    }


    public NeuralBuilder() {

    }

    public NeuronFactory neuronFactory;
    public WeightFactory weightFactory;
    public NeuralNetFactory networkFactory;

    public void setFactoriesFrom(NeuralBuilder other) {
        this.neuronFactory = other.neuronFactory;
        this.networkFactory = other.networkFactory;
        this.weightFactory = other.weightFactory;
    }
}