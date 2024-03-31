package it.univr.xplg.execution.estimation;

import java.lang.reflect.Constructor;
import java.util.HashMap;

public class EstimatorFactory
{
    public final static String SIMPLESUM = "SIMPLESUM";
    public final static String PROBABILISTIC = "PROBABILISTIC";
    private final HashMap<String,String> classiEstimaotri;

    public EstimatorFactory()
    {
        classiEstimaotri =new HashMap<String,String>();
        classiEstimaotri.put(SIMPLESUM,"it.univr.xplg.execution.estimation.SimpleSumEstimator");
        classiEstimaotri.put(PROBABILISTIC,"it.univr.xplg.execution.estimation.ProbabilistcEstimator");
        // per estendere, rifattorizzare con external string....
    }

    public ImpactsEstimator getInstance(String nomeClasse)
    {
        ImpactsEstimator result=null;
        try
        {
            Class<?> concreteClass = Class.forName(nomeClasse);
            Constructor<?> ct= concreteClass.getConstructor();
            result = (ImpactsEstimator) ct.newInstance();
        }
        catch(Exception e)
        {
            //logger.error(e.getMessage());
            //logger.error(nomeClasse);
            e.printStackTrace();
        }
        return result;
    }
}
