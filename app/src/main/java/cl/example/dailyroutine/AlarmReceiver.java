package cl.example.dailyroutine;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class AlarmReceiver extends BroadcastReceiver {

    public static final String EXTRA_NOTIFICATION_ID = "cl.example.dailyroutine.NOTIFICATION_ID";
    public static final String EXTRA_RUTINA_NOMBRE = "cl.example.dailyroutine.RUTINA_NOMBRE";
    public static final String EXTRA_RUTINA_HORA = "cl.example.dailyroutine.RUTINA_HORA";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("AlarmReceiver", "Alarma recibida!");

        int notificationId = intent.getIntExtra(EXTRA_NOTIFICATION_ID, 0);
        String rutinaNombre = intent.getStringExtra(EXTRA_RUTINA_NOMBRE);
        String rutinaHora = intent.getStringExtra(EXTRA_RUTINA_HORA);

        String title = "Recordatorio de Rutina";
        String message;

        if (rutinaNombre != null && !rutinaNombre.isEmpty()) {
            message = "Es hora de tu rutina: " + rutinaNombre;
            if (rutinaHora != null && !rutinaHora.isEmpty()) {
                message += " (" + rutinaHora + ")";
            }
        } else {
            message = "¡Es hora de una de tus rutinas programadas!";
        }

        if (notificationId == 0) {
            notificationId = (int) System.currentTimeMillis();
        }


        NotificationHelper.showNotification(context, title, message, notificationId);
    }
}