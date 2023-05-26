package it.cnr.istc.stlab.framester.uriengineer;

import org.apache.commons.cli.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class UriEngineer {

    private static final Logger logger = LoggerFactory.getLogger(UriEngineer.class);

    private static final Options options = new Options();
    private static final String COLLECT_PREFIXES = "collect-prefixes";
    private static final String REFACTOR_PREFIXES = "refactor-prefixes";

    private static final String METHOD = "m";
    private static final String INPUT = "i";
    private static final String COLLECT_EXAMPLES = "e";
    private static final String MAPPING_FILE = "mf";
    private static final String OUTPUT = "o";
    private static final String GENERATE_SAME_AS = "s";

    public static void printHelp() {
        HelpFormatter formatter = new HelpFormatter();
        formatter.setOptionComparator(null);
        formatter.printHelp("java -jar framester.uri-engineer-<version>.jar -m (" + COLLECT_PREFIXES + "|" + REFACTOR_PREFIXES + ") -i filepath [-e -mf filepath -o filepath] ", options);
    }

    public static void main(String[] args) throws ParseException, IOException {

        logger.info("URI Engineer");

        options.addOption(Option.builder(METHOD).argName(COLLECT_PREFIXES + "|" + REFACTOR_PREFIXES).hasArg().required(true).desc("The method to invoke. Only two methods available '" + COLLECT_PREFIXES + "' and '" + REFACTOR_PREFIXES + "'. By passing '" + COLLECT_PREFIXES + "' the tool returns the set of prefixes used in the URIs files. By passing '" + REFACTOR_PREFIXES + "', the tool performs the refactoring of the input files.").longOpt("method").build());
        options.addOption(Option.builder(INPUT).argName("filepath").hasArg().required(true).desc("A path to a file or a folder.").longOpt("input").build());
        options.addOption(Option.builder(MAPPING_FILE).argName("filepath").hasArg().required(false).desc("A path to a csv file (Mandatory for " + REFACTOR_PREFIXES + ").").longOpt("mapping-file").build());
        options.addOption(Option.builder(OUTPUT).argName("filepath").hasArg().required(false).desc("A path to an output folder (Mandatory for " + REFACTOR_PREFIXES + ").").longOpt("output-folder").build());
        options.addOption(Option.builder(COLLECT_EXAMPLES).argName("collect-examples").required(false).desc("If set, the tool will collect examples of URIs for each prefix.").longOpt("collect-examples").build());
        options.addOption(Option.builder(GENERATE_SAME_AS).hasArg().argName("filepath").required(false).desc("If set, the tool will generate sameAs links for the URIs changed. SameAs links will be included in a single file that will be stored in the directory whose path is passed as parameter.").longOpt("generate-sameAs-links").build());

        if (args.length == 0) {
            printHelp();
            return;
        }

        CommandLineParser cmdLineParser = new DefaultParser();
        CommandLine commandLine = cmdLineParser.parse(options, args);

        String method = getStringMandatoryOption(commandLine, METHOD);
        String input = getStringMandatoryOption(commandLine, INPUT);
        String mappingFile = getStringOption(commandLine, MAPPING_FILE);
        String output = getStringOption(commandLine, OUTPUT);
        String generateSameAs = getStringOption(commandLine, GENERATE_SAME_AS);

        InputTraverser it = new InputTraverser(input);

        if (method != null || input != null) {
            if (method.equals(COLLECT_PREFIXES)) {
                logger.info("Collect prefixes");
                PrefixCollector pc = new PrefixCollector();
                if (commandLine.hasOption(COLLECT_EXAMPLES)) {
                    logger.info("Collect examples");
                    pc.setCollectUriExamples(true);
                }

                it.traverse(pc);
                if (commandLine.hasOption(COLLECT_EXAMPLES)) {
                    pc.getCollectedExamples().forEach((prefix, examples) -> {
                        System.out.print(prefix);
                        System.out.print(" -> ");
                        for (String example : examples) {
                            System.out.print(example);
                            System.out.print(" ");
                        }
                        System.out.print("\n");
                    });
                }

                pc.getCollectedPrefixes().forEach(System.out::println);

            } else if (method.equals(REFACTOR_PREFIXES)) {
                logger.info("Refactor prefixes");
                PrefixRefactorizer pr = new PrefixRefactorizer(input, output, mappingFile);
                if (generateSameAs != null) {
                    pr.setGenerateSameAsLinks(true);
                    pr.setSameAsFile(generateSameAs);
                }
                it.traverse(pr);
            }
        }


    }


    public static String getStringMandatoryOption(CommandLine commandLine, String option) {
        if (commandLine.hasOption(option)) {
            logger.trace("{} {}", option, commandLine.getOptionValue(option));
            return commandLine.getOptionValue(option);
        } else {
            printHelp();
        }
        return null;
    }

    public static String getStringOption(CommandLine commandLine, String option) {
        if (commandLine.hasOption(option)) {
            logger.trace("{} {}", option, commandLine.getOptionValue(option));
            return commandLine.getOptionValue(option);
        }
        return null;
    }

    public static boolean getBooleanOption(CommandLine commandLine, String option) {
        if (commandLine.hasOption(option)) {
            logger.trace("{} {}", option, commandLine.getOptionValue(option));
            return true;
        }
        return false;
    }
}
