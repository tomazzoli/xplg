package it.univr.xplg.io.exporter;

import it.univr.xplg.model.ProcessWithImpacts;
import it.univr.xplg.model.TaskWithImpacts;
import org.deckfour.spex.SXDocument;
import org.deckfour.spex.SXTag;
import plg.annotations.Exporter;
import plg.generator.IProgressVisualizer;
import plg.io.importer.PLGImporter;
import plg.model.activity.Task;
import plg.model.data.*;
import plg.model.event.EndEvent;
import plg.model.event.StartEvent;
import plg.model.gateway.ExclusiveGateway;
import plg.model.gateway.Gateway;
import plg.model.gateway.ParallelGateway;
import plg.model.sequence.Sequence;
import plg.utils.Logger;
import plg.utils.PlgConstants;

import java.io.File;
import java.io.IOException;
import java.util.Vector;

/**
 * This class contains the exporter towards custom-defined file format
 *
 * @author Claudio Tomazzoli
 * @see PLGImporter
 */
@Exporter(
        name = "PLG 2 file",
        fileExtension = "plg"
)

public class PLGExporter extends plg.io.exporter.PLGExporter
{
    public void exportModel(ProcessWithImpacts model, String filename, IProgressVisualizer progress) {
        progress.setMinimum(0);
        progress.setMaximum(7);
        progress.setText("Exporting PLG file...");
        progress.start();
        Logger.instance().info("Starting process exportation");
        try {
            File file = new File(filename);
            SXDocument doc = new SXDocument(file);

            // header
            doc.addComment("\n\n"
                    + "WARNING: Do not manually edit this file, unless you know what you are doing!\n\n"
                    + "This file has been generated with " + PlgConstants.libPLG_SIGNATURE + ".\n"
                    + "Check https://github.com/delas/plg for sources and other stuff.\n\n");

            // meta information
            SXTag process = doc.addNode("process");
            process.addComment("This is the list of all meta-attributes of the process");
            SXTag meta = process.addChildNode("meta");
            meta.addChildNode("LibPLG_NAME").addTextNode(PlgConstants.LibPLG_NAME);
            meta.addChildNode("libPLG_VERSION").addTextNode(PlgConstants.libPLG_VERSION);
            meta.addChildNode("name").addTextNode(model.getName());
            meta.addChildNode("id").addTextNode(model.getId());
            Logger.instance().debug("Dumped meta info");
            progress.inc();

            // process elements
            process.addComment("This is the list of all actual process elements");
            SXTag elements = process.addChildNode("elements");

            for(StartEvent se : model.getStartEvents()) {
                SXTag seTag = elements.addChildNode("startEvent");
                seTag.addAttribute("id", se.getId());
            }
            Logger.instance().debug("Dumped start events");
            progress.inc();
            for(Task t : model.getTasks())
            {
                SXTag tTag = elements.addChildNode("task");
                tTag.addAttribute("id", t.getId());
                tTag.addAttribute("name", t.getName());
                if (t instanceof TaskWithImpacts)
                {
                    Vector<Double> impatti = ((TaskWithImpacts) t).getImpacts();
                    if ((impatti!=null)&&(impatti.size()>0))
                    {
                        SXTag impTag = tTag.addChildNode("impacts");
                        for(Double i:impatti)
                        {
                            SXTag simpTag = impTag.addChildNode("impact");
                            simpTag.addTextNode(i.toString().trim());
                        }
                    }
                }
                for (DataObject d : t.getDataObjects()) {
                    SXTag dObj = tTag.addChildNode("dataObject");
                    dObj.addAttribute("id", d.getId());
                    dObj.addAttribute("direction", d.getDirectionOwner().toString());
                }
                SXTag script = tTag.addChildNode("script");
                if (t.getActivityScript() != null) {
                    script.addCDataNode(t.getActivityScript().getScript());
                }
            }
            Logger.instance().debug("Dumped tasks");
            progress.inc();
            for(Gateway g : model.getGateways()) {
                SXTag gTag = elements.addChildNode("gateway");
                gTag.addAttribute("id", g.getId());
                if (g instanceof ExclusiveGateway) {
                    gTag.addAttribute("type", "ExclusiveGateway");
                } else if (g instanceof ParallelGateway) {
                    gTag.addAttribute("type", "ParallelGateway");
                }
            }
            Logger.instance().debug("Dumped gateways");
            progress.inc();
            for(EndEvent ee : model.getEndEvents()) {
                SXTag eeTag = elements.addChildNode("endEvent");
                eeTag.addAttribute("id", ee.getId());
            }
            Logger.instance().debug("Dumped end events");
            progress.inc();
            for(Sequence s : model.getSequences()) {
                SXTag sTag = elements.addChildNode("sequenceFlow");
                sTag.addAttribute("id", s.getId());
                sTag.addAttribute("sourceRef", s.getSource().getId());
                sTag.addAttribute("targetRef", s.getSink().getId());
            }
            Logger.instance().debug("Dumped sequences");
            progress.inc();
            // data objects
            for(DataObject dobj : model.getDataObjects()) {
                SXTag dobjTag = elements.addChildNode("dataObject");
                dobjTag.addAttribute("id", dobj.getId());
                IDataObjectOwner owner= dobj.getObjectOwner();
                if(owner!=null) {
                    dobjTag.addAttribute("owner", owner.getId());
                }
                if (dobj instanceof StringDataObject) {
                    dobjTag.addAttribute("name", dobj.getName());
                    dobjTag.addAttribute("type", "StringDataObject");
                    dobjTag.addChildNode("script").addCDataNode(((GeneratedDataObject) dobj).getScriptExecutor().getScript());
                } else if (dobj instanceof IntegerDataObject) {
                    dobjTag.addAttribute("name", dobj.getName());
                    dobjTag.addAttribute("type", "IntegerDataObject");
                    dobjTag.addChildNode("script").addCDataNode(((GeneratedDataObject) dobj).getScriptExecutor().getScript());
                } else {
                    dobjTag.addAttribute("name", dobj.getName());
                    dobjTag.addAttribute("value", (String) dobj.getValue());
                    dobjTag.addAttribute("type", "DataObject");
                }
            }
            Logger.instance().debug("Dumped data objects");
            progress.inc();

            doc.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Logger.instance().info("Process exportation complete");
        progress.finished();
    }
}
