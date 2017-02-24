/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package lexa.core.host;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import lexa.core.comms.*;
import lexa.core.data.config.ConfigDataSet;
import lexa.core.data.DataSet;
import lexa.core.data.ArrayDataSet;
import lexa.core.data.exception.DataException;
import lexa.core.expression.ExpressionException;
import lexa.core.expression.function.FunctionLibrary;
import lexa.core.logging.Logger;
import lexa.core.server.Broker;
import lexa.core.server.context.Config;
import lexa.core.process.ProcessException;
/**
 *
 * @author William Norman-Walker
 * @since YYYY-MM
 */

public class HostServer
        extends Thread
        implements SessionListener {

    private final Logger logger;
    private final ServerSession serverSession;
	private final Set<InboundConnection> connections;
    private final Broker broker;
    private boolean running;

    public HostServer(ConfigDataSet config)
            throws DataException,
                    IOException,
                    ExpressionException,
                    ProcessException {

        this.logger = new Logger(HostServer.class.getSimpleName(),config.getString(Config.NAME));
        // set up the listener
        ConfigDataSet server = config.getDataSet("server");
        this.serverSession = new ServerSession(this, server);
        server.close();

        // build the expression parser
        DataSet fd = null;
        if(config.contains("functions")) {
            ConfigDataSet fc = config.getDataSet("functions");
            fd = new ArrayDataSet(fc);
            fc.close();
        }
        FunctionLibrary functionLibrary = new FunctionLibrary(fd);

        // load the broker
        ConfigDataSet brokerConfig = config.getDataSet("broker");
        this.broker = new Broker(brokerConfig,functionLibrary);
        brokerConfig.close();

		this.connections = new HashSet();
    }

	@Override
    public void start() {
        // pre run checks and start the thread
        this.running = true;
        this.serverSession.open();
		try
		{
			this.broker.start();
			super.start();
		}
		catch (ProcessException ex)
		{
			this.logger.error("failed to start host", ex);
			this.running = false;
		}
    }

    @Override
    public void run() {
        while(this.running) {
            this.checkHost();
        }
    }

    private synchronized void checkHost() {
        try {
            this.wait();
        } catch (InterruptedException ex) {
            this.logger.error("checkHost" ,ex);
        }
    }

    @Override
    public synchronized void message(Session session, DataSet message) {
        // check that the message content is:
        // message - newSession
		this.logger.debug("message", message);
        if (!"newSession".equals(message.getString("message"))) {
            // ignore the message we don't want it.
            return;
        }
        try {
            InboundConnection connection =
                    new InboundConnection(session,
                            this.broker.getConnection());
			this.connections.add(connection);
			connection.open();
        } catch (ProcessException ex) {
            this.logger.error("Unable to create connection",message ,ex);
        }
    }
}
