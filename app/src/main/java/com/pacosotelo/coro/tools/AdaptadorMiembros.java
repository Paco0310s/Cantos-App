package com.pacosotelo.coro.tools;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.pacosotelo.coro.R;
import com.pacosotelo.coro.modelos.Usuario;
import com.squareup.picasso.Picasso;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class AdaptadorMiembros extends RecyclerView.Adapter<AdaptadorMiembros.ViewHolder> {

    private final LayoutInflater mInflater;
    private final Context contexto;
    private final List<Usuario> listaMiembros;
    private final List<String> listaAdmins;
    private final String creadorId;

    public AdaptadorMiembros(Context contexto, List<Usuario> listaMiembros, List<String> listaAdmins, String creadorId) {
        this.mInflater = LayoutInflater.from(contexto);
        this.contexto = contexto;
        this.listaMiembros = listaMiembros;
        this.listaAdmins = listaAdmins;
        this.creadorId = creadorId;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.item_miembro, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bindData(listaMiembros.get(position));
    }

    @Override
    public int getItemCount() {
        return listaMiembros != null ? listaMiembros.size() : 0;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        CircleImageView civFoto;
        TextView tvNombre, tvEmail, tvBadgeAdmin;
        ImageView ivOpciones;

        ViewHolder(View itemView) {
            super(itemView);
            civFoto = itemView.findViewById(R.id.civFotoMiembro);
            tvNombre = itemView.findViewById(R.id.tvNombreMiembro);
            tvEmail = itemView.findViewById(R.id.tvEmailMiembro);
            tvBadgeAdmin = itemView.findViewById(R.id.tvBadgeAdmin);
            ivOpciones = itemView.findViewById(R.id.ivOpcionesMiembro);
        }

        private void bindData(final Usuario usuario) {
            tvNombre.setText(usuario.getNombre());
            tvEmail.setText(usuario.getEmail() != null ? usuario.getEmail() : "Sin correo");

            if (usuario.getFoto() != null && !usuario.getFoto().isEmpty()) {
                Picasso.get().load(usuario.getFoto()).into(civFoto);
            } else {
                civFoto.setImageResource(R.drawable.ic_usuario);
            }

            boolean esAdmin = (listaAdmins != null && listaAdmins.contains(usuario.getUid())) || (creadorId != null && usuario.getUid().equals(creadorId));

            if (esAdmin) {
                tvBadgeAdmin.setVisibility(View.VISIBLE);
            } else {
                tvBadgeAdmin.setVisibility(View.GONE);
            }

            if (ivOpciones != null) {
                ivOpciones.setVisibility(View.GONE);
            }
        }
    }
}
