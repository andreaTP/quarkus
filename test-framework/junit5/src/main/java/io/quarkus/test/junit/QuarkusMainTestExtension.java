package io.quarkus.test.junit;

import static io.quarkus.test.junit.IntegrationTestUtil.getAdditionalTestResources;

import java.io.Closeable;
import java.util.*;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.logging.Handler;

import org.jboss.logmanager.LogContext;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;

import io.quarkus.bootstrap.app.StartupAction;
import io.quarkus.deployment.dev.testing.LogCapturingOutputFilter;
import io.quarkus.dev.console.QuarkusConsole;
import io.quarkus.dev.testing.TracingHandler;
import io.quarkus.test.common.TestResourceManager;
import io.quarkus.test.junit.main.Launch;
import io.quarkus.test.junit.main.LaunchResult;
import io.quarkus.test.junit.main.QuarkusMainIntegrationTest;
import io.quarkus.test.junit.main.QuarkusMainLauncher;

public class QuarkusMainTestExtension extends AbstractJvmQuarkusTestExtension
        implements BeforeEachCallback, AfterEachCallback, ParameterResolver, BeforeAllCallback, AfterAllCallback {

    public static final ExtensionContext.Namespace NAMESPACE = ExtensionContext.Namespace
            .create("io.quarkus.test.main.jvm");

    private static Map<String, String> devServicesProps;

    PrepareResult prepareResult;
    private static boolean hasPerTestResources;

    /**
     * The result from an {@link Launch} test
     */
    LaunchResult result;

    @Override
    public void beforeEach(ExtensionContext context) throws Exception {
        if (isIntegrationTest(context.getRequiredTestClass())) {
            return;
        }

        Class<? extends QuarkusTestProfile> profile = getQuarkusTestProfile(context);
        ensurePrepared(context, profile);
        var launch = context.getRequiredTestMethod().getAnnotation(Launch.class);
        if (launch != null) {
            String[] arguments = launch.value();
            LaunchResult r = doLaunch(context, profile, arguments);
            Assertions.assertEquals(launch.exitCode(), r.exitCode(),
                    "Exit code did not match, output: " + r.getOutput() + " " + r.getErrorOutput());
            this.result = r;
        }
    }

    private void ensurePrepared(ExtensionContext extensionContext, Class<? extends QuarkusTestProfile> profile)
            throws Exception {
        ExtensionContext.Store store = extensionContext.getStore(ExtensionContext.Namespace.GLOBAL);
        QuarkusTestExtension.ExtensionState state = store.get(QuarkusTestExtension.ExtensionState.class.getName(),
                QuarkusTestExtension.ExtensionState.class);
        boolean wrongProfile = !Objects.equals(profile, quarkusTestProfile);
        // we reload the test resources if we changed test class and if we had or will have per-test test resources
        boolean reloadTestResources = !Objects.equals(extensionContext.getRequiredTestClass(), currentJUnitTestClass)
                && (hasPerTestResources || hasPerTestResources(extensionContext));
        if (wrongProfile || reloadTestResources) {
            if (state != null) {
                try {
                    state.close();
                } catch (Throwable throwable) {
                    throwable.printStackTrace();
                }
            }
            prepareResult = null;
        }
        if (prepareResult == null) {
            final LinkedBlockingDeque<Runnable> shutdownTasks = new LinkedBlockingDeque<>();
            PrepareResult result = createAugmentor(extensionContext, profile, shutdownTasks);
            prepareResult = result;
        }
    }

    private LaunchResult doLaunch(ExtensionContext context, Class<? extends QuarkusTestProfile> selectedProfile,
            String[] arguments) throws Exception {
        ensurePrepared(context, selectedProfile);
        LogCapturingOutputFilter filter = new LogCapturingOutputFilter(prepareResult.curatedApplication, false, false,
                () -> true);
        QuarkusConsole.addOutputFilter(filter);
        try {
            var result = doJavaStart(context, selectedProfile, arguments);
            //merge all the output into one, strip ansi, then split into lines
            List<String> out = Arrays
                    .asList(String.join("", filter.captureOutput()).replaceAll("\\u001B\\[(.*?)[a-zA-Z]", "").split("\n"));
            List<String> err = Arrays
                    .asList(String.join("", filter.captureErrorOutput()).replaceAll("\\u001B\\[(.*?)[a-zA-Z]", "").split("\n"));
            return new LaunchResult() {
                @Override
                public List<String> getOutputStream() {
                    return out;
                }

                @Override
                public List<String> getErrorStream() {
                    return err;
                }

                @Override
                public int exitCode() {
                    return result;
                }
            };
        } finally {
            QuarkusConsole.removeOutputFilter(filter);
            Thread.currentThread().setContextClassLoader(originalCl);
        }
    }

    @Override
    public void afterEach(ExtensionContext context) throws Exception {
        result = null;
    }

    private void patchConsoleHandler() throws Exception {
        //        var rootLogger = LogContext.getLogContext().getLogger("");
        //        var handlers = rootLogger.getHandlers();
        //
        //        var defaultConfig = "loggers=org.jboss.logmanager\n" +
        //                "\n" +
        //                "# Root logger\n" +
        //                "logger.level=INFO\n" +
        //                "logger.handlers=CONSOLE\n" +
        //                "\n" +
        //                "logger.org.jboss.logmanager.useParentHandlers=true\n" +
        //                "logger.org.jboss.logmanager.level=INFO\n" +
        //                "\n" +
        //                "handler.CONSOLE=org.jboss.logmanager.handlers.ConsoleHandler\n" +
        //                "handler.CONSOLE.formatter=PATTERN\n" +
        //                "handler.CONSOLE.properties=autoFlush,target\n" +
        //                "handler.CONSOLE.autoFlush=true\n" +
        //                "handler.CONSOLE.target=SYSTEM_OUT\n" +
        //                "\n" +
        //                "formatter.PATTERN=org.jboss.logmanager.formatters.PatternFormatter\n" +
        //                "formatter.PATTERN.properties=pattern\n" +
        //                "formatter.PATTERN.pattern=PIPPO %d{HH:mm:ss,SSS} %-5p [%c] (%t) %s%E%n";

        //        LogManager.getLogManager().readConfiguration();
        //        LogManager.getLogManager().readConfiguration();
        //        LogManager.getLogManager().readConfiguration(new ByteArrayInputStream(defaultConfig.getBytes()));

        //        Logger root = LogManager.getLogManager().getLogger("");
        //
        //        var myHandler = new ConsoleHandler();
        //        myHandler.setOutputStream(QuarkusConsole.REDIRECT_OUT);
        //
        //        root.addHandler(myHandler);

        //        Enumeration<String> loggerNames = org.jboss.logmanager.LogContext.getLogContext().getLoggerNames();
        //        while (loggerNames != null && loggerNames.hasMoreElements()) {
        //            String loggerName = loggerNames.nextElement();
        //        for (Handler h : root.getHandlers()) {
        //
        //            QuarkusConsole.ORIGINAL_OUT.println("CIAO " + h.getClass());
        //            recursivelyPatchHandlers(h);
        //        }
        //        }

        //        LogManager.getLogManager().getLoggerNames()
        //        for (var h : rootLogger.getHandlers()) {
        //            recursivelyPatchHandlers(h);
        //        }
    }

    //    private void recursivelyPatchHandlers(Handler handler) {
    //        if (handler instanceof ConsoleHandler) {
    //            ((ConsoleHandler) handler).setOutputStream(QuarkusConsole.REDIRECT_OUT);
    //            for (var h : ((ConsoleHandler) handler).getHandlers()) {
    //                recursivelyPatchHandlers(h);
    //            }
    //        } else if (handler instanceof QuarkusDelayedHandler) {
    //            for (var h : ((QuarkusDelayedHandler) handler).getHandlers()) {
    //                recursivelyPatchHandlers(h);
    //            }
    //        } else {
    //            QuarkusConsole.ORIGINAL_OUT.println("DEBUG " + handler.getClass());
    //        }
    //    }

    private void unpatchConsoleHandler() {
        //        var rootLogger = LogContext.getLogContext().getLogger("");
        //        var handlers = rootLogger.getHandlers();

        //        for (var h : rootLogger.getHandlers()) {
        //            recursivelyPatchHandlers(h);
        //        }
    }

    private void flushAllLoggers() {
        Enumeration<String> loggerNames = org.jboss.logmanager.LogContext.getLogContext().getLoggerNames();
        while (loggerNames != null && loggerNames.hasMoreElements()) {
            String loggerName = loggerNames.nextElement();
            var logger = org.jboss.logmanager.LogContext.getLogContext().getLogger(loggerName);
            for (Handler h : logger.getHandlers()) {
                h.flush();
            }
        }
    }

    private int doJavaStart(ExtensionContext context, Class<? extends QuarkusTestProfile> profile, String[] arguments)
            throws Exception {
        TracingHandler.quarkusStarting();
        Closeable testResourceManager = null;
        try {
            StartupAction startupAction = prepareResult.augmentAction.createInitialRuntimeApplication();
            Thread.currentThread().setContextClassLoader(startupAction.getClassLoader());
            QuarkusConsole.installRedirects();
            flushAllLoggers();
            patchConsoleHandler();
            //            setFinalStatic(org.jboss.logmanager.handlers.ConsoleHandler.class,
            //                    org.jboss.logmanager.handlers.ConsoleHandler.class.getDeclaredField("out"),
            //                    QuarkusConsole.REDIRECT_OUT);

            QuarkusTestProfile profileInstance = prepareResult.profileInstance;

            //must be done after the TCCL has been set
            testResourceManager = (Closeable) startupAction.getClassLoader().loadClass(TestResourceManager.class.getName())
                    .getConstructor(Class.class, Class.class, List.class, boolean.class, Map.class, Optional.class)
                    .newInstance(context.getRequiredTestClass(),
                            profile != null ? profile : null,
                            getAdditionalTestResources(profileInstance, startupAction.getClassLoader()),
                            profileInstance != null && profileInstance.disableGlobalTestResources(),
                            startupAction.getDevServicesProperties(), Optional.empty());
            testResourceManager.getClass().getMethod("init", String.class).invoke(testResourceManager,
                    profile != null ? profile.getName() : null);
            Map<String, String> properties = (Map<String, String>) testResourceManager.getClass().getMethod("start")
                    .invoke(testResourceManager);
            startupAction.overrideConfig(properties);
            hasPerTestResources = (boolean) testResourceManager.getClass().getMethod("hasPerTestResources")
                    .invoke(testResourceManager);

            var result = startupAction.runMainClassBlocking(arguments);
            flushAllLoggers();
            return result;
        } catch (Throwable e) {

            try {
                if (testResourceManager != null) {
                    testResourceManager.close();
                }
            } catch (Exception ex) {
                e.addSuppressed(ex);
            }
            throw e;
        } finally {
            unpatchConsoleHandler();
            QuarkusConsole.uninstallRedirects();
            if (originalCl != null) {
                Thread.currentThread().setContextClassLoader(originalCl);
            }
        }
    }

    @Override
    public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext)
            throws ParameterResolutionException {
        if (isIntegrationTest(extensionContext.getRequiredTestClass())) {
            return false;
        }
        Class<?> type = parameterContext.getParameter().getType();
        return type == LaunchResult.class || type == QuarkusMainLauncher.class;
    }

    @Override
    public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext)
            throws ParameterResolutionException {
        Class<?> type = parameterContext.getParameter().getType();
        Class<? extends QuarkusTestProfile> profile = getQuarkusTestProfile(extensionContext);
        if (type == LaunchResult.class) {
            return result;
        } else if (type == QuarkusMainLauncher.class) {
            return new QuarkusMainLauncher() {
                @Override
                public LaunchResult launch(String... args) {
                    try {
                        return doLaunch(extensionContext, profile, args);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
            };
        } else {
            throw new RuntimeException("Parameter type not supported");
        }
    }

    private boolean isIntegrationTest(Class<?> clazz) {
        for (Class<?> i : currentTestClassStack) {
            if (i.isAnnotationPresent(QuarkusMainIntegrationTest.class)) {
                return true;
            }
        }
        if (clazz.isAnnotationPresent(QuarkusMainIntegrationTest.class)) {
            return true;
        }
        return false;
    }

    @Override
    public void afterAll(ExtensionContext context) throws Exception {
        currentTestClassStack.pop();
    }

    @Override
    public void beforeAll(ExtensionContext context) throws Exception {
        currentTestClassStack.push(context.getRequiredTestClass());
    }
}
