package networks.computation.training.strategies.Hyperparameters;

import networks.computation.evaluation.results.Progress;

import java.util.logging.Logger;

public class StaticRestartingStrategy extends RestartingStrategy {
    private static final Logger LOG = Logger.getLogger(StaticRestartingStrategy.class.getName());
    private int maxSteps;

    public StaticRestartingStrategy(int maxSteps){
        this.maxSteps = maxSteps;
    }

    @Override
    public boolean continueRestart(Progress progress) {
        if (progress.getEpochCount() > maxSteps){
            return false;
        }
        return true;
    }
}
