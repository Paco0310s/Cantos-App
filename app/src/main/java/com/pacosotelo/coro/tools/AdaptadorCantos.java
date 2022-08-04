package com.pacosotelo.coro.tools;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.PopupMenu;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.pacosotelo.coro.R;
import com.pacosotelo.coro.modelos.Canto;
import com.pacosotelo.coro.ui.CantoActivity;
import java.util.List;

public class AdaptadorCantos extends RecyclerView.Adapter<AdaptadorCantos.ViewHolder> {
    private final LayoutInflater mInflater;
    private final Context contexto;
    private List<Canto> listaCantos;

    public AdaptadorCantos(List<Canto> listaCantos, Context contexto) {
        this.mInflater = LayoutInflater.from(contexto);
        this.contexto = contexto;
        this.listaCantos = listaCantos;
    }

    @NonNull
    @Override
    public AdaptadorCantos.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.item_canto, null);
        return new AdaptadorCantos.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AdaptadorCantos.ViewHolder holder, int position) {
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
        //ImageButton bOpciones;
        CardView cv;

        @SuppressLint("NonConstantResourceId")
        ViewHolder(View itemView) {
            super(itemView);

            nombreCanto = itemView.findViewById(R.id.tNombreCanto);
            momentoCanto = itemView.findViewById(R.id.tMomentoCanto);
            tiempoCanto = itemView.findViewById(R.id.tTiempoCanto);
            //bOpciones = itemView.findViewById(R.id.bOpciones);
            cv = itemView.findViewById(R.id.cvCanto);

            itemView.setOnClickListener(v -> {
                Canto canto = listaCantos.get(getAbsoluteAdapterPosition());

                Intent i = new Intent(contexto, CantoActivity.class);
                i.putExtra("canto",canto);

                contexto.startActivity(i);
                ((Activity) contexto).overridePendingTransition(R.anim.left_in,R.anim.left_out);
            });

            /*bOpciones.setOnClickListener(v -> {
                final PopupMenu popupMenu = new PopupMenu(contexto, bOpciones);
                popupMenu.getMenuInflater().inflate(R.menu.popup_menu_canto,popupMenu.getMenu());
                popupMenu.setOnMenuItemClickListener(menuItem -> {
                    switch (menuItem.getItemId()) {
                        case R.id.pdf:
                            return true;
                        default:
                            return false;
                    }
                });
                popupMenu.show();
            });*/
        }

        private void bindData(final Canto canto) {
            nombreCanto.setText(canto.getNombre());
            momentoCanto.setText(canto.getMomentos().toString());
            tiempoCanto.setText(canto.getTiempos().toString());
        }

    }
}
