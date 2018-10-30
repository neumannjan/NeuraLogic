package networks.computation.evaluation.results;

import java.util.List;

/**
 * Created by gusta on 8.3.17.
 */
public class ClassificationResults extends RegressionResults {
    private Double precision;
    private Double recall;
    private Double error;
    private Double f_Measure;
    private Double majorityErr;

    public ClassificationResults(List<Result> outputs) {
        super(outputs);
    }

    @Override
    public String toString() {
        return null;
    }

    @Override
    public boolean recalculate() {
        return false;
    }
}