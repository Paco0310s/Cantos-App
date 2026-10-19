package com.pacosotelo.coro.tools;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SwitchCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.pacosotelo.coro.R;
import com.pacosotelo.coro.modelos.Grupo;

import java.util.List;

public class AdaptadorGrupos extends RecyclerView.Adapter<AdaptadorGrupos.ViewHolder> {

    public interface OnGrupoDetailsClickListener {
        void onGrupoDetailsClick(Grupo grupo);
    }

    public interface OnGrupoActiveClickListener {
        void onGrupoActiveClick(Grupo grupo, boolean isActive);
    }

    private final LayoutInflater mInflater;
    private final Context contexto;
    private List<Grupo> listaGrupos;
    private final OnGrupoDetailsClickListener detailsListener;
    private final OnGrupoActiveClickListener activeListener;

    public AdaptadorGrupos(List<Grupo> listaGrupos, Context contexto, OnGrupoDetailsClickListener detailsListener, OnGrupoActiveClickListener activeListener) {
        this.mInflater = LayoutInflater.from(contexto);
        this.contexto = contexto;
        this.listaGrupos = listaGrupos;
        this.detailsListener = detailsListener;
        this.activeListener = activeListener;
    }

    public AdaptadorGrupos(List<Grupo> listaGrupos, Context contexto) {
        this(listaGrupos, contexto, null, null);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.item_grupo, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bindData(listaGrupos.get(position));
    }

    @Override
    public int getItemCount() {
        return listaGrupos != null ? listaGrupos.size() : 0;
    }

    public void setLista(List<Grupo> listaGrupos) {
        this.listaGrupos = listaGrupos;
        notifyDataSetChanged();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvNombre, tvCodigo;
        SwitchCompat switchActivo;

        ViewHolder(View itemView) {
            super(itemView);
            tvNombre = itemView.findViewById(R.id.tvNombreGrupo);
            tvCodigo = itemView.findViewById(R.id.tvCodigoGrupo);
            switchActivo = itemView.findViewById(R.id.switchActivo);

            itemView.setOnClickListener(v -> {
                int pos = getBindingAdapterPosition();
                if (pos != RecyclerView.NO_POSITION && detailsListener != null && listaGrupos != null) {
                    detailsListener.onGrupoDetailsClick(listaGrupos.get(pos));
                }
            });

            if (switchActivo != null) {
                switchActivo.setOnClickListener(v -> {
                    int pos = getBindingAdapterPosition();
                    if (pos != RecyclerView.NO_POSITION && activeListener != null && listaGrupos != null) {
                        activeListener.onGrupoActiveClick(listaGrupos.get(pos), switchActivo.isChecked());
                    }
                });
            }
        }

        private void bindData(final Grupo grupo) {
            tvNombre.setText(grupo.getNombre());
            if (tvCodigo != null) {
                if (grupo.getCodigo() != null && !grupo.getCodigo().isEmpty()) {
                    tvCodigo.setText("Código: " + grupo.getCodigo());
                    tvCodigo.setVisibility(View.VISIBLE);
                } else {
                    tvCodigo.setVisibility(View.GONE);
                }
            }

            if (switchActivo != null) {
                boolean isActive = grupo.getUuid() != null && grupo.getUuid().equals(Constantes.GRUPO_SELECCIONADO);
                switchActivo.setChecked(isActive);
                // Si ya está activo, no permitimos apagarlo desde aquí (funciona como RadioButton)
                // Se desactivará solo cuando se active otro
                switchActivo.setEnabled(!isActive);
            }
        }
    }
}
