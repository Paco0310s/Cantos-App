package com.pacosotelo.coro.tools;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
// ...existing imports...
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
// ...existing imports...

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
        View view = mInflater.inflate(R.layout.item_esquema, parent, false);
        return new AdaptadorEsquemas.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AdaptadorEsquemas.ViewHolder holder, int position) {
        if (listaEsquemas == null || position < 0 || position >= listaEsquemas.size()) return;
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
                    int pos = getAbsoluteAdapterPosition();
                    if (listaEsquemas == null || pos < 0 || pos >= listaEsquemas.size()) return;
                    Esquema esquema = listaEsquemas.get(pos);

                    Intent i = new Intent(contexto, ModificarEsquemaActivity.class);
                    i.putExtra("esquema", esquema);
                    contexto.startActivity(i);
                    ((Activity) contexto).overridePendingTransition(R.anim.left_in,R.anim.left_out);
                }
            });

            listaCantosEsquema.setOnItemClickListener((adapterView, view, i, l) ->  {
                int pos = getAbsoluteAdapterPosition();
                if (listaEsquemas == null || pos < 0 || pos >= listaEsquemas.size()) return;

                Esquema esquema = listaEsquemas.get(pos);
                if (esquema == null || esquema.getCantos() == null || i < 0 || i >= esquema.getCantos().size()) return;

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

            List<Canto> cantos = esquema.getCantos();
            if (cantos == null) cantos = new java.util.ArrayList<>();
            ArrayAdapter<Canto> adapter = new ArrayAdapter<>(contexto
                    , R.layout.item_canto_esquema, cantos);
            listaCantosEsquema.setAdapter(adapter);

        }

    }
}
