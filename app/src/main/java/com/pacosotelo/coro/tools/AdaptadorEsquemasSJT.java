package com.pacosotelo.coro.tools;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.pacosotelo.coro.R;
import com.pacosotelo.coro.modelos.Canto;
import com.pacosotelo.coro.modelos.Esquema;
import com.pacosotelo.coro.ui.CantoActivity;
import com.pacosotelo.coro.ui.ModificarEsquemaActivity;
import com.pacosotelo.coro.ui.ModificarEsquemaActivitySJT;

import java.util.List;

public class AdaptadorEsquemasSJT extends RecyclerView.Adapter<AdaptadorEsquemasSJT.ViewHolder> {
    private final LayoutInflater mInflater;
    private final Context contexto;
    private List<Esquema> listaEsquemas;

    public AdaptadorEsquemasSJT(List<Esquema> listaEsquemas, Context contexto) {
        this.mInflater = LayoutInflater.from(contexto);
        this.contexto = contexto;
        this.listaEsquemas = listaEsquemas;
    }

    @NonNull
    @Override
    public AdaptadorEsquemasSJT.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.item_esquema, null);
        return new AdaptadorEsquemasSJT.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AdaptadorEsquemasSJT.ViewHolder holder, int position) {
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
        ListView listaCantosEsquema;
        CardView cvEsquema;
        RelativeLayout rlEsquema;

        @SuppressLint("ClickableViewAccessibility")
        ViewHolder(View itemView) {
            super(itemView);

            nombreEsquema = itemView.findViewById(R.id.tvNombreEsquema);
            listaCantosEsquema = itemView.findViewById(R.id.lvCantosEsquema);
            cvEsquema = itemView.findViewById(R.id.cvEsquema);
            rlEsquema = itemView.findViewById(R.id.itemEsquema);

            listaCantosEsquema.setOnTouchListener((v, event) -> {
                v.getParent().requestDisallowInterceptTouchEvent(true);
                return false;
            });

            cvEsquema.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Esquema esquema = listaEsquemas.get(getAbsoluteAdapterPosition());

                    Intent i = new Intent(contexto, ModificarEsquemaActivitySJT.class);
                    i.putExtra("esquema", esquema);
                    contexto.startActivity(i);
                    ((Activity) contexto).overridePendingTransition(R.anim.left_in,R.anim.left_out);
                }
            });

            listaCantosEsquema.setOnItemClickListener((adapterView, view, i, l) ->  {
                Esquema esquema = listaEsquemas.get(getAbsoluteAdapterPosition());

                Intent intent = new Intent(contexto, CantoActivity.class);
                intent.putExtra("canto", esquema.getCantos().get(i));
                intent.putExtra("esquema", esquema);
                intent.putExtra("bandera",true);
                intent.putExtra("indice",i);

                contexto.startActivity(intent);
                ((Activity) contexto).overridePendingTransition(R.anim.left_in,R.anim.left_out);

            });
        }

        private void bindData(final Esquema esquema) {
            nombreEsquema.setText(esquema.getNombre());

            ArrayAdapter<Canto> adapter = new ArrayAdapter<>(contexto
                    , R.layout.item_canto_esquema, esquema.getCantos());
            listaCantosEsquema.setAdapter(adapter);

        }

    }
}
