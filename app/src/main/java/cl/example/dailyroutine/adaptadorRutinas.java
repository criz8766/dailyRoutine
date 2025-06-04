package cl.example.dailyroutine;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.cardview.widget.CardView;

import java.util.ArrayList;

public class adaptadorRutinas extends BaseAdapter {
    private ArrayList<Rutina> listaRutinas;
    private Context context;
    public static final String EXTRA_POSICION_RUTINA = "cl.example.dailyroutine.POSICION_RUTINA";

    public adaptadorRutinas(ArrayList<Rutina> listaRutinas, Context context) {
        this.listaRutinas = listaRutinas;
        this.context = context;
    }

    @Override
    public int getCount() {
        return listaRutinas.size();
    }

    @Override
    public Object getItem(int position) {
        return listaRutinas.get(position);
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
            holder.cardViewItem = (CardView) view;
            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
        }

        final Rutina rutina = (Rutina) getItem(position);

        holder.nombreRutinaTV.setText(rutina.getNombre());
        holder.fechaTV.setText(rutina.getFecha());

        // Mostrar la categoría
        if (rutina.getCategoria() != null && !rutina.getCategoria().isEmpty()) {
            holder.categoriaTV.setText("Categoría: " + rutina.getCategoria());
            holder.categoriaTV.setVisibility(View.VISIBLE);
        } else {
            holder.categoriaTV.setText("Categoría: Ninguna");
        }


        holder.editarButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, CrearRutina.class);
                intent.putExtra(EXTRA_POSICION_RUTINA, position);
                context.startActivity(intent);
            }
        });

        holder.cardViewItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, DetalleRutinaActivity.class);
                intent.putExtra(DetalleRutinaActivity.EXTRA_POSICION_RUTINA_DETALLE, position);
                context.startActivity(intent);
            }
        });

        return view;
    }

    private static class ViewHolder {
        TextView nombreRutinaTV;
        TextView fechaTV;
        TextView categoriaTV;
        ImageButton editarButton;
        CardView cardViewItem;
    }
}