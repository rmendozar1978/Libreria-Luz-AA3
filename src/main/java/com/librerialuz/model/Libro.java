package com.librerialuz.model;

import java.math.BigDecimal;

public class Libro {
    private Long id;
    private String titulo;
    private String isbn;
    private BigDecimal precio;
    private Integer stock;

    public Libro() {}

    public Libro(String titulo, BigDecimal precio, Integer stock) {
        this.titulo = titulo;
        this.precio = precio;
        this.stock = stock;
    }

    public Libro(String titulo, String isbn, BigDecimal precio, Integer stock) {
        this.titulo = titulo;
        this.isbn = isbn;
        this.precio = precio;
        this.stock = stock;
    }

    public Libro(Long id, String titulo, BigDecimal precio, Integer stock) {
        this.id = id;
        this.titulo = titulo;
        this.precio = precio;
        this.stock = stock;
    }

    public Libro(Long id, String titulo, String isbn, BigDecimal precio, Integer stock) {
        this.id = id;
        this.titulo = titulo;
        this.isbn = isbn;
        this.precio = precio;
        this.stock = stock;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }
    public String getIsbn() { return isbn; }
    public void setIsbn(String isbn) { this.isbn = isbn; }
    public BigDecimal getPrecio() { return precio; }
    public void setPrecio(BigDecimal precio) { this.precio = precio; }
    public Integer getStock() { return stock; }
    public void setStock(Integer stock) { this.stock = stock; }

    @Override
    public String toString() {
        return "Libro{" + "id=" + id + ", titulo='" + titulo + '\'' + ", isbn='" + isbn + '\'' + ", precio=" + precio + ", stock=" + stock + '}';
    }
}