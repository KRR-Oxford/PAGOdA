package uk.ac.ox.cs.pagoda;

import org.apache.commons.cli.*;
import org.apache.commons.io.FilenameUtils;
import uk.ac.ox.cs.pagoda.query.QueryRecord;
import uk.ac.ox.cs.pagoda.reasoner.QueryReasoner;
import uk.ac.ox.cs.pagoda.util.PagodaProperties;
import uk.ac.ox.cs.pagoda.util.Timer;
import uk.ac.ox.cs.pagoda.util.Utility;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Executable command line user interface.
 */
public class Pagoda implements Runnable {

    private static final String OPTION_ONTOLOGY = "o";
    private static final String OPTION_DATA = "d";
    private static final String OPTION_QUERY = "q";
    private static final String OPTION_ANSWER = "a";
    private static final String OPTION_CLASSIFY = "c";
    private static final String OPTION_HERMIT = "f";
    private final PagodaProperties properties;

    /**
     * Do not use it
     */
    private Pagoda() {
        properties = new PagodaProperties();
    }

    public static void main(String... args) {

        // TODO treat the mandatory options as simple args
        Options options = new Options();
        options.addOption(Option.builder(OPTION_ONTOLOGY)
                                .argName(OPTION_ONTOLOGY)
                                .required()
                                .hasArg()
                                .desc("The ontology path")
                                .build());
        options.addOption(Option.builder(OPTION_DATA).argName(OPTION_DATA).hasArg().desc("The data path").build());
        options.addOption(Option.builder(OPTION_QUERY)
                                .argName(OPTION_QUERY)
                                .required()
                                .hasArg()
                                .desc("The query path")
                                .build());
        options.addOption(Option.builder(OPTION_ANSWER)
                                .argName(OPTION_ANSWER)
                                .hasArg()
                                .desc("The answer path")
                                .build());
        options.addOption(Option.builder(OPTION_CLASSIFY)
                                .argName(OPTION_CLASSIFY)
                                .desc("Tell whether to classify")
                                .type(Boolean.class)
                                .build());
        options.addOption(Option.builder(OPTION_HERMIT)
                                .argName(OPTION_HERMIT)
                                .desc("Tell whether to call Hermit")
                                .type(Boolean.class)
                                .build());

        CommandLineParser parser = new DefaultParser();
        try {
            CommandLine cmd = parser.parse(options, args);
            PagodaBuilder pagodaBuilder = Pagoda.builder()
                                                .ontology(cmd.getOptionValue(OPTION_ONTOLOGY))
                                                .query(cmd.getOptionValue(OPTION_QUERY));
            if(cmd.hasOption(OPTION_DATA)) pagodaBuilder.data(cmd.getOptionValue(OPTION_DATA));
            if(cmd.hasOption(OPTION_ANSWER)) pagodaBuilder.answer(cmd.getOptionValue(OPTION_ANSWER));
            if(cmd.hasOption(OPTION_CLASSIFY))
                pagodaBuilder.classify(Boolean.parseBoolean(cmd.getOptionValue(OPTION_CLASSIFY)));
            if(cmd.hasOption(OPTION_HERMIT))
                pagodaBuilder.hermit(Boolean.parseBoolean(cmd.getOptionValue(OPTION_HERMIT)));

            pagodaBuilder.build().run();
        } catch(ParseException exp) {
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp("PAGOdA", options);
            Utility.logError("Parsing failed.  Reason: " + exp.getMessage());
            System.exit(0);
        }
    }

    /**
     * Get a builder.
     */
    public static PagodaBuilder builder() {
        return new PagodaBuilder();
    }

