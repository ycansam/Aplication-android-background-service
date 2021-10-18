package com.candelyeray.bluetoothReceiver;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.annotation.TargetApi;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.UUID;
// --------------------------------------------------------------
//
// Yeray Candel Sampedro
// 2021 - 10 - 18
//
// --------------------------------------------------------------
public class MainActivity extends AppCompatActivity {

    private static final String ETIQUETA_LOG = ">>>>";

    // SERVICIOS -------------
    private Intent elIntentDelServicio = null;

    // BTLE ------------------
    private BluetoothAdapter.LeScanCallback  callbackLeScan = null;
    private static final int PERMISSION_REQUEST_FINE_LOCATION = 1;
    private static final int PERMISSION_REQUEST_BACKGROUND_LOCATION = 2;

    // PETICIONARIO REST -----
    private Button elBotonEnviar;
    private TextView elTexto;
    private TextView sensor_id;
    private TextView nombreSensor;
    private TextView dioxido_carbono;

    /**
     * @function botonArrancarServicioPulsado
     * Se encarga de arrancar el servicio de escuchar beacons en segundo plano.
     */
    public void botonArrancarServicioPulsado( View v ) {
        Log.d(ETIQUETA_LOG, " boton arrancar servicio Pulsado" );

        if ( this.elIntentDelServicio != null ) {
            // ya estaba arrancado
            return;
        }

        Log.d(ETIQUETA_LOG, " MainActivity.constructor : voy a arrancar el servicio");
        BluetoothAdapter.getDefaultAdapter().startLeScan( this.callbackLeScan );
        this.elIntentDelServicio = new Intent(this, ServicioEscucharBeacons.class);

        this.elIntentDelServicio.putExtra("tiempoDeEspera", (long) 5000);
        
        startService( this.elIntentDelServicio );

    } // ()

    /**
     * @function botonDetenerServicioPulsado
     * Se encarga de detener el servicio de escuchar beacons en segundo plano.
     */
    public void botonDetenerServicioPulsado( View v ) {

        if ( this.elIntentDelServicio == null ) {
            // no estaba arrancado
            return;
        }

        stopService( this.elIntentDelServicio );

        this.elIntentDelServicio = null;

        Log.d(ETIQUETA_LOG, " boton detener servicio Pulsado" );


    } // ()
    // --------------------------------------------------------------
    // FUNCIONES BTLE
    // --------------------------------------------------------------

    /**
     * Busca todos los dispositivos bluetooth activados que esten al alcance.
     */
    public void buscarTodosLosDispositivosBTLE() {
        this.callbackLeScan = new BluetoothAdapter.LeScanCallback() {
            @Override
            public void onLeScan(BluetoothDevice bluetoothDevice, int rssi, byte[] bytes) {

                //
                //  se ha encontrado un dispositivo
                //
                mostrarInformacionDispositivoBTLE( bluetoothDevice, rssi, bytes );

            } // onLeScan()
        }; // new LeScanCallback
        //
        //
        boolean resultado = BluetoothAdapter.getDefaultAdapter().startLeScan( this.callbackLeScan );

        // Log.d(ETIQUETA_LOG, " buscarTodosLosDispositivosBTL(): startLeScan(), resultado= " + resultado );
    } // ()

