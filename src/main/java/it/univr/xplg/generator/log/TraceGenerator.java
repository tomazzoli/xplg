package it.univr.xplg.generator.log;

import it.univr.xplg.model.ProcessWithImpacts;
import it.univr.xplg.model.TaskWithImpacts;
import org.deckfour.xes.extension.std.XLifecycleExtension;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XTrace;
import plg.exceptions.InvalidScript;
import plg.generator.engine.SimulationEngine;
import plg.generator.engine.ThreadWithException;
import plg.generator.log.SimulationConfiguration;
import plg.generator.log.noise.NoiseProcessor;
import plg.model.Component;
import plg.model.FlowObject;
import plg.model.data.*;
import plg.model.event.StartEvent;
import plg.model.gateway.ExclusiveGateway;
import plg.model.gateway.ParallelGateway;
import plg.model.sequence.Sequence;
import plg.utils.Pair;
import plg.utils.SetUtils;
import plg.utils.XLogHelper;

import java.util.*;

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
 * @see it.univr.xplg.generator.log.TraceGenerator
 * @see plg.generator.engine.SimulationEngine
 */
public class TraceGenerator extends ThreadWithException<XTrace> {
    private Map<Component, Long> componentsObservationTime;
    private Map<Sequence, Integer> observationsCounter;
    private Set<Sequence> tokens;

    private ProcessWithImpacts process;
    private String caseId;
    private SimulationConfiguration parameters;
    private NoiseProcessor noiseProcessor;

    /**
     * Basic class constructor
     *
     * @param process    the process that is going to originate this trace
     * @param caseId     the name to provide to this trace
     * @param parameters the configuration parameters for the simulation
     */
    public TraceGenerator(ProcessWithImpacts process, String caseId, SimulationConfiguration parameters) {
        this.process = process;
        this.caseId = caseId;
        this.parameters = parameters;

        this.noiseProcessor = new NoiseProcessor(parameters.getNoiseConfiguration());
        this.componentsObservationTime = new HashMap<Component, Long>();
        this.observationsCounter = new HashMap<Sequence, Integer>();
        this.tokens = new HashSet<Sequence>();
    }

    /**
     * This method returns the trace that has been generated. If the generation
     * results in some error, then this method returns <tt>null</tt>. To get
     * the thrown exception, use the {@link #getThrownExeption()} method.
     *
     * @return the trace that has been created, or <tt>null</tt> if the
     * <tt>super.</tt>{@link #run()} method has never been called
     */
    public XTrace getGeneratedTrace() {
        return getComputedValue();
    }


    protected XTrace runWithException() throws InvalidScript {
        XTrace trace = XLogHelper.createTrace(caseId);

        // simulation of the control-flow
        processFlowObject(null, SetUtils.getRandom(process.getStartEvents()), trace, 0);

        // event sorting
        XLogHelper.sort(trace);

        // data object simulation for the instance
        for (DataObject dataObj : process.getDataObjects()) {
            if (dataObj.getObjectOwner() == null) {
                if (dataObj instanceof IntegerDataObject) {
                    ((GeneratedDataObject) dataObj).generateInstance(caseId);
                    noiseProcessor.applyIntegerDataNoise((IntegerDataObject) dataObj);
                    XLogHelper.decorateElement(trace, dataObj.getName(), (Integer) dataObj.getValue());
                } else if (dataObj instanceof StringDataObject) {
                    ((GeneratedDataObject) dataObj).generateInstance(caseId);
                    noiseProcessor.applyStringDataNoise((StringDataObject) dataObj);
                    XLogHelper.decorateElement(trace, dataObj.getName(), (String) dataObj.getValue());
                } else if (dataObj instanceof DataObject) {
                    noiseProcessor.applyStringDataNoise(dataObj);
                    XLogHelper.decorateElement(trace, dataObj.getName(), (String) dataObj.getValue());
                }
            }
        }

        // noise at the trace level
        noiseProcessor.applyTraceNoise(trace);

        return trace;
    }

