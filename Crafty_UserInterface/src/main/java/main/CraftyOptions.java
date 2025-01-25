package main;

public class CraftyOptions {

    public static final String CONFIG_FILE_PATH_OPTION = "config-file-path";
    public static final String PROJECT_DIRECTORY_PATH_OPTION = "project-directory-path";

    private String configFilePath;
    private String projectDirectoryPath;

    public String getConfigFilePath() {
        return configFilePath;
    }

    public void setConfigFilePath(String configFilePath) {
        this.configFilePath = configFilePath;
    }

    public String getProjectDirectoryPath() {
        return projectDirectoryPath;
    }

    public void setProjectDirectoryPath(String projectDirectoryPath) {
        this.projectDirectoryPath = projectDirectoryPath;
    }
}
