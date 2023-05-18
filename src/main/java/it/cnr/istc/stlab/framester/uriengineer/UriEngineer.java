package it.cnr.istc.stlab.framester.uriengineer;

import org.apache.commons.cli.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UriEngineer {

    private static final Logger logger = LoggerFactory.getLogger(UriEngineer.class);

    private static final Options options = new Options();
    private static final String COLLECT_PREFIXES = "collect-prefixes";
    private static final String REFACTOR_PREFIXES = "refactor-prefixes";

    private static final String METHOD = "m";
    private static final String INPUT = "i";
    private static final String COLLECT_EXAMPLES = "e";

    public static void printHelp() {
        HelpFormatter formatter = new HelpFormatter();
        formatter.setOptionComparator(null);
        formatter.printHelp("java -jar framester.uri-engineer.jar -m (" + COLLECT_PREFIXES + "|" + REFACTOR_PREFIXES + ") -i filepath [-e] ", options);
    }

    public static void main(String[] args) throws ParseException {

        logger.info("Prefix Engineer");

        options.addOption(Option.builder(METHOD).argName(COLLECT_PREFIXES + "|" + REFACTOR_PREFIXES).hasArg().required(true).desc("The method to invoke. Only two methods available '" + COLLECT_PREFIXES + "' and '" + REFACTOR_PREFIXES + "'. By passing '" + COLLECT_PREFIXES + "' the tool returns the set of prefixes used in the URIs files. By passing '" + REFACTOR_PREFIXES + "', the tool performs the refactoring of the input files.").longOpt("method").build());
        options.addOption(Option.builder(INPUT).argName("filepath").hasArg().required(true).desc("A path to a file or a folder.").longOpt("input").build());
        options.addOption(Option.builder(COLLECT_EXAMPLES).argName("collect-examples").required(false).desc("If set, the tool will collect examples of URIs for each prefix.").longOpt("collect-examples").build());

        if (args.length == 0) {
            printHelp();
            return;
        }

        CommandLineParser cmdLineParser = new DefaultParser();
        CommandLine commandLine = cmdLineParser.parse(options, args);

        String method = getMethod(commandLine);
        String input = getInput(commandLine);

        if (method != null || input != null) {
            if (method.equals(COLLECT_PREFIXES)) {
                logger.info("Collect prefixes");
                PrefixCollector pc = new PrefixCollector();
                if (commandLine.hasOption(COLLECT_EXAMPLES)) {
                    logger.info("Collect examples");
                    pc.setCollectUriExamples(true);
                }
                InputTraverser it = new InputTraverser(input);
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
                } else {

                    pc.getCollectedPrefixes().forEach(System.out::println);
                }
            }
        }


    }

    public static String getMethod(CommandLine commandLine) {
        if (commandLine.hasOption(METHOD)) {
            logger.trace("Method {}", commandLine.getOptionValue(METHOD));
            return commandLine.getOptionValue(METHOD);
        } else {
            printHelp();
        }
        return null;
    }

    public static String getInput(CommandLine commandLine) {
        if (commandLine.hasOption(INPUT)) {
            logger.trace("Input {}", commandLine.getOptionValue(INPUT));
            return commandLine.getOptionValue(INPUT);
        } else {
            printHelp();
        }
        return null;
    }
}
