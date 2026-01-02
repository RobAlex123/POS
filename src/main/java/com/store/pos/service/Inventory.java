package com.store.pos.service;

import com.store.pos.model.Product;

import java.io.*;
import java.nio.file.*;
import java.util.*;

public class Inventory {
    private final Map<String, Product> products = new HashMap<>();
    private final Path csvPath;

    public Inventory(String csvFile) {
        this.csvPath = Paths.get(csvFile);
    }

    public void load() throws IOException {
        products.clear();
        if (!Files.exists(csvPath)) {
            // Seed with demo products
            products.put("SKU001", new Product("SKU001", "Coffee", 2.50, 50));
            products.put("SKU002", new Product("SKU002", "Croissant", 1.75, 30));
            products.put("SKU003", new Product("SKU003", "Sandwich", 5.99, 20));
            save();
            return;
        }
        try (BufferedReader br = Files.newBufferedReader(csvPath)) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty() || line.startsWith("#")) continue;
                String[] parts = line.split(",");
                if (parts.length < 4) continue;
                String sku = parts[0].trim();
                String name = parts[1].trim();
                double price = Double.parseDouble(parts[2].trim());
                int stock = Integer.parseInt(parts[3].trim());
                products.put(sku, new Product(sku, name, price, stock));
            }
        }
    }

    public void save() throws IOException {
        try (BufferedWriter bw = Files.newBufferedWriter(csvPath)) {
            bw.write("# sku,name,price,stock\n");
            for (Product p : products.values()) {
                bw.write(String.format("%s,%s,%.2f,%d%n",
                        p.getSku(), p.getName(), p.getPrice(), p.getStock()));
            }
        }
    }

    public Optional<Product> findBySku(String sku) {
        return Optional.ofNullable(products.get(sku));
    }

    public Collection<Product> listAll() {
        return Collections.unmodifiableCollection(products.values());
    }

    public boolean adjustStock(String sku, int delta) {
        Product p = products.get(sku);
        if (p == null) return false;
        int newStock = p.getStock() + delta;
        if (newStock < 0) return false;
        p.setStock(newStock);
        return true;
    }

    public void upsertProduct(Product product) {
        products.put(product.getSku(), product);
    }
}