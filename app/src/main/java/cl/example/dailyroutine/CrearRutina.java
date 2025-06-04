package cl.example.dailyroutine;

import android.Manifest;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.material.datepicker.MaterialDatePicker;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone; // Importar TimeZone

public class CrearRutina extends AppCompatActivity {

    // ... (variables existentes sin cambios)
    private EditText campoNombre;
    private EditText campoFecha;
    private EditText campoCategoria;
    private EditText campoActividades;
    private CheckBox checkboxRecordatorio;
    private TextView textViewHoraRecordatorio;

    private Button botonGuardarCrearRutina;
    private Button botonEliminarRutina;
    private TextView textViewTitulo;

    private boolean modoEdicion = false;
    private int posicionRutinaAEditar = -1;
    private Rutina rutinaActual;

    private int horaSeleccionadaAlarma = -1;
    private int minutoSeleccionadoAlarma = -1;

    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    Toast.makeText(this, "Permiso de notificaciones concedido.", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Permiso de notificaciones denegado. Los recordatorios no se mostrarán.", Toast.LENGTH_LONG).show();
                }
            });


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crear_rutina); //

        campoNombre = findViewById(R.id.imputNombre); //
        campoFecha = findViewById(R.id.imputFecha); //
        campoCategoria = findViewById(R.id.imputCategoria); //
        campoActividades = findViewById(R.id.imputActividades); //
        checkboxRecordatorio = findViewById(R.id.checkbox_recordatorio); //
        textViewHoraRecordatorio = findViewById(R.id.textview_hora_recordatorio); //

        botonGuardarCrearRutina = findViewById(R.id.CrearRutina); //
        botonEliminarRutina = findViewById(R.id.botonEliminarRutina); //
        textViewTitulo = findViewById(R.id.textViewTituloCrearRutina); //

        campoFecha.setFocusable(false);
        campoFecha.setClickable(true);
        campoFecha.setOnClickListener(v -> mostrarCalendario(campoFecha));

        checkboxRecordatorio.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                textViewHoraRecordatorio.setVisibility(isChecked ? View.VISIBLE : View.GONE);
                if (isChecked) {
                    solicitarPermisoNotificaciones();
                    if (horaSeleccionadaAlarma == -1 || minutoSeleccionadoAlarma == -1) {
                        textViewHoraRecordatorio.setText("Establecer hora");
                    } else {
                        actualizarTextoHoraRecordatorio();
                    }
                }
            }
        });

        textViewHoraRecordatorio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkboxRecordatorio.isChecked()) {
                    mostrarDialogoSeleccionHora();
                }
            }
        });

        if (getIntent().hasExtra(adaptadorRutinas.EXTRA_POSICION_RUTINA)) { //
            posicionRutinaAEditar = getIntent().getIntExtra(adaptadorRutinas.EXTRA_POSICION_RUTINA, -1);
            if (posicionRutinaAEditar != -1 && MenuPrincipal.listaRutinas != null && posicionRutinaAEditar < MenuPrincipal.listaRutinas.size()) {
                modoEdicion = true;
                rutinaActual = MenuPrincipal.listaRutinas.get(posicionRutinaAEditar); //
            } else {
                Toast.makeText(this, "Error: No se pudo cargar la rutina para editar.", Toast.LENGTH_LONG).show();
                finish();
                return;
            }
        }

        configurarUIModo();
        if (modoEdicion && rutinaActual != null) {
            cargarDatosRutina();
        } else {
            textViewHoraRecordatorio.setVisibility(View.GONE);
            horaSeleccionadaAlarma = -1;
            minutoSeleccionadoAlarma = -1;
            checkboxRecordatorio.setChecked(false);
        }

        botonGuardarCrearRutina.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                procesarGuardarOActualizar();
            }
        });

        if (modoEdicion) {
            botonEliminarRutina.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    confirmarEliminacion();
                }
            });
        }
    }

    private void solicitarPermisoNotificaciones() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.POST_NOTIFICATIONS)) {
                    new AlertDialog.Builder(this)
                            .setTitle("Permiso Necesario")
                            .setMessage("Esta aplicación necesita permiso para mostrar notificaciones para los recordatorios.")
                            .setPositiveButton("Entendido", (dialog, which) -> {
                                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
                            })
                            .setNegativeButton("Cancelar", (dialog, which) -> dialog.dismiss())
                            .create().show();
                } else {
                    requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
                }
            }
        }
        // Comprobar permiso de alarmas exactas
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
            if (alarmManager != null && !alarmManager.canScheduleExactAlarms()) {
            }
        }
    }


    private void configurarUIModo() {
        if (modoEdicion) {
            textViewTitulo.setText("Editar Rutina");
            botonGuardarCrearRutina.setText("Guardar Cambios");
            botonEliminarRutina.setVisibility(View.VISIBLE);
        } else {
            textViewTitulo.setText("Crear Rutina");
            botonGuardarCrearRutina.setText("Crear");
            botonEliminarRutina.setVisibility(View.GONE);
        }
    }

    private void cargarDatosRutina() {
        campoNombre.setText(rutinaActual.getNombre());
        campoFecha.setText(rutinaActual.getFecha());
        campoCategoria.setText(rutinaActual.getCategoria());

        checkboxRecordatorio.setChecked(rutinaActual.isRecordatorioActivo());
        if (rutinaActual.isRecordatorioActivo() && rutinaActual.getHoraRecordatorio() != null && !rutinaActual.getHoraRecordatorio().isEmpty()) {
            textViewHoraRecordatorio.setText(rutinaActual.getHoraRecordatorio());
            textViewHoraRecordatorio.setVisibility(View.VISIBLE);
            try {
                String[] partes = rutinaActual.getHoraRecordatorio().split(":");
                if (partes.length == 2) {
                    horaSeleccionadaAlarma = Integer.parseInt(partes[0]);
                    minutoSeleccionadoAlarma = Integer.parseInt(partes[1]);
                } else { horaSeleccionadaAlarma = -1; minutoSeleccionadoAlarma = -1;}
            } catch (NumberFormatException e) {
                horaSeleccionadaAlarma = -1; minutoSeleccionadoAlarma = -1;
                textViewHoraRecordatorio.setText("Error de hora");
            }
        } else {
            textViewHoraRecordatorio.setVisibility(View.GONE);
            textViewHoraRecordatorio.setText("--:--");
            horaSeleccionadaAlarma = -1;
            minutoSeleccionadoAlarma = -1;
        }

        if (rutinaActual.getActividades() != null && !rutinaActual.getActividades().isEmpty()) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < rutinaActual.getActividades().size(); i++) {
                sb.append(rutinaActual.getActividades().get(i).getNombre());
                if (i < rutinaActual.getActividades().size() - 1) { sb.append("\n"); }
            }
            campoActividades.setText(sb.toString());
        } else {
            campoActividades.setText("");
        }
    }

    private void mostrarDialogoSeleccionHora() {
        final Calendar c = Calendar.getInstance();
        int horaDialogo = (horaSeleccionadaAlarma != -1) ? horaSeleccionadaAlarma : c.get(Calendar.HOUR_OF_DAY);
        int minutoDialogo = (minutoSeleccionadoAlarma != -1) ? minutoSeleccionadoAlarma : c.get(Calendar.MINUTE);

        TimePickerDialog timePickerDialog = new TimePickerDialog(this,
                (view, hourOfDay, minute) -> {
                    horaSeleccionadaAlarma = hourOfDay;
                    minutoSeleccionadoAlarma = minute;
                    actualizarTextoHoraRecordatorio();
                }, horaDialogo, minutoDialogo, true);
        timePickerDialog.show();
    }

    private void actualizarTextoHoraRecordatorio() {
        if (horaSeleccionadaAlarma != -1 && minutoSeleccionadoAlarma != -1) {
            textViewHoraRecordatorio.setText(String.format(Locale.getDefault(), "%02d:%02d", horaSeleccionadaAlarma, minutoSeleccionadoAlarma));
        } else {
            textViewHoraRecordatorio.setText("Establecer hora");
        }
    }


    private void procesarGuardarOActualizar() {
        String nombreRutina = campoNombre.getText().toString().trim();
        String fechaRutinaStr = campoFecha.getText().toString().trim();
        String categoriaRutina = campoCategoria.getText().toString().trim();
        String textoActividades = campoActividades.getText().toString().trim();

        boolean recordatorioEstaActivo = checkboxRecordatorio.isChecked();
        String horaDelRecordatorioFormateada = "";
        long tiempoAlarmaMillis = 0;

        if (recordatorioEstaActivo) {
            if (horaSeleccionadaAlarma != -1 && minutoSeleccionadoAlarma != -1) {
                horaDelRecordatorioFormateada = String.format(Locale.getDefault(), "%02d:%02d", horaSeleccionadaAlarma, minutoSeleccionadoAlarma);
                tiempoAlarmaMillis = convertirFechaHoraAMillis(fechaRutinaStr, horaDelRecordatorioFormateada);
                if (tiempoAlarmaMillis == 0) { // Error de parseo
                    Toast.makeText(this, "Fecha u hora inválida para el recordatorio.", Toast.LENGTH_LONG).show();
                    return;
                }
                if (tiempoAlarmaMillis < System.currentTimeMillis()) {
                    Toast.makeText(this, "La fecha y hora del recordatorio deben ser futuras.", Toast.LENGTH_LONG).show();
                    // Considera si quieres permitir guardar de todas formas, o retornar.
                    // Por ahora, permitiremos guardar pero la alarma no se programará si es pasada.
                }
            } else {
                Toast.makeText(this, "Por favor, establece una hora para el recordatorio o desactívalo.", Toast.LENGTH_LONG).show();
                return;
            }
        }

        if (nombreRutina.isEmpty() || fechaRutinaStr.isEmpty()) {
            Toast.makeText(this, "El nombre y la fecha son obligatorios.", Toast.LENGTH_SHORT).show();
            return;
        }

        ArrayList<Actividad> listaDeObjetosActividad = new ArrayList<>();
        if (!textoActividades.isEmpty()) {
            String[] nombresActividadesArray = textoActividades.split("\\r?\\n");
            for (String nombreAct : nombresActividadesArray) {
                String nombreActividadLimpia = nombreAct.trim();
                if (!nombreActividadLimpia.isEmpty()) {
                    boolean encontradaYCompletada = false;
                    if (modoEdicion && rutinaActual != null && rutinaActual.getActividades() != null) {
                        for (Actividad actExistente : rutinaActual.getActividades()) {
                            if (actExistente.getNombre().equals(nombreActividadLimpia)) {
                                listaDeObjetosActividad.add(new Actividad(nombreActividadLimpia, actExistente.isCompletada()));
                                encontradaYCompletada = true;
                                break;
                            }
                        }
                    }
                    if (!encontradaYCompletada) {
                        listaDeObjetosActividad.add(new Actividad(nombreActividadLimpia));
                    }
                }
            }
        }

        if (modoEdicion) {
            if (rutinaActual != null && MenuPrincipal.listaRutinas != null && posicionRutinaAEditar >= 0 && posicionRutinaAEditar < MenuPrincipal.listaRutinas.size()) { //
                if (rutinaActual.isRecordatorioActivo()) {
                    cancelarAlarma(this, rutinaActual.getId());
                }

                rutinaActual.setNombre(nombreRutina);
                rutinaActual.setFecha(fechaRutinaStr);
                rutinaActual.setCategoria(categoriaRutina);
                rutinaActual.setActividades(listaDeObjetosActividad);
                rutinaActual.setRecordatorioActivo(recordatorioEstaActivo);
                rutinaActual.setHoraRecordatorio(horaDelRecordatorioFormateada);

                if (recordatorioEstaActivo && tiempoAlarmaMillis > System.currentTimeMillis()) {
                    programarAlarma(this, tiempoAlarmaMillis, rutinaActual.getId(), rutinaActual.getNombre(), rutinaActual.getHoraRecordatorio());
                }
                Toast.makeText(this, "Rutina '" + nombreRutina + "' actualizada.", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Error al actualizar la rutina. Datos no válidos.", Toast.LENGTH_LONG).show();
                return;
            }
        } else {
            Rutina nuevaRutina = new Rutina();
            nuevaRutina.setNombre(nombreRutina);
            nuevaRutina.setFecha(fechaRutinaStr);
            nuevaRutina.setCategoria(categoriaRutina);
            nuevaRutina.setActividades(listaDeObjetosActividad);
            nuevaRutina.setRecordatorioActivo(recordatorioEstaActivo);
            nuevaRutina.setHoraRecordatorio(horaDelRecordatorioFormateada);

            if (MenuPrincipal.listaRutinas != null) { //
                MenuPrincipal.listaRutinas.add(nuevaRutina); //
                if (recordatorioEstaActivo && tiempoAlarmaMillis > System.currentTimeMillis()) {
                    programarAlarma(this, tiempoAlarmaMillis, nuevaRutina.getId(), nuevaRutina.getNombre(), nuevaRutina.getHoraRecordatorio());
                }
                Toast.makeText(this, "Rutina '" + nombreRutina + "' creada.", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, "Error: La lista de rutinas no está inicializada.", Toast.LENGTH_LONG).show();
                return;
            }
        }
        finish();
    }

    private void confirmarEliminacion() {
        String nombreParaDialogo = (rutinaActual != null && rutinaActual.getNombre() != null) ? rutinaActual.getNombre() : "esta rutina";
        new AlertDialog.Builder(this)
                .setTitle("Eliminar Rutina")
                .setMessage("¿Estás seguro de que deseas eliminar la rutina '" + nombreParaDialogo + "'?")
                .setPositiveButton("Eliminar", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        if (modoEdicion && rutinaActual != null && MenuPrincipal.listaRutinas != null && posicionRutinaAEditar >=0 && posicionRutinaAEditar < MenuPrincipal.listaRutinas.size()) {
                            if (rutinaActual.isRecordatorioActivo()) {
                                cancelarAlarma(CrearRutina.this, rutinaActual.getId());
                            }
                            MenuPrincipal.listaRutinas.remove(posicionRutinaAEditar); //
                            Toast.makeText(CrearRutina.this, "Rutina eliminada.", Toast.LENGTH_SHORT).show();
                            finish();
                        } else {
                            Toast.makeText(CrearRutina.this, "Error al eliminar la rutina. Datos no válidos.", Toast.LENGTH_LONG).show();
                        }
                    }
                })
                .setNegativeButton("Cancelar", null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    private void mostrarCalendario(EditText campoFecha) {
        if (isFinishing() || isDestroyed()) { return; }

        MaterialDatePicker<Long> datePicker = MaterialDatePicker.Builder.datePicker()
                .setTitleText("Selecciona una fecha")
                .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
                .build();

        datePicker.addOnPositiveButtonClickListener(selection -> {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
            String fechaSeleccionada = sdf.format(new Date(selection));
            campoFecha.setText(fechaSeleccionada);
        });
        datePicker.show(getSupportFragmentManager(), "DATE_PICKER");
    }

    private long convertirFechaHoraAMillis(String fechaStr, String horaStr) {
        SimpleDateFormat formatoFechaHoraLocal = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
        try {
            Date fechaHoraDateLocal = formatoFechaHoraLocal.parse(fechaStr + " " + horaStr);
            if (fechaHoraDateLocal != null) {
                return fechaHoraDateLocal.getTime();
            }
        } catch (ParseException e) {
            Log.e("CrearRutina", "Error al parsear fecha-hora local: " + fechaStr + " " + horaStr, e);
            Toast.makeText(this, "Formato de fecha u hora inválido para el recordatorio.", Toast.LENGTH_SHORT).show();
        }
        return 0;
    }

    private void programarAlarma(Context context, long tiempoMillis, int rutinaId, String rutinaNombre, String rutinaHora) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(context, "Permiso de notificación no concedido. No se puede programar recordatorio.", Toast.LENGTH_LONG).show();
            if(checkboxRecordatorio.isChecked()) checkboxRecordatorio.setChecked(false);
            return;
        }

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, AlarmReceiver.class);
        intent.putExtra(AlarmReceiver.EXTRA_NOTIFICATION_ID, rutinaId);
        intent.putExtra(AlarmReceiver.EXTRA_RUTINA_NOMBRE, rutinaNombre);
        intent.putExtra(AlarmReceiver.EXTRA_RUTINA_HORA, rutinaHora);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, rutinaId, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        if (alarmManager != null) {
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) { // Android 12+
                    if (alarmManager.canScheduleExactAlarms()) {
                        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, tiempoMillis, pendingIntent);
                        Log.i("CrearRutina", "Alarma EXACTA programada para ID " + rutinaId + " a las " + new Date(tiempoMillis));
                    } else {
                        alarmManager.setWindow(AlarmManager.RTC_WAKEUP, tiempoMillis, 15 * 60 * 1000, pendingIntent);
                        Log.w("CrearRutina", "Alarma INEXACTA programada para ID " + rutinaId + " cerca de " + new Date(tiempoMillis) + ". Falta permiso SCHEDULE_EXACT_ALARM o no se puede programar exacta.");
                        // Toast.makeText(context, "Permiso para alarmas exactas no disponible. Recordatorio podría no ser preciso.", Toast.LENGTH_LONG).show();
                    }
                } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) { // Android 6 a 11
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, tiempoMillis, pendingIntent);
                    Log.i("CrearRutina", "Alarma EXACTA (M+) programada para ID " + rutinaId + " a las " + new Date(tiempoMillis));
                } else {
                    alarmManager.setExact(AlarmManager.RTC_WAKEUP, tiempoMillis, pendingIntent);
                    Log.i("CrearRutina", "Alarma EXACTA (pre-M) programada para ID " + rutinaId + " a las " + new Date(tiempoMillis));
                }
            } catch (SecurityException se) {
                Log.e("CrearRutina", "SecurityException al programar alarma.", se);
                Toast.makeText(context, "No se pudo programar el recordatorio debido a restricciones de seguridad.", Toast.LENGTH_LONG).show();
            }
        } else {
            Log.e("CrearRutina", "AlarmManager es null. No se puede programar alarma.");
        }
    }

    private void cancelarAlarma(Context context, int rutinaId) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, rutinaId, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        if (alarmManager != null && pendingIntent != null) {
            alarmManager.cancel(pendingIntent);
            pendingIntent.cancel();
            Log.i("CrearRutina", "Alarma cancelada para rutina ID: " + rutinaId);
        } else {
            Log.w("CrearRutina", "No se pudo cancelar alarma para ID: " + rutinaId + ". AlarmManager o PendingIntent null.");
        }
    }
}