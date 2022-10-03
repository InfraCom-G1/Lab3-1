import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.MessageDigest;
import java.util.Date;
import java.util.Scanner;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AppServer extends Thread {
    private static final int PORT = 8080;
    private static String BigFile = "data\250MiB.bin";
    private static String SmallFile = "data\100MiB.bin";
    // Tamanio 0 = 100MiB y 1 = 250MiB
    private static int TamanioArchivo = 0;
    private static File logFile;

    private static int nClientes = 0;

    public static void main(String[] args) throws Exception {
        // Get current time for logFile name <año-mes-dia-hora-minuto-segundo-log.txt>
        Date now = new Date();
        String logFileName = String.format("%tY-%tm-%td-%tH-%tM-%tS-log.txt", now, now, now, now, now, now);
        logFile = new File(logFileName);

        Scanner scanner = new Scanner(System.in);
        System.out.println("Ingrese el tamaño del archivo a enviar: 0 = 100MiB y 1 = 250MiB");
        TamanioArchivo = scanner.nextInt();
        System.out.println("Ingrese el puerto a utilizar: ");
        int PORT = scanner.nextInt();
        System.out.println("Ingrese la cantidad de clientes a atender: ");
        nClientes = scanner.nextInt();

        File file;
        if (TamanioArchivo == 0) {
            file = new File(SmallFile);
        } else {
            file = new File(BigFile);
        }

        ServerSocket serverSocket = new ServerSocket(PORT);

        ExecutorService executor = Executors.newFixedThreadPool(nClientes);
        CyclicBarrier barrera = new CyclicBarrier(nClientes);

        for (int i = 0; i < nClientes; i++) {
            Socket socketCliente = serverSocket.accept();
            executor.execute(new AppServer(socketCliente, file, barrera, i));
        }

        executor.shutdown();
        serverSocket.close();
        scanner.close();
    }

    public static synchronized void log(String msg) {
        try {
            Date now = new Date();
            FileWriter fw = new FileWriter(logFile, true);
            fw.write(now.toString() + " - " + msg + "\n");
            fw.close();
        } catch (Exception e) {
            System.err.println("Error al escribir en el log");
        }
    }

    private static String getFileChecksum(MessageDigest digest, File file) throws IOException {
        // Get file input stream for reading the file content
        FileInputStream fis = new FileInputStream(file);

        // Create byte array to read data in chunks
        byte[] byteArray = new byte[1024];
        int bytesCount = 0;

        // Read file data and update in message digest
        while ((bytesCount = fis.read(byteArray)) != -1) {
            digest.update(byteArray, 0, bytesCount);
        }
        ;

        // close the stream; We don't need it now.
        fis.close();

        // Get the hash's bytes
        byte[] bytes = digest.digest();

        // This bytes[] has bytes in decimal format;
        // Convert it to hexadecimal format
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < bytes.length; i++) {
            sb.append(Integer.toString((bytes[i] & 0xff) + 0x100, 16).substring(1));
        }

        // return complete hash
        return sb.toString();
    }

    private Socket socketCliente;

    private File fileEnviar;
    private CyclicBarrier barrera;

    private int idDelegado;

    public AppServer(Socket socketCliente, File file, CyclicBarrier barrera, int id) {
        this.socketCliente = socketCliente;
        this.fileEnviar = file;
        this.barrera = barrera;
        this.idDelegado = id;
    }

    @Override
    public void run() {
        try {
            MessageDigest md5Digest = MessageDigest.getInstance("MD5");
            String checksum = getFileChecksum(md5Digest, fileEnviar);
            // System.out.println("Checksum del archivo: " + checksum);

            InputStream in = socketCliente.getInputStream();
            OutputStream out = socketCliente.getOutputStream();

            String msg = in.read() + "";
            log("Server: " + idDelegado + " - Mensaje recibido" + msg);
            String partes[] = msg.split(" ");
            Integer idCliente = Integer.parseInt(partes[4]);

            msg = "Conexion establecida con cliente " + idCliente.toString();
            log("Server: " + idDelegado + " - " + msg);
            out.write(msg.getBytes());

            msg = in.read() + "";
            log("Server: " + idDelegado + " - Mensaje recibido" + msg);
            partes = msg.split(" ");
            Boolean idCorrecto = partes[1].equals(idCliente.toString());
            if (!idCorrecto) {
                msg = "Error en el id del cliente";
                log("Server: " + idDelegado + " - " + msg);
                socketCliente.close();
                return;
            }
            long tamanioEnvio = fileEnviar.length();
            msg = "Tamanio del archivo a enviar: " + tamanioEnvio;
            log("Server: " + idDelegado + " - " + msg);
            out.write(msg.getBytes());

            msg = in.read() + "";
            log("Server: " + idDelegado + " - Mensaje recibido" + msg);
            partes = msg.split(" ");
            idCorrecto = partes[1].equals(idCliente.toString());
            if (!idCorrecto) {
                msg = "Error en el id del cliente";
                log("Server: " + idDelegado + " - " + msg);
                socketCliente.close();
                return;
            }

            if (!msg.contains("listo")) {
                msg = "Error en el mensaje de listo";
                log("Server: " + idDelegado + " - " + msg);
                socketCliente.close();
                return;
            }

            long start = System.currentTimeMillis();

            Boolean enviado = false;
            while (!enviado) {
                // Create Streams to send the file
                FileInputStream fileInputStream = new FileInputStream(fileEnviar);
                DataOutputStream dataOutputStream = new DataOutputStream(out);

                log("Server: " + idDelegado + " - Esperando a los demas clientes");
                barrera.await();

                // Send the file
                msg = "Enviando archivo...";
                log("Server: " + idDelegado + " - " + msg);
                byte[] buffer = new byte[1024];
                int len;
                while ((len = fileInputStream.read(buffer)) > 0) {
                    dataOutputStream.write(buffer, 0, len);
                }
                fileInputStream.close();
                dataOutputStream.close();

                msg = "Archivo enviado, esperando checksum";
                log("Server: " + idDelegado + " - " + msg + " del cliente " + idCliente);

                msg = in.read() + "";
                log("Server: " + idDelegado + " - Mensaje recibido" + msg);
                partes = msg.split(" ");
                idCorrecto = partes[1].equals(idCliente.toString());
                if (!idCorrecto) {
                    msg = "Error en el id del cliente";
                    log("Server: " + idDelegado + " - " + msg);
                    socketCliente.close();
                    return;
                }
                // Send hash
                msg = "Enviando checksum";
                log("Server: " + idDelegado + " - " + msg + " a cliente " + idCliente);
                out.write(checksum.getBytes());

                msg = in.read() + "";
                log("Server: " + idDelegado + " - Mensaje recibido" + msg);
                partes = msg.split(" ");
                idCorrecto = partes[1].equals(idCliente.toString());
                if (!idCorrecto) {
                    msg = "Error en el id del cliente";
                    log("Server: " + idDelegado + " - " + msg);
                    socketCliente.close();
                    return;
                }
                if (msg.contains("incorrecto")) {
                    msg = "Error en el mensaje de listo";
                    log("Server: " + idDelegado + " - " + msg);
                    enviado = false;
                } else {
                    enviado = true;
                }
            }
            long end = System.currentTimeMillis();
            log("Server: " + idDelegado + " - Tiempo de envio: " + (end - start) + " ms");
            // Terminar conexion

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
