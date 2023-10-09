package org.qualityannotate.core;

import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

@Command(name = "qualityannotate", mixinStandardHelpOptions = true)
public class MainCommand implements Runnable {

    @Parameters(paramLabel = "<qualitytool>", defaultValue = "sonarqube",
            description = "From which quality-tool you want to retrieve the data")
    String qualityTool;

    @Override
    public void run() {
        System.out.printf("Using %s\n", qualityTool);
    }

}
