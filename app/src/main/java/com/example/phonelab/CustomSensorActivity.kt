package com.example.phonelab

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class CustomSensorActivity : AppCompatActivity(), SensorEventListener {

    private lateinit var sensorManager: SensorManager
    private var lightSensor: Sensor? = null

    private lateinit var tvAvailableSensors: TextView
    private lateinit var tvImplementationStatus: TextView
    private lateinit var tvLightValue: TextView
    private lateinit var tvLightLevel: TextView
    private lateinit var tvLightRecommendation: TextView
    private lateinit var tvLightSensorName: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_custom_sensor)

        tvAvailableSensors = findViewById(R.id.tvAvailableSensors)
        tvImplementationStatus = findViewById(R.id.tvImplementationStatus)
        tvLightValue = findViewById(R.id.tvLightValue)
        tvLightLevel = findViewById(R.id.tvLightLevel)
        tvLightRecommendation = findViewById(R.id.tvLightRecommendation)
        tvLightSensorName = findViewById(R.id.tvLightSensorName)

        sensorManager = getSystemService(SensorManager::class.java)

        // Conservar la lista de sensores de la plantilla original
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

        // RETO S1: Obtener el sensor de iluminación ambiental
        lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT)

        if (lightSensor == null) {
            tvImplementationStatus.text = "Sensor de luz no disponible"
            tvLightSensorName.text = "No se detectó hardware compatible."
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
    }

    override fun onResume() {
        super.onResume()

        // Registrar el sensor mientras la pantalla está activa
        lightSensor?.let { sensor ->
            val registered = sensorManager.registerListener(
                this,
                sensor,
                SensorManager.SENSOR_DELAY_UI
            )

            if (!registered) {
                tvLightLevel.text = "Estado: No disponible"
                tvLightRecommendation.text =
                    "No se pudo iniciar la lectura del sensor."
            }
        }
    }

    override fun onPause() {
        super.onPause()

        // Liberar el sensor para evitar consumo innecesario
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type != Sensor.TYPE_LIGHT) return

        // RETO S1: Obtener la iluminación ambiental en lux
        val lux = event.values[0]

        tvLightValue.text = "Iluminación: %.2f lx".format(lux)

        // Interpretar la lectura mediante umbrales
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
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // No se requiere una acción adicional para esta práctica
    }
}
