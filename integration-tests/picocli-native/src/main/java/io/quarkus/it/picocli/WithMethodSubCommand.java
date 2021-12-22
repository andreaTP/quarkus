package io.quarkus.it.picocli;

import java.util.logging.Level;
import java.util.logging.LogRecord;

import org.jboss.logging.Logger;
import org.jboss.logmanager.LogManager;
import org.jboss.logmanager.handlers.ConsoleHandler;
import org.jboss.logmanager.handlers.OutputStreamHandler;

import io.quarkus.bootstrap.logging.QuarkusDelayedHandler;
import io.quarkus.dev.console.QuarkusConsole;
import picocli.CommandLine;

@CommandLine.Command(name = "with-method-sub-command")
public class WithMethodSubCommand {

    private final static Logger LOGGER = Logger.getLogger(WithMethodSubCommand.class);

    @CommandLine.Command
    void hello(@CommandLine.Option(names = { "-n", "--names" }, description = "Parameter option") String name) {
        System.out.println("Hello " + name);
    }

    @CommandLine.Command
    void loggingHello(@CommandLine.Option(names = { "-n", "--names" }, description = "Parameter option") String name) {
        var logger = LogManager.getLogManager().getLogger("");
        QuarkusConsole.ORIGINAL_OUT.println("CIAO");
        //        Formatter formatter = null;

        //        java.util.logging.Level lvl = null;

        for (var h : logger.getHandlers()) {
            QuarkusConsole.ORIGINAL_OUT.println("CIAO " + h.getClass());
            // logger.removeHandler(h);
            /// formatter = h.getFormatter();
            //            QuarkusConsole.ORIGINAL_OUT.println("formatter " + formatter);
            // lvl = h.getLevel();
            //            QuarkusConsole.ORIGINAL_OUT.println("level " + lvl);

            if (h instanceof io.quarkus.bootstrap.logging.QuarkusDelayedHandler) {
                //                var myWriter = new OutputStreamHandler();
                //                myWriter.setOutputStream(QuarkusConsole.REDIRECT_OUT);
                //                myWriter.setLevel(Level.ALL);
                for (var h2 : ((QuarkusDelayedHandler) h).getHandlers()) {
                    QuarkusConsole.ORIGINAL_OUT.println("ANCORA - " + h2.getClass());

                    if (h2 instanceof ConsoleHandler) {
                        QuarkusConsole.ORIGINAL_OUT
                                .println("TEST 1 - " + h2.getFormatter().format(new LogRecord(Level.ALL, "ciao")));

                        var formatter = h2.getFormatter();

                        var osh = new OutputStreamHandler(formatter);
                        osh.setOutputStream(QuarkusConsole.REDIRECT_OUT);
                        logger.addHandler(osh);
                    } else {
                        //                        QuarkusConsole.ORIGINAL_OUT
                        //                                .println("TEST 2 - " + h2.getClass());
                        //                        for (var h3 : ((QuarkusDelayedHandler) h2).getHandlers()) {
                        //                            QuarkusConsole.ORIGINAL_OUT
                        //                                    .println("TEST 3 - " + h3.getClass());
                        //                        }

                        //                        h2.getFormatter().format(new LogRecord(Level.ALL, "ciao"));
                    }

                }
            }
            //            if (h instanceof org.jboss.logmanager.handlers.ConsoleHandler) {
            //                //                formatter = h.getFormatter();
            //                //                lvl = h.getLevel();
            //
            //                ((ConsoleHandler) h).setOutputStream(QuarkusConsole.REDIRECT_OUT);
            //
            //                //                QuarkusConsole.ORIGINAL_OUT.println("EXAMPLE 1 " + formatter.format(new LogRecord(Level.ERROR, "ciao")));
            //                //                QuarkusConsole.ORIGINAL_OUT.println("EXAMPLE 2 " + formatter.formatMessage(new LogRecord(Level.ERROR, "ciao")));
            //
            //                //                            QuarkusConsole.ORIGINAL_OUT.println("REDIRECTING TO REDIRECT OUT " + QuarkusConsole.REDIRECT_OUT);
            //                //                            QuarkusConsole.REDIRECT_OUT.println("DEBUGGGO");
            //                //                            ((ConsoleHandler) h).setOutputStream(QuarkusConsole.REDIRECT_OUT);
            //            }
        }

        //        QuarkusConsole.ORIGINAL_OUT.println(" e adesso??? " + formatter.format(new LogRecord(Level.WARNING, "test")));

        //        Formatter finalFormatter = formatter;
        //        java.util.logging.Level finalLvl = lvl;

        //        myWriter.setFormatter(formatter);
        //        myWriter.setEnabled(true);
        //        myWriter.setLevel(lvl);
        //        myWriter.setOutputStream(QuarkusConsole.REDIRECT_OUT);
        //        var myWriter = new ConsoleHandler() {
        //            @Override
        //            protected void doPublish(ExtLogRecord record) {
        //                //                this.setLevel(finalLvl);
        //                QuarkusConsole.ORIGINAL_OUT.println("DEBUG DA QUI!");
        //                String formatted;
        //                try {
        //                    formatted = this.getFormatter().format(record);
        //                    QuarkusConsole.ORIGINAL_OUT.println("WTF ->" + formatted);
        //                } catch (Exception cause) {
        //                    this.reportError("Formatting error", cause, 5);
        //                    return;
        //                }
        //
        //                QuarkusConsole.REDIRECT_OUT.println("AND NOW " + formatted.length() + " - " + this.getLevel());
        //                if (formatted.length() != 0) {
        //                    try {
        //                        QuarkusConsole.REDIRECT_OUT.println("???");
        //                        synchronized (this.outputLock) {
        //                            QuarkusConsole.REDIRECT_OUT.print(formatted);
        //                        }
        //                    } catch (Exception cause) {
        //                        this.reportError("Error writing log message", cause, 1);
        //                    }
        //                }
        //            }
        //        };

        //        var myos = new OutputStream() {
        //            @Override
        //            public void write(int b) throws IOException {
        //                QuarkusConsole.REDIRECT_OUT.println("DEBUGGGO " + b);
        //                QuarkusConsole.REDIRECT_OUT.write(b);
        //            }
        //        };
        //
        //        myWriter.setOutputStream(myos);

        //        logger.addHandler(myWriter);

        //        var myOutputStream = new OutputStreamHandler();
        //        myOutputStream.setOutputStream(QuarkusConsole.REDIRECT_OUT);
        // myOutputStream.setWriter(new OutputStreamWriter(QuarkusConsole.REDIRECT_OUT));
        //        logger.addHandler(new Handler() {
        //            @Override
        //            public void publish(LogRecord record) {
        //
        //            }
        //
        //            @Override
        //            public void flush() {
        //
        //            }
        //
        //            @Override
        //            public void close() throws SecurityException {
        //
        //            }
        //        });
        // logger.addHandler(myOutputStream);
        //        logger.addHandler(new OutputStreamHandler() {
        //            @Override
        //            public void publish(LogRecord record) {
        //                QuarkusConsole.REDIRECT_OUT.println("E ADESSO? " + record.getMessage());
        //                this.setWriter(new OutputStreamWriter(QuarkusConsole.REDIRECT_OUT));
        //                this.setOutputStream(QuarkusConsole.REDIRECT_OUT);
        //                super.publish(record);
        //            }
        //        });

        //        logger.addHandler(new OutputStreamHandler() {
        //            @Override
        //            public void publish(LogRecord record) {
        //                QuarkusConsole.REDIRECT_OUT.println("E ADESSO?");
        //            }
        //
        //            @Override
        //            public void flush() {
        //                QuarkusConsole.REDIRECT_OUT.println("FLUSH");
        //            }
        //
        //            @Override
        //            public void close() throws SecurityException {
        //                QuarkusConsole.REDIRECT_OUT.println("CLOSE");
        //            }
        //        });

        //        var myHandler = new ConsoleHandler() {
        //        };
        //        myHandler.setOutputStream(QuarkusConsole.REDIRECT_OUT);
        //
        //        logger.addHandler(myHandler);

        LOGGER.error("Hello " + name);
    }

    @CommandLine.Command
    void goodBye(@CommandLine.Mixin NameMixin mixin) {
        System.out.println("Goodbye " + mixin.name);
    }
}
