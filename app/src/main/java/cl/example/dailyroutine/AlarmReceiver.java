package cl.example.dailyroutine;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class AlarmReceiver extends BroadcastReceiver {

    public static final String EXTRA_NOTIFICATION_ID = "cl.example.dailyroutine.NOTIFICATION_ID";
    public static final String EXTRA_RUTINA_NOMBRE = "cl.example.dailyroutine.RUTINA_NOMBRE";
    public static final String EXTRA_RUTINA_HORA = "cl.example.dailyroutine.RUTINA_HORA";
    public static final String EXTRA_TEXTO_PERSONALIZADO = "cl.example.dailyroutine.TEXTO_PERSONALIZADO"; // Nueva constante

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("AlarmReceiver", "Alarma recibida!");

        int notificationId = intent.getIntExtra(EXTRA_NOTIFICATION_ID, (int) System.currentTimeMillis());
        String rutinaNombre = intent.getStringExtra(EXTRA_RUTINA_NOMBRE);
        String rutinaHora = intent.getStringExtra(EXTRA_RUTINA_HORA);
        String textoPersonalizado = intent.getStringExtra(EXTRA_TEXTO_PERSONALIZADO);

        String title = "Recordatorio: " + (rutinaNombre != null && !rutinaNombre.isEmpty() ? rutinaNombre : "Tu Rutina");
        String message;

        if (textoPersonalizado != null && !textoPersonalizado.isEmpty()) {
            message = textoPersonalizado;
            // Opcional: añadir la hora si el texto personalizado no la incluye explícitamente
            // if (rutinaHora != null && !rutinaHora.isEmpty()) {
            //     message += " (Programada para las " + rutinaHora + ")";
            // }
        } else {
            // Mensaje por defecto si no hay texto personalizado
            message = "Es hora de tu rutina";
            if (rutinaNombre != null && !rutinaNombre.isEmpty()) {
                message += ": " + rutinaNombre;
            }
            if (rutinaHora != null && !rutinaHora.isEmpty()) {
                message += " (" + rutinaHora + ")";
            }
            // Fallback si todo es nulo o vacío
            if (message.equals("Es hora de tu rutina")){
                message = "¡Es hora de una de tus rutinas programadas!";
            }
        }

        NotificationHelper.showNotification(context, title, message, notificationId); //
    }
}