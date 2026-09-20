package com.librerialuz.dao;

import com.librerialuz.model.Libro;
import java.util.List;

public interface LibroDAO {
    boolean crear(Libro libro);
    Libro obtenerPorId(Long id);
    List<Libro> obtenerTodos();
    boolean actualizar(Libro libro);
    boolean eliminar(Long id);
}