import java.net.Socket;
import java.security.MessageDigest;
import java.util.Date;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;

public class Cliente extends Thread {
    private Integer id;
    private Integer totalConexiones;
    private File logFile;
    private final static Integer PORT = 5555;
    private final static String HOST = "192.168.159.128";

    public Cliente(Integer id, Integer totalConexiones, File logFile) {
        this.id = id;
        this.totalConexiones = totalConexiones;
        this.logFile = logFile;
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


    public static synchronized void log(String msg, File logFile) {
        try {
            Date now = new Date();
            FileWriter fw = new FileWriter(logFile, true);
            fw.write(now.toString() + " - " + msg + "\n");
            fw.close();
        } catch (Exception e) {
            System.err.println("Error al escribir en el log");
        }
    }

    public void run() {
        try {
            Socket socket = new Socket(HOST, PORT);
            PrintWriter escritorS = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader lectorS = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            InputStream in = socket.getInputStream();
            DataInputStream clientData = new DataInputStream(in);
            int bytesRead;
            String mensajeInicio = "Cliente " + id + " intentando conectarse al servidor";
            log(mensajeInicio, logFile);
            escritorS.println(mensajeInicio);
            String conexionE = lectorS.readLine();
            if (conexionE.equals("Conexion establecida con cliente " + id)) {
                log("Cliente " + id + " establecio conexion con el servidor", logFile);
                String peticionArchivo = "Cliente " + id + " solicita tamanio del archivo";
                log(peticionArchivo, logFile);
                escritorS.println(peticionArchivo);
                long size = Long.valueOf(lectorS.readLine().split(" ")[5]);
                log("Cliente " + id + " recibio tamanio archivo " + size, logFile);
                String clienteListo = "Cliente " + id + " listo para recibir archivo";
                log(clienteListo, logFile);
                escritorS.println(clienteListo);
                Boolean archivoRecepcion = false;
                long start = System.currentTimeMillis();
                while (archivoRecepcion == false){
                    String fileName = "ArchivosRecibidos\\"+id+"-Prueba-"+totalConexiones+".bin" ;
                    File file = new File(fileName);
                    file.createNewFile();
                    OutputStream output = new FileOutputStream(file);
                    byte[] buffer = new byte[1024];

                    while (size > 0 && (bytesRead = clientData.read(buffer, 0, (int)Math.min(buffer.length, size))) != -1)
                    {

                        output.write(buffer, 0, bytesRead);
                        size -= bytesRead;
                    }
                    String nombreArchivo = "Cliente " + id + " recibio archivo "+ fileName;
                    log(nombreArchivo, logFile);
                    String archivoRecibido = "Cliente " + id + " solicita hash";
                    log(archivoRecibido, logFile);
                    escritorS.println(archivoRecibido);
                    String hash = lectorS.readLine();
                    log("Cliente " + id + " recibio hash " + hash, logFile);
                    file = new File(fileName);
                    MessageDigest md5Digest = MessageDigest.getInstance("MD5");
                    String shaChecksum = getFileChecksum(md5Digest, file);
                    if (hash.equals(shaChecksum)){
                        archivoRecepcion = true;
                    }else{
                        String archivoInCorrecto = "Cliente " + id + " recibio archivo incorrecto";
                        log(archivoInCorrecto, logFile);
                        escritorS.println(archivoInCorrecto);
                    }
                    output.close();
                }
                long end = System.currentTimeMillis();
                log("Cliente " + id + " - Tiempo de envio: " + ((end - start)/1000) + " s", logFile);
                String archivoCorrecto = "Cliente " + id + " recibo archivo correcto";
                log(archivoCorrecto, logFile);
                escritorS.println(archivoCorrecto);
                String conexionF = lectorS.readLine();
                log(conexionF + " Cliente " + id, logFile);
                //TO-DO log
            }
            escritorS.close();
            lectorS.close();
            in.close();
            clientData.close();
            socket.close();
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            e.printStackTrace();;
        }
    }

}
