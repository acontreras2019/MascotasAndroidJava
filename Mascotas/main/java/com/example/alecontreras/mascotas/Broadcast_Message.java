package com.example.alecontreras.mascotas;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.service.notification.StatusBarNotification;
import android.util.Log;
import android.widget.Toast;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

public class Broadcast_Message extends BroadcastReceiver {

    int eventProcess, eventsCount;
    public static final String ACTION_SEND_NOTIFICATION = "com.example.alecontreras.mascotas.SEND_NOTIFICATION";
    public static final String ACTION_NOTIFICATION_CANCELLED = "com.example.alecontreras.mascotas.NOTIFICATION_CANCELLED";

    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO: This method is called when the BroadcastReceiver is receiving

        String action = intent.getAction();
        Bundle extras = intent.getExtras();
        Log.d(" Broadcast", "Procesando Mensajes");

        if (ACTION_SEND_NOTIFICATION.equals(action)) {

            if (extras.getStringArray("limpiarLista") != null && ServiceMascota.ACTION_SEND_NOTIFICATION.equals(intent.getAction())) {
                //Notificaciones Procesadas
                 String[] listaPendiente = extras.getStringArray("limpiarLista");
                // del Broadcast al Service

                Intent intentToService = new Intent(ServiceMascota.ACTION_RECEIVE_RESULT);
                intentToService.putExtra("limpiarLista", listaPendiente);
                LocalBroadcastManager.getInstance(context).sendBroadcast(intentToService);

            }
        } else if (ACTION_NOTIFICATION_CANCELLED.equals(action)) {
            if (action != null ) {
                //Notificaciones Canceladas

                if (extras.getStringArray("listaPendiente") != null) {

                    String[] listaBorrar = extras.getStringArray("listaPendiente");
                    // Notificar al servicio con la información de la notificación cancelada
                    Intent intentToService = new Intent(ServiceMascota.ACTION_RECEIVE_CANCELED);
                    intentToService.putExtra("limpiarLista", listaBorrar);
                    LocalBroadcastManager.getInstance(context).sendBroadcast(intentToService);

                    //Del Broadcast a la Actividad
                    eventsCount = extras.getInt("eventsCount");
                    Intent intentMain = new Intent(MainActivity.ACTION_RECEIVE_CANCELED);
                    intentMain.putExtra("eventsCount", eventsCount);
                    intentMain.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    LocalBroadcastManager.getInstance(context).sendBroadcast(intentMain);

                }
            }

        }

    }
}