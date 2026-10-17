import unittest
import json
from app import app
from database import init_db

class LibreriaLuzTestCase(unittest.TestCase):
    def setUp(self):
        self.client = app.test_client()
        init_db()

    def test_home_page(self):
        response = self.client.get("/")
        self.assertEqual(response.status_code, 200)
        html = response.get_data(as_text=True)
        self.assertIn("Librería Luz", html)
        self.assertIn("Libros para", html)
        self.assertIn("volver a casa", html)
        self.assertIn("Cuaderno cosido marfil", html)
        self.assertIn("Cien años de soledad", html)

    def test_api_products(self):
        response = self.client.get("/api/products")
        self.assertEqual(response.status_code, 200)
        data = json.loads(response.data)
        self.assertEqual(data["count"], 12)

    def test_search_filter(self):
        response = self.client.get("/api/products?search=Baldor")
        self.assertEqual(response.status_code, 200)
        data = json.loads(response.data)
        self.assertEqual(len(data["products"]), 1)
        self.assertEqual(data["products"][0]["title"], "Álgebra de Baldor")

    def test_modality_filter(self):
        response = self.client.get("/api/products?modality=renta")
        self.assertEqual(response.status_code, 200)
        data = json.loads(response.data)
        self.assertTrue(len(data["products"]) > 0)
        for p in data["products"]:
            self.assertIn("renta", p["modalities"])

    def test_checkout_and_stock(self):
        # Tomar el primer producto
        res = self.client.get("/api/products/1")
        initial_stock = json.loads(res.data)["stock"]

        # Hacer checkout de 1 unidad
        checkout_res = self.client.post("/api/checkout", json={
            "productId": 1,
            "type": "venta",
            "quantity": 1,
            "customerName": "Test Lector",
            "customerEmail": "test@luz.com"
        })
        self.assertEqual(checkout_res.status_code, 200)
        cdata = json.loads(checkout_res.data)
        self.assertTrue(cdata["success"])
        self.assertEqual(cdata["newStock"], initial_stock - 1)

    def test_admin_stats(self):
        response = self.client.get("/api/admin/stats")
        self.assertEqual(response.status_code, 200)
        data = json.loads(response.data)
        self.assertIn("totalItems", data)
        self.assertIn("totalTransactions", data)

    def tearDown(self):
        # Restaurar estado
        self.client.post("/api/admin/reset")

if __name__ == "__main__":
    unittest.main()
