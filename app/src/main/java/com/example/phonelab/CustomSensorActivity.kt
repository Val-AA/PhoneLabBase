package com.example.phonelab

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.os.Bundle
import android.os.SystemClock
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton

class CustomSensorActivity : AppCompatActivity(), SensorEventListener {

    private lateinit var sensorManager: SensorManager
    private var lightSensor: Sensor? = null

    private lateinit var cameraManager: CameraManager
    private var flashCameraId: String? = null

    private lateinit var tvAvailableSensors: TextView
    private lateinit var tvImplementationStatus: TextView
    private lateinit var tvLightValue: TextView
    private lateinit var tvLightLevel: TextView
    private lateinit var tvLightRecommendation: TextView
    private lateinit var tvLightSensorName: TextView

    // Integración del sensor de luz y la linterna
    private lateinit var btnAutoFlash: MaterialButton
    private lateinit var tvAutoFlashStatus: TextView

    private var automaticMode = false
    private var isFlashOn = false

    // Umbrales de iluminación (lux)
    private val darkThreshold = 50f
    private val brightThreshold = 100f

    // Confirmar tres lecturas consecutivas antes de cambiar
    private var darkReadings = 0
    private var brightReadings = 0
    private val requiredReadings = 3

    // Esperar 3 segundos después de cada cambio
    private val stabilizationTime = 3000L
    private var lastFlashChange = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_custom_sensor)

        tvAvailableSensors = findViewById(R.id.tvAvailableSensors)
        tvImplementationStatus = findViewById(R.id.tvImplementationStatus)
        tvLightValue = findViewById(R.id.tvLightValue)
        tvLightLevel = findViewById(R.id.tvLightLevel)
        tvLightRecommendation = findViewById(R.id.tvLightRecommendation)
        tvLightSensorName = findViewById(R.id.tvLightSensorName)

        btnAutoFlash = findViewById(R.id.btnAutoFlash)
        tvAutoFlashStatus = findViewById(R.id.tvAutoFlashStatus)

        sensorManager = getSystemService(SensorManager::class.java)
        cameraManager = getSystemService(CameraManager::class.java)

        // Conservar la lista original de sensores
        val availableSensors = sensorManager
            .getSensorList(Sensor.TYPE_ALL)
            .sortedBy { it.name }

        tvAvailableSensors.text = if (availableSensors.isEmpty()) {
            "No se detectaron sensores mediante SensorManager."
        } else {
            availableSensors.joinToString("\n") { sensor ->
                "• ${sensor.name} | tipo ${sensor.type}"
            }
        }

        // RETO S1: Sensor de luz ambiental
        lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT)

        if (lightSensor == null) {
            tvImplementationStatus.text =
                "Sensor de luz no disponible"

            tvLightSensorName.text =
                "No se detectó hardware compatible."

            tvLightValue.text = "Lectura no disponible"
            tvLightLevel.text = "Estado: No disponible"
            tvLightRecommendation.text =
                "Este dispositivo no cuenta con un sensor de luz accesible."
        } else {
            tvImplementationStatus.text =
                "Reto S1: Sensor de luz implementado"

            tvLightSensorName.text =
                "Sensor detectado: ${lightSensor?.name}"

            tvLightValue.text = "Esperando lectura..."
            tvLightLevel.text = "Estado: Analizando iluminación"
            tvLightRecommendation.text =
                "Cambia la iluminación alrededor del celular para probarlo."
        }

        // Buscar una cámara con flash disponible
        flashCameraId = findCameraWithFlash()

        if (lightSensor == null || flashCameraId == null) {
            btnAutoFlash.isEnabled = false
            tvAutoFlashStatus.text =
                "Modo automático no disponible: falta el sensor de luz o el flash."
        } else {
            tvAutoFlashStatus.text = "Modo automático desactivado"
        }

        btnAutoFlash.setOnClickListener {
            if (automaticMode) {
                stopAutomaticMode()
            } else {
                startAutomaticMode()
            }
        }
    }

    // Buscar hardware con unidad de flash
    private fun findCameraWithFlash(): String? {
        return try {
            cameraManager.cameraIdList.firstOrNull { cameraId ->
                val characteristics =
                    cameraManager.getCameraCharacteristics(cameraId)

                characteristics.get(
                    CameraCharacteristics.FLASH_INFO_AVAILABLE
                ) == true
            }
        } catch (e: Exception) {
            null
        }
    }

    override fun onResume() {
        super.onResume()

        lightSensor?.let { sensor ->
            val registered = sensorManager.registerListener(
                this,
                sensor,
                SensorManager.SENSOR_DELAY_UI
            )

            if (!registered) {
                btnAutoFlash.isEnabled = false
                tvLightLevel.text = "Estado: No disponible"
                tvLightRecommendation.text =
                    "No se pudo iniciar la lectura del sensor."
            } else {
                btnAutoFlash.isEnabled = flashCameraId != null
            }
        }
    }

    override fun onPause() {
        // Liberar sensor y apagar la linterna
        stopAutomaticMode()
        sensorManager.unregisterListener(this)
        super.onPause()
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type != Sensor.TYPE_LIGHT) return

        val lux = event.values[0]

        // Mostrar lectura real del sensor
        tvLightValue.text = "Iluminación: %.2f lx".format(lux)

        // RETO S1: Interpretar la iluminación
        val lightLevel = when {
            lux < 50f -> "Ambiente oscuro"
            lux < 500f -> "Iluminación media"
            else -> "Ambiente muy iluminado"
        }

        val recommendation = when {
            lux < 50f ->
                "Se detecta poca iluminación. Puede ser necesario encender una luz."

            lux < 500f ->
                "Se detecta iluminación intermedia en el ambiente."

            else ->
                "Se detecta un ambiente muy iluminado."
        }

        tvLightLevel.text = "Estado: $lightLevel"
        tvLightRecommendation.text = recommendation

        // INTEGRACIÓN: Control automático del flash
        if (automaticMode) {
            evaluateLighting(lux)
        }
    }

    private fun startAutomaticMode() {
        if (lightSensor == null || flashCameraId == null) return

        automaticMode = true
        darkReadings = 0
        brightReadings = 0
        lastFlashChange = 0L

        btnAutoFlash.text = "Desactivar modo automático"
        tvAutoFlashStatus.text =
            "Modo automático activado. Analizando iluminación..."
    }

    private fun stopAutomaticMode() {
        automaticMode = false
        darkReadings = 0
        brightReadings = 0

        btnAutoFlash.text = "Activar modo automático"

        // Apagar la linterna al desactivar o salir
        if (isFlashOn) {
            setFlash(false)
        }

        tvAutoFlashStatus.text = "Modo automático desactivado"
    }

    // Integración: decidir el estado a partir del sensor de luz
    private fun evaluateLighting(lux: Float) {

        // Esperar estabilización después de cambiar el flash
        val currentTime = SystemClock.elapsedRealtime()

        if (currentTime - lastFlashChange < stabilizationTime) {
            return
        }

        when {
            lux < darkThreshold -> {
                darkReadings++
                brightReadings = 0
            }

            lux >= brightThreshold -> {
                brightReadings++
                darkReadings = 0
            }

            else -> {
                // Zona intermedia: conservar el estado actual
                darkReadings = 0
                brightReadings = 0
            }
        }

        if (!isFlashOn && darkReadings >= requiredReadings) {
            if (setFlash(true)) {
                tvAutoFlashStatus.text =
                    "Ambiente oscuro: linterna encendida"
            } else {
                stopAutomaticMode()
                tvAutoFlashStatus.text =
                    "Modo detenido: no fue posible encender el flash."
            }
            darkReadings = 0
            brightReadings = 0
        }

        if (isFlashOn && brightReadings >= requiredReadings) {
            if (setFlash(false)) {
                tvAutoFlashStatus.text =
                    "Iluminación suficiente: linterna apagada"
            } else {
                stopAutomaticMode()
                tvAutoFlashStatus.text =
                    "Modo detenido: no fue posible apagar el flash."
            }
            darkReadings = 0
            brightReadings = 0
        }
    }

    // Control físico de la linterna
    private fun setFlash(enabled: Boolean): Boolean {
        val cameraId = flashCameraId ?: return false

        return try {
            cameraManager.setTorchMode(cameraId, enabled)
            isFlashOn = enabled
            lastFlashChange = SystemClock.elapsedRealtime()
            true

        } catch (e: CameraAccessException) {
            tvAutoFlashStatus.text =
                "Error al acceder a la cámara: ${e.reason}"
            false

        } catch (e: IllegalArgumentException) {
            tvAutoFlashStatus.text =
                "El flash seleccionado no está disponible."
            false

        } catch (e: SecurityException) {
            tvAutoFlashStatus.text =
                "No se cuenta con autorización para usar el flash."
            false
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // No se requiere una acción adicional
    }
}
