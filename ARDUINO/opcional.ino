#include <WiFi.h>
#include <ESP32Servo.h>
/* Pin de los servos y creacion de los servos */
int pinServos[3][2] = {
    {12, 14},
    {27, 26},
    {25, 33}
};
Servo servos[3][2];
/* -------------- */

boolean Estado = false;
int anguloActual = 0;  // Angulo inicial de los servos

const uint32_t TiempoEsperaWifi = 5000;
unsigned long TiempoActual = 0;
unsigned long TiempoAnterior = 0;
const long TiempoCancelacion = 500;

WiFiServer servidor(80);

IPAddress ip_local(192, 168, 99, 50);//192.168.99.31
IPAddress gateway(192, 168, 99, 145);//192.168.99.145
IPAddress subnet(255, 255, 255, 0);

void setup() {
  Serial.begin(115200);
  Serial.println("\nIniciando multi Wifi");
  
  /* Conectar los servos al pin y ponerlos en posicion inicial */
  for (int i = 0; i < 3; i++) {
    for (int j = 0; j < 2; j++) {
      servos[i][j].attach(pinServos[i][j]);
      servos[i][j].write(anguloActual);
    }
  }

  if (!WiFi.config(ip_local, gateway, subnet)) {
    Serial.println("Error en configuracion");
  }

  WiFi.mode(WIFI_STA);
  Serial.print("Conectando a Wifi ..");
  WiFi.begin("Galaxy A52 - Dorian", "txqb3122");
  while (WiFi.status() != WL_CONNECTED) {
    Serial.print(".");
    delay(500);
  }

  Serial.println(".. Conectado");
  Serial.print("SSID:");
  Serial.print(WiFi.SSID());
  Serial.print(" ID:");
  Serial.println(WiFi.localIP());

  servidor.begin();
}

void loop() {
  WiFiClient cliente = servidor.available();

  if (cliente) {
    Serial.println("Nuevo Cliente");
    TiempoActual = millis();
    TiempoAnterior = TiempoActual;
    String LineaActual = "";

    while (cliente.connected() && TiempoActual - TiempoAnterior <= TiempoCancelacion) {
      if (cliente.available()) {
        TiempoActual = millis();
        char Letra = cliente.read();
        if (Letra == '\n') {
          if (LineaActual.length() == 0) {
            ResponderCliente(cliente);
            break;
          } else {
            Serial.println(LineaActual);
            VerificarMensaje(LineaActual);
            ProcesarMensaje(LineaActual);
            LineaActual = "";
          }
        } else if (Letra != '\r') {
          LineaActual += Letra;
        }
      }
    }
    cliente.stop();
    Serial.println("Cliente Desconectado");
    Serial.println();
  }
}

void VerificarMensaje(String Mensaje) {
  if (Mensaje.indexOf("GET /girar/90") >= 0) {
    Serial.println("Girar servo a 90 grados");
    anguloActual = 90;
    servos[0][0].write(anguloActual);
  } else if (Mensaje.indexOf("GET /girar/180") >= 0) {
    Serial.println("Girar servo a 180 grados");
    anguloActual = 180;
    servos[0][0].write(anguloActual);
  } else if (Mensaje.indexOf("GET /girar/0") >= 0) {
    Serial.println("Girar servo a 0 grados");
    anguloActual = 0;
    servos[0][0].write(anguloActual);
  }
}

String URLDecode(String texto) {
  String decodedText = "";
  char c;
  for (int i = 0; i < texto.length(); i++) {
    c = texto[i];
    if (c == '+') {
      decodedText += ' ';
    } else if (c == '%') {
      String hex = texto.substring(i + 1, i + 3);
      c = (char) strtol(hex.c_str(), NULL, 16);
      decodedText += c;
      i += 2;
    } else {
      decodedText += c;
    }
  }
  return decodedText;
}

