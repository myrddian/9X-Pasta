package ciotola.connection;

import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

public final class ChannelAttributesImpl implements ChannelAttributes {

    private Long channelId;
    private SocketChannel channel;
    private SelectionKey selectionKey;
    private boolean isCallBackHandler = false;
    private Long timeSinceLastUpdate  = 0L;
    private CiotolaConnectionHandler callback;
    private Boolean isInError  = false;

    public ChannelAttributesImpl(Long channelId, SocketChannel channel, SelectionKey selectionKey) {
        this.channelId = channelId;
        this.channel = channel;
        this.selectionKey = selectionKey;
    }

    public void setCallBackHandler(CiotolaConnectionHandler handler) {
        isCallBackHandler = true;
        callback = handler;
    }

    public boolean isCallBackHandler() {
        return isCallBackHandler;
    }

    public void setTimeSinceLastUpdate(Long newTime) {
        this.timeSinceLastUpdate = newTime;
    }

    @Override
    public Long getChannelId() {
        return channelId;
    }

    @Override
    public SocketChannel getChannel() {
        return channel;
    }

    @Override
    public SelectionKey getSelectionKey() {
        return selectionKey;
    }

    @Override
    public Long getTimeSinceLastUpdate() {
        return timeSinceLastUpdate;
    }

    @Override
    public Boolean noErrors() {
        return isInError;
    }

    @Override
    public void setError() {
        isInError = true;
    }

    @Override
    public CiotolaConnectionHandler getCallback() {
        return this.callback;
    }

}
