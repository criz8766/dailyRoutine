package cl.example.dailyroutine;

import android.Manifest;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
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
import com.google.gson.Gson;
import android.content.SharedPreferences;

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
        String textoRecordatorioUsuario = editTextTextoRecordatorio.getText().toString().trim();
        long tiempoAlarmaMillis = 0;

        if (recordatorioEstaActivo) {
            if (horaSeleccionadaAlarma == -1 || minutoSeleccionadoAlarma == -1) {
                Toast.makeText(this, "Establece una hora para el recordatorio o desactívalo.", Toast.LENGTH_LONG).show();
                return;
            }
            if (diasSeleccionados.isEmpty() && fechaRutinaStr.isEmpty()) {
                Toast.makeText(this, "Selecciona al menos un día para el recordatorio o ingresa una fecha.", Toast.LENGTH_LONG).show();
                return;
            }

            horaDelRecordatorioFormateada = String.format(Locale.getDefault(), "%02d:%02d", horaSeleccionadaAlarma, minutoSeleccionadoAlarma);

            if (textoRecordatorioUsuario.isEmpty()) {
                textoRecordatorioUsuario = "Es hora de tu rutina: " + nombreRutina + " (" + horaDelRecordatorioFormateada + ")";
            }

            Calendar calendarAlarma = Calendar.getInstance();
            if (!fechaRutinaStr.isEmpty()) {
                try {
                    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                    Date fechaRutinaDate = sdf.parse(fechaRutinaStr);
                    if (fechaRutinaDate != null) {
                        calendarAlarma.setTime(fechaRutinaDate);
                    }
                } catch (ParseException e) {
                    Log.e("CrearRutina", "Error parsing date for alarm scheduling: " + e.getMessage());
                    if (diasSeleccionados.isEmpty()) {
                        recordatorioEstaActivo = false;
                        Toast.makeText(this, "Error en el formato de fecha. Recordatorio desactivado.", Toast.LENGTH_LONG).show();
                        return;
                    }
                }
            }

            calendarAlarma.set(Calendar.HOUR_OF_DAY, horaSeleccionadaAlarma);
            calendarAlarma.set(Calendar.MINUTE, minutoSeleccionadoAlarma);
            calendarAlarma.set(Calendar.SECOND, 0);
            calendarAlarma.set(Calendar.MILLISECOND, 0);

            if (!diasSeleccionados.isEmpty()) {
                long currentTimeMillis = System.currentTimeMillis();
                boolean foundNextDay = false;
                for (int i = 0; i < 7; i++) {
                    Calendar tempCal = (Calendar) calendarAlarma.clone();
                    tempCal.add(Calendar.DAY_OF_YEAR, i);
                    if (diasSeleccionados.contains(tempCal.get(Calendar.DAY_OF_WEEK))) {
                        if (tempCal.getTimeInMillis() < currentTimeMillis && i == 0) {
                            continue;
                        }
                        calendarAlarma.setTimeInMillis(tempCal.getTimeInMillis());
                        foundNextDay = true;
                        break;
                    }
                }
                if (!foundNextDay) {
                    Toast.makeText(this, "No se encontró un día válido para programar el recordatorio. Ajuste los días o la hora.", Toast.LENGTH_LONG).show();
                    recordatorioEstaActivo = false;
                    return;
                }
            } else {
                if (calendarAlarma.getTimeInMillis() < System.currentTimeMillis()) {
                    Toast.makeText(this, "La hora de recordatorio seleccionada ya ha pasado para la fecha especificada. Recordatorio no programado.", Toast.LENGTH_LONG).show();
                    recordatorioEstaActivo = false;
                    return;
                }
            }
            tiempoAlarmaMillis = calendarAlarma.getTimeInMillis();
        }


        if (nombreRutina.isEmpty()) {
            Toast.makeText(this, "El nombre de la rutina es obligatorio.", Toast.LENGTH_SHORT).show(); return;
        }
        if (diasSeleccionados.isEmpty() && fechaRutinaStr.isEmpty() && recordatorioEstaActivo) {
            Toast.makeText(this, "Selecciona días para programar o ingresa una fecha (o desactiva el recordatorio).", Toast.LENGTH_SHORT).show(); return;
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

                if (recordatorioEstaActivo) {
                    if (!diasSeleccionados.isEmpty()) {
                        programarAlarmaRepetitiva(this, rutinaActual.getId(), rutinaActual.getNombre(), rutinaActual.getHoraRecordatorio(), rutinaActual.getTextoRecordatorioPersonalizado(), rutinaActual.getDiasSemana());
                    } else if (tiempoAlarmaMillis > System.currentTimeMillis()) {
                        programarAlarma(this, tiempoAlarmaMillis, rutinaActual.getId(), rutinaActual.getNombre(), rutinaActual.getHoraRecordatorio(), rutinaActual.getTextoRecordatorioPersonalizado());
                    }
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
                if (recordatorioEstaActivo) {
                    if (!diasSeleccionados.isEmpty()) {
                        programarAlarmaRepetitiva(this, nuevaRutina.getId(), nuevaRutina.getNombre(), nuevaRutina.getHoraRecordatorio(), nuevaRutina.getTextoRecordatorioPersonalizado(), nuevaRutina.getDiasSemana());
                    } else if (tiempoAlarmaMillis > System.currentTimeMillis()) {
                        programarAlarma(this, tiempoAlarmaMillis, nuevaRutina.getId(), nuevaRutina.getNombre(), nuevaRutina.getHoraRecordatorio(), nuevaRutina.getTextoRecordatorioPersonalizado());
                    }
                }
                Toast.makeText(this, "Rutina creada.", Toast.LENGTH_LONG).show();
            } else { Toast.makeText(this, "Error al crear.", Toast.LENGTH_LONG).show(); return; }
        }

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
        intent.putExtra(AlarmReceiver.EXTRA_RUTINA_ID, rutinaId);

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

    private void programarAlarmaRepetitiva(Context context, int rutinaId, String rutinaNombre, String rutinaHora, String textoPersonalizado, List<Integer> diasSemana) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(context, "Permiso de notificación no concedido.", Toast.LENGTH_LONG).show();
            return;
        }

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null) return;

        cancelarAlarma(context, rutinaId);

        Calendar now = Calendar.getInstance();
        int currentDayOfWeek = now.get(Calendar.DAY_OF_WEEK);
        int currentHour = now.get(Calendar.HOUR_OF_DAY);
        int currentMinute = now.get(Calendar.MINUTE);

        int targetHour = Integer.parseInt(rutinaHora.split(":")[0]);
        int targetMinute = Integer.parseInt(rutinaHora.split(":")[1]);

        for (Integer dayOfWeek : diasSemana) {
            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.HOUR_OF_DAY, targetHour);
            calendar.set(Calendar.MINUTE, targetMinute);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);

            int daysToAdd = (dayOfWeek - currentDayOfWeek + 7) % 7;
            if (daysToAdd == 0) {
                if (calendar.getTimeInMillis() <= now.getTimeInMillis()) {
                    calendar.add(Calendar.WEEK_OF_YEAR, 1);
                }
            } else {
                calendar.add(Calendar.DAY_OF_YEAR, daysToAdd);
            }

            int uniqueRequestCode = rutinaId * 10 + dayOfWeek;

            Intent intent = new Intent(context, AlarmReceiver.class);
            intent.putExtra(AlarmReceiver.EXTRA_NOTIFICATION_ID, uniqueRequestCode);
            intent.putExtra(AlarmReceiver.EXTRA_RUTINA_NOMBRE, rutinaNombre);
            intent.putExtra(AlarmReceiver.EXTRA_RUTINA_HORA, rutinaHora);
            intent.putExtra(AlarmReceiver.EXTRA_TEXTO_PERSONALIZADO, textoPersonalizado);
            intent.putExtra(AlarmReceiver.EXTRA_DAY_OF_WEEK, dayOfWeek);
            intent.putExtra(AlarmReceiver.EXTRA_RUTINA_ID, rutinaId); // Pasa el ID de la rutina aquí

            PendingIntent pendingIntent = PendingIntent.getBroadcast(context, uniqueRequestCode, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
                } else {
                    alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
                }
                Log.i("CrearRutina", "Alarma repetitiva programada para ID " + rutinaId + " (Día " + dayOfWeek + ") a las " + rutinaHora + " en " + new Date(calendar.getTimeInMillis()));
            } catch (SecurityException se) { Log.e("CrearRutina", "SecurityException al programar alarma.", se); }
        }
    }


    private void cancelarAlarma(Context context, int rutinaId) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager != null) {
            Intent intent = new Intent(context, AlarmReceiver.class);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(context, rutinaId, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
            if (pendingIntent != null) {
                alarmManager.cancel(pendingIntent);
                pendingIntent.cancel();
                Log.i("CrearRutina", "Alarma única cancelada para ID: " + rutinaId);
            }

            for (int dayOfWeek = Calendar.SUNDAY; dayOfWeek <= Calendar.SATURDAY; dayOfWeek++) {
                int uniqueRequestCode = rutinaId * 10 + dayOfWeek;
                PendingIntent dailyPendingIntent = PendingIntent.getBroadcast(context, uniqueRequestCode, intent, PendingIntent.FLAG_NO_CREATE | PendingIntent.FLAG_IMMUTABLE);
                if (dailyPendingIntent != null) {
                    alarmManager.cancel(dailyPendingIntent);
                    dailyPendingIntent.cancel();
                    Log.i("CrearRutina", "Alarma repetitiva cancelada para ID: " + rutinaId + " (Día " + dayOfWeek + ")");
                }
            }
        }
    }

    private void guardarRutinasLocales() {
        SharedPreferences sharedPreferences = getSharedPreferences(MenuPrincipal.PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        Gson gson = new Gson();
        String json = gson.toJson(MenuPrincipal.listaRutinas);
        editor.putString(MenuPrincipal.KEY_RUTINAS, json);
        editor.apply();
        Log.d("CrearRutina", "Rutinas guardadas localmente.");
    }
}