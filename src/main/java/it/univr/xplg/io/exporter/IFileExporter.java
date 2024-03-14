package it.univr.xplg.io.exporter;

import it.univr.xplg.model.ProcessWithImpacts;
import plg.generator.IProgressVisualizer;

public interface IFileExporter {

    /**
     * General interface of a method that exports a model.
     *
     * @param model the model to export
     * @param filename the target of the model to export
     * @param progress the progress to notify the user
     */
    public void exportModel(ProcessWithImpacts model, String filename, IProgressVisualizer progress);

    /**
     * General interface of a method that exports a model without any progress.
     *
     * @param model the model to export
     * @param filename the target of the model to export
     */
    public void exportModel(ProcessWithImpacts model, String filename);
}