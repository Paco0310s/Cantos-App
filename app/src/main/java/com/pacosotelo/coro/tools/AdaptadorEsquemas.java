package com.pacosotelo.coro.tools;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
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
import com.pacosotelo.coro.ui.NuevoEsquemaActivity;

import java.util.ArrayList;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class AdaptadorEsquemas extends RecyclerView.Adapter<AdaptadorEsquemas.ViewHolder> {
    private final LayoutInflater mInflater;
    private final Context contexto;
    private List<Esquema> listaEsquemas;

    public AdaptadorEsquemas(List<Esquema> listaEsquemas, Context contexto) {
        this.mInflater = LayoutInflater.from(contexto);
        this.contexto = contexto;
        this.listaEsquemas = new ArrayList<>(listaEsquemas);
    }

    @NonNull
    @Override
    public AdaptadorEsquemas.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.item_esquema, parent, false);
        return new ViewHolder(view);
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
        actualizarLista(listaEsquemas);
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
                try {
                    Esquema oldE = listaEsquemas.get(oldItemPosition);
                    Esquema newE = nuevaLista.get(newItemPosition);
                    if (oldE == null || newE == null || oldE.getId() == null || newE.getId() == null) {
                        return false;
                    }
                    return oldE.getId().equals(newE.getId());
                } catch (Exception e) {
                    return false;
                }
            }

            @Override
            public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
                try {
                    Esquema oldE = listaEsquemas.get(oldItemPosition);
                    Esquema newE = nuevaLista.get(newItemPosition);

                    if (oldE == null || newE == null) return false;

                    boolean nombreIgual = Objects.equals(oldE.getNombre(), newE.getNombre());
                    boolean cantosIgual = Objects.equals(oldE.getCantos(), newE.getCantos());

                    return nombreIgual && cantosIgual;
                } catch (Exception e) {
                    return false;
                }
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
                    Transiciones.left((Activity) contexto);
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
                    Transiciones.left((Activity) contexto);
                }
            });
            listaCantosEsquema.setAdapter(adapter);
        }

    }
}