    /**
     * Muestra la informacion del dispositivo bluetooth cuando se detecta.
     *
     * @param BluetoothDevice dispositivo
     * @param rssi rssi del dispositivo
     * @param byte[] array de bytes que contienen la informacion del sensor
     */
    private void mostrarInformacionDispositivoBTLE( BluetoothDevice bluetoothDevice, int rssi, byte[] bytes) {

        Log.d(ETIQUETA_LOG, " ****************************************************");
        Log.d(ETIQUETA_LOG, " ****** DISPOSITIVO DETECTADO BTLE ****************** ");
        Log.d(ETIQUETA_LOG, " ****************************************************");
        Log.d(ETIQUETA_LOG, " nombre = " + bluetoothDevice.getName());
        Log.d(ETIQUETA_LOG, " dirección = " + bluetoothDevice.getAddress());
        Log.d(ETIQUETA_LOG, " rssi = " + rssi );

        Log.d(ETIQUETA_LOG, " bytes = " + new String(bytes));
        Log.d(ETIQUETA_LOG, " bytes (" + bytes.length + ") = " + Utilidades.bytesToHexString(bytes));

        TramaIBeacon tib = new TramaIBeacon(bytes);

        Log.d(ETIQUETA_LOG, " ----------------------------------------------------");
        Log.d(ETIQUETA_LOG, " prefijo  = " + Utilidades.bytesToHexString(tib.getPrefijo()));
        Log.d(ETIQUETA_LOG, "          advFlags = " + Utilidades.bytesToHexString(tib.getAdvFlags()));
        Log.d(ETIQUETA_LOG, "          advHeader = " + Utilidades.bytesToHexString(tib.getAdvHeader()));
        Log.d(ETIQUETA_LOG, "          companyID = " + Utilidades.bytesToHexString(tib.getCompanyID()));
        Log.d(ETIQUETA_LOG, "          iBeacon type = " + Integer.toHexString(tib.getiBeaconType()));
        Log.d(ETIQUETA_LOG, "          iBeacon length 0x = " + Integer.toHexString(tib.getiBeaconLength()) + " ( "
                + tib.getiBeaconLength() + " ) ");
        Log.d(ETIQUETA_LOG, " uuid  = " + Utilidades.bytesToHexString(tib.getUUID()));
        Log.d(ETIQUETA_LOG, " uuid  = " + Utilidades.bytesToString(tib.getUUID()));
        Log.d(ETIQUETA_LOG, " major  = " + Utilidades.bytesToHexString(tib.getMajor()) + "( "
                + Utilidades.bytesToInt(tib.getMajor()) + " ) ");
        Log.d(ETIQUETA_LOG, " minor  = " + Utilidades.bytesToHexString(tib.getMinor()) + "( "
                + Utilidades.bytesToInt(tib.getMinor()) + " ) ");
        Log.d(ETIQUETA_LOG, " txPower  = " + Integer.toHexString(tib.getTxPower()) + " ( " + tib.getTxPower() + " )");
        Log.d(ETIQUETA_LOG, " ****************************************************");

        if(tib.getMinor() != null ){
            String major;
            major = Utilidades.bytesToHexString(tib.getMajor());
            int valorMajor = Integer.parseInt(major.substring(0, 2), 16);

            this.nombreSensor.setText( bluetoothDevice.getName());
            this.sensor_id.setText(bluetoothDevice.getAddress());
            this.dioxido_carbono.setText(String.valueOf(valorMajor) );
        }

    } // ()

    /**
     * Busca un dispositivo bluetooth especifico
     *
     * @param UUID Identificador del dispositivo.
     */
    private void buscarEsteDispositivoBTLE(final UUID dispositivoBuscado ) {
        this.callbackLeScan = new BluetoothAdapter.LeScanCallback() {
            @Override
            public void onLeScan(BluetoothDevice bluetoothDevice, int rssi, byte[] bytes) {

                //
                // dispostivo encontrado
                //

                TramaIBeacon tib = new TramaIBeacon( bytes );
                String uuidString =  Utilidades.bytesToString( tib.getUUID() );

                if ( uuidString.compareTo( Utilidades.uuidToString( dispositivoBuscado ) ) == 0 )  {
                    detenerBusquedaDispositivosBTLE();
                    mostrarInformacionDispositivoBTLE( bluetoothDevice, rssi, bytes );

                } else {
                    Log.d( MainActivity.ETIQUETA_LOG, " * UUID buscado >" +
                            Utilidades.uuidToString( dispositivoBuscado ) + "< no concuerda con este uuid = >" + uuidString + "<");
                }

            } // onLeScan()
        }; // new LeScanCallback

        //
        BluetoothAdapter.getDefaultAdapter().startLeScan( this.callbackLeScan );
    } // ()


    /**
     * Detiene la busqueda de dispositivos
     */
    public void detenerBusquedaDispositivosBTLE() {
        if ( this.callbackLeScan == null ) {
            return;
        }

        //
        BluetoothAdapter.getDefaultAdapter().stopLeScan(this.callbackLeScan);
        this.callbackLeScan = null;
    } // ()

    /**
     * Boton que activa la busqueda de todos los dispositivos
     */
    public void botonBuscarDispositivosBTLEPulsado( View v ) {
        Log.d(ETIQUETA_LOG, " boton buscar dispositivos BTLE Pulsado" );
        this.buscarTodosLosDispositivosBTLE();
    } // ()

    /**
     * Boton que activa la busqueda de nuestro dispositivo bluetooth
     */
    public void botonBuscarNuestroDispositivoBTLEPulsado( View v ) {
        Log.d(ETIQUETA_LOG, " boton nuestro dispositivo BTLE Pulsado" );
        this.buscarEsteDispositivoBTLE( Utilidades.stringToUUID( "EPSG-GTI-YERAY3A" ) );
    } // ()

