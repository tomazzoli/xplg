package it.univr.xplg.generator.log;

import it.univr.xplg.model.ProcessWithImpacts;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.out.XSerializer;
import plg.generator.IProgressVisualizer;
import plg.generator.engine.SimulationEngine;
import plg.generator.log.SimulationConfiguration;
import plg.model.event.StartEvent;
import plg.utils.Logger;
import plg.utils.XLogHelper;

import java.io.File;
import java.io.FileOutputStream;
import java.util.HashSet;
import java.util.Set;

public class LogGenerator
{
    private ProcessWithImpacts process;
    private SimulationConfiguration parameters;
    private IProgressVisualizer progress;
    private XLog lastGeneratedLog = null;

    /**
     * Basic constructor for a log generator
     *
     * @param process the process to use for the log generation
     * @param parameters the simulation configuration
     * @param progress the progress visualizer
     */
    public LogGenerator(ProcessWithImpacts process, SimulationConfiguration parameters, IProgressVisualizer progress) {
        this.process = process;
        this.parameters = parameters;
        this.progress = progress;
    }

    /**
     * This method returns an {@link XLog} object with the generated log.
     *
     * <p> If more than one {@link StartEvent} is available, the simulator picks
     * one randomly.
     *
     * @return the generated log
     * @throws Exception
     */
    public XLog generateLog() throws Exception {
        XLog log = unfinishedLogGeneration();
        progress.finished();
        return log;
    }

    /**
     * This method generates an {@link XLog}, using {@link #generateLog()} and
     * saves it to a {@link File}, using the provided {@link XSerializer}.
     *
     * @see plg.generator.log.LogGenerator#generateLog()
     * @param serializer the serializer to use for the log serialization
     * @param file the target file
     * @return the generated log
     * @throws Exception
     */
    public XLog generateAndSerializeLog(XSerializer serializer, File file) throws Exception {
        XLog log = null;
        try {
            log = unfinishedLogGeneration();
            progress.setIndeterminate(true);
            progress.setText("Saving log to file...");

            FileOutputStream fos = new FileOutputStream(file);
            serializer.serialize(log, fos);
            fos.close();
        } catch(Exception e) {
            throw e;
        } finally {
            progress.finished();
        }
        return log;
    }

    /**
     * This method returns the last log which has been generated
     *
     * @return the last log generated, or <tt>null</tt> if no log has been
     * generated yet by the current log generator
     */
    public XLog getLastGeneratedLog() {
        return lastGeneratedLog;
    }

    /**
     * This method generates a log but does not finish the provided
     * {@link IProgressVisualizer}. This is interally useful to perform other
     * tasks after the simulation.
     *
     * @return the generated log
     * @throws Exception
     */
    private XLog unfinishedLogGeneration() throws Exception {
        // configure progress
        progress.setText("Simulating process...");
        progress.setIndeterminate(false);
        progress.setMinimum(0);
        progress.setMaximum(parameters.getNumberOfTraces());
        progress.start();

        // define the number of CPU cores to use
        int coresToUse = 1; //(parameters.useMultithreading())? CPUUtils.CPUAvailable() : 1;
        Logger.instance().info("Starting simulation with " + coresToUse + " cores");

        // prepare the engine
        Set<TraceGenerator> traceGenerators = new HashSet<TraceGenerator>();
        SimulationEngine se = new SimulationEngine(coresToUse, parameters.getNumberOfTraces(), progress);

        for (int i = 0; i < parameters.getNumberOfTraces(); i++) {
            // the actual trace generator
            TraceGenerator tg = new TraceGenerator(
                    process,
                    String.format(parameters.getCaseIdPattern(), i),
                    parameters);
            traceGenerators.add(tg);
            se.enqueue(tg);
        }

        // power on!
        se.start();

        // collect all traces into the generated log
        progress.setIndeterminate(true);
        lastGeneratedLog = XLogHelper.generateNewXLog("tmp-process");
        for(TraceGenerator tg : traceGenerators) {
            lastGeneratedLog.add(tg.getGeneratedTrace());
        }

        return lastGeneratedLog;
    }
}
