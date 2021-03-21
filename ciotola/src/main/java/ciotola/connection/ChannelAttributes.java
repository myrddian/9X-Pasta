package ciotola.connection;

import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

public interface ChannelAttributes {

    Long getChannelId();
    SocketChannel getChannel();
    SelectionKey getSelectionKey();
    Long getTimeSinceLastUpdate();
    Boolean noErrors();
    void setError();
    CiotolaConnectionHandler getCallback();
}
