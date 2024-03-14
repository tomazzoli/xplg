package it.univr.xplg.generator.process;

import it.univr.xplg.model.ProcessWithImpacts;
import it.univr.xplg.model.TaskWithImpacts;
import plg.generator.process.PatternFrame;
import plg.generator.process.RandomizationConfiguration;
import plg.model.data.IDataObjectOwner;
import plg.model.event.EndEvent;
import plg.model.event.StartEvent;
import plg.utils.Logger;
import plg.utils.SetUtils;

/**
 * This class contains the random generator of processes with impacts. Actually, this class
 * is responsible for the randomization of a process with impacts.
 *
 * @see plg.generator.process.ProcessGenerator
 * @author Claudio Tomzzoli
 */
public class ProcessGenerator extends plg.generator.process.ProcessGenerator
{
    private ProcessWithImpacts process;
    private RandomizationConfiguration parameterswi;
    protected ProcessGenerator(ProcessWithImpacts processwi, RandomizationConfiguration parameters) {
        super(processwi, parameters);
        process = processwi;
        parameterswi = parameters;
    }

    /**
     * This public static method is the main interface for the process
     * randomization. Specifically, this method adds to the provided process a
     * control-flow structure, which starts from a {@link StartEvent}, and
     * finishes into an {@link EndEvent}.
     *
     * <p> If the provided process is not empty, the new control-flow is added
     * to the existing process.
     *
     * @param process the process to randomize
     * @param parameters the randomization parameters to use
     */
    public static void randomizeProcess(ProcessWithImpacts process, RandomizationConfiguration parameters) {
        new ProcessGenerator(process, parameters).begin();
    }

    /**
     * This method generates a new activity.
     *
     * @return the frame containing the generated pattern
     */
    protected PatternFrame newActivity() {
        String activityName = askNewActivityName();
        Logger.instance().debug("New activity created (`" + activityName + "')");
        TaskWithImpacts t = process.newTask(activityName);
        if (parameterswi.generateDataObject()) {
            newDataObject().setObjectOwner(t, SetUtils.getRandom(IDataObjectOwner.DATA_OBJECT_DIRECTION.values()));
        }
        return new PatternFrame(t);
    }
}
