package com.librerialuz.dao.impl;

import com.librerialuz.config.DatabaseConnection;
import com.librerialuz.dao.LibroDAO;
import com.librerialuz.model.Libro;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class LibroDAOImpl implements LibroDAO {

    @Override
    public boolean crear(Libro libro) {
        String sql = "INSERT INTO libro (titulo, isbn, precio, stock) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, libro.getTitulo());
            stmt.setString(2, libro.getIsbn());
            stmt.setBigDecimal(3, libro.getPrecio());
            stmt.setInt(4, libro.getStock());

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error al insertar libro: " + e.getMessage());
            return false;
        }
    }

    @Override
    public Libro obtenerPorId(Long id) {
        String sql = "SELECT id, titulo, isbn, precio, stock FROM libro WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new Libro(
                        rs.getLong("id"),
                        rs.getString("titulo"),
                        rs.getString("isbn"),
                        rs.getBigDecimal("precio"),
                        rs.getInt("stock")
                    );
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener libro por ID: " + e.getMessage());
        }
        return null;
    }

    @Override
    public List<Libro> obtenerTodos() {
        List<Libro> lista = new ArrayList<>();
        String sql = "SELECT id, titulo, isbn, precio, stock FROM libro";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Libro libro = new Libro();
                libro.setId(rs.getLong("id"));
                libro.setTitulo(rs.getString("titulo"));
                libro.setIsbn(rs.getString("isbn"));
                libro.setPrecio(rs.getBigDecimal("precio"));
                libro.setStock(rs.getInt("stock"));

                lista.add(libro);
            }
        } catch (SQLException e) {
            System.err.println("Error al listar libros: " + e.getMessage());
        }
        return lista;
    }

    @Override
    public boolean actualizar(Libro libro) {
        String sql = "UPDATE libro SET titulo = ?, isbn = ?, precio = ?, stock = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, libro.getTitulo());
            stmt.setString(2, libro.getIsbn());
            stmt.setBigDecimal(3, libro.getPrecio());
            stmt.setInt(4, libro.getStock());
            stmt.setLong(5, libro.getId());

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error al actualizar libro: " + e.getMessage());
        }
        return false;
    }

    @Override
    public boolean eliminar(Long id) {
        String sql = "DELETE FROM libro WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error al eliminar libro: " + e.getMessage());
        }
        return false;
    }
}