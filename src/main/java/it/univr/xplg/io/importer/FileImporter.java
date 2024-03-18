package it.univr.xplg.io.importer;

import it.univr.xplg.model.ProcessWithImpacts;
import plg.generator.IProgressVisualizer;
import plg.generator.ProgressAdapter;


public abstract class FileImporter implements IFileImporter {

    public abstract ProcessWithImpacts importModel(String filename, IProgressVisualizer progress);

    public ProcessWithImpacts importModel(java.lang.String filename)
    {
        return importModel(filename, new ProgressAdapter());
    }
}
