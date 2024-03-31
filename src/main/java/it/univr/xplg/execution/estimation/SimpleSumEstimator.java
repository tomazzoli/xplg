package it.univr.xplg.execution.estimation;

import it.univr.xplg.execution.TraceOfExecutionWithImpacts;
import it.univr.xplg.model.TaskWithImpacts;

import java.util.List;
import java.util.Vector;

public class SimpleSumEstimator extends ImpactsEstimator
{
    public SimpleSumEstimator(){;}

    @Override
    public Vector<Double> getImpacts(TraceOfExecutionWithImpacts thisExecution)
    {
        List<TaskWithImpacts> tasks= thisExecution.get_executedTasks();
        Vector<Double> result = new Vector<Double>();
        if(tasks!=null && tasks.size()>0)
        {
            TaskWithImpacts primo = tasks.get(0);
            Vector<Double> primi = primo.getImpacts();
            if(primi!=null && primi.size()>0)
            {
                int quanteComponenti = primi.size();
                for(int i = 0; i < quanteComponenti; i++)
                {
                    double somma = 0;
                    for(TaskWithImpacts t:tasks)
                    {
                        Vector<Double> impatti = t.getImpacts();
                        somma = somma + impatti.get(i);
                    }
                    result.add(somma);
                }
            }
        }
        return result;
    }
}
