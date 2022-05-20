package utils;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public record SocketResources(Socket socket, ObjectOutputStream out, ObjectInputStream in) {
}
