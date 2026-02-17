package com.pacosotelo.coro.tools;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.pacosotelo.coro.R;
import com.pacosotelo.coro.modelos.Grupo;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class AdaptadorGrupos extends RecyclerView.Adapter<AdaptadorGrupos.ViewHolder> {
    private final LayoutInflater mInflater;
    private final Context contexto;
    private List<Grupo> listaGrupos;

    public AdaptadorGrupos(List<Grupo> listaGrupos, Context contexto) {
        this.mInflater = LayoutInflater.from(contexto);
        this.contexto = contexto;
        this.listaGrupos = listaGrupos;
    }

    @NonNull
    @Override
    public AdaptadorGrupos.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.item_grupo, null);
        return new AdaptadorGrupos.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AdaptadorGrupos.ViewHolder holder, int position) {
        holder.bindData(listaGrupos.get(position));
    }

    @Override
    public int getItemCount() {
        return listaGrupos.size();
    }

    public void setLista(List<Grupo> listaGrupos) {
        this.listaGrupos = listaGrupos;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvNombre;
        CardView cv;

        @SuppressLint("NonConstantResourceId")
        ViewHolder(View itemView) {
            super(itemView);

            tvNombre = itemView.findViewById(R.id.tvNombreGrupo);

            itemView.setOnClickListener(v -> {

            });
        }

        private void bindData(final Grupo grupo) {
            tvNombre.setText(grupo.getNombre());
        }

    }
}
