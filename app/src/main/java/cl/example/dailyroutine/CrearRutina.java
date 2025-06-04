// app/src/main/java/cl/example/dailyroutine/CrearRutina.java
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
import com.google.gson.Gson; // Importar Gson
import android.content.SharedPreferences; // Importar SharedPreferences

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class CrearRutina extends AppCompatActivity {

    private EditText campoNombre;
    private EditText campoFecha;
    private EditText campoCategoria;
    private EditText campoActividades;
    private CheckBox checkboxRecordatorio;
    private TextView textViewHoraRecordatorio;
    private EditText editTextTextoRecordatorio;

    private CheckBox checkboxLunes;
    private CheckBox checkboxMartes;
    private CheckBox checkboxMiercoles;
    private CheckBox checkboxJueves;
    private CheckBox checkboxViernes;
    private CheckBox checkboxSabado;
    private CheckBox checkboxDomingo;

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
                    Toast.makeText(this, "Permiso denegado. Los recordatorios podrían no funcionar.", Toast.LENGTH_LONG).show();
                    if (checkboxRecordatorio.isChecked()) {
                        checkboxRecordatorio.setChecked(false);
                    }
                }
            });


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crear_rutina);

        campoNombre = findViewById(R.id.imputNombre);
        campoFecha = findViewById(R.id.imputFecha);
        campoCategoria = findViewById(R.id.imputCategoria);
        campoActividades = findViewById(R.id.imputActividades);
        checkboxRecordatorio = findViewById(R.id.checkbox_recordatorio);
        textViewHoraRecordatorio = findViewById(R.id.textview_hora_recordatorio);
        editTextTextoRecordatorio = findViewById(R.id.edittext_texto_recordatorio);

        checkboxLunes = findViewById(R.id.checkbox_lunes);
        checkboxMartes = findViewById(R.id.checkbox_martes);
        checkboxMiercoles = findViewById(R.id.checkbox_miercoles);
        checkboxJueves = findViewById(R.id.checkbox_jueves);
        checkboxViernes = findViewById(R.id.checkbox_viernes);
        checkboxSabado = findViewById(R.id.checkbox_sabado);
        checkboxDomingo = findViewById(R.id.checkbox_domingo);

        botonGuardarCrearRutina = findViewById(R.id.CrearRutina);
        botonEliminarRutina = findViewById(R.id.botonEliminarRutina);
        textViewTitulo = findViewById(R.id.textViewTituloCrearRutina);

        campoFecha.setFocusable(false);
        campoFecha.setClickable(true);
        campoFecha.setOnClickListener(v -> mostrarCalendario(campoFecha));

        checkboxRecordatorio.setOnCheckedChangeListener((buttonView, isChecked) -> {
            textViewHoraRecordatorio.setVisibility(isChecked ? View.VISIBLE : View.GONE);
            editTextTextoRecordatorio.setVisibility(isChecked ? View.VISIBLE : View.GONE);
            if (isChecked) {
                solicitarPermisoNotificaciones();
                if (horaSeleccionadaAlarma == -1) {
                    textViewHoraRecordatorio.setText("Establecer hora");
                } else {
                    actualizarTextoHoraRecordatorio();
                }
            }
        });

        textViewHoraRecordatorio.setOnClickListener(v -> {
            if (checkboxRecordatorio.isChecked()) mostrarDialogoSeleccionHora();
        });

        if (getIntent().hasExtra(adaptadorRutinas.EXTRA_POSICION_RUTINA)) {
            posicionRutinaAEditar = getIntent().getIntExtra(adaptadorRutinas.EXTRA_POSICION_RUTINA, -1);
            if (posicionRutinaAEditar != -1 && MenuPrincipal.listaRutinas != null && posicionRutinaAEditar < MenuPrincipal.listaRutinas.size()) {
                modoEdicion = true;
                rutinaActual = MenuPrincipal.listaRutinas.get(posicionRutinaAEditar);
            } else {
                Toast.makeText(this, "Error al cargar rutina para editar.", Toast.LENGTH_LONG).show();
                finish(); return;
            }
        }

        configurarUIModo();
        if (modoEdicion && rutinaActual != null) {
            cargarDatosRutina();
        } else {
            textViewHoraRecordatorio.setVisibility(View.GONE);
            editTextTextoRecordatorio.setVisibility(View.GONE);
            horaSeleccionadaAlarma = -1;
            minutoSeleccionadoAlarma = -1;
            checkboxRecordatorio.setChecked(false);
            checkboxLunes.setChecked(false);
            checkboxMartes.setChecked(false);
            checkboxMiercoles.setChecked(false);
            checkboxJueves.setChecked(false);
            checkboxViernes.setChecked(false);
            checkboxSabado.setChecked(false);
            checkboxDomingo.setChecked(false);
        }

        botonGuardarCrearRutina.setOnClickListener(v -> procesarGuardarOActualizar());
        if (modoEdicion) {
            botonEliminarRutina.setOnClickListener(v -> confirmarEliminacion());
        }
    }

    private void solicitarPermisoNotificaciones() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.POST_NOTIFICATIONS)) {
                    new AlertDialog.Builder(this)
                            .setTitle("Permiso Necesario")
                            .setMessage("Para recibir recordatorios, la app necesita permiso para mostrar notificaciones.")
                            .setPositiveButton("Conceder", (dialog, which) -> requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS))
                            .setNegativeButton("Cancelar", (dialog, which) -> checkboxRecordatorio.setChecked(false))
                            .create().show();
                } else {
                    requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
                }
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
        if (rutinaActual.isRecordatorioActivo()) {
            textViewHoraRecordatorio.setVisibility(View.VISIBLE);
            editTextTextoRecordatorio.setVisibility(View.VISIBLE);
            if (rutinaActual.getHoraRecordatorio() != null && !rutinaActual.getHoraRecordatorio().isEmpty()) {
                textViewHoraRecordatorio.setText(rutinaActual.getHoraRecordatorio());
                try {
                    String[] partes = rutinaActual.getHoraRecordatorio().split(":");
                    if (partes.length == 2) {
                        horaSeleccionadaAlarma = Integer.parseInt(partes[0]);
                        minutoSeleccionadoAlarma = Integer.parseInt(partes[1]);
                    } else { horaSeleccionadaAlarma = -1; minutoSeleccionadoAlarma = -1; }
                } catch (NumberFormatException e) { horaSeleccionadaAlarma = -1; minutoSeleccionadoAlarma = -1;}
            } else {
                textViewHoraRecordatorio.setText("--:--");
                horaSeleccionadaAlarma = -1; minutoSeleccionadoAlarma = -1;
            }
            editTextTextoRecordatorio.setText(rutinaActual.getTextoRecordatorioPersonalizado());
        } else {
            textViewHoraRecordatorio.setVisibility(View.GONE);
            editTextTextoRecordatorio.setVisibility(View.GONE);
            textViewHoraRecordatorio.setText("--:--");
            horaSeleccionadaAlarma = -1; minutoSeleccionadoAlarma = -1;
            editTextTextoRecordatorio.setText("");
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

        if (rutinaActual.getDiasSemana() != null) {
            List<Integer> diasProgramados = rutinaActual.getDiasSemana();
            checkboxLunes.setChecked(diasProgramados.contains(Calendar.MONDAY));
            checkboxMartes.setChecked(diasProgramados.contains(Calendar.TUESDAY));
            checkboxMiercoles.setChecked(diasProgramados.contains(Calendar.WEDNESDAY));
            checkboxJueves.setChecked(diasProgramados.contains(Calendar.THURSDAY));
            checkboxViernes.setChecked(diasProgramados.contains(Calendar.FRIDAY));
            checkboxSabado.setChecked(diasProgramados.contains(Calendar.SATURDAY));
            checkboxDomingo.setChecked(diasProgramados.contains(Calendar.SUNDAY));
        } else {
            checkboxLunes.setChecked(false);
            checkboxMartes.setChecked(false);
            checkboxMiercoles.setChecked(false);
            checkboxJueves.setChecked(false);
            checkboxViernes.setChecked(false);
            checkboxSabado.setChecked(false);
            checkboxDomingo.setChecked(false);
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

        List<Integer> diasSeleccionados = new ArrayList<>();
        if (checkboxLunes.isChecked()) diasSeleccionados.add(Calendar.MONDAY);
        if (checkboxMartes.isChecked()) diasSeleccionados.add(Calendar.TUESDAY);
        if (checkboxMiercoles.isChecked()) diasSeleccionados.add(Calendar.WEDNESDAY);
        if (checkboxJueves.isChecked()) diasSeleccionados.add(Calendar.THURSDAY);
        if (checkboxViernes.isChecked()) diasSeleccionados.add(Calendar.FRIDAY);
        if (checkboxSabado.isChecked()) diasSeleccionados.add(Calendar.SATURDAY);
        if (checkboxDomingo.isChecked()) diasSeleccionados.add(Calendar.SUNDAY);

        boolean recordatorioEstaActivo = checkboxRecordatorio.isChecked();
        String horaDelRecordatorioFormateada = "";
        String textoRecordatorioUsuario = "";
        long tiempoAlarmaMillis = 0;

        if (recordatorioEstaActivo) {
            if (horaSeleccionadaAlarma != -1 && minutoSeleccionadoAlarma != -1) {
                horaDelRecordatorioFormateada = String.format(Locale.getDefault(), "%02d:%02d", horaSeleccionadaAlarma, minutoSeleccionadoAlarma);
                textoRecordatorioUsuario = editTextTextoRecordatorio.getText().toString().trim();

                tiempoAlarmaMillis = convertirFechaHoraAMillis(fechaRutinaStr, horaDelRecordatorioFormateada);

                if (tiempoAlarmaMillis > 0 && tiempoAlarmaMillis < System.currentTimeMillis()) {
                    Calendar cal = Calendar.getInstance();
                    try {
                        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                        Date fechaRutinaDate = sdf.parse(fechaRutinaStr);
                        if (fechaRutinaDate != null) {
                            cal.setTime(fechaRutinaDate);
                        }
                    } catch (ParseException e) {
                        Log.e("CrearRutina", "Error parsing date for alarm scheduling");
                        recordatorioEstaActivo = false;
                        tiempoAlarmaMillis = 0;
                    }

                    if (recordatorioEstaActivo) {
                        cal.set(Calendar.HOUR_OF_DAY, horaSeleccionadaAlarma);
                        cal.set(Calendar.MINUTE, minutoSeleccionadoAlarma);
                        cal.set(Calendar.SECOND, 0);
                        cal.set(Calendar.MILLISECOND, 0);

                        Calendar now = Calendar.getInstance();
                        if (cal.getTimeInMillis() < now.getTimeInMillis()) {
                            cal.add(Calendar.DAY_OF_YEAR, 1);
                            while (!diasSeleccionados.contains(cal.get(Calendar.DAY_OF_WEEK))) {
                                cal.add(Calendar.DAY_OF_YEAR, 1);
                            }
                        } else {
                            if (!diasSeleccionados.contains(cal.get(Calendar.DAY_OF_WEEK))) {
                                cal.add(Calendar.DAY_OF_YEAR, 1);
                                while (!diasSeleccionados.contains(cal.get(Calendar.DAY_OF_WEEK))) {
                                    cal.add(Calendar.DAY_OF_YEAR, 1);
                                }
                            }
                        }
                        tiempoAlarmaMillis = cal.getTimeInMillis();
                    }
                }
                if (diasSeleccionados.isEmpty()) {
                    Toast.makeText(this, "Selecciona al menos un día para el recordatorio.", Toast.LENGTH_LONG).show();
                    recordatorioEstaActivo = false;
                    tiempoAlarmaMillis = 0;
                }


            } else {
                Toast.makeText(this, "Establece una hora para el recordatorio o desactívalo.", Toast.LENGTH_LONG).show(); return;
            }
        }


        if (nombreRutina.isEmpty()) {
            Toast.makeText(this, "El nombre de la rutina es obligatorio.", Toast.LENGTH_SHORT).show(); return;
        }
        if (diasSeleccionados.isEmpty() && fechaRutinaStr.isEmpty()) {
            Toast.makeText(this, "Selecciona días para programar o ingresa una fecha.", Toast.LENGTH_SHORT).show(); return;
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
                                encontradaYCompletada = true; break;
                            }
                        }
                    }
                    if (!encontradaYCompletada) listaDeObjetosActividad.add(new Actividad(nombreActividadLimpia));
                }
            }
        }

        if (modoEdicion) {
            if (rutinaActual != null && MenuPrincipal.listaRutinas != null && posicionRutinaAEditar >= 0 && posicionRutinaAEditar < MenuPrincipal.listaRutinas.size()) {
                if (rutinaActual.isRecordatorioActivo()) cancelarAlarma(this, rutinaActual.getId());

                rutinaActual.setNombre(nombreRutina);
                rutinaActual.setFecha(fechaRutinaStr);
                rutinaActual.setCategoria(categoriaRutina);
                rutinaActual.setActividades(listaDeObjetosActividad);
                rutinaActual.setRecordatorioActivo(recordatorioEstaActivo);
                rutinaActual.setHoraRecordatorio(horaDelRecordatorioFormateada);
                rutinaActual.setTextoRecordatorioPersonalizado(textoRecordatorioUsuario);
                rutinaActual.setDiasSemana(diasSeleccionados);

                if (recordatorioEstaActivo && !diasSeleccionados.isEmpty() && tiempoAlarmaMillis > System.currentTimeMillis()) {
                    programarAlarma(this, tiempoAlarmaMillis, rutinaActual.getId(), rutinaActual.getNombre(), rutinaActual.getHoraRecordatorio(), rutinaActual.getTextoRecordatorioPersonalizado());
                } else if (recordatorioEstaActivo && diasSeleccionados.isEmpty()) {
                    Toast.makeText(this, "Recordatorio activo pero sin días seleccionados.", Toast.LENGTH_SHORT).show();
                } else if (recordatorioEstaActivo && tiempoAlarmaMillis <= System.currentTimeMillis()) {
                    Toast.makeText(this, "Hora de recordatorio en el pasado. Ajusta la hora.", Toast.LENGTH_LONG).show();
                }

                Toast.makeText(this, "Rutina actualizada.", Toast.LENGTH_SHORT).show();
            } else { Toast.makeText(this, "Error al actualizar.", Toast.LENGTH_LONG).show(); return; }
        } else {
            Rutina nuevaRutina = new Rutina();
            nuevaRutina.setNombre(nombreRutina);
            nuevaRutina.setFecha(fechaRutinaStr);
            nuevaRutina.setCategoria(categoriaRutina);
            nuevaRutina.setActividades(listaDeObjetosActividad);
            nuevaRutina.setRecordatorioActivo(recordatorioEstaActivo);
            nuevaRutina.setHoraRecordatorio(horaDelRecordatorioFormateada);
            nuevaRutina.setTextoRecordatorioPersonalizado(textoRecordatorioUsuario);
            nuevaRutina.setDiasSemana(diasSeleccionados);

            if (MenuPrincipal.listaRutinas != null) {
                MenuPrincipal.listaRutinas.add(nuevaRutina);
                if (recordatorioEstaActivo && !diasSeleccionados.isEmpty() && tiempoAlarmaMillis > System.currentTimeMillis()) {
                    programarAlarma(this, tiempoAlarmaMillis, nuevaRutina.getId(), nuevaRutina.getNombre(), nuevaRutina.getHoraRecordatorio(), nuevaRutina.getTextoRecordatorioPersonalizado());
                } else if (recordatorioEstaActivo && diasSeleccionados.isEmpty()) {
                    Toast.makeText(this, "Recordatorio activo pero sin días seleccionados.", Toast.LENGTH_SHORT).show();
                } else if (recordatorioEstaActivo && tiempoAlarmaMillis <= System.currentTimeMillis()) {
                    Toast.makeText(this, "Hora de recordatorio en el pasado. Ajusta la hora.", Toast.LENGTH_LONG).show();
                }
                Toast.makeText(this, "Rutina creada.", Toast.LENGTH_LONG).show();
            } else { Toast.makeText(this, "Error al crear.", Toast.LENGTH_LONG).show(); return; }
        }

        // Llamar a guardarRutinas() al finalizar la operación
        guardarRutinasLocales();
        finish();
    }

    private void confirmarEliminacion() {
        String nombreParaDialogo = (rutinaActual != null && rutinaActual.getNombre() != null) ? rutinaActual.getNombre() : "esta rutina";
        new AlertDialog.Builder(this)
                .setTitle("Eliminar Rutina")
                .setMessage("¿Seguro que deseas eliminar '" + nombreParaDialogo + "'?")
                .setPositiveButton("Eliminar", (dialog, which) -> {
                    if (modoEdicion && rutinaActual != null && MenuPrincipal.listaRutinas != null && posicionRutinaAEditar >=0 && posicionRutinaAEditar < MenuPrincipal.listaRutinas.size()) {
                        if (rutinaActual.isRecordatorioActivo()) cancelarAlarma(CrearRutina.this, rutinaActual.getId());
                        MenuPrincipal.listaRutinas.remove(posicionRutinaAEditar);
                        Toast.makeText(CrearRutina.this, "Rutina eliminada.", Toast.LENGTH_SHORT).show();
                        // Llamar a guardarRutinas() después de eliminar
                        guardarRutinasLocales();
                        finish();
                    } else Toast.makeText(CrearRutina.this, "Error al eliminar.", Toast.LENGTH_LONG).show();
                })
                .setNegativeButton("Cancelar", null).setIcon(android.R.drawable.ic_dialog_alert).show();
    }

    private void mostrarCalendario(EditText campoFecha) {
        if (isFinishing() || isDestroyed()) { return; }
        MaterialDatePicker<Long> datePicker = MaterialDatePicker.Builder.datePicker().setTitleText("Selecciona una fecha").setSelection(MaterialDatePicker.todayInUtcMilliseconds()).build();
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
            if (fechaHoraDateLocal != null) return fechaHoraDateLocal.getTime();
        } catch (ParseException e) { Log.e("CrearRutina", "Error al parsear fecha-hora: " + e.getMessage()); }
        return 0;
    }

    private void programarAlarma(Context context, long tiempoMillis, int rutinaId, String rutinaNombre, String rutinaHora, String textoPersonalizado) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(context, "Permiso de notificación no concedido.", Toast.LENGTH_LONG).show();
            return;
        }
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, AlarmReceiver.class);
        intent.putExtra(AlarmReceiver.EXTRA_NOTIFICATION_ID, rutinaId);
        intent.putExtra(AlarmReceiver.EXTRA_RUTINA_NOMBRE, rutinaNombre);
        intent.putExtra(AlarmReceiver.EXTRA_RUTINA_HORA, rutinaHora);
        intent.putExtra(AlarmReceiver.EXTRA_TEXTO_PERSONALIZADO, textoPersonalizado);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, rutinaId, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        if (alarmManager != null) {
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    if (alarmManager.canScheduleExactAlarms()) alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, tiempoMillis, pendingIntent);
                    else alarmManager.setWindow(AlarmManager.RTC_WAKEUP, tiempoMillis, 15 * 60 * 1000, pendingIntent);
                } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, tiempoMillis, pendingIntent);
                else alarmManager.setExact(AlarmManager.RTC_WAKEUP, tiempoMillis, pendingIntent);

                Log.i("CrearRutina", "Alarma programada para ID " + rutinaId + " en " + new Date(tiempoMillis));
            } catch (SecurityException se) { Log.e("CrearRutina", "SecurityException al programar alarma.", se); }
        }
    }

    private void cancelarAlarma(Context context, int rutinaId) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, rutinaId, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        if (alarmManager != null && pendingIntent != null) {
            alarmManager.cancel(pendingIntent);
            pendingIntent.cancel();
            Log.i("CrearRutina", "Alarma cancelada para ID: " + rutinaId);
        }
    }

    // Método para guardar las rutinas. Llama a un método estático o público en MenuPrincipal
    private void guardarRutinasLocales() {
        // Accede a SharedPreferences y Gson directamente para guardar la lista estática
        SharedPreferences sharedPreferences = getSharedPreferences(MenuPrincipal.PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        Gson gson = new Gson();
        String json = gson.toJson(MenuPrincipal.listaRutinas); // Accede a la lista estática
        editor.putString(MenuPrincipal.KEY_RUTINAS, json);
        editor.apply();
        Log.d("CrearRutina", "Rutinas guardadas localmente.");
    }
}