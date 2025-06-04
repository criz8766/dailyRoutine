package cl.example.dailyroutine;

import android.content.Context;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import java.util.ArrayList;

public class AdaptadorActividades extends BaseAdapter {

    private Context context;
    private ArrayList<Actividad> listaActividades;
    private OnActividadCheckedChangeListener listener; // Listener

    // Interfaz para comunicar cambios en el CheckBox
    public interface OnActividadCheckedChangeListener {
        void onActividadCheckedChanged(int position, boolean isChecked);
    }

    public AdaptadorActividades(Context context, ArrayList<Actividad> listaActividades, OnActividadCheckedChangeListener listener) {
        this.context = context;
        this.listaActividades = listaActividades;
        this.listener = listener; // Asignar listener
    }

    @Override
    public int getCount() {
        return listaActividades.size();
    }

    @Override
    public Object getItem(int position) {
        return listaActividades.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.layout_item_actividad, parent, false);
            holder = new ViewHolder();
            holder.nombreActividadTV = convertView.findViewById(R.id.textview_nombre_actividad);
            holder.completadaCB = convertView.findViewById(R.id.checkbox_actividad_completada);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        final Actividad actividadActual = listaActividades.get(position);

        holder.nombreActividadTV.setText(actividadActual.getNombre());

        if (actividadActual.isCompletada()) {
            holder.nombreActividadTV.setPaintFlags(holder.nombreActividadTV.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        } else {
            holder.nombreActividadTV.setPaintFlags(holder.nombreActividadTV.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
        }

        // Importante: Quitar el listener ANTES de llamar a setChecked() para evitar llamadas recursivas o inesperadas.
        holder.completadaCB.setOnCheckedChangeListener(null);
        holder.completadaCB.setChecked(actividadActual.isCompletada());

        holder.completadaCB.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                actividadActual.setCompletada(isChecked);
                // Aplicar/quitar tachado inmediatamente
                if (isChecked) {
                    holder.nombreActividadTV.setPaintFlags(holder.nombreActividadTV.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                } else {
                    holder.nombreActividadTV.setPaintFlags(holder.nombreActividadTV.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
                }
                // Notificar a la actividad contenedora sobre el cambio
                if (listener != null) {
                    listener.onActividadCheckedChanged(position, isChecked);
                }
            }
        });

        return convertView;
    }

    private static class ViewHolder {
        TextView nombreActividadTV;
        CheckBox completadaCB;
    }
}