package networks.evaluation.values;

/**
 * Created by gusta on 8.3.17.
 */
public class ScalarValue extends Value {
    public double value;

    public ScalarValue() {
        value = 0;
    }

    public ScalarValue(double val) {
        value = val;
    }

    public ScalarValue(ValueInitializer valueInitializer) {
        initialize(valueInitializer);
    }

    @Override
    public void initialize(ValueInitializer valueInitializer) {
        valueInitializer.initScalar(this);
    }

    @Override
    protected final Value multiplyByMatrix(MatrixValue val2) {
        return null;
    }

    @Override
    protected final Value multiplyByVector(VectorValue val2) {
        return null;
    }

    @Override
    protected final Value multiplyByScalar(ScalarValue val2) {
        value *= val2.value;
        return this;
    }

    @Override
    protected final Value addMatrix(MatrixValue val2) {
        return null;
    }

    @Override
    protected final Value addVector(VectorValue val2) {
        return null;
    }

    @Override
    protected final Value addScalar(ScalarValue val2) {
        return null;
    }


}