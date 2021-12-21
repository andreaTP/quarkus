package io.quarkus.it.picocli;

import java.util.Arrays;

import org.jboss.logging.Logger;

import picocli.CommandLine;

@CommandLine.Command(name = "quarkus", versionProvider = DynamicVersionProvider.class)
public class DynamicVersionProviderCommand implements Runnable {

    @CommandLine.Spec
    CommandLine.Model.CommandSpec commandSpec;

    @Override
    public void run() {
        Logger.getLogger("test").error("error");
        System.out.println(Arrays.toString(commandSpec.version()));
    }
}
