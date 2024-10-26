package com.example.sonidosdeltacto

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.example.sonidosdeltacto.databinding.ActivityMainBinding
import java.net.HttpURLConnection
import java.net.URL

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val esp32Ip = "http://192.168.100.50/"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        verificarConexionRed()

        binding.buttonGrados0.setOnClickListener {
            enviarComando("girar/0")

        }
        binding.buttonGrados90.setOnClickListener {
            enviarComando("girar/90")
        }
        binding.buttonGrados180.setOnClickListener {
            enviarComando("girar/180")
        }
        binding.buttonEnviarMensaje.setOnClickListener {
            val mensaje = binding.editTextMensaje.text.toString()
            Log.v("SonidosDelTacto", "Mensaje enviado: $mensaje")
            enviarComando("mensaje?texto=$mensaje")
            binding.editTextMensaje.text.clear()
        }


        binding.buttonReintentar.setOnClickListener {
            verificarConexionRed()
        }
    }

    private fun verificarConexionRed() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val url = URL(esp32Ip)
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "GET"
                connection.connectTimeout = 2000
                connection.connect()

                val responseCode = connection.responseCode
                val conectado = responseCode == HttpURLConnection.HTTP_OK

                runOnUiThread {
                    if (conectado) {
                        binding.tvStatus.text = "Estado de conexión: Conectado a ESP32"
                        binding.tvStatus.setTextColor(ContextCompat.getColor(this@MainActivity, R.color.green))
                    } else {
                        binding.tvStatus.text = "Estado de conexión: No se pudo conectar al ESP32"
                        binding.tvStatus.setTextColor(ContextCompat.getColor(this@MainActivity, R.color.red))
                    }
                }
                connection.disconnect()
            } catch (e: Exception) {
                Log.e("ConnectionError", "Error: ${e.message}")
                e.printStackTrace()
                runOnUiThread {
                    binding.tvStatus.text = "Estado de conexión: No se pudo conectar al ESP32"
                    binding.tvStatus.setTextColor(ContextCompat.getColor(this@MainActivity, R.color.red))
                }
            }
        }
    }

    private fun enviarComando(endpoint: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val url = URL("$esp32Ip$endpoint")
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "GET"
                val responseCode = connection.responseCode
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    println("Comando enviado correctamente")
                } else {
                    println("Error al enviar comando: Código $responseCode")
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
