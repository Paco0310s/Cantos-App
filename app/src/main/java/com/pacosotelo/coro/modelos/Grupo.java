package com.pacosotelo.coro.modelos;

import java.io.Serializable;
import java.util.ArrayList;

public class Grupo implements Serializable {
    private String id;
    private String nombre;
    private String clave;
    private ArrayList<Usuario> usuarios;


}
