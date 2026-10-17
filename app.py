import sqlite3
from flask import Flask, render_template, request, jsonify
from database import get_db, init_db

app = Flask(__name__)

# Asegurar que la base de datos esté lista
init_db()

def product_to_dict(row):
    return {
        "id": row["id"],
        "title": row["title"],
        "author": row["author"],
        "category": row["category"],
        "price": row["price"],
        "stock": row["stock"],
        "modalities": [m.strip() for m in row["modalities"].split(",") if m.strip()],
        "description": row["description"] or "",
        "is_featured": bool(row["is_featured"])
    }

@app.route("/")
def index():
    conn = get_db()
    cursor = conn.cursor()
    cursor.execute("SELECT * FROM products ORDER BY id ASC")
    products = [product_to_dict(r) for r in cursor.fetchall()]
    conn.close()
    return render_template("index.html", products=products)

@app.route("/api/products")
def get_products():
    search = request.args.get("search", "").strip()
    modality = request.args.get("modality", "todo").strip().lower()
    category = request.args.get("category", "todas").strip().lower()

    conn = get_db()
    cursor = conn.cursor()

    query = "SELECT * FROM products WHERE 1=1"
    params = []

    if search:
        query += " AND (title LIKE ? OR author LIKE ?)"
        params.extend([f"%{search}%", f"%{search}%"])

    if modality and modality != "todo":
        query += " AND modalities LIKE ?"
        params.append(f"%{modality}%")

    if category and category != "todas":
        query += " AND LOWER(category) = ?"
        params.append(category)

    query += " ORDER BY id ASC"

    cursor.execute(query, params)
    rows = cursor.fetchall()
    products = [product_to_dict(r) for r in rows]
    conn.close()

    return jsonify({"products": products, "count": len(products)})

@app.route("/api/products/<int:product_id>")
def get_product(product_id):
    conn = get_db()
    cursor = conn.cursor()
    cursor.execute("SELECT * FROM products WHERE id = ?", (product_id,))
    row = cursor.fetchone()
    conn.close()

    if not row:
        return jsonify({"error": "Producto no encontrado"}), 404

    return jsonify(product_to_dict(row))

