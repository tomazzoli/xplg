package it.univr.xplg.io.importer;

import it.univr.xplg.model.ProcessWithImpacts;
import it.univr.xplg.model.TaskWithImpacts;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import plg.annotations.Importer;
import plg.exceptions.IllegalSequenceException;
import plg.exceptions.UnsupportedPLGFileFormat;
import plg.generator.IProgressVisualizer;
import plg.generator.scriptexecuter.IntegerScriptExecutor;
import plg.generator.scriptexecuter.StringScriptExecutor;
import plg.model.FlowObject;
import plg.model.data.DataObject;
import plg.model.data.IDataObjectOwner;
import plg.model.data.IntegerDataObject;
import plg.model.data.StringDataObject;
import plg.model.event.EndEvent;
import plg.model.event.StartEvent;
import plg.model.gateway.Gateway;
import plg.model.sequence.Sequence;
import plg.utils.Logger;
import plg.utils.ZipHelper;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Vector;

@Importer(
        name = "PLG file",
        fileExtension = "plg"
)
public class PLGImporter extends FileImporter
{
    protected boolean importPythonScript = true;
    protected ProcessWithImpacts importFromPlg2(String filename) throws JDOMException, IOException, UnsupportedPLGFileFormat {
        FileInputStream input = new FileInputStream(filename);
        SAXBuilder builder = new SAXBuilder();
        Document document = (Document) builder.build(input);

        Element process = document.getRootElement();
        Element meta = process.getChild("meta");
        Element elements = process.getChild("elements");

        Element LibPLG_NAME = meta.getChild("LibPLG_NAME");
        Element libPLG_VERSION = meta.getChild("libPLG_VERSION");

        // check the current file PLG version
        if (LibPLG_NAME == null || libPLG_VERSION == null) {
            throw new UnsupportedPLGFileFormat("The PLG file provided is not supported");
        }

        // creates the new process
        ProcessWithImpacts p = new ProcessWithImpacts(meta.getChildText("name"));
        p.setId(meta.getChildText("id"));

        // data objects
        for (Element ss : elements.getChildren("dataObject")) {
            String type = ss.getAttributeValue("type");
            DataObject d = null;
            if (type.equals("DataObject")) {
                d = new DataObject(p);
                d.setValue(ss.getAttributeValue("value"));
            } else if (importPythonScript && type.equals("StringDataObject")) {
                String script = ss.getChildText("script").trim();
                StringScriptExecutor executor = new StringScriptExecutor(script);
                d = new StringDataObject(p, executor);
            } else if (importPythonScript && type.equals("IntegerDataObject")) {
                String script = ss.getChildText("script").trim();
                IntegerScriptExecutor executor = new IntegerScriptExecutor(script);
                d = new IntegerDataObject(p, executor);
            }
            p.removeComponent(d);
            d.setName(ss.getAttributeValue("name"));
            d.setComponentId(ss.getAttribute("id").getIntValue());
            p.registerComponent(d);
        }
        // start events
        for (Element ss : elements.getChildren("startEvent")) {
            StartEvent s = p.newStartEvent();
            p.removeComponent(s);
            s.setComponentId(ss.getAttribute("id").getIntValue());
            p.registerComponent(s);
        }
        // end events
        for (Element ss : elements.getChildren("endEvent")) {
            EndEvent e = p.newEndEvent();
            p.removeComponent(e);
            e.setComponentId(ss.getAttribute("id").getIntValue());
            p.registerComponent(e);
        }
        // tasks
        for (Element ss : elements.getChildren("task")) {
            TaskWithImpacts t = p.newTask(ss.getAttributeValue("name"));
            p.removeComponent(t);
            // inizio lettura del tag degli impatti
            Vector<Double> impatti = new Vector<Double>();
            for (Element imps :ss.getChildren("impacts"))
            {
                for(Element imp: imps.getChildren("impact"))
                {
                    String sval = imp.getValue();
                    try
                    {
                        if(sval!=null)
                        {
                            Double val = Double.parseDouble(sval);
                            impatti.add(val);
                        }
                    }
                    catch(NumberFormatException ne)
                    {
                        ;// do nothing
                    }
                }
            }
            t.setImpacts(impatti);
            // fine lettura del tag degli impatti
            t.setComponentId(ss.getAttribute("id").getIntValue());
            p.registerComponent(t);
            String script = ss.getChildText("script").trim();
            if (importPythonScript) {
                IntegerScriptExecutor executor = new IntegerScriptExecutor(script);
                t.setActivityScript(executor);
            }
            for (Element dos : ss.getChildren("dataObject")) {
                IDataObjectOwner.DATA_OBJECT_DIRECTION direction = IDataObjectOwner.DATA_OBJECT_DIRECTION.valueOf(dos.getAttributeValue("direction"));
                t.addDataObject((DataObject) p.searchComponent(dos.getAttributeValue("id")), direction);
            }
        }
        // gateways
        for (Element ss : elements.getChildren("gateway")) {
            Gateway g = null;
            if (ss.getAttributeValue("type").equals("ExclusiveGateway")) {
                g = p.newExclusiveGateway();
            } else if (ss.getAttributeValue("type").equals("ParallelGateway")) {
                g = p.newParallelGateway();
            }
            p.removeComponent(g);
            g.setComponentId(ss.getAttribute("id").getIntValue());
            p.registerComponent(g);
        }
        // sequences
        for (Element ss : elements.getChildren("sequenceFlow")) {
            try {
                Sequence s = p.newSequence(
                        (FlowObject) p.searchComponent(ss.getAttributeValue("sourceRef")),
                        (FlowObject) p.searchComponent(ss.getAttributeValue("targetRef")));
                p.removeComponent(s);
                s.setComponentId(ss.getAttribute("id").getIntValue());
                p.registerComponent(s);
            } catch (IllegalSequenceException e) {
                e.printStackTrace();
            }
        }

        return p;
    }

    @Override
    public ProcessWithImpacts importModel(String filename, IProgressVisualizer progress) {
        progress.setIndeterminate(true);
        progress.setText("Importing PLG file...");
        progress.start();
        Logger.instance().info("Starting process import");
        try {
            if (ZipHelper.isValid(new File(filename))) {
                ProcessWithImpacts p = importFromPlg1(filename);
                progress.finished();
                return p;
            } else {
                ProcessWithImpacts p = importFromPlg2(filename);
                progress.finished();
                return p;
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
        Logger.instance().info("Process import complete");
        progress.finished();
        return null;
    }

    protected ProcessWithImpacts importFromPlg1(String filename) throws UnsupportedPLGFileFormat {
        throw new UnsupportedPLGFileFormat("This implementation currently support only second generation of PLG files");
    }

}
