package utils;

import java.io.Serializable;
import java.util.Objects;

public class ClientData implements Serializable {
    private static final long serialVersionUID = 1L;
    private String name;
    private String hostName;
    private int port;

    public ClientData(String name, String hostName, int port) {
        this.name = name;
        this.hostName = hostName;
        this.port = port;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ClientData that = (ClientData) o;
        return Objects.equals(name, that.name) && Objects.equals(hostName, that.hostName) && Objects.equals(port, that.port);
    }

    @Override
    public String toString() {
        return name;
    }

    public String getName() {
        return name;
    }

    public String getHostName() {
        return hostName;
    }

    public int getPort() {
        return port;
    }
}
