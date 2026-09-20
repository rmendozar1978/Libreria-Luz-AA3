package com.librerialuz.servlet;

import com.librerialuz.dao.LibroDAO;
import com.librerialuz.dao.impl.LibroDAOImpl;
import com.librerialuz.model.Libro;

import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet(name = "LibroServlet", urlPatterns = {"/LibroServlet"})
public class LibroServlet extends HttpServlet {

    private final LibroDAO libroDAO = new LibroDAOImpl();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setContentType("text/html;charset=UTF-8");

        String accion = request.getParameter("accion");
        if (accion == null) {
            accion = "listar";
        }

        switch (accion) {
            case "editar":
                mostrarFormularioEdicion(request, response);
                break;
            case "eliminar":
                procesarEliminacion(request, response);
                break;
            case "listar":
            default:
                mostrarListadoLibros(request, response);
                break;
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setContentType("text/html;charset=UTF-8");

        String accion = request.getParameter("accion");
        if ("actualizar".equals(accion)) {
            procesarActualizacion(request, response);
        } else {
            procesarRegistro(request, response);
        }
    }

    /**
     * Renderiza la tabla con todos los libros guardados y opciones de acción (Editar / Eliminar).
     */
    private void mostrarListadoLibros(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        List<Libro> listaLibros = libroDAO.obtenerTodos();
        if (listaLibros == null) {
            listaLibros = new ArrayList<>();
        }

        try (PrintWriter out = response.getWriter()) {
            out.println("<!DOCTYPE html>");
            out.println("<html lang='es'>");
            out.println("<head>");
            out.println("<meta charset='UTF-8'>");
            out.println("<meta name='viewport' content='width=device-width, initial-scale=1.0'>");
            out.println("<title>Listado de Libros - Librería Luz</title>");
            out.println("<style>");
            out.println("body { font-family: Arial, sans-serif; background-color: #f4f6f9; margin: 0; padding: 30px 20px; }");
            out.println(".container { max-width: 900px; margin: 0 auto; background: white; padding: 25px; border-radius: 8px; box-shadow: 0 2px 10px rgba(0,0,0,0.1); }");
            out.println("h1 { color: #2c3e50; text-align: center; margin-top: 0; }");
            out.println(".header-bar { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; }");
            out.println("table { width: 100%; border-collapse: collapse; margin-top: 10px; }");
            out.println("th, td { border: 1px solid #ddd; padding: 10px 12px; text-align: left; }");
            out.println("th { background-color: #34495e; color: white; }");
            out.println("tr:nth-child(even) { background-color: #f9f9f9; }");
            out.println("tr:hover { background-color: #f1f1f1; }");
            out.println(".btn { display: inline-block; padding: 10px 16px; background-color: #27ae60; color: white; text-decoration: none; border-radius: 4px; font-weight: bold; }");
            out.println(".btn:hover { background-color: #219653; }");
            out.println(".btn-action { display: inline-block; padding: 6px 12px; text-decoration: none; border-radius: 4px; font-size: 13px; font-weight: bold; margin-right: 5px; }");
            out.println(".btn-edit { background-color: #2980b9; color: white; }");
            out.println(".btn-edit:hover { background-color: #1f618d; }");
            out.println(".btn-delete { background-color: #e74c3c; color: white; }");
            out.println(".btn-delete:hover { background-color: #c0392b; }");
            out.println(".empty-msg { text-align: center; color: #7f8c8d; padding: 30px; }");
            out.println(".actions-cell { white-space: nowrap; text-align: center; }");
            out.println("</style>");
            out.println("</head>");
            out.println("<body>");
            out.println("<div class='container'>");
            out.println("<div class='header-bar'>");
            out.println("<h1>Catálogo de Libros</h1>");
            out.println("<a href='index.html' class='btn'>+ Registrar Libro</a>");
            out.println("</div>");

            if (listaLibros.isEmpty()) {
                out.println("<div class='empty-msg'><p>No hay libros registrados actualmente en la base de datos.</p>");
                out.println("<a href='index.html' class='btn'>Registrar el primer libro</a></div>");
            } else {
                out.println("<table>");
                out.println("<thead>");
                out.println("<tr>");
                out.println("<th>ID</th>");
                out.println("<th>Título</th>");
                out.println("<th>ISBN</th>");
                out.println("<th>Precio ($)</th>");
                out.println("<th>Stock</th>");
                out.println("<th style='text-align: center;'>Acciones</th>");
                out.println("</tr>");
                out.println("</thead>");
                out.println("<tbody>");
                for (Libro libro : listaLibros) {
                    out.println("<tr>");
                    out.println("<td>" + (libro.getId() != null ? libro.getId() : "") + "</td>");
                    out.println("<td>" + escapeHtml(libro.getTitulo()) + "</td>");
                    out.println("<td>" + escapeHtml(libro.getIsbn() != null ? libro.getIsbn() : "N/A") + "</td>");
                    out.println("<td>" + (libro.getPrecio() != null ? libro.getPrecio().toPlainString() : "0.00") + "</td>");
                    out.println("<td>" + (libro.getStock() != null ? libro.getStock() : 0) + "</td>");
                    out.println("<td class='actions-cell'>");
                    out.println("<a href='LibroServlet?accion=editar&id=" + libro.getId() + "' class='btn-action btn-edit'>Editar</a>");
                    out.println("<a href='LibroServlet?accion=eliminar&id=" + libro.getId() + "' class='btn-action btn-delete' onclick=\"return confirm('¿Está seguro de eliminar el libro: " + escapeJs(libro.getTitulo()) + "?');\">Eliminar</a>");
                    out.println("</td>");
                    out.println("</tr>");
                }
                out.println("</tbody>");
                out.println("</table>");
            }

            out.println("</div>");
            out.println("</body>");
            out.println("</html>");
        }
    }

