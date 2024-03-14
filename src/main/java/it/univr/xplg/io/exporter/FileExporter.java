package it.univr.xplg.io.exporter;

import it.univr.xplg.model.ProcessWithImpacts;
import plg.generator.ProgressAdapter;
import it.univr.xplg.io.exporter.IFileExporter;

public abstract class FileExporter implements IFileExporter {

    @Override
    public void exportModel(ProcessWithImpacts model, String filename) {
        exportModel(model, filename, new ProgressAdapter());
    }
}