    /**
     * Boton que desactiva la busqueda de dispositivos bluetooth
     */
    public void botonDetenerBusquedaDispositivosBTLEPulsado( View v ) {
        Log.d(ETIQUETA_LOG, " boton nuestro dispositivo BTLE Pulsado" );
        this.detenerBusquedaDispositivosBTLE();
    } // ()

    // Funciones Peticionario Rest

    /**
     * Boton que envia a una direccion web los datos recibidos de nuestro dispositivo bluetooth
     */
    public void boton_enviar_pulsado (View quien) throws JSONException {
        Log.d("clienterestandroid", "boton_enviar_pulsado");

        // ojo: creo que hay que crear uno nuevo cada vez
        PeticionarioREST elPeticionario = new PeticionarioREST();
        /*
        elPeticionario.hacerPeticionREST("GET",  "http://81.202.37.9:3050/obtenerTodasMediciones", null,
                new PeticionarioREST.RespuestaREST () {
                    @Override
                    public void callback(int codigo, String cuerpo) {
                        elTexto.setText ("codigo respuesta= " + codigo + " <-> \n" + cuerpo);
                    }
                }
        );*/

        // hacer peticionario post
        //String url = "http://172.20.10.2:3050/anyadirMedicion";
        String url = "http://81.202.37.9:3050/anyadirMedicion";

        // creo el objeto json
        JSONObject postData = new JSONObject();
        postData.put("direccion_sensor", this.sensor_id.getText());
        postData.put("nombre", this.nombreSensor.getText());
        postData.put("dioxido_carbono", String.valueOf( this.dioxido_carbono.getText()) );


        // hago una peticion json a la url
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.POST, url, postData, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        elTexto.setText("Response: " + response.toString());
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d("Volley Error", error.toString());
                    }
                });

        Volley.newRequestQueue(this).add(jsonObjectRequest);
    } // pulsado ()

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Log.d(ETIQUETA_LOG, " MainActivity.constructor : empieza");
        Log.d(ETIQUETA_LOG, " MainActivity.constructor : acaba");

        // Servicio Rest
        this.elTexto = (TextView) findViewById(R.id.elTexto);
        this.nombreSensor = (TextView) findViewById(R.id.nombre);
        this.dioxido_carbono = (TextView) findViewById(R.id.dioxido_carbono);
        this.sensor_id = (TextView) findViewById(R.id.id_sensor);

        this.elBotonEnviar = (Button) findViewById(R.id.botonEnviar);
        StrictMode.ThreadPolicy policy = new
                StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        Log.d("clienterestandroid", "fin onCreate()");

        // Permisos
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (this.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                if (this.checkSelfPermission(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {
                    if (this.shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_BACKGROUND_LOCATION)) {
                        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                        builder.setTitle("This app needs background location access");
                        builder.setMessage("Please grant location access so this app can detect beacons in the background.");
                        builder.setPositiveButton(android.R.string.ok, null);
                        builder.setOnDismissListener(new DialogInterface.OnDismissListener() {

                            @TargetApi(23)
                            @Override
                            public void onDismiss(DialogInterface dialog) {
                                requestPermissions(new String[]{Manifest.permission.ACCESS_BACKGROUND_LOCATION},
                                        PERMISSION_REQUEST_BACKGROUND_LOCATION);
                            }

                        });
                        builder.show();
                    }
                    else {
                        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                        builder.setTitle("Functionality limited");
                        builder.setMessage("Since background location access has not been granted, this app will not be able to discover beacons in the background.  Please go to Settings -> Applications -> Permissions and grant background location access to this app.");
                        builder.setPositiveButton(android.R.string.ok, null);
                        builder.setOnDismissListener(new DialogInterface.OnDismissListener() {

                            @Override
                            public void onDismiss(DialogInterface dialog) {
                            }

                        });
                        builder.show();
                    }

                }
            } else {
                if (!this.shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)) {
                    requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                                    Manifest.permission.ACCESS_BACKGROUND_LOCATION},
                            PERMISSION_REQUEST_FINE_LOCATION);
                }
                else {
                    final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle("Functionality limited");
                    builder.setMessage("Since location access has not been granted, this app will not be able to discover beacons.  Please go to Settings -> Applications -> Permissions and grant location access to this app.");
                    builder.setPositiveButton(android.R.string.ok, null);
                    builder.setOnDismissListener(new DialogInterface.OnDismissListener() {

                        @Override
                        public void onDismiss(DialogInterface dialog) {
                        }

                    });
                    builder.show();
                }

            }
        }
    }
}