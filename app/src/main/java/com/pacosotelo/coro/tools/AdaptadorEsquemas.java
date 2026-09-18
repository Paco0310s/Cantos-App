package com.pacosotelo.coro.tools;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
// ...existing imports...
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.pacosotelo.coro.R;
import com.pacosotelo.coro.modelos.Canto;
import com.pacosotelo.coro.modelos.Esquema;
import com.pacosotelo.coro.ui.CantoActivity;
import com.pacosotelo.coro.ui.ModificarEsquemaActivity;
// ...existing imports...

import java.util.ArrayList;
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
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.item_esquema, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        if (listaEsquemas == null || position < 0 || position >= listaEsquemas.size()) return;
        holder.bindData(listaEsquemas.get(position));
    }

    @Override
    public int getItemCount() {
        return listaEsquemas.size();
    }

    public void setLista(List<Esquema> listaEsquemas) {
        this.listaEsquemas = listaEsquemas;
        notifyDataSetChanged();
    }

    public void actualizarLista(List<Esquema> nuevaLista) {
        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new DiffUtil.Callback() {
            @Override
            public int getOldListSize() {
                return listaEsquemas.size();
            }

            @Override
            public int getNewListSize() {
                return nuevaLista.size();
            }

            @Override
            public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
                return listaEsquemas.get(oldItemPosition).getId().equals(nuevaLista.get(newItemPosition).getId());
            }

            @Override
            public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
                return listaEsquemas.get(oldItemPosition).getNombre().equals(nuevaLista.get(newItemPosition).getNombre());
            }
        });

        this.listaEsquemas.clear();
        this.listaEsquemas.addAll(nuevaLista);
        diffResult.dispatchUpdatesTo(this);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView nombreEsquema;
        RecyclerView listaCantosEsquema;
        CardView cvEsquema;
        RelativeLayout rlEsquema;

        @SuppressLint("ClickableViewAccessibility")
        ViewHolder(View itemView) {
            super(itemView);

            nombreEsquema = itemView.findViewById(R.id.tvNombreEsquema);
            listaCantosEsquema = itemView.findViewById(R.id.lvCantosEsquema);
            listaCantosEsquema.setLayoutManager(new LinearLayoutManager(contexto));
            cvEsquema = itemView.findViewById(R.id.cvEsquema);
            rlEsquema = itemView.findViewById(R.id.itemEsquema);

            listaCantosEsquema.setOnTouchListener((v, event) -> {
                v.getParent().requestDisallowInterceptTouchEvent(true);
                return false;
            });

            cvEsquema.setOnClickListener(view -> {
                int pos = getAbsoluteAdapterPosition();
                if (listaEsquemas == null || pos < 0 || pos >= listaEsquemas.size()) return;
                Esquema esquema = listaEsquemas.get(pos);

                Intent i = new Intent(contexto, ModificarEsquemaActivity.class);
                i.putExtra("esquema", esquema);
                contexto.startActivity(i);
                if (contexto instanceof Activity) {
                    ((Activity) contexto).overridePendingTransition(R.anim.left_in, R.anim.left_out);
                }
            });
        }

        private void bindData(final Esquema esquema) {
            nombreEsquema.setText(esquema.getNombre());

            List<Canto> cantos = esquema.getCantos();
            if (cantos == null) cantos = new ArrayList<>();

            CantoItemAdapter adapter = new CantoItemAdapter(cantos, (canto, i) -> {
                int pos = getAbsoluteAdapterPosition();
                if (listaEsquemas == null || pos < 0 || pos >= listaEsquemas.size()) return;

                Esquema esq = listaEsquemas.get(pos);
                Intent intent = new Intent(contexto, CantoActivity.class);
                intent.putExtra("canto", canto);
                intent.putExtra("esquema", esq);
                intent.putExtra("bandera", true);
                intent.putExtra("indice", i);

                contexto.startActivity(intent);
                if (contexto instanceof Activity) {
                    ((Activity) contexto).overridePendingTransition(R.anim.left_in, R.anim.left_out);
                }
            });
            listaCantosEsquema.setAdapter(adapter);
        }

    }
}