@app.route("/api/checkout", methods=["POST"])
def checkout():
    data = request.get_json() or {}
    product_id = data.get("productId")
    action_type = data.get("type", "venta").lower() # venta, renta, prestamo
    quantity = int(data.get("quantity", 1))
    customer_name = data.get("customerName", "Lector anónimo").strip()
    customer_email = data.get("customerEmail", "").strip()
    days = int(data.get("days", 7))

    if not product_id or quantity <= 0:
        return jsonify({"error": "Datos de solicitud inválidos"}), 400

    conn = get_db()
    cursor = conn.cursor()

    cursor.execute("SELECT * FROM products WHERE id = ?", (product_id,))
    product = cursor.fetchone()

    if not product:
        conn.close()
        return jsonify({"error": "El producto no existe"}), 404

    current_stock = product["stock"]
    if current_stock < quantity:
        conn.close()
        return jsonify({"error": f"Stock insuficiente. Solo quedan {current_stock} disponibles."}), 400

    new_stock = current_stock - quantity

    # Calcular monto según tipo
    unit_price = product["price"]
    if action_type == "venta":
        total_amount = unit_price * quantity
    elif action_type == "renta":
        # Renta aprox 15% del valor base por semana
        weeks = max(1, (days + 6) // 7)
        total_amount = round(unit_price * 0.15 * weeks * quantity, 0)
    else: # prestamo
        total_amount = 0.0 # Préstamo gratuito con sistema de casa

    # Actualizar stock
    cursor.execute("UPDATE products SET stock = ? WHERE id = ?", (new_stock, product_id))

    # Registrar transacción
    cursor.execute('''
        INSERT INTO transactions (product_id, type, quantity, customer_name, customer_email, total_amount, days, status)
        VALUES (?, ?, ?, ?, ?, ?, ?, ?)
    ''', (product_id, action_type, quantity, customer_name, customer_email, total_amount, days, "completado"))

    conn.commit()

    transaction_id = cursor.lastrowid
    conn.close()

    return jsonify({
        "success": True,
        "transactionId": transaction_id,
        "newStock": new_stock,
        "totalAmount": total_amount,
        "message": f"Operación de {action_type} confirmada exitosamente."
    })

@app.route("/api/admin/stats")
def admin_stats():
    conn = get_db()
    cursor = conn.cursor()

    cursor.execute("SELECT COUNT(*) as total_items, SUM(stock) as total_units FROM products")
    inventory = cursor.fetchone()

    cursor.execute("SELECT COUNT(*) as low_stock FROM products WHERE stock <= 2")
    low_stock = cursor.fetchone()["low_stock"]

    cursor.execute("""
        SELECT COUNT(*) as total_transactions, 
               COALESCE(SUM(total_amount), 0) as total_sales,
               SUM(CASE WHEN type='prestamo' THEN 1 ELSE 0 END) as total_prestamos,
               SUM(CASE WHEN type='renta' THEN 1 ELSE 0 END) as total_rentas,
               SUM(CASE WHEN type='venta' THEN 1 ELSE 0 END) as total_ventas
        FROM transactions
    """)
    tx_stats = cursor.fetchone()

    cursor.execute("""
        SELECT t.id, t.type, t.quantity, t.customer_name, t.total_amount, t.created_at, p.title as product_title
        FROM transactions t
        JOIN products p ON t.product_id = p.id
        ORDER BY t.created_at DESC
        LIMIT 8
    """)
    recent_tx = [dict(r) for r in cursor.fetchall()]

    conn.close()

    return jsonify({
        "totalItems": inventory["total_items"] or 0,
        "totalUnits": inventory["total_units"] or 0,
        "lowStock": low_stock or 0,
        "totalTransactions": tx_stats["total_transactions"] or 0,
        "totalSales": tx_stats["total_sales"] or 0,
        "totalLoans": tx_stats["total_prestamos"] or 0,
        "totalRentals": tx_stats["total_rentas"] or 0,
        "totalPurchases": tx_stats["total_ventas"] or 0,
        "recentTransactions": recent_tx
    })

@app.route("/api/admin/products", methods=["POST"])
def add_product():
    data = request.get_json() or {}
    title = data.get("title", "").strip()
    author = data.get("author", "Librería Luz").strip()
    category = data.get("category", "Literatura").strip()
    price = float(data.get("price", 0))
    stock = int(data.get("stock", 0))
    modalities = data.get("modalities", "venta,prestamo").strip()
    description = data.get("description", "").strip()

    if not title:
        return jsonify({"error": "El título es obligatorio"}), 400

    conn = get_db()
    cursor = conn.cursor()
    cursor.execute("""
        INSERT INTO products (title, author, category, price, stock, modalities, description)
        VALUES (?, ?, ?, ?, ?, ?, ?)
    """, (title, author, category, price, stock, modalities, description))
    conn.commit()
    new_id = cursor.lastrowid
    conn.close()

    return jsonify({"success": True, "id": new_id, "message": "Ejemplar añadido al catálogo."})

@app.route("/api/admin/products/<int:product_id>", methods=["PUT"])
def update_product(product_id):
    data = request.get_json() or {}
    stock = data.get("stock")
    price = data.get("price")

    conn = get_db()
    cursor = conn.cursor()

    if stock is not None and price is not None:
        cursor.execute("UPDATE products SET stock = ?, price = ? WHERE id = ?", (int(stock), float(price), product_id))
    elif stock is not None:
        cursor.execute("UPDATE products SET stock = ? WHERE id = ?", (int(stock), product_id))
    elif price is not None:
        cursor.execute("UPDATE products SET price = ? WHERE id = ?", (float(price), product_id))
    
    conn.commit()
    conn.close()

    return jsonify({"success": True, "message": "Producto actualizado correctamente."})

@app.route("/api/admin/reset", methods=["POST"])
def reset_db():
    conn = get_db()
    cursor = conn.cursor()
    cursor.execute("DROP TABLE IF EXISTS products")
    cursor.execute("DROP TABLE IF EXISTS transactions")
    conn.commit()
    conn.close()
    init_db()
    return jsonify({"success": True, "message": "Catálogo restaurado a los valores iniciales del diseño."})

if __name__ == "__main__":
    app.run(host="0.0.0.0", port=5000, debug=True)
