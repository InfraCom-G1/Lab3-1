import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
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

    byte[] buffer = new byte[1024];

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
            //Socket socket = new Socket(HOST, PORT);
            DatagramSocket socket = new DatagramSocket();
            //PrintWriter escritorS = new PrintWriter(socket.getOutputStream(), true);
            //BufferedReader lectorS = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            //InputStream in = socket.getInputStream();
            //DataInputStream clientData = new DataInputStream(in);
            InetAddress direccionServidor = InetAddress.getByName(HOST);
            int bytesRead;
            String mensajeInicio = "Cliente " + id + " intentando conectarse al servidor";
            log(mensajeInicio, logFile);
            buffer = mensajeInicio.getBytes();
 
            //Creo un datagrama
            DatagramPacket pregunta = new DatagramPacket(buffer, buffer.length, direccionServidor, PORT);
            socket.send(pregunta);
            //escritorS.println(mensajeInicio);
            DatagramPacket peticion = new DatagramPacket(buffer, buffer.length);
 
            //Recibo la respuesta
            socket.receive(peticion);
            String conexionE = new String(peticion.getData());
            if (conexionE.equals("Conexion establecida con cliente " + id)) {
                log("Cliente " + id + " establecio conexion con el servidor", logFile);
                String peticionArchivo = "Cliente " + id + " solicita tamanio del archivo";
                log(peticionArchivo, logFile);
                buffer = peticionArchivo.getBytes();
                DatagramPacket pa = new DatagramPacket(buffer, buffer.length, direccionServidor, PORT);
                socket.send(pa);
                //escritorS.println(peticionArchivo);
                DatagramPacket rpa = new DatagramPacket(buffer, buffer.length);
 
                //Recibo la respuesta
                socket.receive(rpa);
                String strTamanio=new String(rpa.getData());
                long size = Long.valueOf(strTamanio.split(" ")[5]);
                log("Cliente " + id + " recibio tamanio archivo " + size, logFile);
                String clienteListo = "Cliente " + id + " listo para recibir archivo";
                log(clienteListo, logFile);
                buffer = clienteListo.getBytes();
                DatagramPacket cListo = new DatagramPacket(buffer, buffer.length, direccionServidor, PORT);
                socket.send(cListo);
                //escritorS.println(clienteListo);
                Boolean archivoRecepcion = false;
                long start = System.currentTimeMillis();
                while (archivoRecepcion == false){
                    String fileName = "ArchivosRecibidos\\"+id+"-Prueba-"+totalConexiones+".bin" ;
                    File file = new File(fileName);
                    file.createNewFile();
                    FileOutputStream output = new FileOutputStream(file);
                    byte[] buffer = new byte[1024];
                    int i=0;

                    DatagramPacket dp=new DatagramPacket(buffer,buffer.length);
                    socket.receive(dp);
                    String pac =new String(dp.getData(),0,dp.getLength());
                    //System.out.println(pac);
                    pac.getBytes();
                    output.write(buffer);
                    output.flush();
                    output.close();

                    String nombreArchivo = "Cliente " + id + " recibio archivo "+ fileName;
                    log(nombreArchivo, logFile);
                }
                long end = System.currentTimeMillis();
                log("Cliente " + id + " - Tiempo de envio: " + ((end - start)/1000) + " s", logFile);
                String archivoCorrecto = "Cliente " + id + " recibo archivo correcto";
                log(archivoCorrecto, logFile);
                
                DatagramPacket finalC = new DatagramPacket(buffer, buffer.length);
                socket.receive(finalC);
                String conexionF = new String(finalC.getData());
                log(conexionF + " Cliente " + id, logFile);
                //TO-DO log
            }
            socket.close();
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            e.printStackTrace();;
        }
    }

}