    @Override
    public void run() {
        Utility.logInfo("Ontology file: " + properties.getOntologyPath());
        Utility.logInfo("Data files: " + properties.getDataPath());
        Utility.logInfo("Query files: " + properties.getQueryPath());
        Utility.logInfo("Answer file: " + properties.getAnswerPath());

        QueryReasoner pagoda = null;

        try {
            Timer t = new Timer();
            pagoda = QueryReasoner.getInstance(properties);
            if(pagoda == null) return;

            Utility.logInfo("Preprocessing Done in " + t.duration() + " seconds.");

            if(properties.getQueryPath() != null) {
                for(String queryFile : properties.getQueryPath().split(";")) {
                    Collection<QueryRecord> queryRecords = pagoda.getQueryManager().collectQueryRecords(queryFile);
                    pagoda.evaluate(queryRecords);

                    if(PagodaProperties.isDebuggingMode()) {
                        HashMap<String, Map<String, String>> statistics = new HashMap<>();
                        for(QueryRecord queryRecord : queryRecords) {
                            statistics.put(queryRecord.getQueryID(), queryRecord.getStatistics());
                        }
                        String statisticsFilename = getStatisticsFilename(properties, queryFile);
                        try(BufferedWriter writer = Files.newBufferedWriter(Paths.get(statisticsFilename))) {
                            QueryRecord.GsonCreator.getInstance().toJson(statistics, writer);
                        } catch(IOException e) {
                            Utility.logError("Unable to save statistics");
                        }
                    }
                }
            }
        } finally {
            if(pagoda != null) pagoda.dispose();
        }
    }

    private String getStatisticsFilename(PagodaProperties properties, String queryFile) {
        String statisticsFilename = "statistics_" +
                FilenameUtils.removeExtension(FilenameUtils.getName(properties.getOntologyPath().replaceAll("_", "-")));
        statisticsFilename += "_" + FilenameUtils.removeExtension(FilenameUtils.getName(queryFile).replaceAll("_", "-"));
        statisticsFilename += "_" + ((properties.getUseSkolemUpperBound()) ? "skolem" : "");
        statisticsFilename += ".json";
        statisticsFilename = FilenameUtils.concat(properties.getStatisticsDir().toString(),
                                                  statisticsFilename);
        return statisticsFilename;
    }

    /**
     * Allows to set the parameters before creating a Pagoda instance.
     */
    public static class PagodaBuilder {

        private Pagoda instance;

        private PagodaBuilder() {
            instance = new Pagoda();
        }

        public PagodaBuilder ontology(String ontologyPath) {
            if(instance == null) return null;
            instance.properties.setOntologyPath(ontologyPath);
            return this;
        }

        public PagodaBuilder ontology(Path ontologyPath) {
            return ontology(ontologyPath.toString());
        }

        public PagodaBuilder data(String dataPath) {
            if(instance == null) return null;
            instance.properties.setDataPath(dataPath);
            return this;
        }

        public PagodaBuilder data(Path dataPath) {
            return data(dataPath.toString());
        }

        public PagodaBuilder query(String queryPath) {
            if(instance == null) return null;
            instance.properties.setQueryPath(queryPath);
            return this;
        }

        public PagodaBuilder query(Path queryPath) {
            return query(queryPath.toString());
        }

        public PagodaBuilder answer(String answerPath) {
            if(instance == null) return null;
            instance.properties.setAnswerPath(answerPath);
            return this;
        }

        public PagodaBuilder answer(Path answerPath) {
            return answer(answerPath.toString());
        }

        public PagodaBuilder classify(Boolean toClassify) {
            if(instance == null) return null;
            instance.properties.setToClassify(toClassify);
            return this;
        }

        public PagodaBuilder hermit(Boolean callHermit) {
            if(instance == null) return null;
            instance.properties.setToCallHermiT(callHermit);
            return this;
        }

        public PagodaBuilder skolem(Boolean isEnabled) {
            if(instance == null) return null;
            instance.properties.setUseSkolemUpperBound(isEnabled);
            return this;
        }

        public Pagoda build() {
            Pagoda builtInstance = instance;
            instance = null;
            return builtInstance;
        }

    }
}
