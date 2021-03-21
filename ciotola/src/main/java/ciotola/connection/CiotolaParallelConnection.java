package ciotola.connection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;

class CiotolaParallelConnection extends Thread{

    private Selector selector;
    private boolean isStarted  = false;
    private final Logger logger = LoggerFactory.getLogger(CiotolaParallelConnection.class);

    public CiotolaParallelConnection() throws IOException {
        this.selector = Selector.open();
    }

    public boolean registerChannel(SocketChannel newSocket, ChannelAttributesImpl channelId) {
        try {
            newSocket.configureBlocking(false);
            newSocket.register(selector, SelectionKey.OP_READ, channelId);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    private void process() throws IOException {
        selector.selectNow();
            Iterator<SelectionKey> iter;
            SelectionKey key;
            iter = selector.selectedKeys().iterator();
            while(iter.hasNext()) {
                key = iter.next();
                iter.remove();
                if(key.isReadable()) {
                    ChannelAttributesImpl attributes = (ChannelAttributesImpl) key.attachment();
                    attributes.getCallback().process(attributes);
                }
            }

    }

    @Override
    public void run() {
        while(isStarted) {
            try {
                process();
            }catch (Throwable ex) {
                logger.error("An Exception has occurred ",ex);
            }
        }
    }

    public boolean getStarted() { return this.isStarted;}

    public void setStarted() {
        this.isStarted = true;
        this.start();
    }

    public void halt() {
        this.isStarted = false;
    }

}
