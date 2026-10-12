package com.example.phonelab

import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.widget.Spinner
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton

class VibrationActivity : AppCompatActivity() {

    private lateinit var vibrator: Vibrator
    private lateinit var spinnerPatterns: Spinner
    private lateinit var tvVibrationInfo: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_vibration)

        vibrator = getSystemService(Vibrator::class.java)
        spinnerPatterns = findViewById(R.id.spinnerPatterns)
        tvVibrationInfo = findViewById(R.id.tvVibrationInfo)

        val btnVibrate =
            findViewById<MaterialButton>(R.id.btnVibrate)

        // Verificar si el dispositivo tiene vibrador
        if (!vibrator.hasVibrator()) {
            btnVibrate.isEnabled = false
            tvVibrationInfo.text =
                getString(R.string.vibrator_unavailable)
        }

        btnVibrate.setOnClickListener {
            playSelectedPattern()
        }
    }

    private fun playSelectedPattern() {

        // Detener cualquier vibración anterior
        vibrator.cancel()

        when (spinnerPatterns.selectedItemPosition) {

            // Patrón 1: Vibración corta
            0 -> {
                vibrator.vibrate(
                    VibrationEffect.createOneShot(
                        150,
                        VibrationEffect.DEFAULT_AMPLITUDE
                    )
                )

                tvVibrationInfo.text =
                    "Patrón ejecutado: vibración corta (150 ms)"
            }

            // Patrón 2: Vibración larga
            1 -> {
                vibrator.vibrate(
                    VibrationEffect.createOneShot(
                        700,
                        VibrationEffect.DEFAULT_AMPLITUDE
                    )
                )

                tvVibrationInfo.text =
                    "Patrón ejecutado: vibración larga (700 ms)"
            }

            // Patrón 3: Doble vibración
            2 -> {
                // Inicio inmediato, pulso de 180 ms,
                // pausa de 120 ms y segundo pulso de 180 ms.
                val pattern = longArrayOf(
                    0, 180, 120, 180
                )

                vibrator.vibrate(
                    VibrationEffect.createWaveform(pattern, -1)
                )

                tvVibrationInfo.text =
                    "Patrón ejecutado: doble vibración"
            }

            // RETO V1: Triple pulso progresivo
            3 -> {

                // Secuencia de tiempos en milisegundos:
                // 0 ms: iniciar inmediatamente.
                // 150 ms: primera vibración corta.
                // 150 ms: pausa.
                // 250 ms: segunda vibración media.
                // 150 ms: pausa.
                // 400 ms: tercera vibración larga.
                //
                // El patrón tiene tres pulsos progresivos.
                // No se repite automáticamente (-1).

                val customPattern = longArrayOf(
                    0, 150, 150, 250, 150, 400
                )

                vibrator.vibrate(
                    VibrationEffect.createWaveform(
                        customPattern,
                        -1
                    )
                )

                tvVibrationInfo.text =
                    "Patrón ejecutado: triple pulso progresivo\n" +
                            "Vibraciones: 150 ms, 250 ms y 400 ms\n" +
                            "Pausas: 150 ms entre pulsos"
            }
        }
    }

    override fun onStop() {
        super.onStop()

        // Detener cualquier vibración al salir
        vibrator.cancel()
    }
}
