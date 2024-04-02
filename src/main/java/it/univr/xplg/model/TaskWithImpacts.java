package it.univr.xplg.model;

import plg.model.activity.Task;
import plg.model.Process;

import java.util.Vector;

public class TaskWithImpacts extends Task
{
        public static final String IMPACT_LABEL="impacts";

        private Vector<Double>impacts;


        public TaskWithImpacts(Process owner, String name) {
            super(owner,name);
            this.setName(name);
        }

        public Vector<Double> getImpacts() {
            return impacts;
        }

        public void setImpacts(Vector<Double> impacts) {
            this.impacts = impacts;
        }
}
