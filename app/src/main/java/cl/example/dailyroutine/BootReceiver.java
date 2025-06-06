package cl.example.dailyroutine;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import android.os.Build;
import android.content.pm.PackageManager;
import androidx.core.content.ContextCompat;
import android.Manifest;

public class BootReceiver extends BroadcastReceiver {

    private static final String TAG = "BootReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            Log.d(TAG, "BOOT_COMPLETED recibido. Reprogramando alarmas...");
            reprogramarTodasLasAlarmas(context);
        }
    }

    private void reprogramarTodasLasAlarmas(Context context) {
        // Cargar todas las rutinas guardadas
        SharedPreferences sharedPreferences = context.getSharedPreferences(MenuPrincipal.PREFS_NAME, Context.MODE_PRIVATE);
        Gson gson = new Gson();
        String json = sharedPreferences.getString(MenuPrincipal.KEY_RUTINAS, null);
        ArrayList<Rutina> listaRutinas = new ArrayList<>();

        if (json != null) {
            Type type = new TypeToken<ArrayList<Rutina>>() {}.getType();
            listaRutinas = gson.fromJson(json, type);
        } else {
            Log.d(TAG, "No hay rutinas guardadas para reprogramar.");
            return;
        }

        if (listaRutinas.isEmpty()) {
            Log.d(TAG, "Lista de rutinas vacía. No hay alarmas que reprogramar.");
            return;
        }

        // Verificar permisos de notificación para Android 13+
        boolean hasNotificationPermission = true;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                hasNotificationPermission = false;
                Log.w(TAG, "Permiso POST_NOTIFICATIONS no concedido. Las alarmas no se reprogramarán.");
                // Si no hay permiso, no se puede programar la notificación, así que no se reprograman las alarmas.
                return;
            }
        }

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null) {
            Log.e(TAG, "AlarmManager es nulo.");
            return;
        }

        for (Rutina rutina : listaRutinas) {
            if (rutina.isRecordatorioActivo()) {
                Log.d(TAG, "Intentando reprogramar recordatorio para rutina: " + rutina.getNombre());

                String rutinaHora = rutina.getHoraRecordatorio();
                if (rutinaHora == null || !rutinaHora.matches("\\d{2}:\\d{2}")) {
                    Log.w(TAG, "Hora de recordatorio inválida para rutina " + rutina.getNombre());
                    continue;
                }

                int targetHour, targetMinute;
                try {
                    String[] partes = rutinaHora.split(":");
                    targetHour = Integer.parseInt(partes[0]);
                    targetMinute = Integer.parseInt(partes[1]);
                } catch (NumberFormatException e) {
                    Log.e(TAG, "Error al parsear la hora de recordatorio: " + rutinaHora, e);
                    continue;
                }

                List<Integer> diasSemana = rutina.getDiasSemana();
                String fechaRutinaStr = rutina.getFecha();

                if (!diasSemana.isEmpty()) {
                    // Reprogramar alarmas semanales
                    for (Integer dayOfWeek : diasSemana) {
                        Calendar calendar = Calendar.getInstance();
                        calendar.set(Calendar.HOUR_OF_DAY, targetHour);
                        calendar.set(Calendar.MINUTE, targetMinute);
                        calendar.set(Calendar.SECOND, 0);
                        calendar.set(Calendar.MILLISECOND, 0);

                        int currentDayOfWeek = Calendar.getInstance().get(Calendar.DAY_OF_WEEK);
                        long currentTimeMillis = System.currentTimeMillis();

                        int daysToAdd = (dayOfWeek - currentDayOfWeek + 7) % 7;
                        if (daysToAdd == 0) { // Si es hoy
                            if (calendar.getTimeInMillis() <= currentTimeMillis) {
                                calendar.add(Calendar.WEEK_OF_YEAR, 1);
                            }
                        } else { // Si es un día futuro en esta semana
                            calendar.add(Calendar.DAY_OF_YEAR, daysToAdd);
                        }

                        int uniqueRequestCode = rutina.getId() * 10 + dayOfWeek;
                        Intent intent = new Intent(context, AlarmReceiver.class);
                        intent.putExtra(AlarmReceiver.EXTRA_NOTIFICATION_ID, uniqueRequestCode);
                        intent.putExtra(AlarmReceiver.EXTRA_RUTINA_NOMBRE, rutina.getNombre());
                        intent.putExtra(AlarmReceiver.EXTRA_RUTINA_HORA, rutina.getHoraRecordatorio());
                        intent.putExtra(AlarmReceiver.EXTRA_TEXTO_PERSONALIZADO, rutina.getTextoRecordatorioPersonalizado());
                        intent.putExtra(AlarmReceiver.EXTRA_DAY_OF_WEEK, dayOfWeek);

                        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, uniqueRequestCode, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

                        try {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
                            } else {
                                alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
                            }
                            Log.i(TAG, "Alarma semanal reprogramada para ID " + rutina.getId() + " (Día " + dayOfWeek + ") a las " + rutina.getHoraRecordatorio() + " para " + new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(new Date(calendar.getTimeInMillis())));
                        } catch (SecurityException e) {
                            Log.e(TAG, "SecurityException al reprogramar alarma semanal para " + rutina.getNombre(), e);
                        }
                    }
                } else if (!fechaRutinaStr.isEmpty()) {
                    // Reprogramar alarma de una sola vez
                    Calendar calendar = Calendar.getInstance();
                    try {
                        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                        Date fechaRutinaDate = sdf.parse(fechaRutinaStr);
                        if (fechaRutinaDate != null) {
                            calendar.setTime(fechaRutinaDate);
                        }
                    } catch (ParseException e) {
                        Log.e(TAG, "Error al parsear la fecha de rutina: " + fechaRutinaStr, e);
                        continue;
                    }

                    calendar.set(Calendar.HOUR_OF_DAY, targetHour);
                    calendar.set(Calendar.MINUTE, targetMinute);
                    calendar.set(Calendar.SECOND, 0);
                    calendar.set(Calendar.MILLISECOND, 0);

                    // Solo reprogramar si la alarma no ha pasado
                    if (calendar.getTimeInMillis() > System.currentTimeMillis()) {
                        Intent intent = new Intent(context, AlarmReceiver.class);
                        intent.putExtra(AlarmReceiver.EXTRA_NOTIFICATION_ID, rutina.getId());
                        intent.putExtra(AlarmReceiver.EXTRA_RUTINA_NOMBRE, rutina.getNombre());
                        intent.putExtra(AlarmReceiver.EXTRA_RUTINA_HORA, rutina.getHoraRecordatorio());
                        intent.putExtra(AlarmReceiver.EXTRA_TEXTO_PERSONALIZADO, rutina.getTextoRecordatorioPersonalizado());

                        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, rutina.getId(), intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

                        try {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
                            } else {
                                alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
                            }
                            Log.i(TAG, "Alarma única reprogramada para ID " + rutina.getId() + " en " + new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(new Date(calendar.getTimeInMillis())));
                        } catch (SecurityException e) {
                            Log.e(TAG, "SecurityException al reprogramar alarma única para " + rutina.getNombre(), e);
                        }
                    } else {
                        Log.d(TAG, "Alarma única para " + rutina.getNombre() + " ya ha pasado. No se reprograma.");
                    }
                }
            }
        }
        Log.d(TAG, "Reprogramación de alarmas completada.");
    }
}