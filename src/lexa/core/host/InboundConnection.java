/*==============================================================================
 * Lexa - Property of William Norman-Walker
 *------------------------------------------------------------------------------
 * InboundConnection.java
 *------------------------------------------------------------------------------
 * Author:  William Norman-Walker
 * Created: August 2013
 *==============================================================================
 */
package lexa.core.host;

import java.io.IOException;
import java.util.*;
import lexa.core.comms.Session;
import lexa.core.comms.SessionListener;
import lexa.core.data.DataSet;
import lexa.core.data.ArrayDataSet;
import lexa.core.data.exception.DataException;
import lexa.core.logging.Logger;
import lexa.core.server.connection.Connection;
import lexa.core.server.context.Context;
import lexa.core.server.messaging.Message;
import lexa.core.server.messaging.MessageSource;

/**
 * An inbound connection to the host.
 * @author william
 * @since 2013-08
 */
class InboundConnection
        implements SessionListener,
                MessageSource {

	private final Logger logger;
    private final Session session;
    private final Connection connection;
    private final List<Message> messages;
    InboundConnection(Session session, Connection connection) {
		this.logger = new Logger(InboundConnection.class.getSimpleName(),null);
        this.session = session;
        this.connection = connection;
        this.messages = new ArrayList();
    }

    void close()
    {
        throw new UnsupportedOperationException("InboundConnection.close not supported yet.");
    }

    void open() {
		this.logger.debug("open");
        this.session.setSessionListener(this);
		this.session.open();
    }

    @Override
    public void message(Session session, DataSet data) {
		this.logger.debug("message",data);
        Message message = new Message(this,data);
        int id = this.connection.submit(message);
        this.messages.add(message);
        DataSet confirm = new ArrayDataSet()
				.put(Context.SYSTEM_REQUEST, "messageReceived")
				.put(data.get(Context.SOURCE_REF))
				.put(Context.SOURCE_ID, id);
        try {
            this.session.send(confirm);
        } catch (DataException | IOException ex) {
			this.logger.error("Send confirm failed", confirm, ex);
        }
    }

    @Override
    public void messageClosed(Message message) {
		DataSet reply =message.getReply();
        try {
            this.session.send(reply);
        } catch (DataException | IOException ex) {
			this.logger.error("Message close failed #" + message.getSourceReference(), reply, ex);
        } finally {
            this.messages.remove(message);
        }
    }

    @Override
    public void replyReceived(Message message) {
		DataSet reply =message.getReply();
        try {
            this.session.send(reply);
        } catch (DataException | IOException ex) {
			this.logger.error("Reply received failed #" + message.getSourceReference(), reply, ex);
        }
    }

    @Override
    public void updateReceived(Message message) {
		DataSet reply =message.getReply();
        try {
            this.session.send(reply);
        } catch (DataException | IOException ex) {
			this.logger.error("Update received failed #" + message.getSourceReference(), reply, ex);
        }
    }
}