    /**
     * Muestra el formulario para editar un libro existente cargando sus datos.
     */
    private void mostrarFormularioEdicion(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        String idStr = request.getParameter("id");
        Long id = parseId(idStr);

        if (id == null) {
            List<String> errores = new ArrayList<>();
            errores.add("Identificador de libro no válido.");
            mostrarMensajeError(response, "Error al editar", errores);
            return;
        }

        Libro libro = libroDAO.obtenerPorId(id);
        if (libro == null) {
            List<String> errores = new ArrayList<>();
            errores.add("El libro con ID " + id + " no fue encontrado en la base de datos.");
            mostrarMensajeError(response, "Libro no encontrado", errores);
            return;
        }

        try (PrintWriter out = response.getWriter()) {
            out.println("<!DOCTYPE html>");
            out.println("<html lang='es'>");
            out.println("<head>");
            out.println("<meta charset='UTF-8'>");
            out.println("<meta name='viewport' content='width=device-width, initial-scale=1.0'>");
            out.println("<title>Editar Libro - Librería Luz</title>");
            out.println("<style>");
            out.println("body { font-family: Arial, sans-serif; background-color: #f4f6f9; margin: 0; padding: 30px 20px; }");
            out.println(".container { max-width: 500px; margin: 0 auto; background: #ffffff; padding: 30px; border-radius: 8px; box-shadow: 0 2px 10px rgba(0,0,0,0.1); }");
            out.println("h2 { color: #2c3e50; text-align: center; margin-top: 0; margin-bottom: 20px; }");
            out.println(".form-group { margin-bottom: 18px; }");
            out.println("label { display: block; margin-bottom: 6px; font-weight: bold; color: #34495e; }");
            out.println("input[type='text'], input[type='number'] { width: 100%; padding: 10px; border: 1px solid #ccd1d9; border-radius: 4px; font-size: 14px; box-sizing: border-box; }");
            out.println("input[readonly] { background-color: #eef1f5; color: #7f8c8d; cursor: not-allowed; }");
            out.println("input:focus { border-color: #2980b9; outline: none; }");
            out.println(".btn-submit { width: 100%; padding: 12px; background-color: #2980b9; color: white; border: none; border-radius: 4px; font-size: 16px; font-weight: bold; cursor: pointer; }");
            out.println(".btn-submit:hover { background-color: #1f618d; }");
            out.println(".btn-cancel { display: block; text-align: center; margin-top: 15px; color: #7f8c8d; text-decoration: none; font-size: 14px; }");
            out.println(".btn-cancel:hover { text-decoration: underline; color: #34495e; }");
            out.println("</style>");
            out.println("</head>");
            out.println("<body>");
            out.println("<div class='container'>");
            out.println("<h2>Editar Libro</h2>");
            out.println("<form action='LibroServlet' method='POST'>");
            out.println("<input type='hidden' name='accion' value='actualizar'>");
            out.println("<div class='form-group'>");
            out.println("<label for='id'>ID (no editable):</label>");
            out.println("<input type='text' id='id' name='id' value='" + libro.getId() + "' readonly>");
            out.println("</div>");
            out.println("<div class='form-group'>");
            out.println("<label for='titulo'>Título:</label>");
            out.println("<input type='text' id='titulo' name='titulo' value='" + escapeHtml(libro.getTitulo()) + "' maxlength='150' required>");
            out.println("</div>");
            out.println("<div class='form-group'>");
            out.println("<label for='isbn'>ISBN:</label>");
            out.println("<input type='text' id='isbn' name='isbn' value='" + escapeHtml(libro.getIsbn() != null ? libro.getIsbn() : "") + "' maxlength='20'>");
            out.println("</div>");
            out.println("<div class='form-group'>");
            out.println("<label for='precio'>Precio ($):</label>");
            out.println("<input type='number' id='precio' name='precio' step='0.01' min='0' value='" + (libro.getPrecio() != null ? libro.getPrecio().toPlainString() : "") + "' required>");
            out.println("</div>");
            out.println("<div class='form-group'>");
            out.println("<label for='stock'>Stock:</label>");
            out.println("<input type='number' id='stock' name='stock' step='1' min='0' value='" + (libro.getStock() != null ? libro.getStock() : "") + "' required>");
            out.println("</div>");
            out.println("<button type='submit' class='btn-submit'>Guardar Cambios</button>");
            out.println("<a href='LibroServlet' class='btn-cancel'>Cancelar y Volver al Listado</a>");
            out.println("</form>");
            out.println("</div>");
            out.println("</body>");
            out.println("</html>");
        }
    }

