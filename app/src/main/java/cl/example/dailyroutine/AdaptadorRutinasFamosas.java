package cl.example.dailyroutine;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
// import android.widget.Button; // Descomentar si se añade el botón
// import android.widget.Toast;   // Descomentar si se añade el botón

import java.util.ArrayList;

public class AdaptadorRutinasFamosas extends BaseAdapter {

    private Context context;
    private ArrayList<Rutina> listaRutinasFamosas;

    public AdaptadorRutinasFamosas(Context context, ArrayList<Rutina> listaRutinasFamosas) {
        this.context = context;
        this.listaRutinasFamosas = listaRutinasFamosas;
    }

    @Override
    public int getCount() {
        return listaRutinasFamosas.size();
    }

    @Override
    public Object getItem(int position) {
        return listaRutinasFamosas.get(position);
    }

    @Override
    public long getItemId(int position) {
        return listaRutinasFamosas.get(position).getId();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.layout_item_rutina_famosa, parent, false);
            holder = new ViewHolder();
            holder.tvNombre = convertView.findViewById(R.id.textview_nombre_rutina_famosa);
            holder.tvCategoria = convertView.findViewById(R.id.textview_categoria_rutina_famosa);
            holder.tvDescripcion = convertView.findViewById(R.id.textview_descripcion_rutina_famosa);
            holder.tvActividadesLista = convertView.findViewById(R.id.textview_actividades_rutina_famosa_lista);
            holder.tvActividadesTitulo = convertView.findViewById(R.id.textview_actividades_rutina_famosa_titulo);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        final Rutina rutinaFamosa = listaRutinasFamosas.get(position);

        holder.tvNombre.setText(rutinaFamosa.getNombre());
        holder.tvCategoria.setText("Categoría: " + (rutinaFamosa.getCategoria() != null && !rutinaFamosa.getCategoria().isEmpty() ? rutinaFamosa.getCategoria() : "General"));

        if (rutinaFamosa.getFecha() != null && !rutinaFamosa.getFecha().isEmpty()) {
            holder.tvDescripcion.setText(rutinaFamosa.getFecha());
            holder.tvDescripcion.setVisibility(View.VISIBLE);
        } else {
            holder.tvDescripcion.setVisibility(View.GONE);
        }


        if (rutinaFamosa.getActividades() != null && !rutinaFamosa.getActividades().isEmpty()) {
            StringBuilder sbActividades = new StringBuilder();
            for (Actividad act : rutinaFamosa.getActividades()) {
                sbActividades.append("- ").append(act.getNombre()).append("\n");
            }
            if (sbActividades.length() > 0) {
                sbActividades.setLength(sbActividades.length() - 1);
            }
            holder.tvActividadesLista.setText(sbActividades.toString());
            holder.tvActividadesLista.setVisibility(View.VISIBLE);
            holder.tvActividadesTitulo.setVisibility(View.VISIBLE);
        } else {
            holder.tvActividadesLista.setVisibility(View.GONE);
            holder.tvActividadesTitulo.setVisibility(View.GONE);
        }

        // Lógica para el botón (si se implementa mas adelante)
        /*
        if (holder.btnAnadir != null) {
            holder.btnAnadir.setOnClickListener(v -> {
                // Lógica para añadir esta rutina a MenuPrincipal.listaRutinas
                Toast.makeText(context, "Añadiendo: " + rutinaFamosa.getNombre(), Toast.LENGTH_SHORT).show();
                // Ejemplo:
                // Intent intent = new Intent(context, CrearRutina.class);
                // // Pasar datos de la rutina famosa para precargar CrearRutina
                // context.startActivity(intent);
            });
        }
        */

        return convertView;
    }

    private static class ViewHolder {
        TextView tvNombre;
        TextView tvCategoria;
        TextView tvDescripcion;
        TextView tvActividadesTitulo;
        TextView tvActividadesLista;
    }
}