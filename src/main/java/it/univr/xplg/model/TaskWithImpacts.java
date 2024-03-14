package it.univr.xplg.model;

import plg.model.activity.Task;
import plg.model.Process;

import java.util.Vector;

public class TaskWithImpacts extends Task
{
    private Vector<Integer> impacts;

    public TaskWithImpacts(Process owner, String name) {
        super(owner,name);
        this.setName(name);
    }

    public Vector<Integer> getImpacts() {
        return impacts;
    }

    public void setImpacts(Vector<Integer> impacts) {
        this.impacts = impacts;
    }
}
