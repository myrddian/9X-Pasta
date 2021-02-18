package ciotola.connection;

import java.nio.ByteBuffer;

public interface CiotolaChannelStage {
    boolean process(ByteBuffer byteBuffer);
}
