package com.pacosotelo.coro.tools;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import com.pacosotelo.coro.R;
import com.pacosotelo.coro.modelos.Esquema;

import java.util.List;

public class AdaptadorEsquemas extends RecyclerView.Adapter<AdaptadorEsquemas.ViewHolder> {
    private final LayoutInflater mInflater;
    private final Context contexto;
    private List<Esquema> listaEsquemas;

    public AdaptadorEsquemas(List<Esquema> listaEsquemas, Context contexto) {
        this.mInflater = LayoutInflater.from(contexto);
        this.contexto = contexto;
        this.listaEsquemas = listaEsquemas;
    }

    @NonNull
    @Override
    public AdaptadorEsquemas.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.item_esquema, null);
        return new AdaptadorEsquemas.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AdaptadorEsquemas.ViewHolder holder, int position) {
        holder.bindData(listaEsquemas.get(position));
    }

    @Override
    public int getItemCount() {
        return listaEsquemas.size();
    }

    public void setLista(List<Esquema> listaEsquemas) {
        this.listaEsquemas = listaEsquemas;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView nombreEsquema;

        @SuppressLint("NonConstantResourceId")
        ViewHolder(View itemView) {
            super(itemView);

        }

        private void bindData(final Esquema esquema) {

        }

    }
}
