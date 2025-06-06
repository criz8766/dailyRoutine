package cl.example.dailyroutine;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class adaptadorRutinas extends BaseAdapter implements Filterable {
    private Context context;
    public static final String EXTRA_POSICION_RUTINA = "cl.example.dailyroutine.POSICION_RUTINA";

    private List<Rutina> listaRutinasOriginal;
    private List<Rutina> listaRutinasFiltrada;

    public adaptadorRutinas(ArrayList<Rutina> listaRutinas, Context context) {
        this.listaRutinasOriginal = listaRutinas;
        this.listaRutinasFiltrada = new ArrayList<>(listaRutinas);
        this.context = context;
    }

    @Override
    public int getCount() {
        return listaRutinasFiltrada.size();
    }

    @Override
    public Object getItem(int position) {
        return listaRutinasFiltrada.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        ViewHolder holder;

        if (view == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.adaptador_rutinas, parent, false);
            holder = new ViewHolder();
            holder.nombreRutinaTV = view.findViewById(R.id.nombreRutina);
            holder.fechaTV = view.findViewById(R.id.fecha);
            holder.categoriaTV = view.findViewById(R.id.categoriaRutina);
            holder.editarButton = view.findViewById(R.id.editar);
            holder.itemLayout = view.findViewById(R.id.layout_info_rutina);
            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
        }

        final Rutina rutina = listaRutinasFiltrada.get(position);

        holder.nombreRutinaTV.setText(rutina.getNombre());
        holder.fechaTV.setText(rutina.getFecha());

        if (rutina.getCategoria() != null && !rutina.getCategoria().isEmpty()) {
            holder.categoriaTV.setText("Categoría: " + rutina.getCategoria());
            holder.categoriaTV.setVisibility(View.VISIBLE);
        } else {
            holder.categoriaTV.setText("Categoría: Ninguna");
            holder.categoriaTV.setVisibility(View.VISIBLE);
        }

        int originalPosition = listaRutinasOriginal.indexOf(rutina);

        holder.editarButton.setOnClickListener(v -> {
            if (originalPosition != -1) {
                Intent intent = new Intent(context, CrearRutina.class);
                intent.putExtra(EXTRA_POSICION_RUTINA, originalPosition);
                context.startActivity(intent);
            }
        });

        holder.itemLayout.setOnClickListener(v -> {
            if (originalPosition != -1) {
                Intent intent = new Intent(context, DetalleRutinaActivity.class);
                intent.putExtra(DetalleRutinaActivity.EXTRA_POSICION_RUTINA_DETALLE, originalPosition);
                context.startActivity(intent);
            }
        });

        return view;
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                FilterResults results = new FilterResults();
                List<Rutina> filteredList = new ArrayList<>();
                String filterPattern = constraint.toString().toLowerCase().trim();

                if (filterPattern.isEmpty()) {
                    filteredList.addAll(listaRutinasOriginal);
                } else {
                    for (Rutina rutina : listaRutinasOriginal) {
                        if (rutina.getCategoria().toLowerCase().contains(filterPattern)) {
                            filteredList.add(rutina);
                        }
                    }
                }

                results.values = filteredList;
                results.count = filteredList.size();
                return results;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                listaRutinasFiltrada.clear();
                listaRutinasFiltrada.addAll((List) results.values);
                notifyDataSetChanged();
            }
        };
    }

    private static class ViewHolder {
        TextView nombreRutinaTV;
        TextView fechaTV;
        TextView categoriaTV;
        ImageButton editarButton;
        LinearLayout itemLayout;
    }
}