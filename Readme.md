# Ejecución de las Apps
El repositorio cuenta con dos proyectos en Java, uno para servidor y uno para cliente. 
### Aplicación servidor
Para ejecutar la aplicación de servidor se requiere una máquina con javac y java para realizar la compilación y ejecución del código, una vez se tenga el repositorio en la máquina se debe compilar usando _javac Lab3-1/AppServer/src/AppServer.java_ y luego ejecutar con _java Lab3-1/AppServer/src/AppServer_, una vez hecho esto en la terminal se solicitará el tamaño del archivo a transferir con las opciones:<br>
  (0) Archivo de 100MB<br>
  (1) Archivo de 250MB **Este archivo no está en el repositorio por lo que debe ser descargado del drive donde están las capturas de wireshark**<br>
Luego se solicitará el puerto pero para efectos prácticos se debe insertar el puerto 5555 para que este sea coherente con el cliente.<br>
Finalmente se debe indicar la cantidad de clientes que se van a conectar para generar esta cantidad de delegados.<br>
Una vez las comunicaciones se terminan el programa terminará de ejecutar y dejará el Log en la carpeta _Lab3-1/AppServer/logs_ con la fecha de la ejecución como nombre.

### Aplicación cliente
Para ejecutar la aplicación de cliente se requiere una máquina con javac y java para realizar la compilación y ejecución del código, una vez se tenga el repositorio en la máquina se debe compilar usando _javac Lab3-1/AppCliente/src/AppCliente.java_ y luego ejecutar con _java Lab3-1/AppCliente/src/AppCliente_, una vez hecho esto en la terminal se solicitará la cantidad de clientes que se conectarán al servidor. <br>
**Importante: En la aplicación de cliente ya se toma por defecto el purto 5555 y la dirección del host está establecida como 192.168.20.32, en caso de ser necesario esta debe ser cambiada por la de la máquina que ejecute el servidor durante las pruebas**<br>
Esta aplicación también termina la ejecución una vez se terminan las comunicaciones y deja el log de la prueba en la carpeta _Lab3-1/AppCliente/logs_. además deja los archivos recibidos en la carperta _Lab3-1/Appcliente/ArchivosRecibidos_
