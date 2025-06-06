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

    public static final String PREFS_RACHAS_NAME = "PreferenciasRachas";
    public static final String KEY_ULTIMA_FECHA_RUTINA_COMPLETADA_RACHA = "ultimaFechaRutinaCompletadaRacha";
    public static final String KEY_CONTADOR_RACHA_ACTUAL = "contadorRachaActual";
    private static final String KEY_ULTIMA_FECHA_NOTIFICACION_RACHA = "ultimaFechaNotificacionRacha";
    public static final String KEY_CONGELADORES_RACHA = "congeladoresRacha";
    public static final String KEY_PUNTOS = "puntosUsuario";

    // Hitos para notificar rachas (en días)
    private static final List<Integer> HITOS_RACHA = Arrays.asList(3, 7, 14, 30, 50, 100, 150, 200, 300, 365);
    private static final int ID_NOTIFICACION_RACHA_BASE = 2000;


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
        int congeladoresRacha = prefs.getInt(KEY_CONGELADORES_RACHA, 0);

        if (ultimaFechaCompletadaParaRacha.equals(fechaActual)) {
            Log.d("GestorDeRachas", "Ya se contó una rutina para la racha hoy. Racha actual: " + contadorRachaActual);
            if (HITOS_RACHA.contains(contadorRachaActual) && !ultimaFechaNotificacionDeRacha.equals(fechaActual)) {
                String tituloNotif = "¡Felicidades por tu Racha!";
                String mensajeNotif = "¡Mantienes tu racha de " + contadorRachaActual + " días! ¡Sigue así!";
                int notificationId = ID_NOTIFICACION_RACHA_BASE + contadorRachaActual;
                NotificationHelper.showNotification(context, tituloNotif, mensajeNotif, notificationId);
                Log.i("GestorDeRachas", "Notificación de racha (ya contada hoy) de " + contadorRachaActual + " días enviada.");
                editor.putString(KEY_ULTIMA_FECHA_NOTIFICACION_RACHA, fechaActual);
                editor.apply();
            }
            return;
        }

        String fechaAyer = getFechaAyerISO();

        if (ultimaFechaCompletadaParaRacha.equals(fechaAyer)) {
            contadorRachaActual++;
            Log.d("GestorDeRachas", "¡Racha continúa! Días: " + contadorRachaActual);
        } else {
            if (congeladoresRacha > 0) {
                congeladoresRacha--;
                editor.putInt(KEY_CONGELADORES_RACHA, congeladoresRacha);
                Log.i("GestorDeRachas", "¡Congelador de racha usado! Congeladores restantes: " + congeladoresRacha);
                String tituloNotifCongelador = "¡Congelador de Racha Activado!";
                String mensajeNotifCongelador = "No completaste una rutina ayer, pero usamos un congelador. Tu racha de " + contadorRachaActual + " días está a salvo. Te quedan " + congeladoresRacha + " congeladores.";
                NotificationHelper.showNotification(context, tituloNotifCongelador, mensajeNotifCongelador, ID_NOTIFICACION_RACHA_BASE -1);
            } else {
                contadorRachaActual = 1;
                Log.d("GestorDeRachas", "Racha reiniciada o primer día. Días: " + contadorRachaActual);
            }
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
            // Notificación por el primer día de racha, veremos si implementarlo
        }

        editor.apply();
    }

    public static void añadirCongeladores(Context context, int cantidad) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_RACHAS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        int congeladoresActuales = prefs.getInt(KEY_CONGELADORES_RACHA, 0);
        editor.putInt(KEY_CONGELADORES_RACHA, congeladoresActuales + cantidad);
        editor.apply();
        Log.d("GestorDeRachas", "Añadidos " + cantidad + " congeladores. Total: " + (congeladoresActuales + cantidad));
    }

    public static int getCantidadCongeladores(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_RACHAS_NAME, Context.MODE_PRIVATE);
        return prefs.getInt(KEY_CONGELADORES_RACHA, 0);
    }

    //Método para obtener la cantidad actual de puntos
    public static int getCantidadPuntos(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_RACHAS_NAME, Context.MODE_PRIVATE);
        return prefs.getInt(KEY_PUNTOS, 0);
    }

    //Método para sumar puntos
    public static void sumarPuntos(Context context, int cantidad) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_RACHAS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        int puntosActuales = prefs.getInt(KEY_PUNTOS, 0);
        editor.putInt(KEY_PUNTOS, puntosActuales + cantidad);
        editor.apply();
        Log.d("GestorDeRachas", "Sumados " + cantidad + " puntos. Total: " + (puntosActuales + cantidad));
    }

    //Método para restar puntos
    public static boolean restarPuntos(Context context, int cantidad) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_RACHAS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        int puntosActuales = prefs.getInt(KEY_PUNTOS, 0);
        if (puntosActuales >= cantidad) {
            editor.putInt(KEY_PUNTOS, puntosActuales - cantidad);
            editor.apply();
            Log.d("GestorDeRachas", "Restados " + cantidad + " puntos. Total: " + (puntosActuales - cantidad));
            return true; // Puntos restados con éxito
        } else {
            Log.d("GestorDeRachas", "No hay suficientes puntos para restar " + cantidad + ". Actual: " + puntosActuales);
            return false; // No hay suficientes puntos
        }
    }
}