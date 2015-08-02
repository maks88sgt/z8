package org.zenframework.z8.server.ie;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Hashtable;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.DeliveryMode;
import javax.jms.Destination;
import javax.jms.ExceptionListener;
import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.ObjectMessage;
import javax.jms.Session;
import javax.jms.StreamMessage;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.zenframework.z8.server.base.file.FileInfo;
import org.zenframework.z8.server.base.file.FilesFactory;
import org.zenframework.z8.server.base.table.system.Files;
import org.zenframework.z8.server.base.table.system.Properties;
import org.zenframework.z8.server.logs.Trace;
import org.zenframework.z8.server.runtime.ServerRuntime;
import org.zenframework.z8.server.utils.IOUtils;

public class JmsTransport extends AbstractTransport implements ExceptionListener, Properties.Listener {

    public static final String PROTOCOL = "jms";

    private AtomicBoolean propertyChanged = new AtomicBoolean(false);

    private Connection connection;
    private Session session;
    private Destination self;
    private MessageConsumer consumer = null;

    public JmsTransport(TransportContext context) {
        super(context);
    }

    @Override
    public void onPropertyChange(String key, String value) {
        if (ServerRuntime.ConnectionFactoryProperty.equalsKey(key) || ServerRuntime.ConnectionUrlProperty.equalsKey(key)) {
            propertyChanged.set(true);
        }
    }

    @Override
    public void connect() throws TransportException {
        if (propertyChanged.getAndSet(false)) {
            close();
        }
        if (connection == null) {
            try {
                String jmsFactoryClass = Properties.getProperty(ServerRuntime.ConnectionFactoryProperty);
                String jmsUrl = Properties.getProperty(ServerRuntime.ConnectionUrlProperty);
                String selfAddress = context.getProperty(TransportContext.SelfAddressProperty);
                ConnectionFactory connectionFactory = getConnectionFactory(jmsFactoryClass, jmsUrl);
                connection = connectionFactory.createConnection();
                connection.start();
                connection.setExceptionListener(this);
                session = connection.createSession(true, -1 /* arg not used */);
                self = session.createQueue(selfAddress);
                consumer = session.createConsumer(self);
                Trace.logEvent("JMS transport: Connected to '" + jmsUrl + "'");
                Trace.logEvent("JMS Transport: Listening to '" + selfAddress + "'");
            } catch (JMSException e) {
                throw new TransportException("Can't open JMS connection", e);
            }
        }
    }

    @Override
    public void commit() throws TransportException {
        try {
            session.commit();
        } catch (JMSException e) {
            throw new TransportException("Can't commit JMS session", e);
        }
    }

    @Override
    public void rollback() throws TransportException {
        try {
            session.rollback();
        } catch (JMSException e) {
            throw new TransportException("Can't rollback JMS session", e);
        }
    }

    @Override
    public void init() {
        Properties.addListener(this);
    }

    @Override
    public void shutdown() {
        Properties.removeListener(this);
        close();
    }

    @Override
    public void send(Message message) throws TransportException {
        try {
            Destination destination = session.createQueue(message.getAddress());
            MessageProducer producer = session.createProducer(destination);
            producer.setDeliveryMode(DeliveryMode.PERSISTENT);
            javax.jms.Message jmsMessage = createObjectMessage(session, message);
            jmsMessage.setJMSReplyTo(self);
            producer.send(jmsMessage);
            session.commit();
            Trace.logEvent("Send IE message [" + message.getId() + "] to " + getUrl(message.getAddress()));
        } catch (Exception e) {
            throw new TransportException("Can't send IE message [" + message.getId() + "] to '" + message.getAddress()
                    + "' by JMS transport", e);
        }
    }

    @Override
    public String getProtocol() {
        return PROTOCOL;
    }

    @Override
    public boolean usePersistency() {
        return true;
    }

    @Override
    public void onException(JMSException e) {
        Trace.logError("JMS exception occured", e);
    }

    @Override
    public Message receive() throws TransportException {
        try {
            return parseObjectMessage(consumer.receive(100));
        } catch (Exception e) {
            throw new TransportException("Can't receive JMS message", e);
        }
    }

    @Override
    public void close() {
        if (consumer != null) {
            try {
                consumer.close();
                consumer = null;
            } catch (JMSException e) {
                Trace.logError("Can't close JMS message consumer", e);
            }
        }
        if (session != null) {
            try {
                session.close();
                session = null;
            } catch (JMSException e) {
                Trace.logError("Can't close JMS session", e);
            }
        }
        if (connection != null) {
            try {
                connection.close();
                connection = null;
            } catch (JMSException e) {
                Trace.logError("Can't close JMS connection", e);
            }
        }
        self = null;
    }

