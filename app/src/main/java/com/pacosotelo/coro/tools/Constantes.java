package com.pacosotelo.coro.tools;

import com.pacosotelo.coro.modelos.Usuario;

public class Constantes {
    // Hacer volátil reduce riesgos en acceso concurrente desde diferentes hilos
    public static volatile Usuario usuario;
    public static final String[] tonos = {"DO#","REb","RE#","MIb","FA#","SOLb",
            "SOL#","LAb","LA#","SIb","DO","RE","MI","FA","SOL","LA","SI"};
    public static final String[] extras = {"","m","7","m7"};
    public static final int VERSION = 12;

    // Usar volatile para visibilidad entre hilos
    public static volatile String GRUPO_SELECCIONADO = "";

    // Accessors thread-safe
    public static synchronized void setUsuario(Usuario u) { usuario = u; }
    public static synchronized Usuario getUsuario() { return usuario; }

    public static synchronized void setGrupoSeleccionado(String g) { GRUPO_SELECCIONADO = g; }
    public static synchronized String getGrupoSeleccionado() { return GRUPO_SELECCIONADO; }

}
