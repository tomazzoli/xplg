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

    @CommandLine.Option(names = {"-nt", "--numberOfTraces"}, required = false, description = "max number of traces to be generated, in omitted default is 100")
    private int numberOfTraces=1000;

    @CommandLine.Option(names = {"-rab", "--ANDBranches"}, required = false, description = "RandomizationConfiguration : max AND branches")
    private int ANDBranches=5;

    @CommandLine.Option(names = {"-rxb", "--XORBranches"}, required = false, description = "RandomizationConfiguration : max XOR branches")
    private int XORBranches=5;

    @CommandLine.Option(names = {"-rlw", "--loopWeight"}, required = false, description = "RandomizationConfiguration : loop weight")
    private double loopWeight=0.1;

    @CommandLine.Option(names = {"-rsa", "--singleActivityWeight"}, required = false, description = "RandomizationConfiguration : single activity weight")
    private double singleActivityWeight=0.2;

    @CommandLine.Option(names = {"-rsw", "--skipWeight"}, required = false, description = "RandomizationConfiguration : skip weight")
    private double skipWeight=0.1;

    @CommandLine.Option(names = {"-rsq", "--sequenceWeight"}, required = false, description = "RandomizationConfiguration : sequence weight")
    private double sequenceWeight=0.7;

    @CommandLine.Option(names = {"-raw", "--ANDWeight"}, required = false, description = "RandomizationConfiguration : AND weight")
    private double ANDWeight=0.3;

    @CommandLine.Option(names = {"-rxw", "--XORWeight"}, required = false, description = "RandomizationConfiguration : XOR weight")
    private double XORWeight=0.3;

    @CommandLine.Option(names = {"-rdt", "--maxDepth"}, required = false, description = "RandomizationConfiguration : max depth")
    private int maxDepth=3;

    @CommandLine.Option(names = {"-rop", "--dataObjectProbability"}, required = false, description = "RandomizationConfiguration : dataObject Probability")
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