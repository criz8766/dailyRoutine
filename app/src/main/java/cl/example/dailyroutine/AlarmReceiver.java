package cl.example.dailyroutine;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.util.Calendar;
import java.util.List;
import java.text.SimpleDateFormat;
import java.util.Locale;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import android.content.SharedPreferences;
import java.lang.reflect.Type;
import java.util.Date;
import java.util.ArrayList;

import androidx.core.app.NotificationCompat;

public class AlarmReceiver extends BroadcastReceiver {

    public static final String EXTRA_NOTIFICATION_ID = "cl.example.dailyroutine.NOTIFICATION_ID";
    public static final String EXTRA_RUTINA_NOMBRE = "cl.example.dailyroutine.RUTINA_NOMBRE";
    public static final String EXTRA_RUTINA_HORA = "cl.example.dailyroutine.RUTINA_HORA";
    public static final String EXTRA_TEXTO_PERSONALIZADO = "cl.example.dailyroutine.TEXTO_PERSONALIZADO";
    public static final String EXTRA_DAY_OF_WEEK = "dayOfWeek";

    public static final String ACTION_MARK_COMPLETED = "cl.example.dailyroutine.ACTION_MARK_COMPLETED";
    public static final String EXTRA_RUTINA_ID = "cl.example.dailyroutine.RUTINA_ID";


    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("AlarmReceiver", "Alarma recibida!");

        int notificationId = intent.getIntExtra(EXTRA_NOTIFICATION_ID, (int) System.currentTimeMillis());
        String rutinaNombre = intent.getStringExtra(EXTRA_RUTINA_NOMBRE);
        String rutinaHora = intent.getStringExtra(EXTRA_RUTINA_HORA);
        String textoPersonalizado = intent.getStringExtra(EXTRA_TEXTO_PERSONALIZADO);
        int triggeredDayOfWeek = intent.getIntExtra(EXTRA_DAY_OF_WEEK, -1);
        int rutinaId = intent.getIntExtra(EXTRA_RUTINA_ID, -1); // Recuperar el ID de la rutina

        String title = "Recordatorio: " + (rutinaNombre != null && !rutinaNombre.isEmpty() ? rutinaNombre : "Tu Rutina");
        String message;

        if (textoPersonalizado != null && !textoPersonalizado.isEmpty()) {
            message = textoPersonalizado;
        } else {
            message = "Es hora de tu rutina";
            if (rutinaNombre != null && !rutinaNombre.isEmpty()) {
                message += ": " + rutinaNombre;
            }
            if (rutinaHora != null && !rutinaHora.isEmpty()) {
                message += " (" + rutinaHora + ")";
            }
            if (message.equals("Es hora de tu rutina")){
                message = "¡Es hora de una de tus rutinas programadas!";
            }
        }

        Intent resultIntent = new Intent(context, SplashActivity.class);
        resultIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent resultPendingIntent = PendingIntent.getActivity(context,
                notificationId,
                resultIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, NotificationHelper.CHANNEL_ID)
                .setSmallIcon(R.drawable.logo)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(resultPendingIntent)
                .setAutoCancel(true)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC);

        // Añadir acción "Marcar como Completada"
        // si rutinaId es -1, el botón no se añadirá.
        if (rutinaId != -1) {
            Intent markCompletedIntent = new Intent(context, NotificationActionReceiver.class);
            markCompletedIntent.setAction(ACTION_MARK_COMPLETED);
            markCompletedIntent.putExtra(EXTRA_RUTINA_ID, rutinaId);
            markCompletedIntent.putExtra(EXTRA_NOTIFICATION_ID, notificationId);

            PendingIntent markCompletedPendingIntent = PendingIntent.getBroadcast(context,
                    notificationId + 1000,
                    markCompletedIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

            builder.addAction(R.drawable.ic_check_pomodoro, "Completar", markCompletedPendingIntent);
        }

        NotificationHelper.showNotification(context, notificationId, builder);

        // Lógica para reprogramar la alarma si es una rutina semanal
        if (triggeredDayOfWeek != -1) {
            reprogramarAlarmaSemanal(context, notificationId, rutinaNombre, rutinaHora, textoPersonalizado, triggeredDayOfWeek);
        }
    }

    private void reprogramarAlarmaSemanal(Context context, int originalNotificationId, String rutinaNombre, String rutinaHora, String textoPersonalizado, int triggeredDayOfWeek) {
        int rutinaId = originalNotificationId / 10;

        SharedPreferences sharedPreferences = context.getSharedPreferences(MenuPrincipal.PREFS_NAME, Context.MODE_PRIVATE);
        Gson gson = new Gson();
        String json = sharedPreferences.getString(MenuPrincipal.KEY_RUTINAS, null);
        ArrayList<Rutina> listaRutinas = new ArrayList<>();
        if (json != null) {
            Type type = new TypeToken<ArrayList<Rutina>>() {}.getType();
            listaRutinas = gson.fromJson(json, type);
        }

        Rutina rutina = null;
        for (Rutina r : listaRutinas) {
            if (r.getId() == rutinaId) {
                rutina = r;
                break;
            }
        }

        if (rutina == null || !rutina.isRecordatorioActivo() || rutina.getDiasSemana().isEmpty()) {
            Log.d("AlarmReceiver", "No se pudo encontrar la rutina o no es una alarma repetitiva. ID: " + rutinaId);
            return;
        }

        Calendar calendar = Calendar.getInstance();
        try {
            int targetHour = Integer.parseInt(rutinaHora.split(":")[0]);
            int targetMinute = Integer.parseInt(rutinaHora.split(":")[1]);
            calendar.set(Calendar.HOUR_OF_DAY, targetHour);
            calendar.set(Calendar.MINUTE, targetMinute);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);
        } catch (NumberFormatException e) {
            Log.e("AlarmReceiver", "Error parsing routine hour: " + rutinaHora, e);
            return;
        }

        calendar.add(Calendar.WEEK_OF_YEAR, 1);

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager != null) {
            Intent nextIntent = new Intent(context, AlarmReceiver.class);
            nextIntent.putExtra(EXTRA_NOTIFICATION_ID, originalNotificationId);
            nextIntent.putExtra(EXTRA_RUTINA_NOMBRE, rutinaNombre);
            nextIntent.putExtra(EXTRA_RUTINA_HORA, rutinaHora);
            nextIntent.putExtra(EXTRA_TEXTO_PERSONALIZADO, textoPersonalizado);
            nextIntent.putExtra(EXTRA_DAY_OF_WEEK, triggeredDayOfWeek);
            nextIntent.putExtra(EXTRA_RUTINA_ID, rutinaId);

            PendingIntent nextPendingIntent = PendingIntent.getBroadcast(context, originalNotificationId, nextIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), nextPendingIntent);
            } else {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), nextPendingIntent);
            }
            Log.i("AlarmReceiver", "Reprogramada alarma para ID " + rutinaId + " (Día " + triggeredDayOfWeek + ") a las " + rutinaHora + " para " + new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(new Date(calendar.getTimeInMillis())));
        }
    }
}