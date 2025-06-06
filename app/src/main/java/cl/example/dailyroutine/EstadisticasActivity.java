package cl.example.dailyroutine;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.DayViewDecorator;
import com.prolificinteractive.materialcalendarview.DayViewFacade;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;

import java.lang.reflect.Type;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

public class EstadisticasActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private TextView tvTasaCumplimiento;
    private TextView tvTotalCompletadas;
    private MaterialCalendarView calendarView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_estadisticas);

        toolbar = findViewById(R.id.toolbar_estadisticas);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        tvTasaCumplimiento = findViewById(R.id.tv_tasa_cumplimiento);
        tvTotalCompletadas = findViewById(R.id.tv_total_completadas);
        calendarView = findViewById(R.id.calendar_view_estadisticas);

        calcularYMostrarEstadisticas();
        decorarCalendario();
    }

    private void decorarCalendario() {
        SharedPreferences prefs = getSharedPreferences(GestorDeRachas.PREFS_RACHAS_NAME, MODE_PRIVATE);
        Gson gson = new Gson();

        String jsonCompletadas = prefs.getString(GestorDeRachas.KEY_FECHAS_RUTINAS_COMPLETADAS, null);
        Set<String> fechasCompletadasStr = new HashSet<>();
        if (jsonCompletadas != null) {
            Type type = new TypeToken<HashSet<String>>() {}.getType();
            fechasCompletadasStr = gson.fromJson(jsonCompletadas, type);
        }

        String jsonCongelador = prefs.getString(GestorDeRachas.KEY_FECHAS_CONGELADOR_USADO, null);
        Set<String> fechasCongeladorStr = new HashSet<>();
        if (jsonCongelador != null) {
            Type type = new TypeToken<HashSet<String>>() {}.getType();
            fechasCongeladorStr = gson.fromJson(jsonCongelador, type);
        }

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        Set<CalendarDay> diasCompletados = new HashSet<>();
        for (String dateStr : fechasCompletadasStr) {
            try {
                Date date = sdf.parse(dateStr);
                if (date != null) {
                    Calendar cal = Calendar.getInstance();
                    cal.setTime(date);
                    int year = cal.get(Calendar.YEAR);
                    int month = cal.get(Calendar.MONTH) + 1;
                    int day = cal.get(Calendar.DAY_OF_MONTH);
                    diasCompletados.add(CalendarDay.from(year, month, day));
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }

        Set<CalendarDay> diasCongelador = new HashSet<>();
        for (String dateStr : fechasCongeladorStr) {
            try {
                Date date = sdf.parse(dateStr);
                if (date != null) {
                    Calendar cal = Calendar.getInstance();
                    cal.setTime(date);
                    int year = cal.get(Calendar.YEAR);
                    int month = cal.get(Calendar.MONTH) + 1;
                    int day = cal.get(Calendar.DAY_OF_MONTH);
                    diasCongelador.add(CalendarDay.from(year, month, day));
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }

        calendarView.addDecorators(
                new TodayDecorator(this),
                new EventDecorator(ContextCompat.getDrawable(this, R.drawable.decorator_complete_day), diasCompletados),
                new EventDecorator(ContextCompat.getDrawable(this, R.drawable.decorator_freezer_day), diasCongelador)
        );
    }

    private void calcularYMostrarEstadisticas() {
        if (MenuPrincipal.listaRutinas == null || MenuPrincipal.listaRutinas.isEmpty()) {
            tvTasaCumplimiento.setText("Tasa de Cumplimiento (7 días): Sin datos");
            tvTotalCompletadas.setText("Total de Rutinas Completadas: 0");
            return;
        }

        int totalCompletadas = 0;
        for (Rutina rutina : MenuPrincipal.listaRutinas) {
            if (rutina.todasActividadesCompletadas()) {
                totalCompletadas++;
            }
        }
        tvTotalCompletadas.setText("Total de Rutinas Completadas: " + totalCompletadas);

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, -7);
        Date haceSieteDias = cal.getTime();

        int rutinasEnPeriodo = 0;
        int rutinasCompletadasEnPeriodo = 0;

        for (Rutina rutina : MenuPrincipal.listaRutinas) {
            try {
                Date fechaRutina = sdf.parse(rutina.getFecha());
                if (fechaRutina != null && !fechaRutina.before(haceSieteDias)) {
                    rutinasEnPeriodo++;
                    if (rutina.todasActividadesCompletadas()) {
                        rutinasCompletadasEnPeriodo++;
                    }
                }
            } catch (ParseException e) {
                // Ignorar
            }
        }

        if (rutinasEnPeriodo > 0) {
            double tasa = ((double) rutinasCompletadasEnPeriodo / rutinasEnPeriodo) * 100;
            tvTasaCumplimiento.setText(String.format(Locale.getDefault(), "Tasa de Cumplimiento (7 días): %.1f%%", tasa));
        } else {
            tvTasaCumplimiento.setText("Tasa de Cumplimiento (7 días): Sin rutinas recientes");
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}


class TodayDecorator implements DayViewDecorator {
    private final CalendarDay today;
    private final Drawable background;

    public TodayDecorator(Context context) {
        today = CalendarDay.today();
        background = ContextCompat.getDrawable(context, R.drawable.decorator_today);
    }

    @Override
    public boolean shouldDecorate(CalendarDay day) {
        return day.equals(today);
    }

    @Override
    public void decorate(DayViewFacade view) {
        view.setBackgroundDrawable(background);
    }
}

class EventDecorator implements DayViewDecorator {
    private final Drawable drawable;
    private final HashSet<CalendarDay> dates;

    public EventDecorator(Drawable drawable, Collection<CalendarDay> dates) {
        this.drawable = drawable;
        this.dates = new HashSet<>(dates);
    }

    @Override
    public boolean shouldDecorate(CalendarDay day) {
        return dates.contains(day);
    }

    @Override
    public void decorate(DayViewFacade view) {
        view.setBackgroundDrawable(drawable);
    }
}
