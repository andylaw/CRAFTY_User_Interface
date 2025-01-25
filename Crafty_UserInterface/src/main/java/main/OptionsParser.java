package main;

import exceptions.OptionsParsingException;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class OptionsParser {

    private static final Logger logger = LogManager.getLogger(OptionsParser.class);

    public static CraftyOptions parseArguments(String[] args) {
        Options options = new Options();

        // Define option for the location of the config file without short option
        Option configFilePathOption = new Option(
                null, CraftyOptions.CONFIG_FILE_PATH_OPTION,
                true, "Path to the configuration file");
        configFilePathOption.setRequired(true);
        options.addOption(configFilePathOption);

        // Define option for the location of the data files/project structure
        // without short option
        Option projectFolderPathOption = new Option(
                null, CraftyOptions.PROJECT_DIRECTORY_PATH_OPTION,
                true, "Path to the project directory");
        projectFolderPathOption.setRequired(false);
        options.addOption(projectFolderPathOption);

        CommandLineParser parser = new DefaultParser();
        HelpFormatter formatter = new HelpFormatter();
        CraftyOptions craftyOptions = new CraftyOptions();

        try {
            CommandLine cmd = parser.parse(options, args);
            String configFilePath = cmd.getOptionValue(CraftyOptions.CONFIG_FILE_PATH_OPTION);
            craftyOptions.setConfigFilePath(configFilePath);
            String projectDirectoryPath = cmd.getOptionValue(CraftyOptions.PROJECT_DIRECTORY_PATH_OPTION);
            craftyOptions.setProjectDirectoryPath(projectDirectoryPath);
            logger.trace("Successfully parsed the options from the command line");
            logger.trace(craftyOptions.toString());
        } catch (ParseException e) {
            logger.error("Error parsing command line arguments: {}", e.getMessage());
            formatter.printHelp("CRAFTY", options);

            throw new OptionsParsingException("Error parsing command line arguments: " + e.getMessage());
        }

        return craftyOptions;
    }
}
