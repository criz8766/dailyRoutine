package cl.example.dailyroutine;

import java.util.ArrayList;

public class Rutina {

    private int id;
    private String nombre;
    private String fecha;
    private ArrayList<Actividad> actividades;
    private String categoria;
    private boolean recordatorioActivo;
    private String horaRecordatorio;   // Formato "HH:mm"
    private String textoRecordatorioPersonalizado; // Nuevo campo

    private static int proximoId = 1;

    private static synchronized int generarIdUnico() {
        return proximoId++;
    }

    public Rutina() {
        this.id = generarIdUnico();
        this.actividades = new ArrayList<>();
        this.categoria = "";
        this.recordatorioActivo = false;
        this.horaRecordatorio = "08:00"; // Hora por defecto si se activa
        this.textoRecordatorioPersonalizado = ""; // Por defecto vacío
    }

    public Rutina(String nombre, String fecha) {
        this(); // Llama al constructor por defecto
        this.nombre = nombre;
        this.fecha = fecha;
    }

    public Rutina(String nombre, String fecha, ArrayList<Actividad> actividades, String categoria) {
        this(nombre, fecha); // Llama al constructor anterior
        this.actividades = (actividades != null) ? actividades : new ArrayList<>();
        this.categoria = (categoria != null) ? categoria.trim() : "";
    }

    // Constructor más completo, usado si se reconstruye el objeto o para tests.
    // Si el ID es 0 o -1, se genera uno nuevo.
    public Rutina(int id, String nombre, String fecha, ArrayList<Actividad> actividades, String categoria,
                  boolean recordatorioActivo, String horaRecordatorio, String textoRecordatorioPersonalizado) {
        this.id = (id == 0 || id == -1) ? generarIdUnico() : id;
        this.nombre = nombre;
        this.fecha = fecha;
        this.actividades = (actividades != null) ? actividades : new ArrayList<>();
        this.categoria = (categoria != null) ? categoria.trim() : "";
        this.recordatorioActivo = recordatorioActivo;
        this.horaRecordatorio = (horaRecordatorio != null && horaRecordatorio.matches("\\d{2}:\\d{2}")) ? horaRecordatorio : "08:00";
        this.textoRecordatorioPersonalizado = (textoRecordatorioPersonalizado != null) ? textoRecordatorioPersonalizado.trim() : "";
    }


    // Getters
    public int getId() { return id; }
    public String getNombre() { return nombre; }
    public String getFecha() { return fecha; }
    public ArrayList<Actividad> getActividades() { return actividades; }
    public String getCategoria() { return categoria; }
    public boolean isRecordatorioActivo() { return recordatorioActivo; }
    public String getHoraRecordatorio() { return horaRecordatorio; }
    public String getTextoRecordatorioPersonalizado() { return textoRecordatorioPersonalizado; }

    // Setters
    public void setNombre(String nombre) { this.nombre = nombre; }
    public void setFecha(String fecha) { this.fecha = fecha; }
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


    // Métodos de utilidad
    public void addActividad(Actividad actividad) {
        if (this.actividades == null) { this.actividades = new ArrayList<>(); }
        if (actividad != null) { this.actividades.add(actividad); }
    }
    public void addActividad(String nombreActividad) { addActividad(new Actividad(nombreActividad)); } //
    public String getResumenCumplimiento() {
        if (actividades == null || actividades.isEmpty()) { return "Sin actividades"; }
        int completadas = 0;
        for (Actividad act : actividades) { if (act.isCompletada()) { completadas++; } } //
        return completadas + " de " + actividades.size() + " completadas";
    }
    public boolean todasActividadesCompletadas() {
        if (actividades == null || actividades.isEmpty()) { return true; } // O false, según lógica de negocio
        for (Actividad actividad : actividades) { if (!actividad.isCompletada()) { return false; } } //
        return true;
    }
}