package com.pacosotelo.coro.tools;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.pacosotelo.coro.R;
import com.pacosotelo.coro.modelos.Canto;

import java.util.List;

public class CantoEsquemaEditAdapter extends RecyclerView.Adapter<CantoEsquemaEditAdapter.ViewHolder> {

    public interface OnItemClickListener {
        void onItemClick(View view, int position);
    }

    private final List<Canto> cantos;
    private final OnItemClickListener listener;

    public CantoEsquemaEditAdapter(List<Canto> cantos, OnItemClickListener listener) {
        this.cantos = cantos;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_canto_esquema2, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Canto canto = cantos.get(position);
        holder.textView.setText(canto.getNombre());
    }

    @Override
    public int getItemCount() {
        return cantos.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView textView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            textView = itemView.findViewById(R.id.textView);
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    int pos = getBindingAdapterPosition();
                    if (pos != RecyclerView.NO_POSITION) {
                        listener.onItemClick(v, pos);
                    }
                }
            });
        }
    }
}
