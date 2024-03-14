package it.univr.xplg.generator.log;

import plg.generator.engine.SimulationEngine;
import plg.generator.log.SimulationConfiguration;
import plg.model.Process;
import plg.model.event.StartEvent;

/***
 * probabilmemte qui qualche metodo andrà modificato per tenere traccia degli impatti
 *
 * This class represents an object that is able to generate a single process (with impacts)
 * trace. This class is fed to the {@link SimulationEngine} in order to simulate
 * an entire log.
 *
 * <p> The most important method is {@link #runWithException()}. It generates a
 * single process instance and adds the generated trace into the given log.
 *
 * <p> If more than one {@link StartEvent} is available, the simulator picks
 * one randomly.
 *
 * @author Claudio Tomazzoli
 * @see plg.generator.log.TraceGenerator
 * @see plg.generator.engine.SimulationEngine
 */
public class TraceGenerator extends plg.generator.log.TraceGenerator
{
    public TraceGenerator(Process process, String caseId, SimulationConfiguration parameters) {
        super(process, caseId, parameters);
    }
}
