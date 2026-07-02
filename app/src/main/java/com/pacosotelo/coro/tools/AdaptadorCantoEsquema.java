package com.pacosotelo.coro.tools;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
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
import com.pacosotelo.coro.modelos.Canto;
import com.pacosotelo.coro.ui.CantoActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class AdaptadorCantoEsquema extends RecyclerView.Adapter<AdaptadorCantoEsquema.ViewHolder> {
    private final LayoutInflater mInflater;
    private final Context contexto;
    private List<Canto> listaCantos;

    public AdaptadorCantoEsquema(List<Canto> listaCantos, Context contexto) {
        this.mInflater = LayoutInflater.from(contexto);
        this.contexto = contexto;
        this.listaCantos = listaCantos;
    }

    @NonNull
    @Override
    public AdaptadorCantoEsquema.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.item_canto_momento, null);
        return new AdaptadorCantoEsquema.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AdaptadorCantoEsquema.ViewHolder holder, int position) {
        holder.bindData(listaCantos.get(position));
    }

    @Override
    public int getItemCount() {
        return listaCantos.size();
    }

    public void setLista(List<Canto> listaCantos) {
        this.listaCantos = listaCantos;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvNombre;
        Spinner sMomentos;
        CardView cv;

        @SuppressLint("NonConstantResourceId")
        ViewHolder(View itemView) {
            super(itemView);

            tvNombre = itemView.findViewById(R.id.tvNombre);
            sMomentos = itemView.findViewById(R.id.sMomentos);

            itemView.setOnClickListener(v -> {

            });
        }

        private void bindData(final Canto canto) {
            tvNombre.setText(canto.getNombre());

            FirebaseDatabase fd = FirebaseDatabase.getInstance();
            DatabaseReference dr = fd.getReference("Momentos");

            List<String> momentos = new ArrayList<>();

            dr.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {

                    for (DataSnapshot objSnap : snapshot.getChildren()) {
                        momentos.add(Objects.requireNonNull(objSnap.child("momento").getValue()).toString());
                    }

                    sMomentos.setAdapter(new ArrayAdapter<>(contexto,
                            android.R.layout.simple_spinner_dropdown_item, momentos));
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }

    }
}