    private ConnectionFactory getConnectionFactory(String jmsFactoryClass, String jmsUrl) {
        try {
            Hashtable<String, String> props = new Hashtable<String, String>();
            props.put(Context.INITIAL_CONTEXT_FACTORY, jmsFactoryClass);
            props.put(Context.PROVIDER_URL, jmsUrl);
            javax.naming.Context ctx = new InitialContext(props);
            return (ConnectionFactory) ctx.lookup("ConnectionFactory");
        } catch (NamingException e) {
            throw new RuntimeException("Can't get connection factory '" + jmsFactoryClass + "' to '" + jmsUrl + "'", e);
        }
    }

    private static javax.jms.Message createObjectMessage(Session session, Message message) throws JMSException, IOException {
        List<FileInfo> fileInfos = IeUtil.filesToFileInfos(message.getExportEntry().getFiles().getFile());
        for (FileInfo fileInfo : fileInfos) {
            message.getFiles().add(Files.getFile(fileInfo));
        }
        return session.createObjectMessage(message);
    }

    private static Message parseObjectMessage(javax.jms.Message jmsMessage) throws JMSException {
        if (jmsMessage != null) {
            if (jmsMessage instanceof ObjectMessage) {
                Object messageObject = ((ObjectMessage) jmsMessage).getObject();
                if (messageObject instanceof Message) {
                    return (Message) messageObject;
                } else if (messageObject != null) {
                    Trace.logError(new Exception("Incorrect JMS message type: "
                            + messageObject.getClass().getCanonicalName()));
                }
            } else {
                Trace.logError(new Exception("Incorrect JMS message object type: "
                        + jmsMessage.getClass().getCanonicalName()));
            }
        }
        return null;
    }

    @SuppressWarnings("unused")
    private static javax.jms.Message createStreamMessage(Session session, Message message) throws JMSException, IOException {
        //javax.jms.Message jmsMessage = session.createObjectMessage(message);
        StreamMessage streamMessage = session.createStreamMessage();
        // write message object
        byte[] buff = IOUtils.objectToBytes(message);
        streamMessage.writeInt(buff.length);
        streamMessage.writeBytes(buff);
        List<FileInfo> fileInfos = IeUtil.filesToFileInfos(message.getExportEntry().getFiles().getFile());
        for (FileInfo fileInfo : fileInfos) {
            fileInfo = Files.getFile(fileInfo);
            // write file length
            streamMessage.writeLong(fileInfo.file.getSize());
            // write file contents
            InputStream in = fileInfo.file.getInputStream();
            buff = new byte[IOUtils.DefaultBufferSize];
            try {
                int count;
                while ((count = in.read(buff)) != -1) {
                    if (count > 0) {
                        streamMessage.writeBytes(buff, 0, count);
                    }
                }
            } finally {
                in.close();
            }
        }
        return streamMessage;
    }

    @SuppressWarnings("unused")
    private static Message parseStreamMessage(javax.jms.Message jmsMessage) throws JMSException, IOException {
        if (jmsMessage == null)
            return null;
        if (jmsMessage instanceof StreamMessage) {
            StreamMessage streamMessage = (StreamMessage) jmsMessage;
            // read message object
            byte[] buff = new byte[streamMessage.readInt()];
            int count = streamMessage.readBytes(buff);
            if (count < buff.length) {
                throw new IOException("Unexpected eof");
            }
            Object messageObject = IOUtils.bytesToObject(buff);
            if (messageObject instanceof Message) {
                Message message = (Message) messageObject;
                message.setFiles(IeUtil.filesToFileInfos(message.getExportEntry().getFiles().getFile()));
                for (FileInfo fileInfo : message.getFiles()) {
                    fileInfo.file = FilesFactory.createFileItem(fileInfo.name.get());
                    // read file size
                    long size = streamMessage.readLong();
                    buff = new byte[IOUtils.DefaultBufferSize];
                    OutputStream out = fileInfo.file.getOutputStream();
                    try {
                        while (size > 0) {
                            count = streamMessage.readBytes(buff);
                            if (count < IOUtils.DefaultBufferSize) {
                                throw new IOException("Unexpected eof");
                            }
                            out.write(buff);
                            size -= count;
                            if (size > 0 && size < buff.length) {
                                buff = new byte[(int) size];
                            }
                        }
                    } finally {
                        out.close();
                    }
                }
                return message;
            } else if (messageObject != null) {
                Trace.logError(new Exception("Incorrect JMS message object type: "
                        + messageObject.getClass().getCanonicalName()));
            }
        } else {
            Trace.logError(new Exception("Incorrect JMS message type: " + jmsMessage));
        }
        return null;
    }

}