    /**
     * Procesa la eliminación de un libro tras recibir la confirmación por GET.
     */
    private void procesarEliminacion(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        String idStr = request.getParameter("id");
        Long id = parseId(idStr);

        if (id == null) {
            List<String> errores = new ArrayList<>();
            errores.add("Identificador de libro no válido para eliminación.");
            mostrarMensajeError(response, "Error al eliminar", errores);
            return;
        }

        boolean eliminado = libroDAO.eliminar(id);
        if (eliminado) {
            response.sendRedirect("LibroServlet");
        } else {
            List<String> errores = new ArrayList<>();
            errores.add("No se pudo eliminar el libro de la base de datos. Verifique si el registro aún existe.");
            mostrarMensajeError(response, "Error de eliminación", errores);
        }
    }

    /**
     * Procesa el registro de un nuevo libro (POST desde index.html).
     */
    private void procesarRegistro(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        String titulo = request.getParameter("titulo");
        String isbn = request.getParameter("isbn");
        String precioStr = request.getParameter("precio");
        String stockStr = request.getParameter("stock");

        List<String> errores = validarDatosLibro(titulo, isbn, precioStr, stockStr);

        if (!errores.isEmpty()) {
            mostrarMensajeError(response, "Datos del formulario inválidos", errores);
            return;
        }

        BigDecimal precio = new BigDecimal(precioStr.trim());
        Integer stock = Integer.parseInt(stockStr.trim());
        String isbnLimpio = (isbn != null && !isbn.trim().isEmpty()) ? isbn.trim() : null;

        Libro nuevoLibro = new Libro();
        nuevoLibro.setTitulo(titulo.trim());
        nuevoLibro.setIsbn(isbnLimpio);
        nuevoLibro.setPrecio(precio);
        nuevoLibro.setStock(stock);

        boolean guardado = libroDAO.crear(nuevoLibro);
        if (guardado) {
            response.sendRedirect("LibroServlet");
        } else {
            List<String> dbErrores = new ArrayList<>();
            dbErrores.add("No se pudo registrar el libro en la base de datos. Verifique que el servicio de MySQL esté activo y que la tabla 'libro' exista.");
            mostrarMensajeError(response, "Error al guardar el libro", dbErrores);
        }
    }

    /**
     * Procesa la actualización de un libro existente (POST desde formulario de edición).
     */
    private void procesarActualizacion(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        String idStr = request.getParameter("id");
        String titulo = request.getParameter("titulo");
        String isbn = request.getParameter("isbn");
        String precioStr = request.getParameter("precio");
        String stockStr = request.getParameter("stock");

        List<String> errores = new ArrayList<>();

        Long id = parseId(idStr);
        if (id == null) {
            errores.add("El identificador del libro es inválido o no fue recibido.");
        }

        errores.addAll(validarDatosLibro(titulo, isbn, precioStr, stockStr));

        if (!errores.isEmpty()) {
            mostrarMensajeError(response, "Datos de edición inválidos", errores);
            return;
        }

        BigDecimal precio = new BigDecimal(precioStr.trim());
        Integer stock = Integer.parseInt(stockStr.trim());
        String isbnLimpio = (isbn != null && !isbn.trim().isEmpty()) ? isbn.trim() : null;

        Libro libroActualizar = new Libro();
        libroActualizar.setId(id);
        libroActualizar.setTitulo(titulo.trim());
        libroActualizar.setIsbn(isbnLimpio);
        libroActualizar.setPrecio(precio);
        libroActualizar.setStock(stock);

        boolean actualizado = libroDAO.actualizar(libroActualizar);
        if (actualizado) {
            response.sendRedirect("LibroServlet");
        } else {
            List<String> dbErrores = new ArrayList<>();
            dbErrores.add("No se pudo actualizar el libro en la base de datos. Verifique la conexión con MySQL.");
            mostrarMensajeError(response, "Error al actualizar el libro", dbErrores);
        }
    }

