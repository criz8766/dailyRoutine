package cl.example.dailyroutine;

import java.util.ArrayList;
import java.util.List; // Importar List
import java.util.Calendar; // Importar Calendar si usaremos sus constantes
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Rutina {

    private int id;
    private String nombre;
    private String fecha; // Podría representar la fecha de creación ahora
    private ArrayList<Actividad> actividades;
    private String categoria;
    private boolean recordatorioActivo;
    private String horaRecordatorio;   // Formato "HH:mm" - Usado también para la hora de la rutina programada
    private String textoRecordatorioPersonalizado;
    private List<Integer> diasSemana; // <-- Nuevo campo para los días de la semana programados

    private static int proximoId = 1;

    private static synchronized int generarIdUnico() {
        return proximoId++;
    }

    public Rutina() {
        this.id = generarIdUnico();
        this.actividades = new ArrayList<>();
        this.categoria = "";
        this.recordatorioActivo = false;
        this.horaRecordatorio = "08:00"; // Hora por defecto
        this.textoRecordatorioPersonalizado = ""; // Por defecto vacío
        this.diasSemana = new ArrayList<>(); // <-- Inicializar la nueva lista de días
    }

    public Rutina(String nombre, String fecha) {
        this(); // Llama al constructor por defecto
        this.nombre = nombre;
        this.fecha = fecha; // Asignar la fecha (posiblemente fecha de creación)
    }

    public Rutina(String nombre, String fecha, ArrayList<Actividad> actividades, String categoria) {
        this(nombre, fecha); // Llama al constructor anterior
        this.actividades = (actividades != null) ? actividades : new ArrayList<>();
        this.categoria = (categoria != null) ? categoria.trim() : "";
    }

    // Constructor más completo, incluyendo diasSemana
    public Rutina(int id, String nombre, String fecha, ArrayList<Actividad> actividades, String categoria,
                  boolean recordatorioActivo, String horaRecordatorio, String textoRecordatorioPersonalizado,
                  List<Integer> diasSemana) { // <-- Añadir diasSemana como parámetro
        this.id = (id == 0 || id == -1) ? generarIdUnico() : id;
        this.nombre = nombre;
        this.fecha = fecha;
        this.actividades = (actividades != null) ? actividades : new ArrayList<>();
        this.categoria = (categoria != null) ? categoria.trim() : "";
        this.recordatorioActivo = recordatorioActivo;
        this.horaRecordatorio = (horaRecordatorio != null && horaRecordatorio.matches("\\d{2}:\\d{2}")) ? horaRecordatorio : "08:00";
        this.textoRecordatorioPersonalizado = (textoRecordatorioPersonalizado != null) ? textoRecordatorioPersonalizado.trim() : "";
        this.diasSemana = (diasSemana != null) ? new ArrayList<>(diasSemana) : new ArrayList<>(); // <-- Asignar la lista de días
    }


    // Getters (añadir getter para diasSemana)
    public int getId() { return id; }
    public String getNombre() { return nombre; }
    public String getFecha() { return fecha; } // Mantener por ahora
    public ArrayList<Actividad> getActividades() { return actividades; }
    public String getCategoria() { return categoria; }
    public boolean isRecordatorioActivo() { return recordatorioActivo; }
    public String getHoraRecordatorio() { return horaRecordatorio; }
    public String getTextoRecordatorioPersonalizado() { return textoRecordatorioPersonalizado; }
    public List<Integer> getDiasSemana() { return diasSemana; } // <-- Nuevo getter

    // Setters (añadir setter para diasSemana)
    public void setNombre(String nombre) { this.nombre = nombre; }
    public void setFecha(String fecha) { this.fecha = fecha; } // Mantener por ahora
    public void setActividades(ArrayList<Actividad> actividades) { this.actividades = (actividades != null) ? actividades : new ArrayList<>(); }
    public void setCategoria(String categoria) { this.categoria = (categoria != null) ? categoria.trim() : "";}
    public void setRecordatorioActivo(boolean recordatorioActivo) { this.recordatorioActivo = recordatorioActivo; }
    public void setHoraRecordatorio(String horaRecordatorio) {
        if (horaRecordatorio != null && horaRecordatorio.matches("\\d{2}:\\d{2}")) {
            this.horaRecordatorio = horaRecordatorio;
        } else { this.horaRecordatorio = "08:00"; } // Valor por defecto si el formato es inválido
    }
    public void setTextoRecordatorioPersonalizado(String textoRecordatorioPersonalizado) {
        this.textoRecordatorioPersonalizado = (textoRecordatorioPersonalizado != null) ? textoRecordatorioPersonalizado.trim() : "";
    }
    public void setDiasSemana(List<Integer> diasSemana) { this.diasSemana = (diasSemana != null) ? new ArrayList<>(diasSemana) : new ArrayList<>(); } // <-- Nuevo setter


    // Métodos de utilidad
    public void addActividad(Actividad actividad) {
        if (this.actividades == null) { this.actividades = new ArrayList<>(); }
        if (actividad != null) { this.actividades.add(actividad); }
    }
    public void addActividad(String nombreActividad) { addActividad(new Actividad(nombreActividad)); }
    public String getResumenCumplimiento() {
        if (actividades == null || actividades.isEmpty()) { return "Sin actividades"; }
        int completadas = 0;
        for (Actividad act : actividades) { if (act.isCompletada()) { completadas++; } }
        return completadas + " de " + actividades.size() + " completadas";
    }
    public boolean todasActividadesCompletadas() {
        if (actividades == null || actividades.isEmpty()) { return true; } // O false, según lógica de negocio
        for (Actividad actividad : actividades) { if (!actividad.isCompletada()) { return false; } }
        return true;
    }

    // Método de utilidad para verificar si la rutina está programada para un día específico
    public boolean isScheduledForDay(int dayOfWeek) {
        return diasSemana.contains(dayOfWeek);
    }
}
