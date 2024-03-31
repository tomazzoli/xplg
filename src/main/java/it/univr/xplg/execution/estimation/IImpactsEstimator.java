package it.univr.xplg.execution.estimation;

import it.univr.xplg.execution.TraceOfExecutionWithImpacts;

import java.util.Vector;

public interface IImpactsEstimator
{
    public Vector<Double> getImpacts(TraceOfExecutionWithImpacts thisExecution);
}
