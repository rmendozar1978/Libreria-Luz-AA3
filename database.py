import sqlite3
import os

DB_PATH = os.path.join(os.path.dirname(__file__), 'libreria.db')

def get_db():
    conn = sqlite3.connect(DB_PATH)
    conn.row_factory = sqlite3.Row
    return conn

def init_db():
    conn = get_db()
    cursor = conn.cursor()

    # Tabla de productos
    cursor.execute('''
        CREATE TABLE IF NOT EXISTS products (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            title TEXT NOT NULL,
            author TEXT NOT NULL,
            category TEXT NOT NULL,
            price REAL NOT NULL,
            stock INTEGER NOT NULL,
            modalities TEXT NOT NULL,
            description TEXT,
            is_featured INTEGER DEFAULT 0
        )
    ''')

    # Tabla de transacciones (Ventas, Rentas, Préstamos)
    cursor.execute('''
        CREATE TABLE IF NOT EXISTS transactions (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            product_id INTEGER NOT NULL,
            type TEXT NOT NULL,
            quantity INTEGER NOT NULL,
            customer_name TEXT,
            customer_email TEXT,
            total_amount REAL,
            days INTEGER DEFAULT 0,
            status TEXT DEFAULT 'completado',
            created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
            FOREIGN KEY (product_id) REFERENCES products (id)
        )
    ''')

    # Verificar si ya existen registros
    cursor.execute('SELECT COUNT(*) as count FROM products')
    count = cursor.fetchone()['count']

    if count == 0:
        seed_data = [
            (
                "Cuaderno cosido marfil",
                "Librería Luz",
                "Papelería",
                12000.0,
                24,
                "venta,prestamo",
                "Cuaderno de encuadernación artesanal cosida con hojas de papel marfil de 90g. Ideal para tinta y anotaciones diarias.",
                0
            ),
            (
                "Set de resaltadores cálidos",
                "Librería Luz",
                "Papelería",
                18000.0,
                17,
                "venta",
                "Colección de tonos tierra y pasteles suaves diseñados para subrayar sin cansar la vista ni traspasar el papel.",
                0
            ),
            (
                "Pluma fuente nogal",
                "Librería Luz",
                "Papelería",
                39000.0,
                0,
                "venta",
                "Instrumento de escritura tallado en madera de nogal con plumín de acero cromado y trazo fluido y elegante.",
                0
            ),
            (
                "Organizador de escritorio nogal",
                "Librería Luz",
                "Oficina",
                48000.0,
                4,
                "venta",
                "Pieza de sobremesa en madera maciza de nogal para albergar plumas, libretas pequeñas y clips de lectura.",
                0
            ),
            (
                "Lámpara de lectura cálida",
                "Librería Luz",
                "Oficina",
                95000.0,
                1,
                "venta,renta",
                "Iluminación puntual con temperatura de color de 2700K regulable, pensada para cuidar la vista en tus sesiones de lectura.",
                0
            ),
            (
                "Cien años de soledad",
                "Gabriel García Márquez",
                "Literatura",
                59000.0,
                7,
                "venta,renta,prestamo",
                "La obra cumbre del realismo mágico latinoamericano que narra la historia de la estirpe de los Buendía a lo largo de siete generaciones en Macondo.",
                1
            ),
            (
                "El amor en los tiempos del cólera",
                "Gabriel García Márquez",
                "Literatura",
                52000.0,
                4,
                "venta,renta,prestamo",
                "Una emocionante novela sobre la perseverancia, el anhelo y el amor incombustible de Florentino Ariza y Fermina Daza.",
                0
            ),
            (
                "La sombra del viento",
                "Carlos Ruiz Zafón",
                "Literatura",
                68000.0,
                5,
                "venta,renta,prestamo",
                "Un misterio gótico inolvidable ambientado en la Barcelona de posguerra que comienza en las entrañas del Cementerio de los Libros Olvidados.",
                0
            ),
            (
                "La Odisea",
                "Homero",
                "Literatura",
                45000.0,
                8,
                "venta,renta,prestamo",
                "El gran clásico de la literatura universal que relata las legendarias peripecias de Odiseo en su travesía de regreso a Ítaca.",
                0
            ),
            (
                "Álgebra de Baldor",
                "Aurelio Baldor",
                "Académico",
                78000.0,
                2,
                "venta,renta,prestamo",
                "El tratado de matemáticas más célebre de habla hispana, con explicaciones paso a paso y miles de ejercicios prácticos.",
                0
            ),
            (
                "Matemáticas 10",
                "Varios autores",
                "Académico",
                64000.0,
                1,
                "venta,renta,prestamo",
                "Compendio completo de trigonometría analítica, funciones y geometría para el aprendizaje en educación secundaria.",
                0
            ),
            (
                "Física para curiosos",
                "Rafael García Molina",
                "Académico",
                55000.0,
                6,
                "venta,renta,prestamo",
                "Un fascinante recorrido a través de experimentos cotidianos y explicaciones claras que descubren cómo funciona la naturaleza.",
                0
            )
        ]

        cursor.executemany('''
            INSERT INTO products (title, author, category, price, stock, modalities, description, is_featured)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
        ''', seed_data)

    conn.commit()
    conn.close()

if __name__ == '__main__':
    init_db()
    print("Base de datos inicializada correctamente.")
