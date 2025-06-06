package cl.example.dailyroutine;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.core.app.NotificationManagerCompat;

public class NotificationActionReceiver extends BroadcastReceiver {

    private static final String TAG = "NotificationActionRcv";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent != null && AlarmReceiver.ACTION_MARK_COMPLETED.equals(intent.getAction())) {
            int rutinaId = intent.getIntExtra(AlarmReceiver.EXTRA_RUTINA_ID, -1);
            int notificationId = intent.getIntExtra(AlarmReceiver.EXTRA_NOTIFICATION_ID, -1);

            Log.d(TAG, "Acción de notificación recibida: Marcar como completada para Rutina ID: " + rutinaId);

            if (rutinaId != -1) {
                // Llama al método estático en MenuPrincipal para marcar la rutina y guardar
                MenuPrincipal.marcarRutinaComoCompletadaYGuardar(context, rutinaId);
                if (notificationId != -1) {
                    NotificationManagerCompat.from(context).cancel(notificationId);
                    Log.d(TAG, "Notificación ID " + notificationId + " cerrada.");
                }
            } else {
                Log.e(TAG, "ID de rutina no válido para marcar como completada.");
            }
        }
    }
}