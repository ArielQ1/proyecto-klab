package com.example.sonidosdeltacto

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import android.Manifest
import com.example.sonidosdeltacto.databinding.ActivityMainBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import okio.ByteString
import java.net.HttpURLConnection
import java.net.URL
import java.util.Locale

class MainActivity : AppCompatActivity(), TextToSpeech.OnInitListener {
    private lateinit var binding: ActivityMainBinding
    private val esp32Ip = "http://192.168.4.1/"
    private var webSocket: WebSocket? = null
    private val client = OkHttpClient()
    private var isWebSocketConnected = false
    private var textToSpeech: TextToSpeech? = null
    private var speechRecognizer: SpeechRecognizer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        textToSpeech = TextToSpeech(this, this)

        verificarConexionRed()

        //solicitar permiso de audio
        requestAudioPermission()
        // Inicializar el reconocedor de voz
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this)

        binding.buttonEscucharMensaje.setOnClickListener {
            iniciarReconocimientoVoz()
        }

        binding.buttonEnviarMensaje.setOnClickListener {
            val mensaje = binding.editTextMensaje.text.toString()
            Log.v("SonidosDelTacto", "Mensaje enviado: $mensaje")
            enviarComando("mensaje?texto=$mensaje")
            binding.editTextMensaje.text.clear()
        }

        binding.buttonConectar.setOnClickListener {
            if (!isWebSocketConnected) {
                configurarWebSocket() // Conectar WebSocket
            } else {
                Log.v("SonidosDelTacto", "WebSocket ya conectado")
            }
        }

        binding.buttonDesconectar.setOnClickListener {
            if (isWebSocketConnected) {
                webSocket?.close(1000, "Desconexión solicitada")
                isWebSocketConnected = false
                Log.v("SonidosDelTacto", "Desconectado de WebSocket")
                actualizarEstadoConexion(false)
            }
        }

        binding.buttonLimpiar.setOnClickListener {
            binding.textViewEscritura.text = ""
        }

        binding.buttonReproducir.setOnClickListener {
            val text = binding.textViewEscritura.text.toString()
            speakOut(text)
        }
    }

    private fun requestAudioPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.RECORD_AUDIO), 1)
        }
    }

    private fun iniciarReconocimientoVoz(){
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
            putExtra(RecognizerIntent.EXTRA_PREFER_OFFLINE, true) // Preferir modo sin conexión
        }
        speechRecognizer?.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {
                Log.d("SonidosDelTacto", "Listo para escuchar")

            }
            override fun onBeginningOfSpeech() {
                Log.d("SonidosDelTacto", "Empieza a hablar")
            }
            override fun onRmsChanged(rmsdB: Float) {}
            override fun onBufferReceived(buffer: ByteArray){}
            override fun onEndOfSpeech(){
                Log.d("SonidosDelTacto", "Finaliza la grabación")
            }
            override fun onError(error: Int) {
                Log.e("SonidosDelTacto", "Error de reconocimiento de voz: $error")
            }
            override fun onResults(results: Bundle?) {
                val palabras = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                if(!palabras.isNullOrEmpty()){
                    val texto = palabras[0]
                    Log.d("SonidosDelTacto", "Texto reconocido: $texto")
                    binding.editTextMensaje.setText(texto)
                }
            }
            override fun onPartialResults(partialResults: Bundle?) {}
            override fun onEvent(eventType: Int, params: Bundle?) {}
        })
        speechRecognizer?.startListening(intent)
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            // Configura el idioma a Español (ajusta según necesidad)
            val result = textToSpeech?.setLanguage(Locale("es", "ES"))

            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e("TTS", "Idioma no soportado")
            }
        } else {
            Log.e("TTS", "Inicialización fallida")
        }
    }

    private fun speakOut(text: String) {
        textToSpeech?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "")
    }


    private fun configurarWebSocket() {
        val request = Request.Builder().url("ws://192.168.4.1/ws").build()
        webSocket = client.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: okhttp3.Response) {
                isWebSocketConnected = true
                runOnUiThread { actualizarEstadoConexion(true) }
                Log.d("SonidosDelTacto", "WebSocket abierto")
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                Log.d("SonidosDelTacto", "Mensaje recibido: $text")

                runOnUiThread {
                    val textoActual = binding.textViewEscritura.text.toString()
                    binding.textViewEscritura.text = textoActual + text
                }
            }

            override fun onMessage(webSocket: WebSocket, bytes: ByteString) {
                Log.d("SonidosDelTacto", "Mensaje recibido (bytes): $bytes")
            }

            override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                webSocket.close(1000, null)
                isWebSocketConnected = false
                Log.d("SonidosDelTacto", "WebSocket cerrándose: $code / $reason")
                runOnUiThread { actualizarEstadoConexion(false) }
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: okhttp3.Response?) {
                isWebSocketConnected = false
                runOnUiThread { actualizarEstadoConexion(false) }
                Log.d("SonidosDelTacto", "Error en WebSocket", t)
            }
        })
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
                    actualizarEstadoConexion(conectado)
                }
                connection.disconnect()
            } catch (e: Exception) {
                Log.e("ConnectionError", "Error: ${e.message}")
                e.printStackTrace()
                runOnUiThread {
                    actualizarEstadoConexion(false)
                }
            }
        }
    }

    private fun actualizarEstadoConexion(conectado: Boolean) {
        binding.tvStatus.text = if (conectado) "Estado de conexión: Conectado a ESP32" else "Estado de conexión: No conectado"
        val color = if (conectado) R.color.green else R.color.red
        binding.tvStatus.setTextColor(ContextCompat.getColor(this@MainActivity, color))
    }

    private fun enviarComando(endpoint: String) {
        if (isWebSocketConnected) {
            webSocket?.send(endpoint)
            Log.v("SonidosDelTacto", "Comando enviado: $endpoint")
        } else {
            Log.v("SonidosDelTacto", "No se pudo enviar el comando. WebSocket no conectado.")
        }
    }


    override fun onDestroy() {
        textToSpeech?.stop()
        textToSpeech?.shutdown()
        speechRecognizer?.destroy()
        super.onDestroy()
        if (isWebSocketConnected) {
            webSocket?.close(1000, null)
        }
    }
}
