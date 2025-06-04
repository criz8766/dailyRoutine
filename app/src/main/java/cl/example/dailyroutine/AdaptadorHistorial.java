package cl.example.dailyroutine;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class AdaptadorHistorial extends BaseAdapter {

    private Context context;
    private ArrayList<Rutina> listaRutinasHistorial;

    public AdaptadorHistorial(Context context, ArrayList<Rutina> listaRutinas) {
        this.context = context;
        this.listaRutinasHistorial = new ArrayList<>(listaRutinas);

        Collections.sort(this.listaRutinasHistorial, new Comparator<Rutina>() {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            @Override
            public int compare(Rutina r1, Rutina r2) {
                try {
                    Date date1 = sdf.parse(r1.getFecha());
                    Date date2 = sdf.parse(r2.getFecha());
                    if (date1 != null && date2 != null) {
                        return date2.compareTo(date1);
                    } else if (date1 == null && date2 == null) {
                        return 0;
                    } else if (date1 == null) {
                        return 1;
                    } else {
                        return -1;
                    }
                } catch (ParseException e) {
                    return 0;
                }
            }
        });
    }

    @Override
    public int getCount() {
        return listaRutinasHistorial.size();
    }

    @Override
    public Object getItem(int position) {
        return listaRutinasHistorial.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.layout_item_historial_rutina, parent, false);
            holder = new ViewHolder();
            holder.tvNombre = convertView.findViewById(R.id.textview_nombre_rutina_historial);
            holder.tvFecha = convertView.findViewById(R.id.textview_fecha_rutina_historial);
            holder.tvCumplimiento = convertView.findViewById(R.id.textview_cumplimiento_rutina_historial);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        final Rutina rutina = listaRutinasHistorial.get(position);

        holder.tvNombre.setText(rutina.getNombre());
        holder.tvFecha.setText("Fecha: " + rutina.getFecha());
        holder.tvCumplimiento.setText(rutina.getResumenCumplimiento());

        if (rutina.getActividades() != null && !rutina.getActividades().isEmpty()) {
            if (rutina.todasActividadesCompletadas()) {
                holder.tvCumplimiento.setTextColor(context.getResources().getColor(R.color.verde));
            } else {
                holder.tvCumplimiento.setTextColor(context.getResources().getColor(R.color.rojo));
            }
        } else {
            holder.tvCumplimiento.setTextColor(context.getResources().getColor(android.R.color.darker_gray));
        }


        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int originalPosition = -1;
                if (MenuPrincipal.listaRutinas != null) {
                    originalPosition = MenuPrincipal.listaRutinas.indexOf(rutina);
                }

                if (originalPosition != -1) {
                    Intent intent = new Intent(context, DetalleRutinaActivity.class);
                    intent.putExtra(DetalleRutinaActivity.EXTRA_POSICION_RUTINA_DETALLE, originalPosition);
                    context.startActivity(intent);
                }
            }
        });

        return convertView;
    }

    private static class ViewHolder {
        TextView tvNombre;
        TextView tvFecha;
        TextView tvCumplimiento;
    }
}