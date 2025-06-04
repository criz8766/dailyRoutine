package cl.example.dailyroutine;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class GestorDeRachas {

    private static final String PREFS_RACHAS_NAME = "PreferenciasRachas";
    private static final String KEY_ULTIMA_FECHA_RUTINA_COMPLETADA_RACHA = "ultimaFechaRutinaCompletadaRacha"; // YYYY-MM-DD
    private static final String KEY_CONTADOR_RACHA_ACTUAL = "contadorRachaActual";
    private static final String KEY_ULTIMA_FECHA_NOTIFICACION_RACHA = "ultimaFechaNotificacionRacha"; // YYYY-MM-DD

    // Hitos para notificar rachas (en días)
    private static final List<Integer> HITOS_RACHA = Arrays.asList(3, 7, 14, 30, 50, 100, 150, 200, 300, 365);
    private static final int ID_NOTIFICACION_RACHA_BASE = 2000; // Base para IDs de notificación de racha


    private static String getFechaActualISO() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        return sdf.format(new Date());
    }

    private static String getFechaAyerISO() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, -1);
        return sdf.format(cal.getTime());
    }

    public static synchronized void rutinaCompletadaHoy(Context context) {
        Log.d("GestorDeRachas", "Verificando racha por rutina completada hoy.");
        SharedPreferences prefs = context.getSharedPreferences(PREFS_RACHAS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        String fechaActual = getFechaActualISO();
        String ultimaFechaCompletadaParaRacha = prefs.getString(KEY_ULTIMA_FECHA_RUTINA_COMPLETADA_RACHA, "");
        int contadorRachaActual = prefs.getInt(KEY_CONTADOR_RACHA_ACTUAL, 0);
        String ultimaFechaNotificacionDeRacha = prefs.getString(KEY_ULTIMA_FECHA_NOTIFICACION_RACHA, "");

        // Si ya se completó una rutina para la racha hoy, no hacer nada más con el contador.
        if (ultimaFechaCompletadaParaRacha.equals(fechaActual)) {
            Log.d("GestorDeRachas", "Ya se contó una rutina para la racha hoy. Racha actual: " + contadorRachaActual);
            // Podríamos querer notificar si se alcanzó un hito y no se notificó, pero la lógica principal es al incrementar.
            // No obstante, es buena idea verificar si el hito actual ya debería haberse notificado hoy.
            if (HITOS_RACHA.contains(contadorRachaActual) && !ultimaFechaNotificacionDeRacha.equals(fechaActual)) {
                String tituloNotif = "¡Felicidades por tu Racha!";
                String mensajeNotif = "¡Mantienes tu racha de " + contadorRachaActual + " días! ¡Sigue así!";
                int notificationId = ID_NOTIFICACION_RACHA_BASE + contadorRachaActual;
                NotificationHelper.showNotification(context, tituloNotif, mensajeNotif, notificationId);
                Log.i("GestorDeRachas", "Notificación de racha (ya contada hoy) de " + contadorRachaActual + " días enviada.");
                editor.putString(KEY_ULTIMA_FECHA_NOTIFICACION_RACHA, fechaActual);
                editor.apply(); // Aplicar este cambio de inmediato.
            }
            return;
        }

        String fechaAyer = getFechaAyerISO();

        if (ultimaFechaCompletadaParaRacha.equals(fechaAyer)) {
            // La racha continúa
            contadorRachaActual++;
            Log.d("GestorDeRachas", "¡Racha continúa! Días: " + contadorRachaActual);
        } else {
            // La racha se rompió (no fue ayer ni hoy) o es el primer día
            contadorRachaActual = 1;
            Log.d("GestorDeRachas", "Racha reiniciada o primer día. Días: " + contadorRachaActual);
        }

        editor.putString(KEY_ULTIMA_FECHA_RUTINA_COMPLETADA_RACHA, fechaActual);
        editor.putInt(KEY_CONTADOR_RACHA_ACTUAL, contadorRachaActual);

        // Verificar si se debe notificar por alcanzar un nuevo hito
        if (HITOS_RACHA.contains(contadorRachaActual) && !ultimaFechaNotificacionDeRacha.equals(fechaActual)) {
            String tituloNotif = "¡Felicidades por tu Racha!";
            String mensajeNotif = "¡Has alcanzado una racha de " + contadorRachaActual + " días completando tus rutinas! ¡Sigue así!";
            int notificationId = ID_NOTIFICACION_RACHA_BASE + contadorRachaActual;

            NotificationHelper.showNotification(context, tituloNotif, mensajeNotif, notificationId);
            Log.i("GestorDeRachas", "Notificación de racha de " + contadorRachaActual + " días enviada.");
            editor.putString(KEY_ULTIMA_FECHA_NOTIFICACION_RACHA, fechaActual);
        } else if (contadorRachaActual == 1 && ultimaFechaCompletadaParaRacha.isEmpty()) {
            // Opcional: Notificación por el primer día de racha (si se desea)
            // String tituloNotif = "¡Primer Día!";
            // String mensajeNotif = "¡Completaste tu primera rutina hoy! ¡Este es el comienzo de una gran racha!";
            // NotificationHelper.showNotification(context, tituloNotif, mensajeNotif, ID_NOTIFICACION_RACHA_BASE + contadorRachaActual);
            // editor.putString(KEY_ULTIMA_FECHA_NOTIFICACION_RACHA, fechaActual); // También registrar notificación
        }


        editor.apply();
    }
}