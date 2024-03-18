package it.univr.xplg.io.importer;

public interface IFileImporter
{
    /**
     * General interface of a method that imports a model.
     *
     * @param filename the source of the model to import
     * @param progress the progress to notify the user
     * @return the imported model with impacts
     */
    it.univr.xplg.model.ProcessWithImpacts importModel(java.lang.String filename, plg.generator.IProgressVisualizer progress);

    /**
    * General interface of a method that imports a model without any progress.
    *
    * @param filename the source of the model to import
    *      * @return the imported model with impacts
    *
     */
    it.univr.xplg.model.ProcessWithImpacts importModel(java.lang.String filename);
}
