import java.net.Socket;
import java.security.MessageDigest;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;

public class Cliente extends Thread {
    private Integer id;
    private Integer totalConexiones;

    private final static Integer PORT = 5555;
    private final static String HOST = "127.0.0.1";

    public Cliente(Integer id, Integer totalConexiones) {
        this.id = id;
        this.totalConexiones = totalConexiones;
    }

    private static String getFileChecksum(MessageDigest digest, File file) throws IOException
    {
    //Get file input stream for reading the file content
    FileInputStream fis = new FileInputStream(file);

    //Create byte array to read data in chunks
    byte[] byteArray = new byte[1024];
    int bytesCount = 0;

    //Read file data and update in message digest
    while ((bytesCount = fis.read(byteArray)) != -1) {
        digest.update(byteArray, 0, bytesCount);
    };

    //close the stream; We don't need it now.
    fis.close();

    //Get the hash's bytes
    byte[] bytes = digest.digest();

    //This bytes[] has bytes in decimal format;
    //Convert it to hexadecimal format
    StringBuilder sb = new StringBuilder();
    for(int i=0; i< bytes.length ;i++)
    {
        sb.append(Integer.toString((bytes[i] & 0xff) + 0x100, 16).substring(1));
    }

    //return complete hash
    return sb.toString();
    }

    public void run() {
        try {
            Socket socket = new Socket(HOST, PORT);
            System.out.println("Iniciando conexion-----------------------------");
            PrintWriter escritorS = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader lectorS = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            InputStream in = socket.getInputStream();
            DataInputStream clientData = new DataInputStream(in);
            int bytesRead;
            String mensajeInicio = "Cliente " + id + " intentando conectarse al servidor";
            System.out.println(mensajeInicio);
            escritorS.println(mensajeInicio);
            String conexionE = lectorS.readLine();
            if (conexionE.equals("Conexion establecida con cliente " + id)) {
                System.out.println("Cliente " + id + " establecio conexion con el servidor");
                String peticionArchivo = "Cliente " + id + " solicita tamanio del archivo";
                System.out.println(peticionArchivo);
                escritorS.println(peticionArchivo);
                long size = clientData.readLong();
                System.out.println("Cliente " + id + " recibio tamanio archivo" + size);
                String clienteListo = "Cliente " + id + " listo para recibir archivo";
                System.out.println(clienteListo);
                escritorS.println(clienteListo);
                Boolean archivoRecepcion = false;

                while (archivoRecepcion == false){
                    String fileName = "AppCliente/src/ArchivosRecibidos"+id+"-Prueba-"+totalConexiones+".txt" ;
                    OutputStream output = new FileOutputStream(fileName);
                    byte[] buffer = new byte[1024];
                    while (size > 0 && (bytesRead = clientData.read(buffer, 0, (int)Math.min(buffer.length, size))) != -1)
                    {
                        output.write(buffer, 0, bytesRead);
                        size -= bytesRead;
                    }
                    String archivoRecibido = "Cliente " + id + " solicita hash";
                    System.out.println(archivoRecibido);
                    escritorS.println(archivoRecibido);
                    String hash = lectorS.readLine();
                    File file = new File(fileName);
                    MessageDigest shaDigest = MessageDigest.getInstance("SHA-256");
                    String shaChecksum = getFileChecksum(shaDigest, file);
                    if (hash.equals(shaChecksum)){
                        archivoRecepcion = true;
                    }else{
                        String archivoInCorrecto = "Cliente " + id + " recibo archivo incorrecto";
                        System.out.println(archivoInCorrecto);
                        escritorS.println(archivoInCorrecto);
                    }
                    output.close();
                }
                String archivoCorrecto = "Cliente " + id + " recibo archivo correcto";
                System.out.println(archivoCorrecto);
                escritorS.println(archivoCorrecto);
                String conexionF = lectorS.readLine();
                System.out.println(conexionF + "Cliente " + id);
                //TO-DO log
            }
            escritorS.close();
            lectorS.close();
            in.close();
            clientData.close();
            socket.close();
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

}
