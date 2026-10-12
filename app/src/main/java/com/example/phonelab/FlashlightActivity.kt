
package com.example.phonelab

import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton

class FlashlightActivity : AppCompatActivity() {

    private lateinit var cameraManager: CameraManager
    private var flashCameraId: String? = null
    private var isFlashOn = false

    private lateinit var tvFlashStatus: TextView
    private lateinit var tvFlashMessage: TextView
    private lateinit var btnToggleFlash: MaterialButton
    private lateinit var btnIntermittent: MaterialButton

    // RETO L1: Control del modo intermitente
    private val handler = Handler(Looper.getMainLooper())
    private var isBlinking = false
    private val blinkInterval = 500L

    private val blinkRunnable = object : Runnable {
        override fun run() {
            if (!isBlinking) return

            // Alternar entre encendido y apagado
            val success = setFlash(!isFlashOn)

            if (!success) {
                stopBlinking()
                return
            }

            // Programar el siguiente cambio sin bloquear la interfaz
            if (isBlinking) {
                handler.postDelayed(this, blinkInterval)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_flashlight)

        tvFlashStatus = findViewById(R.id.tvFlashStatus)
        tvFlashMessage = findViewById(R.id.tvFlashMessage)
        btnToggleFlash = findViewById(R.id.btnToggleFlash)
        btnIntermittent = findViewById(R.id.btnIntermittent)

        cameraManager = getSystemService(CameraManager::class.java)
        flashCameraId = findCameraWithFlash()

        if (flashCameraId == null) {
            btnToggleFlash.isEnabled = false
            btnIntermittent.isEnabled = false
            tvFlashMessage.text =
                getString(R.string.flash_unavailable)
        }

        // Encendido y apagado manual
        btnToggleFlash.setOnClickListener {
            if (isBlinking) {
                stopBlinking()
            }
            setFlash(!isFlashOn)
        }

        // RETO L1: Iniciar o detener el parpadeo
        btnIntermittent.setOnClickListener {
            if (isBlinking) {
                stopBlinking()
            } else {
                startBlinking()
            }
        }
    }

    private fun findCameraWithFlash(): String? {
        return try {
            cameraManager.cameraIdList.firstOrNull { cameraId ->
                val characteristics =
                    cameraManager.getCameraCharacteristics(cameraId)

                characteristics.get(
                    CameraCharacteristics.FLASH_INFO_AVAILABLE
                ) == true
            }
        } catch (e: CameraAccessException) {
            null
        }
    }

    // Controlar el estado físico de la linterna
    private fun setFlash(enabled: Boolean): Boolean {
        val cameraId = flashCameraId ?: return false

        return try {
            cameraManager.setTorchMode(cameraId, enabled)
            isFlashOn = enabled

            tvFlashStatus.text = if (enabled) {
                getString(R.string.flash_on)
            } else {
                getString(R.string.flash_off)
            }

            tvFlashMessage.text = if (enabled) {
                "La linterna está activa."
            } else {
                "La linterna está apagada."
            }

            true

        } catch (e: CameraAccessException) {
            tvFlashMessage.text =
                "No fue posible acceder al flash: ${e.reason}"
            false

        } catch (e: IllegalArgumentException) {
            tvFlashMessage.text =
                "El flash seleccionado no está disponible."
            false
        }
    }

    // RETO L1: Iniciar modo intermitente
    private fun startBlinking() {
        if (flashCameraId == null || isBlinking) return

        isBlinking = true

        btnIntermittent.text = "Detener parpadeo"
        btnToggleFlash.isEnabled = false

        tvFlashMessage.text = "Modo intermitente activado."

        handler.post(blinkRunnable)
    }

    // RETO L1: Detener modo intermitente
    private fun stopBlinking() {
        isBlinking = false

        // Cancelar las ejecuciones pendientes
        handler.removeCallbacks(blinkRunnable)

        // Apagar la linterna
        if (isFlashOn) {
            setFlash(false)
        }

        btnIntermittent.text = "Iniciar parpadeo"
        btnToggleFlash.isEnabled = flashCameraId != null

        if (flashCameraId != null) {
            tvFlashMessage.text = "Modo intermitente detenido."
        }
    }

    override fun onStop() {
        super.onStop()

        // Detener el parpadeo al abandonar la pantalla
        stopBlinking()

        // Evitar dejar encendida la linterna
        if (isFlashOn) {
            setFlash(false)
        }
    }

    override fun onDestroy() {
        handler.removeCallbacks(blinkRunnable)
        super.onDestroy()
    }
}
