package ciotola.connection;

import ciotola.Ciotola;
import ciotola.CiotolaServiceInterface;
import ciotola.implementation.DefaultCiotolaContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class CiotolaServerConnection implements CiotolaServiceInterface {

    private int port;
    private ServerSocketChannel serverSocketChannel;
    private Selector selector;
    private boolean running = true;
    private Map<Long, CiotolaParallelConnection> connectionRunners = new ConcurrentHashMap<>();
    private Long connections =0L;
    private final Logger logger = LoggerFactory.getLogger(CiotolaServerConnection.class);

    public CiotolaServerConnection(int port, int services, boolean isCallBack, Ciotola container)  throws IOException {
        this.port = port;
        this.serverSocketChannel = ServerSocketChannel.open();
        this.serverSocketChannel.socket().bind(new InetSocketAddress(port));
        this.serverSocketChannel.configureBlocking(false);
        this.selector = Selector.open();
        this.serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
        for(long val=0; val < services; ++val) {
            connectionRunners.put(val,new CiotolaParallelConnection());
            logger.trace("Starting NIO Connection handler {}",val);
            connectionRunners.get(val).setStarted();
        }
    }


    private void process() {
        logger.info("Listening for connections....");
        try {
            Iterator<SelectionKey> iter;
            SelectionKey key;
            while(serverSocketChannel.isOpen())  {
                if(selector.select() != 0) {
                    iter = selector.selectedKeys().iterator();
                    while(iter.hasNext()) {
                        key = iter.next();
                        iter.remove();
                        if(key.isAcceptable()) {
                            SocketChannel clientConnection = ((ServerSocketChannel) key.channel()).accept();
                            long target  = connections % connectionRunners.size();
                            ChannelAttributesImpl attributes = new ChannelAttributesImpl(connections,clientConnection,key);
                            connectionRunners.get(target).registerChannel(clientConnection,attributes);
                            logger.info("Connection accepted  - ID: {}  - Pool: {}",connections, target);
                            ++connections;
                        }
                    }
                }
            }
        } catch(IOException exception) {
           exception.printStackTrace();
        }
    }

    public int getPort() { return this.port;}


    @Override
    public boolean startUp() {
        return running;
    }

    @Override
    public boolean shutdown() {
        running = false;
        return true;
    }

    @Override
    public boolean run() {
        process();
        return true;
    }

    @Override
    public String serviceName() {
        return CiotolaServerConnection.class.getName();
    }

    @Override
    public Object getObject() {
        return this;
    }
}