    /**
     * This method processes each single process object. This method is
     * responsible for the flow management.
     *
     * @param source
     * @param object
     * @param trace
     * @param traceProgressiveTime
     * @throws InvalidScript
     */
    private void processFlowObject(Sequence source, FlowObject object, XTrace trace, long traceProgressiveTime) throws InvalidScript {
        // store the execution time of the current element
        long executionDuration = recordEventExecution(object, trace, traceProgressiveTime);
        componentsObservationTime.put(object, traceProgressiveTime + executionDuration);

        // frequency update
        if (observationsCounter.containsKey(source)) {
            observationsCounter.put(source, observationsCounter.get(source) + 1);
        } else {
            observationsCounter.put(source, 1);
        }

        // different behavior depending of the element type
        if (object instanceof TaskWithImpacts ||
                object instanceof ExclusiveGateway ||
                object instanceof StartEvent ||
                (object instanceof ParallelGateway &&
                        object.getOutgoingObjects().size() == 1 &&
                        object.getIncomingObjects().size() == 1)) {
            // sequence or xor gateways or an and gateway with 1 incoming and 1 outgoing edge

            // we can consume the provided token
            if (tokens != null) {
                tokens.remove(source);
            }

            // outgoing set population
            Set<Pair<FlowObject, Double>> outgoing = new HashSet<Pair<FlowObject, Double>>();
            for (FlowObject o : object.getOutgoingObjects()) {
                Sequence target = process.getSequence(object, o);
                if (observationsCounter.containsKey(target) &&
                        observationsCounter.get(target) > parameters.getMaximumLoopCycles()) {
                    // if the maximum number of cycles has been reached, then
                    // we set the probability of the loop to 0
                    outgoing.add(new Pair<FlowObject, Double>(o, 0.001));
                } else {
                    outgoing.add(new Pair<FlowObject, Double>(o, 1.0));
                }
            }

            // outgoing element selection and token population
            FlowObject next = SetUtils.getRandomWeighted(outgoing);
            if (next instanceof TaskWithImpacts) {
                Set<DataObject> dataObjs = ((TaskWithImpacts) next).getDataObjects(IDataObjectOwner.DATA_OBJECT_DIRECTION.REQUIRED);
                Vector<Double> impatti = ((TaskWithImpacts) next).getImpacts();
                if (dataObjs.size() > 0 && trace.size() > 0) {
                    recordEventAttributes(trace, dataObjs, trace.get(trace.size() - 1));
                }
                if (impatti != null && impatti.size() > 0 && trace.size() > 0) {
                    recordImpacts(trace, impatti, trace.get(trace.size() - 1));
                }
            }
            Sequence sequenceToNext = process.getSequence(object, next);
            tokens.add(sequenceToNext);

            // firing of next element
            processFlowObject(sequenceToNext, next, trace, traceProgressiveTime + executionDuration);

        } else if (object instanceof ParallelGateway) {

            // in this case we have to distinguish between an AND split or an
            // AND join. to do that we count the number of incoming and outgoing
            // edges
            if (object.getOutgoingObjects().size() > 1) {

                // we can consume the provided token
                if (tokens != null) {
                    tokens.remove(source);
                }

                // and split case
                for (FlowObject next : object.getOutgoingObjects()) {
                    // we first have to add the token of the next activity
                    tokens.add(process.getSequence(object, next));
                }
                for (FlowObject next : SetUtils.randomizeSet(object.getOutgoingObjects())) {
                    // we can fire the flow on each branch
                    Sequence sequenceToNext = process.getSequence(object, next);
                    processFlowObject(sequenceToNext, next, trace, traceProgressiveTime);
                }

            } else if (object.getIncomingObjects().size() > 1) {

                // and join case
                boolean observedAll = true;
                Set<Long> incomingTimestamps = new HashSet<Long>();
                for (FlowObject fo : object.getIncomingObjects()) {
                    Sequence sequenceToNext = process.getSequence(fo, object);
                    if (!tokens.contains(sequenceToNext)) {
                        // we have not yet completed this branch
                        observedAll = false;
                        break;
                    } else {
                        incomingTimestamps.add(componentsObservationTime.get(fo));
                    }
                }
                if (observedAll) {
                    // if we have observed all the tokens, then we can remove
                    // all of them and continue our process execution
                    for (FlowObject fo : object.getIncomingObjects()) {
                        Sequence token = process.getSequence(fo, object);
                        tokens.remove(token);
                    }
                    // we can add the token of the next activity and call the
                    // procedure of the following element
                    Long maxProgressive = Collections.max(incomingTimestamps);
                    FlowObject next = SetUtils.getRandom(object.getOutgoingObjects());
                    Sequence sequenceToNext = process.getSequence(object, next);
                    tokens.add(sequenceToNext);
                    processFlowObject(sequenceToNext, next, trace, maxProgressive);
                }
            }
        }
    }

