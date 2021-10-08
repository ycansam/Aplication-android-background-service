# Androind Servicio de Beacons

Aplicacion android que detecta beacons de cualquier dispositivo bluetooth y de alguno en especifico para posteriormente enviar los datos a una peticion http.

## Propiedades

- Detecta los beacons de cualquier dispositivo
- Detecta beacons de un dispositivo especifico
- Envia los datos de los beacons a una peticion HTTP en formato JSON

## Tecnologia
- Java (Android Studio)

## Configuracion
### 1. Deteccion dispositivo especifico
1.  Abrir el proyecto en Android Studio
2.  Buscar botonBuscarNuestroDispositivoBTLEPulsado() en MainActivity
3.  Cambiar EPSG-GTI-YERAY3A por el UUID del dispositivo a buscar.
```java
    public void botonBuscarNuestroDispositivoBTLEPulsado( View v ) {
        Log.d(ETIQUETA_LOG, " boton nuestro dispositivo BTLE Pulsado" );
        this.buscarEsteDispositivoBTLE( Utilidades.stringToUUID( "EPSG-GTI-YERAY3A" ) );
    } // ()
```
### 2. Direccion HTTP y envio de datos
1.  Abrir el proyecto en Android Studio
2.  Buscar boton_enviar_pulsado() en MainActivity
3.  Modificar direccion HTTP (**tiene que ser metodo POST**)
```java
    String url = "http://81.202.37.9:3050/anyadirSensor"; // cambiar la url de envio de datos
```
4. Datos a enviar a la peticion HTTP 
```java
    // Añadir o quitar propiedades
        JSONObject postData = new JSONObject(); 
        postData.put("id_sensor", "2344");
        postData.put("nombre", "Sensor 2344");
        postData.put("temperatura", "0");
        postData.put("dioxido_carbono", "0");
```
## License

MIT