void ProcesarMensaje(String Mensaje) {
  if (Mensaje.indexOf("GET /mensaje?texto=") >= 0) {
    int posicionInicio = Mensaje.indexOf("texto=") + 6;
    int posicionFin = Mensaje.indexOf(" ", posicionInicio);
    String mensajeRecibido = Mensaje.substring(posicionInicio, posicionFin);
    mensajeRecibido = URLDecode(mensajeRecibido);
    
    Serial.println("Mensaje recibido: ");
    Serial.println(mensajeRecibido);
    /* Prototipo de la Funcion */
    void girarServos(int matriz[3][2], int filas);
    /* ALFABETO DE LECTURA EN MATRICES EN BRAILE*/
    int A[3][2] = {{1, 0}, {0, 0}, {0, 0}};
    int B[3][2] = {{1, 0}, {1, 0}, {0, 0}};
    int C[3][2] = {{1, 1}, {0, 0}, {0, 0}};
    int D[3][2] = {{1, 1}, {0, 1}, {0, 0}};
    int E[3][2] = {{1, 0}, {0, 1}, {0, 0}};
    int F[3][2] = {{1, 1}, {1, 0}, {0, 0}};
    int G[3][2] = {{1, 1}, {1, 1}, {0, 0}};
    int H[3][2] = {{1, 0}, {1, 1}, {0, 0}};
    int I[3][2] = {{0, 1}, {1, 0}, {0, 0}};
    int J[3][2] = {{0, 1}, {1, 1}, {0, 0}};
    int K[3][2] = {{1, 0}, {0, 0}, {1, 0}};
    int L[3][2] = {{1, 0}, {1, 0}, {1, 0}};
    int M[3][2] = {{1, 1}, {0, 0}, {1, 0}};
    int N[3][2] = {{1, 1}, {0, 1}, {1, 0}};
    int O[3][2] = {{1, 0}, {0, 1}, {1, 0}};
    int P[3][2] = {{1, 1}, {1, 0}, {1, 0}};
    int Q[3][2] = {{1, 1}, {1, 1}, {1, 0}};
    int R[3][2] = {{1, 0}, {1, 1}, {1, 0}};
    int S[3][2] = {{0, 1}, {1, 0}, {1, 0}};
    int T[3][2] = {{0, 1}, {1, 1}, {1, 0}};
    int U[3][2] = {{1, 0}, {0, 0}, {1, 1}};
    int V[3][2] = {{1, 0}, {1, 0}, {1, 1}};
    int W[3][2] = {{0, 1}, {1, 1}, {0, 1}};
    int X[3][2] = {{1, 1}, {0, 0}, {1, 1}};
    int Y[3][2] = {{1, 1}, {0, 1}, {1, 1}};
    int Z[3][2] = {{1, 0}, {0, 1}, {1, 1}};
    //array de matrices
    int (*matrices[26])[2] = {A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R, S, T, U, V, W, X, Y, Z};
    for (int i = 0; i < mensajeRecibido.length(); i++) {
      char caracter = toupper(mensajeRecibido[i]);
      //mensajeRecibido[i] = toupper(mensajeRecibido[i]);
      //Hacer que giren los servo
      int indice = caracter - 'A';
      girarServos(matrices[indice], 3);
      delay(500);
      resetearServos(matrices[indice], 3);
    }
  }
}
void girarServos(int (*matriz)[2], int filas){
  anguloActual = 180;
  for (int i = 0; i < filas; i++) {
    for (int j = 0; j < 2; j++) {
      if(matriz[i][j] != 0){
        servos[i][j].write(anguloActual);
      }
      Serial.print(matriz[i][j]);
      Serial.print("  ");
    }
    Serial.println();
  }
  Serial.println("------");
}

void resetearServos(int (*matriz)[2], int filas){
  anguloActual = 0;
  for (int i = 0; i < filas; i++) {
    for (int j = 0; j < 2; j++) {
      servos[i][j].write(anguloActual);
    }
  }
}

void ResponderCliente(WiFiClient& cliente) {
  cliente.print("HTTP/1.1 200 OK\r\nContent-Type: text/html\r\n\r\n");
  cliente.print("<html><head><title>Control de Servo</title></head><body>");
  cliente.print("<h2>Hola, cliente con IP: ");
  cliente.print(cliente.remoteIP());
  cliente.print("</h2>");
  cliente.print("<p>Posición actual del servo: <strong>");
  cliente.print(anguloActual);
  cliente.print(" grados</strong></p>");
  cliente.print("<p><a href='/girar/0'>Mover a 0°</a></p>");
  cliente.print("<p><a href='/girar/90'>Mover a 90°</a></p>");
  cliente.print("<p><a href='/girar/180'>Mover a 180°</a></p>");
  cliente.print("</body></html>");
}
