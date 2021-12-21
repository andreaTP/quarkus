package io.quarkus.it.picocli;

import static org.assertj.core.api.Assertions.assertThat;

import org.jboss.logging.Logger;
import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.main.LaunchResult;
import io.quarkus.test.junit.main.QuarkusMainLauncher;
import io.quarkus.test.junit.main.QuarkusMainTest;

@QuarkusMainTest
public class PicocliTest {

    //    @Test
    //    @Launch({ "test-command", "-f", "test.txt", "-f", "test2.txt", "-f", "test3.txt", "-s", "ERROR", "-h", "SOCKS=5.5.5.5",
    //            "-p", "privateValue", "pos1", "pos2" })
    //    public void testBasicReflection(LaunchResult result) throws UnknownHostException {
    //        assertThat(result.getOutput())
    //                .contains("-s", "ERROR")
    //                .contains("-p:privateValue")
    //                .contains("-p:privateValue")
    //                .contains("positional:[pos1, pos2]");
    //    }
    //
    //    @Test
    //    public void testMethodSubCommand(QuarkusMainLauncher launcher) {
    //        LaunchResult result = launcher.launch("with-method-sub-command", "hello", "-n", "World!");
    //        assertThat(result.exitCode()).isZero();
    //        assertThat(result.getOutput()).isEqualTo("Hello World!");
    //        result = launcher.launch("with-method-sub-command", "goodBye", "-n", "Test?");
    //        assertThat(result.exitCode()).isZero();
    //        assertThat(result.getOutput()).isEqualTo("Goodbye Test?");
    //    }
    //
    //    @Test
    //    public void testLogCapturing(QuarkusMainLauncher launcher) {
    //        org.jboss.logging.Logger.getLogger("test").error("error");
    //        LaunchResult result = launcher.launch("with-method-sub-command", "hello", "-n", "World!");
    //        assertThat(result.exitCode()).isZero();
    //        assertThat(result.getOutput()).isEqualTo("Hello World!");
    //    }
    //
    //    @Test
    //    @Launch({ "command-used-as-parent", "-p", "testValue", "child" })
    //    public void testParentCommand(LaunchResult result) {
    //        assertThat(result.getOutput()).isEqualTo("testValue");
    //    }
    //
    //    @Test
    //    @Launch({ "exclusivedemo", "-b", "150" })
    //    public void testCommandWithArgGroup(LaunchResult result) {
    //        assertThat(result.getOutput())
    //                .contains("-a:0")
    //                .contains("-b:150")
    //                .contains("-c:0");
    //    }
    //
    //    @Test
    //    @Launch({ "dynamic-proxy" })
    //    public void testDynamicProxy(LaunchResult result) {
    //        assertThat(result.getOutput()).isEqualTo("2007-12-03T10:15:30");
    //    }

    //    @Test
    //    public void testLogNotCapturing(QuarkusMainLauncher launcher) {
    //        org.jboss.logging.Logger.getLogger("test").error("error");
    //        LaunchResult result = launcher.launch("with-method-sub-command", "hello", "-n", "World!");
    //        assertThat(result.exitCode()).isZero();
    //        assertThat(result.getOutput()).isEqualTo("Hello World!");
    //    }

    @Test
    public void testLogCapturing(QuarkusMainLauncher launcher) {
        Logger logger1 = org.jboss.logging.Logger.getLogger("test");
        logger1.error("error");
        //        Enumeration<String> loggerNames = org.jboss.logmanager.LogContext.getLogContext().getLoggerNames();
        //        while (loggerNames != null && loggerNames.hasMoreElements()) {
        //            String loggerName = loggerNames.nextElement();
        //        }
        //        org.jboss.logmanager.LogContext.getLogContext().getLogger(loggerName).clearHandlers();
        //        for (String loggerName: org.jboss.logmanager.LogContext.getInstance().getLoggerNames()) {
        //
        //        }
        //        LogManager.getLogManager().reset();
        // QuarkusConsole.installRedirects();
        org.jboss.logging.Logger.getLogger("test2").error("error2");
        LaunchResult result = launcher.launch("with-method-sub-command", "hello", "-n", "World!");
        assertThat(result.exitCode()).isZero();
        assertThat(result.getOutput()).isEqualTo("Hello World!");
    }

    //    @Test
    //    @Launch("quarkus")
    //    public void testDynamicVersionProvider(LaunchResult launchResult) {
    //
    //        System.out.println("DEBUG");
    //        System.out.println(launchResult.getOutput());
    //        System.out.println("ERR");
    //        System.out.println(launchResult.getErrorOutput());
    //        System.out.println("END DEBUG");
    //
    //        assertThat(launchResult.getOutput()).contains("[test] (main) error");
    //        assertThat(launchResult.getOutput()).contains("quarkus version 1.0");
    //    }

    //    @Test
    //    @Launch({ "unmatched", "-x", "-a", "AAA", "More" })
    //    public void testUnmatched(LaunchResult launchResult) {
    //        assertThat(launchResult.getOutput())
    //                .contains("-a:AAA")
    //                .contains("-b:null")
    //                .contains("remainder:[More]")
    //                .contains("unmatched[-x]");
    //    }
    //
    //    @Test
    //    public void testI18s(QuarkusMainLauncher launcher) {
    //        LaunchResult result = launcher.launch("localized-command-one", "--help");
    //        assertThat(result.getOutput())
    //                .contains("First in CommandOne");
    //        result = launcher.launch("localized-command-two", "--help");
    //        assertThat(result.getOutput())
    //                .contains("First in CommandTwo");
    //    }
    //
    //    @Test
    //    @Launch({ "completion-reflection", "test" })
    //    public void testCompletionReflection() {
    //
    //    }
    //
    //    @Test
    //    @Launch("default-value-provider")
    //    public void testDefaultValueProvider(LaunchResult result) {
    //        assertThat(result.getOutput()).isEqualTo("default:default-value");
    //    }
}
