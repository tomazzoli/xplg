package it.univr.xplg.execution.estimation;

import it.univr.xplg.execution.TraceOfExecutionWithImpacts;

import java.util.Vector;

public abstract class ImpactsEstimator implements IImpactsEstimator
{
    public abstract Vector<Double> getImpacts(TraceOfExecutionWithImpacts thisExecution);
}
