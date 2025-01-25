package main;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class OptionsParserTest {

    @Test
    public void testValidConfigFilePath() {
        String[] args = {"--config-file-path", "/path/to/config"};
        CraftyOptions options = OptionsParser.parseArguments(args);
        assertEquals("/path/to/config", options.getConfigFilePath());
        assertNull(options.getProjectDirectoryPath());
    }

    @Test
    public void testMalformedConfigFilePathArgument() {
        String[] args = {"--config-file-path"};
        Exception exception = assertThrows(RuntimeException.class, () -> {
            OptionsParser.parseArguments(args);
        });
        assertTrue(exception.getMessage().contains("Missing argument for option: config-file-path"));
    }

    @Test
    public void testMissingConfigFilePath() {
        String[] args = {};
        Exception exception = assertThrows(RuntimeException.class, () -> {
            OptionsParser.parseArguments(args);
        });
        assertTrue(exception.getMessage().contains("Missing required option"));
    }

    @Test
    public void testInvalidOption() {
        String[] args = {"--invalid-option", "value"};
        Exception exception = assertThrows(RuntimeException.class, () -> {
            OptionsParser.parseArguments(args);
        });
        assertTrue(exception.getMessage().contains("Unrecognized option"));
    }

    @Test
    public void testValidProjectDirectoryPath() {
        String[] args = {
                "--config-file-path", "/path/to/config",
                "--project-directory-path", "/path/to/project"
        };
        CraftyOptions options = OptionsParser.parseArguments(args);
        assertEquals("/path/to/config", options.getConfigFilePath());
        assertEquals("/path/to/project", options.getProjectDirectoryPath());
    }

    @Test
    public void testMalformedProjectDirectoryPathArgument() {
        String[] args = {
                "--config-file-path", "/path/to/config",
                "--project-directory-path"
        };
        Exception exception = assertThrows(RuntimeException.class, () -> {
            OptionsParser.parseArguments(args);
        });
        assertTrue(exception.getMessage().contains("Missing argument for option: project-directory-path"));
    }
}
