package cl.example.dailyroutine;

import java.util.ArrayList;

public class Rutina {

    private int id;
    private String nombre;
    private String fecha;
    private ArrayList<Actividad> actividades;
    private String categoria;
    private boolean recordatorioActivo;
    private String horaRecordatorio;   // Formato "HH:mm", ej. "09:30"

    private static int proximoId = 1; // Contador estático simple para generar IDs.

    private static synchronized int generarIdUnico() {
        return proximoId++;
    }

    public Rutina() {
        this.id = generarIdUnico(); // Asignar ID al crear
        this.actividades = new ArrayList<>();
        this.categoria = "";
        this.recordatorioActivo = false; // Recordatorio desactivado por defecto
        this.horaRecordatorio = "08:00";
    }

    public Rutina(String nombre, String fecha) {
        this();
        this.nombre = nombre;
        this.fecha = fecha;
    }

    public Rutina(String nombre, String fecha, ArrayList<Actividad> actividades, String categoria) {
        this(nombre, fecha);
        this.actividades = (actividades != null) ? actividades : new ArrayList<>();
        this.categoria = (categoria != null) ? categoria.trim() : "";
    }

    public Rutina(int id, String nombre, String fecha, ArrayList<Actividad> actividades, String categoria,
                  boolean recordatorioActivo, String horaRecordatorio) {
        this.id = (id == 0 || id == -1) ? generarIdUnico() : id;
        this.nombre = nombre;
        this.fecha = fecha;
        this.actividades = (actividades != null) ? actividades : new ArrayList<>();
        this.categoria = (categoria != null) ? categoria.trim() : "";
        this.recordatorioActivo = recordatorioActivo;
        this.horaRecordatorio = (horaRecordatorio != null && horaRecordatorio.matches("\\d{2}:\\d{2}")) ? horaRecordatorio : "08:00";
    }


    // Getters
    public int getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public String getFecha() {
        return fecha;
    }

    public ArrayList<Actividad> getActividades() {
        return actividades;
    }

    public String getCategoria() {
        return categoria;
    }

    public boolean isRecordatorioActivo() {
        return recordatorioActivo;
    }

    public String getHoraRecordatorio() {
        return horaRecordatorio;
    }

    // Setters
    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public void setFecha(String fecha) {
        this.fecha = fecha;
    }

    public void setActividades(ArrayList<Actividad> actividades) {
        this.actividades = (actividades != null) ? actividades : new ArrayList<>();
    }

    public void setCategoria(String categoria) {
        this.categoria = (categoria != null) ? categoria.trim() : "";
    }

    public void setRecordatorioActivo(boolean recordatorioActivo) {
        this.recordatorioActivo = recordatorioActivo;
    }

    public void setHoraRecordatorio(String horaRecordatorio) {
        if (horaRecordatorio != null && horaRecordatorio.matches("\\d{2}:\\d{2}")) {
            this.horaRecordatorio = horaRecordatorio;
        } else {
            this.horaRecordatorio = "08:00"; // Valor por defecto si el formato es inválido
        }
    }
    public void addActividad(Actividad actividad) {
        if (this.actividades == null) {
            this.actividades = new ArrayList<>();
        }
        if (actividad != null) {
            this.actividades.add(actividad);
        }
    }

    public void addActividad(String nombreActividad) {
        addActividad(new Actividad(nombreActividad));
    }

    public String getResumenCumplimiento() {
        if (actividades == null || actividades.isEmpty()) {
            return "Sin actividades";
        }
        int completadas = 0;
        for (Actividad act : actividades) {
            if (act.isCompletada()) {
                completadas++;
            }
        }
        return completadas + " de " + actividades.size() + " completadas";
    }

    public boolean todasActividadesCompletadas() {
        if (actividades == null || actividades.isEmpty()) {
            return true;
        }
        for (Actividad actividad : actividades) {
            if (!actividad.isCompletada()) {
                return false;
            }
        }
        return true;
    }
}