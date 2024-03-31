package it.univr.xplg.execution.estimation;

import it.univr.xplg.execution.TraceOfExecutionWithImpacts;
import it.univr.xplg.model.TaskWithImpacts;

import java.util.List;
import java.util.Vector;

public class ProbabilisticEstimator extends ImpactsEstimator
{
    public ProbabilisticEstimator(){;}

    @Override
    public Vector<Double> getImpacts(TraceOfExecutionWithImpacts thisExecution)
    {
        List<TaskWithImpacts> tasks= thisExecution.get_executedTasks();
        Vector<Double> result = new Vector<Double>();
        // da fare, ovviamente
        return result;
    }
}
