/*
 * ================================================================================
 * Lexa - Property of William Norman-Walker
 * --------------------------------------------------------------------------------
 * LexaHostServer.java
 *--------------------------------------------------------------------------------
 * Author:  William Norman-Walker
 * Created: August 2013
 *--------------------------------------------------------------------------------
 * Change Log
 * Date:        By: Ref:        Description:
 * ----------   --- ----------  --------------------------------------------------
 * -            -   -           -
 *================================================================================
 */
package lxhost;

import java.io.File;
import java.io.IOException;
import lexa.core.data.config.ConfigDataSet;
import lexa.core.data.exception.DataException;
import lexa.core.data.io.DataReader;
import lexa.core.expression.ExpressionException;
import lexa.core.host.HostServer;
import lexa.core.logging.Logger;
import lexa.core.process.ProcessException;
import lexa.core.server.context.Config;

/**
 * Entry point for the host server.
 * <p>This opens up a server and a comms session, and waits for it to end.
 *
 * @author William Norman-Walker
 */
public class HostApp {

    private static HostServer host;
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        try {
            if (args.length != 1) {
                throw new IllegalArgumentException(
                        "HostServer takes a single argument of the config file's name");
            }
            String fileName = args[0];

            ConfigDataSet config = new ConfigDataSet(
                    new DataReader(
                            new File(fileName)).read());
            if (config.contains(Config.LOG_FILE)) {
                lexa.core.logging.Logger.setLogWriter(new File(config.getString("logFile")));
            }
            HostApp.host = new HostServer(config);
            config.close();
            HostApp.host.start();
        } catch (IllegalArgumentException |
                IOException |
                DataException |
                ExpressionException |
                ProcessException ex) {
            new Logger("lxHostServer", "main")
                    .error("Cannot start host server", ex);
        }
    }
}
