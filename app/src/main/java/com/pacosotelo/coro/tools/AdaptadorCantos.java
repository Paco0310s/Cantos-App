package com.pacosotelo.coro.tools;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.pacosotelo.coro.R;
import com.pacosotelo.coro.modelos.Canto;
import com.pacosotelo.coro.ui.CantoActivity;

import java.util.List;

public class AdaptadorCantos extends RecyclerView.Adapter<AdaptadorCantos.ViewHolder> {
    private List<Canto> listaCantos;
    private LayoutInflater mInflater;
    private Context contexto;

    public AdaptadorCantos(List<Canto> listaCantos, Context contexto) {
        this.mInflater = LayoutInflater.from(contexto);
        this.contexto = contexto;
        this.listaCantos = listaCantos;
    }

    @NonNull
    @Override
    public AdaptadorCantos.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.item_canto, null);
        return new AdaptadorCantos.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AdaptadorCantos.ViewHolder holder, int position) {
        //holder.cv.setAnimation(AnimationUtils.loadAnimation(contexto, R.anim.right_in));
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
        TextView nombreCanto, momentoCanto, tiempoCanto;
        ImageButton bOpciones;
        CardView cv;

        ViewHolder(View itemView) {
            super(itemView);

            nombreCanto = itemView.findViewById(R.id.tNombreCanto);
            momentoCanto = itemView.findViewById(R.id.tMomentoCanto);
            tiempoCanto = itemView.findViewById(R.id.tTiempoCanto);
            bOpciones = itemView.findViewById(R.id.bOpciones);
            cv = itemView.findViewById(R.id.cvCanto);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Canto canto = listaCantos.get(getAbsoluteAdapterPosition());

                    Intent i = new Intent(contexto, CantoActivity.class);
                    i.putExtra("getID", canto.getId());
                    i.putExtra("getNombre",canto.getNombre());
                    i.putExtra("getLetra", canto.getLetra());
                    i.putExtra("getMomentos", canto.getMomentos());
                    i.putExtra("getTiempos", canto.getTiempos());

                    //ActivityOptions options = ActivityOptions.makeCustomAnimation(contexto, R.anim.left_in,R.anim.left_out);

                    contexto.startActivity(i);
                    ((Activity) contexto).overridePendingTransition(R.anim.left_in,R.anim.left_out);
                    ((Activity) contexto).finish();
                }
            });

            bOpciones.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(contexto, "Proximamente", Toast.LENGTH_SHORT).show();
                }
            });
        }

        private void bindData(final Canto canto) {
            nombreCanto.setText(canto.getNombre());
            momentoCanto.setText(canto.getMomentos().toString());
            tiempoCanto.setText(canto.getTiempos().toString());
        }

    }
}
