import java.io.File;
import java.util.Date;
import java.util.Scanner;

public class AppCliente {
    public static void main(String[] args) throws Exception {
        Integer n;
        Scanner scanner = new Scanner(System.in);
        System.out.print("Ingrese el número de clientes: ");
        n = scanner.nextInt();
        Date now = new Date();
        String logFileName = String.format("%tY-%tm-%td-%tH-%tM-%tS-log.txt", now, now, now, now, now, now);
        File logFile = new File("logs\\" + logFileName);
        Cliente clientes[] = new Cliente[n];
        for (int i = 1; i <= n; i++) {
            clientes[i] = new Cliente(i,n, logFile);
            clientes[i].start();
        }
        scanner.close();
    }
}
