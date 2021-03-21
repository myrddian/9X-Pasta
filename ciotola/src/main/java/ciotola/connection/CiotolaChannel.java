package ciotola.connection;

public interface CiotolaChannel<R,W> {
    R read();
    void write(W msg);
}
