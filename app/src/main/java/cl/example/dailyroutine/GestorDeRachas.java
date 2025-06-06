package cl.example.dailyroutine;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class GestorDeRachas {

    public static final String PREFS_RACHAS_NAME = "PreferenciasRachas";
    public static final String KEY_ULTIMA_FECHA_RUTINA_COMPLETADA_RACHA = "ultimaFechaRutinaCompletadaRacha";
    public static final String KEY_CONTADOR_RACHA_ACTUAL = "contadorRachaActual";
    private static final String KEY_ULTIMA_FECHA_NOTIFICACION_RACHA = "ultimaFechaNotificacionRacha";
    public static final String KEY_CONGELADORES_RACHA = "congeladoresRacha";
    public static final String KEY_PUNTOS = "puntosUsuario";

    // --- NUEVAS CLAVES PARA GUARDAR HISTORIAL DE FECHAS ---
    public static final String KEY_FECHAS_RUTINAS_COMPLETADAS = "fechasRutinasCompletadas";
    public static final String KEY_FECHAS_CONGELADOR_USADO = "fechasCongeladorUsado";


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
        Gson gson = new Gson();

        String fechaActual = getFechaActualISO();
        String ultimaFechaCompletadaParaRacha = prefs.getString(KEY_ULTIMA_FECHA_RUTINA_COMPLETADA_RACHA, "");
        String ultimaFechaNotificacionDeRacha = prefs.getString(KEY_ULTIMA_FECHA_NOTIFICACION_RACHA, "");

        // --- Cargar historiales de fechas ---
        Set<String> fechasCompletadas = getFechasDesdePrefs(prefs, gson, KEY_FECHAS_RUTINAS_COMPLETADAS);
        Set<String> fechasCongelador = getFechasDesdePrefs(prefs, gson, KEY_FECHAS_CONGELADOR_USADO);


        if (ultimaFechaCompletadaParaRacha.equals(fechaActual)) {
            Log.d("GestorDeRachas", "Ya se contó una rutina para la racha hoy.");
            int contadorRachaActual = prefs.getInt(KEY_CONTADOR_RACHA_ACTUAL, 0);
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

        int contadorRachaActual = prefs.getInt(KEY_CONTADOR_RACHA_ACTUAL, 0);
        int congeladoresRacha = prefs.getInt(KEY_CONGELADORES_RACHA, 0);

        if (!ultimaFechaCompletadaParaRacha.isEmpty() && !ultimaFechaCompletadaParaRacha.equals(getFechaAyerISO())) {
            // No se completó ayer, verificar congeladores
            if (congeladoresRacha > 0) {
                congeladoresRacha--;
                editor.putInt(KEY_CONGELADORES_RACHA, congeladoresRacha);
                Log.i("GestorDeRachas", "¡Congelador de racha usado! Congeladores restantes: " + congeladoresRacha);
                String tituloNotifCongelador = "¡Congelador de Racha Activado!";
                String mensajeNotifCongelador = "No completaste una rutina ayer, pero usamos un congelador. Tu racha de " + contadorRachaActual + " días está a salvo. Te quedan " + congeladoresRacha + " congeladores.";
                NotificationHelper.showNotification(context, tituloNotifCongelador, mensajeNotifCongelador, ID_NOTIFICACION_RACHA_BASE -1);

                // --- GUARDAR LOS DÍAS PERDIDOS COMO PROTEGIDOS ---
                try {
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                    Date ultimaFecha = sdf.parse(ultimaFechaCompletadaParaRacha);
                    Date hoy = sdf.parse(fechaActual);
                    if (ultimaFecha != null && hoy != null) {
                        long diffInMillis = Math.abs(hoy.getTime() - ultimaFecha.getTime());
                        long diffInDays = TimeUnit.DAYS.convert(diffInMillis, TimeUnit.MILLISECONDS);

                        // Marcar los días intermedios como protegidos
                        for (int i = 1; i < diffInDays; i++) {
                            Calendar cal = Calendar.getInstance();
                            cal.setTime(ultimaFecha);
                            cal.add(Calendar.DATE, i);
                            fechasCongelador.add(sdf.format(cal.getTime()));
                        }
                    }
                } catch (Exception e) {
                    Log.e("GestorDeRachas", "Error al calcular días perdidos", e);
                }

            } else {
                contadorRachaActual = 1;
                Log.d("GestorDeRachas", "Racha reiniciada. Días: " + contadorRachaActual);
            }
        } else { // Si la racha continúa o es el primer día
            contadorRachaActual++;
            Log.d("GestorDeRachas", "Racha continúa o es el primer día. Días: " + contadorRachaActual);
        }

        // --- AÑADIR FECHA ACTUAL A LAS COMPLETADAS ---
        fechasCompletadas.add(fechaActual);

        // Guardar todo
        editor.putString(KEY_ULTIMA_FECHA_RUTINA_COMPLETADA_RACHA, fechaActual);
        editor.putInt(KEY_CONTADOR_RACHA_ACTUAL, contadorRachaActual);

        // Verificar si se debe notificar y dar PUNTOS EXTRA por alcanzar un nuevo hito
        if (HITOS_RACHA.contains(contadorRachaActual) && !ultimaFechaNotificacionDeRacha.equals(fechaActual)) {
            int puntosBonus = contadorRachaActual * 5; // Por ejemplo: 3 días = 15 pts, 7 días = 35 pts
            sumarPuntos(context, puntosBonus);

            String tituloNotif = "¡Hito de Racha Alcanzado!";
            String mensajeNotif = "¡" + contadorRachaActual + " días de racha! Has ganado " + puntosBonus + " puntos extra. ¡Sigue así!";
            int notificationId = ID_NOTIFICACION_RACHA_BASE + contadorRachaActual;

            NotificationHelper.showNotification(context, tituloNotif, mensajeNotif, notificationId);
            Log.i("GestorDeRachas", "Notificación de hito de racha de " + contadorRachaActual + " días enviada. Puntos bonus: " + puntosBonus);
            editor.putString(KEY_ULTIMA_FECHA_NOTIFICACION_RACHA, fechaActual);
        }

        // --- GUARDAR LOS SETS DE FECHAS EN PREFERENCES ---
        editor.putString(KEY_FECHAS_RUTINAS_COMPLETADAS, gson.toJson(fechasCompletadas));
        editor.putString(KEY_FECHAS_CONGELADOR_USADO, gson.toJson(fechasCongelador));

        editor.apply();
    }

    private static Set<String> getFechasDesdePrefs(SharedPreferences prefs, Gson gson, String key) {
        String json = prefs.getString(key, null);
        if (json != null) {
            Type type = new TypeToken<HashSet<String>>() {}.getType();
            return gson.fromJson(json, type);
        }
        return new HashSet<>();
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

    public static int getCantidadPuntos(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_RACHAS_NAME, Context.MODE_PRIVATE);
        return prefs.getInt(KEY_PUNTOS, 0);
    }

    public static void sumarPuntos(Context context, int cantidad) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_RACHAS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        int puntosActuales = prefs.getInt(KEY_PUNTOS, 0);
        editor.putInt(KEY_PUNTOS, puntosActuales + cantidad);
        editor.apply();
        Log.d("GestorDeRachas", "Sumados " + cantidad + " puntos. Total: " + (puntosActuales + cantidad));
    }

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