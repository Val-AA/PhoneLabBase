# PhoneLab – Sensores y Periféricos del Celular

## Descripción del proyecto

PhoneLab es una aplicación Android desarrollada en **Kotlin y XML**, cuyo objetivo es utilizar los sensores y periféricos del teléfono para obtener información del entorno y ejecutar acciones mediante el hardware del dispositivo.

El proyecto parte del repositorio original [PhoneLabBase](https://github.com/MoisesT90/PhoneLabBase), al cual se le agregaron nuevas funcionalidades y una integración entre periféricos.

## Funcionalidades implementadas

### 1. Acelerómetro
- Lectura de aceleración en los ejes X, Y y Z.
- Cálculo y registro de la magnitud máxima.
- Clasificación del movimiento y orientación del dispositivo.
- Contador de movimientos fuertes, evitando múltiples registros durante un mismo evento continuo.

### 2. Linterna
- Encendido y apagado manual del flash.
- Modo intermitente con intervalos de 500 milisegundos.
- Control para iniciar y detener el parpadeo.
- Apagado automático al abandonar la pantalla.

### 3. Vibración
- Conservación de los patrones corto, largo y doble.
- Implementación del patrón personalizado **Triple pulso progresivo**.
- Secuencia de vibraciones de 150, 250 y 400 milisegundos, con pausas de 150 milisegundos.

### 4. Sensor de luz ambiental
- Lectura de iluminación mediante `Sensor.TYPE_LIGHT`.
- Visualización de valores en lux (lx).
- Clasificación del ambiente como oscuro, iluminación media o muy iluminado.
- Actualización de las lecturas en tiempo real.

### 5. Integración de periféricos
Se implementó un modo de iluminación automática que combina el sensor de luz con el flash del dispositivo.

- **Menos de 50 lux:** encender la linterna.
- **Entre 50 y menos de 100 lux:** mantener el estado anterior.
- **100 lux o más:** apagar la linterna.

Se incluyen un botón de activación y desactivación, verificación de lecturas consecutivas y un tiempo de estabilización para reducir cambios constantes.

## Tecnologías utilizadas

- Android Studio
- Kotlin
- XML
- Android SDK
- SensorManager y SensorEventListener
- CameraManager
- Vibrator y VibrationEffect
- Handler y Runnable
- Git y GitHub

## Dispositivo de prueba

**Dispositivo:** Teléfono Xiaomi con sistema Android.

**Hardware utilizado:**
- Acelerómetro
- Flash LED
- Motor de vibración
- Sensor de iluminación ambiental (`tsl2522_als`)

## Pruebas realizadas

Se comprobó en un dispositivo físico el funcionamiento de:

- Registro de magnitud máxima y detección de movimientos fuertes.
- Encendido manual e intermitente de la linterna.
- Ejecución de los cuatro patrones de vibración.
- Lectura y clasificación de la iluminación ambiental.

La integración automática entre sensor de luz y linterna está implementada; su comportamiento debe verificarse en diferentes condiciones de iluminación.

## Limitaciones

- Algunas funciones dependen de la disponibilidad de sensores y periféricos del dispositivo.
- Los umbrales de iluminación son orientativos.
- La luz del propio flash puede influir en las lecturas del sensor ambiental.
- El modo automático funciona mientras la pantalla correspondiente está activa.

## Control de versiones

El desarrollo se realizó utilizando Git y GitHub, registrando mediante commits los avances de cada módulo.

Se utilizó la rama `desarrollo` para implementar los retos y posteriormente integrar las modificaciones en la rama principal `master` mediante un Pull Request.

## Conclusión

Esta actividad permitió aplicar el uso de sensores y periféricos de Android, interpretar información del entorno y desarrollar funciones automáticas mediante la integración de hardware. También permitió reforzar el trabajo con Kotlin, XML, el ciclo de vida de las Activities y el control de versiones con GitHub.

---

**Repositorio original:** https://github.com/MoisesT90/PhoneLabBase

**Repositorio desarrollado:** https://github.com/Val-AA/PhoneLabBase
