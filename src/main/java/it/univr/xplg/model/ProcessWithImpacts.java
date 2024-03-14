package it.univr.xplg.model;

import plg.exceptions.IllegalSequenceException;
import plg.exceptions.InvalidProcessException;
import plg.generator.scriptexecuter.IntegerScriptExecutor;
import plg.generator.scriptexecuter.StringScriptExecutor;
import plg.model.FlowObject;
import plg.model.Process;
import plg.model.activity.Task;
import plg.model.data.DataObject;
import plg.model.data.IDataObjectOwner;
import plg.model.data.IntegerDataObject;
import plg.model.data.StringDataObject;
import plg.model.event.EndEvent;
import plg.model.event.StartEvent;
import plg.model.gateway.ExclusiveGateway;
import plg.model.gateway.Gateway;
import plg.model.gateway.ParallelGateway;
import plg.model.sequence.Sequence;

public class ProcessWithImpacts extends Process
{
    public ProcessWithImpacts(String name) {
        super(name);
    }

    /**
     * This method creates a new taskwith impacts registered to the current process.
     *
     * @param name the new task name
     * @return the newly created task
     */
    public TaskWithImpacts newTask(String name) {
        return new TaskWithImpacts(this, name);
    }

    public Object clone() {
        ProcessWithImpacts p = new ProcessWithImpacts(super.getName());
        for (StartEvent e : getStartEvents()) {
            p.newStartEvent().setComponentId(Integer.parseInt(e.getId()));
        }
        for (EndEvent e : getEndEvents()) {
            p.newEndEvent().setComponentId(Integer.parseInt(e.getId()));
        }
        for (Gateway g : getGateways()) {
            if (g instanceof ParallelGateway) {
                p.newParallelGateway().setComponentId(Integer.parseInt(g.getId()));
            } else if (g instanceof ExclusiveGateway) {
                p.newExclusiveGateway().setComponentId(Integer.parseInt(g.getId()));
            }
        }
        for (Task t : getTasks()) {
            Task c = p.newTask(t.getName());
            if (t instanceof TaskWithImpacts)
            {
                ((TaskWithImpacts) c).setImpacts(((TaskWithImpacts) t).getImpacts());
            }
            c.setComponentId(Integer.parseInt(t.getId()));
            if (t.getActivityScript() != null) {
                c.setActivityScript(new IntegerScriptExecutor(t.getActivityScript().getScript()));
            }
            for (IDataObjectOwner.DATA_OBJECT_DIRECTION direction : IDataObjectOwner.DATA_OBJECT_DIRECTION.values()) {
                for (DataObject d : t.getDataObjects(direction)) {
                    DataObject newDataObject = null;
                    if (d instanceof IntegerDataObject) {
                        newDataObject = new IntegerDataObject(p, new IntegerScriptExecutor(((IntegerDataObject) d).getScriptExecutor().getScript()));
                    } else if (d instanceof StringDataObject) {
                        newDataObject = new StringDataObject(p, new StringScriptExecutor(((StringDataObject) d).getScriptExecutor().getScript()));
                    } else {
                        newDataObject = new DataObject(p);
                    }
                    newDataObject.setComponentId(Integer.parseInt(d.getId()));
                    newDataObject.setName(d.getName());
                    newDataObject.setValue(d.getValue());
                    newDataObject.setObjectOwner(c, direction);
                }
            }
        }
        for (Sequence s : getSequences()) {
            try {
                FlowObject newSource = (FlowObject) p.searchComponent(s.getSource().getId());
                FlowObject newSink = (FlowObject) p.searchComponent(s.getSink().getId());
                Sequence newSequence = p.newSequence(newSource, newSink);
                newSequence.setComponentId(Integer.parseInt(s.getId()));
            } catch (IllegalSequenceException e) {
                e.printStackTrace();
            }
        }
        try {
            p.check();
        } catch (InvalidProcessException e1) {
            e1.printStackTrace();
        }
        return p;
    }
}
