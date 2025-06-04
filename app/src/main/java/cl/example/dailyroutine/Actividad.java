package cl.example.dailyroutine;

public class Actividad {
    private String nombre;
    private boolean completada;

    public Actividad(String nombre) {
        this.nombre = nombre;
        this.completada = false;
    }

    public Actividad(String nombre, boolean completada) {
        this.nombre = nombre;
        this.completada = completada;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public boolean isCompletada() {
        return completada;
    }

    public void setCompletada(boolean completada) {
        this.completada = completada;
    }

    @Override
    public String toString() {
        return nombre + (completada ? " (Completada)" : " (Pendiente)");
    }
}