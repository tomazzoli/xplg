package it.univr.xplg.test;

import it.univr.xplg.io.exporter.PLGExporter;
import it.univr.xplg.io.importer.PLGImporter;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.out.XesXmlSerializer;
import plg.exceptions.IllegalSequenceException;
import plg.exceptions.InvalidProcessException;
import plg.generator.ProgressAdapter;
import plg.generator.log.LogGenerator;
import plg.generator.log.SimulationConfiguration;
import it.univr.xplg.generator.process.ProcessGenerator;
import plg.generator.process.RandomizationConfiguration;
import it.univr.xplg.io.exporter.GraphvizBPMNExporter;
import plg.model.data.DataObject;
import plg.model.data.IDataObjectOwner;
import plg.model.event.EndEvent;
import plg.model.event.StartEvent;
import plg.model.gateway.Gateway;
import it.univr.xplg.model.ProcessWithImpacts;
import it.univr.xplg.model.TaskWithImpacts;

import java.io.FileOutputStream;
import java.util.Vector;

public class TestModel extends plg.test.TestModel
{
    private static final String processFilenameDir = "/Users/ctomazzoli/temp/";

    public static void main(String[] args) throws Exception
    {
        Vector<Integer> impatti = new Vector<> ();
        impatti.add(1);
        impatti.add(2);
        impatti.add(3);
        ProcessWithImpacts p = generateProcess(impatti);
        PLGExporter pe = new PLGExporter();
        String xmlFile = processFilenameDir+"model.xml";
        pe.exportModel(p, xmlFile,new ProgressAdapter());
        System.out.println("Export done");

        PLGImporter pi = new PLGImporter();
        ProcessWithImpacts pimp=pi.importModel(xmlFile);
        System.out.println("Import done");
        GraphvizBPMNExporter e = new GraphvizBPMNExporter();
        e.exportModel(pimp, processFilenameDir+"model.dot");
        System.out.println("done");

        ProcessGenerator.randomizeProcess(p, RandomizationConfiguration.BASIC_VALUES);

        LogGenerator g = new LogGenerator(p, new SimulationConfiguration(1000), new ProgressAdapter());
        XLog l = g.generateLog();
        XesXmlSerializer s = new XesXmlSerializer();
        s.serialize(l, new FileOutputStream(processFilenameDir+"testlog.xes"));

        System.out.println("finito tutto");
    }
    private static ProcessWithImpacts generateProcess(Vector<Integer> impatti) throws IllegalSequenceException,
            InvalidProcessException {
        ProcessWithImpacts p = new ProcessWithImpacts("test");
        StartEvent start = p.newStartEvent();
        EndEvent end = p.newEndEvent();
        Gateway split = p.newParallelGateway();
        Gateway join = p.newParallelGateway();
        TaskWithImpacts a = p.newTask("a");
        a.setImpacts(impatti);
        TaskWithImpacts b = p.newTask("b");
        b.setImpacts(impatti);
        TaskWithImpacts c = p.newTask("c");
        c.setImpacts(impatti);
        TaskWithImpacts d = p.newTask("d");
        d.setImpacts(impatti);
        TaskWithImpacts e = p.newTask("e");
        e.setImpacts(impatti);
        TaskWithImpacts f = p.newTask("f");
        f.setImpacts(impatti);
        p.newSequence(start, a);
        p.newSequence(a, split);
        p.newSequence(split, b); p.newSequence(b, join);
        p.newSequence(split, c); p.newSequence(c, join);
        p.newSequence(split, d); p.newSequence(d, join);
        p.newSequence(split, e); p.newSequence(e, join);
        p.newSequence(e, join);
        p.newSequence(join, f);
        p.newSequence(f, end);

        new DataObject(p).set("d1", "v1");
        new DataObject(p, b, IDataObjectOwner.DATA_OBJECT_DIRECTION.REQUIRED).set("d2", "v2");
        new DataObject(p, c, IDataObjectOwner.DATA_OBJECT_DIRECTION.GENERATED).set("d3", "v3");

        p.check();

        return p;
    }
}
