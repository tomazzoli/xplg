package it.univr.xplg.batch;

import it.univr.xplg.generator.log.LogGenerator;
import it.univr.xplg.generator.process.ProcessGenerator;
import it.univr.xplg.io.importer.PLGImporter;
import it.univr.xplg.model.ProcessWithImpacts;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.out.XesXmlSerializer;
import picocli.CommandLine;
import plg.generator.ProgressAdapter;
import plg.generator.log.SimulationConfiguration;
import plg.generator.process.RandomizationConfiguration;

import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.concurrent.Callable;

@CommandLine.Command(name = "GenerateLog", mixinStandardHelpOptions = true, version = "1.0", description = "Create log for business process with impacts")
public class GenerateLog implements Callable<Integer> {
    @CommandLine.Parameters(index = "0", description = "input file containing the xml definition of a BP with impacts")
    private String inXmlFile;

    @CommandLine.Option(names = {"-o", "--outputXMLFilePath"}, required = true, description = "output file containing the xml description of all traces")
    private String outXmlFile;

    @CommandLine.Option(names = {"-t", "--numberOfTraces"}, required = false, description = "max number of traces to be generated, in omitted default is 100")
    private int numberOfTraces=1000;

    @CommandLine.Option(names = {"-ab", "--ANDBranches"}, required = false, description = "properties file containing all parameters")
    private int ANDBranches=5;

    @CommandLine.Option(names = {"-xb", "--XORBranches"}, required = false, description = "properties file containing all parameters")
    private int XORBranches=5;

    @CommandLine.Option(names = {"-lw", "--loopWeight"}, required = false, description = "properties file containing all parameters")
    private double loopWeight=0.1;

    @CommandLine.Option(names = {"-sa", "--singleActivityWeight"}, required = false, description = "properties file containing all parameters")
    private double singleActivityWeight=0.2;

    @CommandLine.Option(names = {"-sw", "--skipWeight"}, required = false, description = "properties file containing all parameters")
    private double skipWeight=0.1;

    @CommandLine.Option(names = {"-sq", "--sequenceWeight"}, required = false, description = "properties file containing all parameters")
    private double sequenceWeight=0.7;

    @CommandLine.Option(names = {"-aw", "--ANDWeight"}, required = false, description = "properties file containing all parameters")
    private double ANDWeight=0.3;

    @CommandLine.Option(names = {"-xw", "--XORWeight"}, required = false, description = "properties file containing all parameters")
    private double XORWeight=0.3;

    @CommandLine.Option(names = {"-dt", "--maxDepth"}, required = false, description = "properties file containing all parameters")
    private int maxDepth=3;

    @CommandLine.Option(names = {"-op", "--dataObjectProbability"}, required = false, description = "properties file containing all parameters")
    private double dataObjectProbability=0.1;

    @Override
    public Integer call() throws Exception
    {
        PLGImporter pi = new PLGImporter();
        ProcessWithImpacts p=pi.importModel(inXmlFile);
        System.out.println("Import done from file" + inXmlFile);
        String result = this.executeTask(p,numberOfTraces);


        System.out.println(result);
        return 0;
    }

    private String executeTask(ProcessWithImpacts p, int numberOfTraces)
    {
        String result = "Log Generated and written in file "+outXmlFile;
        ProcessGenerator.randomizeProcess(p, RandomizationConfiguration.BASIC_VALUES);
        LogGenerator g = new LogGenerator(p, new SimulationConfiguration(numberOfTraces), new ProgressAdapter());
        XLog l = null;
        try
        {
            l = g.generateLog();
            XesXmlSerializer s = new XesXmlSerializer();
            s.serialize(l, new FileOutputStream(outXmlFile));
        }
        catch (Exception e)
        {
            StringWriter errors = new StringWriter();
            e.printStackTrace(new PrintWriter(errors));
            result = errors.toString();
        }
        return result;
    }

    public static void main(String[] args) {
        int exitCode = new CommandLine(new GenerateLog()).execute(args);
        System.exit(exitCode);
    }
}