    /**
     * Valida de manera exhaustiva los campos comunes del libro.
     */
    private List<String> validarDatosLibro(String titulo, String isbn, String precioStr, String stockStr) {
        List<String> errores = new ArrayList<>();

        // Validación de título
        if (titulo == null || titulo.trim().isEmpty()) {
            errores.add("El título del libro es obligatorio.");
        } else if (titulo.trim().length() > 150) {
            errores.add("El título no puede exceder los 150 caracteres.");
        }

        // Validación de ISBN (opcional, pero con longitud máxima de 20 si se proporciona)
        if (isbn != null && isbn.trim().length() > 20) {
            errores.add("El ISBN no puede exceder los 20 caracteres.");
        }

        // Validación de precio
        if (precioStr == null || precioStr.trim().isEmpty()) {
            errores.add("El precio es obligatorio.");
        } else {
            try {
                BigDecimal precio = new BigDecimal(precioStr.trim());
                if (precio.compareTo(BigDecimal.ZERO) < 0) {
                    errores.add("El precio no puede ser negativo.");
                }
            } catch (NumberFormatException e) {
                errores.add("El formato del precio es inválido (debe ser un valor numérico).");
            }
        }

        // Validación de stock
        if (stockStr == null || stockStr.trim().isEmpty()) {
            errores.add("El stock es obligatorio.");
        } else {
            try {
                int stock = Integer.parseInt(stockStr.trim());
                if (stock < 0) {
                    errores.add("El stock no puede ser negativo.");
                }
            } catch (NumberFormatException e) {
                errores.add("El stock debe ser un número entero válido.");
            }
        }

        return errores;
    }

    /**
     * Parsea de manera segura un ID numérico.
     */
    private Long parseId(String idStr) {
        if (idStr == null || idStr.trim().isEmpty()) {
            return null;
        }
        try {
            long val = Long.parseLong(idStr.trim());
            return val > 0 ? val : null;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * Muestra una página HTML limpia con los mensajes de error en caso de fallo de validación o BD.
     */
    private void mostrarMensajeError(HttpServletResponse response, String tituloError, List<String> detalles)
            throws IOException {
        response.setContentType("text/html;charset=UTF-8");
        try (PrintWriter out = response.getWriter()) {
            out.println("<!DOCTYPE html>");
            out.println("<html lang='es'>");
            out.println("<head>");
            out.println("<meta charset='UTF-8'>");
            out.println("<meta name='viewport' content='width=device-width, initial-scale=1.0'>");
            out.println("<title>Error - Librería Luz</title>");
            out.println("<style>");
            out.println("body { font-family: Arial, sans-serif; background-color: #fce4e4; margin: 0; padding: 30px 20px; }");
            out.println(".error-box { max-width: 600px; margin: 40px auto; background: white; padding: 25px; border-left: 6px solid #e74c3c; border-radius: 6px; box-shadow: 0 2px 8px rgba(0,0,0,0.1); }");
            out.println("h2 { color: #c0392b; margin-top: 0; }");
            out.println("ul { color: #555; line-height: 1.6; }");
            out.println(".btn { display: inline-block; margin-top: 15px; padding: 10px 18px; background-color: #34495e; color: white; text-decoration: none; border-radius: 4px; font-weight: bold; }");
            out.println(".btn:hover { background-color: #2c3e50; }");
            out.println("</style>");
            out.println("</head>");
            out.println("<body>");
            out.println("<div class='error-box'>");
            out.println("<h2>" + escapeHtml(tituloError) + "</h2>");
            out.println("<ul>");
            for (String error : detalles) {
                out.println("<li>" + escapeHtml(error) + "</li>");
            }
            out.println("</ul>");
            out.println("<a href='LibroServlet' class='btn'>Volver al Catálogo</a>");
            out.println("</div>");
            out.println("</body>");
            out.println("</html>");
        }
    }

    /**
     * Escapa caracteres HTML para evitar ataques Cross-Site Scripting (XSS).
     */
    private String escapeHtml(String input) {
        if (input == null) {
            return "";
        }
        return input.replace("&", "&amp;")
                    .replace("<", "&lt;")
                    .replace(">", "&gt;")
                    .replace("\"", "&quot;")
                    .replace("'", "&#39;");
    }

    /**
     * Escapa caracteres para cadenas JavaScript en el diálogo de confirmación.
     */
    private String escapeJs(String input) {
        if (input == null) {
            return "";
        }
        return input.replace("\\", "\\\\")
                    .replace("'", "\\'")
                    .replace("\"", "\\\"");
    }
}