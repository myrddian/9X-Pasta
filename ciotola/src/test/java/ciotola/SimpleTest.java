package ciotola;

import ciotola.Ciotola;
import ciotola.actor.CiotolaDirector;
import ciotola.connection.CiotolaServerConnection;
import org.junit.jupiter.api.Test;

import java.io.IOException;

public class SimpleTest {

    @Test
    public void simpleTest() throws IOException {
        Ciotola ciotola = Ciotola.getInstance();
        CiotolaServerConnection serverConnection = new CiotolaServerConnection(600,ciotola.threadCapacity(),false,ciotola);
        ciotola.addService(serverConnection);
        ciotola.startContainer();

        while(true);

    }
}
