import java.net.Socket;

public class Cliente extends Thread {
    private Integer id;
    private final static Integer PORT = 5555;
    private final static String HOST = "127.0.0.1";

    public Cliente(Integer id) {
        this.id = id;
    }

    public void run() {
        try {
            Socket socket = new Socket(HOST, PORT);
            System.out.println("Cliente " + id + " conectado al servidor");
            // Esperar el archivo
            
            // Recibir el archivo

            // Cerrar el socket
            socket.close();
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

}
