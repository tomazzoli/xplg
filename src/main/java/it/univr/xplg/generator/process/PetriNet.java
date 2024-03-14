package it.univr.xplg.generator.process;

import plg.generator.engine.SimulationEngine;
import plg.model.Process;
import plg.model.event.StartEvent;

/***
 * probabilmemte qui non ci sarà nulla da modificare, ma intanto preparo la struttura
 *
 * @author Claudio Tomazzoli
 * @see plg.generator.log.TraceGenerator
 * @see plg.generator.engine.SimulationEngine
 */

public class PetriNet extends plg.generator.process.petrinet.PetriNet
{

    public PetriNet(Process originalProcess) {
        super(originalProcess);
    }
}
