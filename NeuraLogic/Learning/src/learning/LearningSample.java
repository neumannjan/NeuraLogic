package learning;

import evaluation.values.Value;

public abstract class LearningSample<Q extends Query> {
    //should learning samples contain reference to Model - probably not (Structure learning)

    public Q query;
    public Value target;

    public String getId(){
        return query.ID;
    }

    public double getImportance(){
        return query.importance;
    }

    @Override
    public String toString() {
        return getId() + ": " + target + "=" + query;
    }
}