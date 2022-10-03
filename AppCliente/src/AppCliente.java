import java.net.Socket;
import java.util.Scanner;

public class AppCliente {
    public static void main(String[] args) throws Exception {
        Integer n;
        Scanner scanner = new Scanner(System.in);
        System.out.print("Ingrese el número de clientes: ");
        n = scanner.nextInt();

        Cliente clientes[] = new Cliente[n];
        for (int i = 0; i < n; i++) {
            clientes[i] = new Cliente(i,n);
            clientes[i].start();
        }
        scanner.close();
    }
}