    /**
     * This method generates the {@link XEvent} for the given {@link TaskWithImpacts}
     * object
     *
     * @param object
     * @param trace
     * @param traceProgressiveTime
     * @throws InvalidScript
     */
    private long recordEventExecution(FlowObject object, XTrace trace, long traceProgressiveTime) throws InvalidScript {
        if (object instanceof TaskWithImpacts) {
            String caseId = XLogHelper.getName(trace);
            TaskWithImpacts t = ((TaskWithImpacts) object);
            String activityName = noiseProcessor.generateActivityNameNoise(t.getName());
            Vector<Double> impatti = t.getImpacts();
            XEvent event_start = XLogHelper.insertEvent(trace, activityName, new Date(traceProgressiveTime));
            XEvent event_complete = null;

            long duration = t.getDutarion(caseId) * 1000;
            if (duration > 0) {
                event_complete = XLogHelper.insertEvent(trace, activityName, new Date(traceProgressiveTime + duration));
                XLifecycleExtension.instance().assignStandardTransition(event_start, XLifecycleExtension.StandardModel.START);
                XLifecycleExtension.instance().assignStandardTransition(event_complete, XLifecycleExtension.StandardModel.COMPLETE);
            }
            Set<DataObject> dataObjs = t.getDataObjects(IDataObjectOwner.DATA_OBJECT_DIRECTION.GENERATED);
            recordEventAttributes(trace, dataObjs, event_start, event_complete);
            if(impatti!=null)
            {
                recordImpacts(trace,impatti,event_start, event_complete);
            }
            return duration + (t.getTimeAfter(caseId) * 1000);
        }

        return 0;
    }

    /**
     * This method decorates an {@link XEvent} with the provided data objects
     *
     * @param trace
     * @param dataObjects
     * @param events
     * @throws InvalidScript
     */
    private void recordEventAttributes(XTrace trace, Set<DataObject> dataObjects, XEvent... events) throws InvalidScript {
        String caseId = XLogHelper.getName(trace);
        for (DataObject dataObj : dataObjects) {
            if (dataObj instanceof IntegerDataObject) {
                ((GeneratedDataObject) dataObj).generateInstance(caseId);
                for (XEvent event : events) {
                    noiseProcessor.applyIntegerDataNoise((IntegerDataObject) dataObj);
                    XLogHelper.decorateElement(event, dataObj.getName(), (Integer) dataObj.getValue());
                }
            } else if (dataObj instanceof StringDataObject) {
                ((GeneratedDataObject) dataObj).generateInstance(caseId);
                for (XEvent event : events) {
                    noiseProcessor.applyStringDataNoise((StringDataObject) dataObj);
                    XLogHelper.decorateElement(event, dataObj.getName(), (String) dataObj.getValue());
                }
            } else if (dataObj instanceof DataObject) {
                for (XEvent event : events) {
                    noiseProcessor.applyStringDataNoise(dataObj);
                    XLogHelper.decorateElement(event, dataObj.getName(), (String) dataObj.getValue());
                }

            }
        }
    }

    /**
     * This method decorates an {@link XEvent} with the provided data objects
     *
     * @param trace
     * @param taskImpacts
     * @param events
     * @throws InvalidScript
     */
    private void recordImpacts(XTrace trace, Vector<Double> taskImpacts, XEvent... events) throws InvalidScript {
        String caseId = XLogHelper.getName(trace);
        System.out.println("recordImpacts"+taskImpacts);
        for (Double impatto : taskImpacts) {
            for (XEvent event : events) {
                XLogHelper.decorateElement(event, "impatto", (Double) impatto);
            }
        }
    }
}