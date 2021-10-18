package com.candelyeray.bluetoothReceiver;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;
// --------------------------------------------------------------
//
// Yeray Candel Sampedro
// 2021 - 10 - 18
//
// --------------------------------------------------------------
public class ServicioEscucharBeacons extends IntentService {
    // ---------------------------------------------------------------------------------------------
    // ---------------------------------------------------------------------------------------------
    private static final String ETIQUETA_LOG = ">>>>";

    private long tiempoDeEspera = 10000;

    private boolean seguir = true;

    private MainActivity mainActivity = new MainActivity();
    /**
     * Servicio que detecta los beacons que se estan recibiendo.
     */
    public ServicioEscucharBeacons() {
        super("HelloIntentService");
        Log.d(ETIQUETA_LOG, " ServicioEscucharBeacons.constructor: termina");

    }


    // ---------------------------------------------------------------------------------------------
    // ---------------------------------------------------------------------------------------------
    /*
    @Override
    public int onStartCommand( Intent elIntent, int losFlags, int startId) {

        // creo que este método no es necesario usarlo. Lo ejecuta el thread principal !!!
        super.onStartCommand( elIntent, losFlags, startId );

        this.tiempoDeEspera = elIntent.getLongExtra("tiempoDeEspera", 50000);

        Log.d(ETIQUETA_LOG, " ServicioEscucharBeacons.onStartCommand : empieza: thread=" + Thread.currentThread().getId() );

        return Service.START_CONTINUATION_MASK | Service.START_STICKY;
    } // ()

     */

    /**
     * Detiene el servicio de escuchar beacons.
     */
    public void parar () {

        Log.d(ETIQUETA_LOG, " ServicioEscucharBeacons.parar() " );


        if ( this.seguir == false ) {
            return;
        }
        mainActivity.detenerBusquedaDispositivosBTLE();
        this.seguir = false;
        this.stopSelf();

        Log.d(ETIQUETA_LOG, " ServicioEscucharBeacons.parar() : acaba " );

    }

    // ---------------------------------------------------------------------------------------------
    // ---------------------------------------------------------------------------------------------
    public void onDestroy() {

        Log.d(ETIQUETA_LOG, " ServicioEscucharBeacons.onDestroy() " );


        this.parar(); // posiblemente no haga falta, si stopService() ya se carga el servicio y su worker thread
    }

    // ---------------------------------------------------------------------------------------------
    // ---------------------------------------------------------------------------------------------
    /**
     * The IntentService calls this method from the default worker thread with
     * the intent that started the service. When this method returns, IntentService
     * stops the service, as appropriate.
     *
     * Mientras el servicio este activado cada 10 segundos hara una escucha de los beacons que se
     * esten recibiendo
     */
    @Override
    protected void onHandleIntent(Intent intent) {

        this.tiempoDeEspera = intent.getLongExtra("tiempoDeEspera", /* default */ 50000);
        this.seguir = true;

        // esto lo ejecuta un WORKER THREAD !

        long contador = 1;

        Log.d(ETIQUETA_LOG, " ServicioEscucharBeacons.onHandleIntent: empieza : thread=" + Thread.currentThread().getId() );

        try {

            while ( this.seguir ) {
                //mainActivity.buscarTodosLosDispositivosBTLE();
                Thread.sleep(tiempoDeEspera);
                Log.d(ETIQUETA_LOG, " ServicioEscucharBeacons.onHandleIntent: tras la espera:  " + contador );
                contador++;
            }

            Log.d(ETIQUETA_LOG, " ServicioEscucharBeacons.onHandleIntent : tarea terminada ( tras while(true) )" );


        } catch (InterruptedException e) {
            // Restore interrupt status.
            Log.d(ETIQUETA_LOG, " ServicioEscucharBeacons.onHandleItent: problema con el thread");

            Thread.currentThread().interrupt();
        }

        Log.d(ETIQUETA_LOG, " ServicioEscucharBeacons.onHandleItent: termina");

    }
}
