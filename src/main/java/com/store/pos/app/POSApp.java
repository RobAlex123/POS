package com.store.pos.app;

import com.store.pos.model.CartItem;
import com.store.pos.model.Product;
import com.store.pos.service.Inventory;
import com.store.pos.service.ReceiptPrinter;

import java.io.IOException;
import java.util.*;

public class POSApp {
    private static final double TAX_RATE = 0.16; // 16% VAT (example for MX)
    private static final Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        Inventory inventory = new Inventory("inventory.csv");
        try {
            inventory.load();
        } catch (IOException e) {
            System.err.println("Failed to load inventory: " + e.getMessage());
            return;
        }

        System.out.println("Point of Sale - Java Console");
        System.out.print("Enter cashier name: ");
        String cashier = scanner.nextLine();

        boolean running = true;
        while (running) {
            switch (menu()) {
                case 1 -> listProducts(inventory);
                case 2 -> processSale(inventory, cashier);
                case 3 -> manageInventory(inventory);
                case 0 -> running = false;
                default -> System.out.println("Invalid option.");
            }
        }
        System.out.println("Goodbye!");
    }

    private static int menu() {
        System.out.println("\nMenu:");
        System.out.println("1) List products");
        System.out.println("2) New sale");
        System.out.println("3) Manage inventory");
        System.out.println("0) Exit");
        System.out.print("Choose: ");
        return readInt();
    }

    private static void listProducts(Inventory inventory) {
        System.out.println("\nInventory:");
        inventory.listAll().forEach(p ->
                System.out.printf("%-8s | %-15s | $%6.2f | stock: %d%n",
                        p.getSku(), p.getName(), p.getPrice(), p.getStock()));
    }

    private static void processSale(Inventory inventory, String cashier) {
        List<CartItem> cart = new ArrayList<>();
        boolean adding = true;
        while (adding) {
            System.out.print("\nEnter SKU (or 'done'): ");
            String sku = scanner.nextLine().trim();
            if (sku.equalsIgnoreCase("done")) break;

            Optional<Product> opt = inventory.findBySku(sku);
            if (opt.isEmpty()) {
                System.out.println("SKU not found.");
                continue;
            }
            Product p = opt.get();

            System.out.print("Quantity: ");
            int qty = readInt();
            if (qty <= 0) {
                System.out.println("Quantity must be positive.");
                continue;
            }
            if (p.getStock() < qty) {
                System.out.println("Not enough stock.");
                continue;
            }

            // Adjust stock and add to cart
            if (inventory.adjustStock(sku, -qty)) {
                // merge line if existing
                Optional<CartItem> existing = cart.stream()
                        .filter(ci -> ci.getProduct().getSku().equals(sku)).findFirst();
                if (existing.isPresent()) {
                    existing.get().setQuantity(existing.get().getQuantity() + qty);
                } else {
                    cart.add(new CartItem(p, qty));
                }
            }
        }

        if (cart.isEmpty()) {
            System.out.println("Cart is empty. Sale canceled.");
            return;
        }

        double subtotal = cart.stream().mapToDouble(CartItem::getLineTotal).sum();
        double tax = subtotal * TAX_RATE;
        double total = subtotal + tax;

        System.out.printf("Subtotal: $%.2f, Tax: $%.2f, Total: $%.2f%n", subtotal, tax, total);
        System.out.print("Cash received: ");
        double cash = readDouble();

        if (cash < total) {
            System.out.println("Insufficient cash. Sale canceled, restoring stock...");
            // restore stock
            cart.forEach(ci -> inventory.adjustStock(ci.getProduct().getSku(), ci.getQuantity()));
            return;
        }

        String receipt = ReceiptPrinter.print(cashier, cart, TAX_RATE, cash);
        System.out.println("\n" + receipt);

        try {
            inventory.save();
        } catch (IOException e) {
            System.err.println("Failed to save inventory: " + e.getMessage());
        }
    }

    private static void manageInventory(Inventory inventory) {
        System.out.println("\nInventory management:");
        System.out.println("1) Add/Update product");
        System.out.println("2) Adjust stock");
        System.out.print("Choose: ");
        int option = readInt();

        switch (option) {
            case 1 -> {
                System.out.print("SKU: "); String sku = scanner.nextLine().trim();
                System.out.print("Name: "); String name = scanner.nextLine().trim();
                System.out.print("Price: "); double price = readDouble();
                System.out.print("Stock: "); int stock = readInt();
                inventory.upsertProduct(new Product(sku, name, price, stock));
                try { inventory.save(); } catch (IOException e) { System.err.println(e.getMessage()); }
                System.out.println("Product saved.");
            }
            case 2 -> {
                System.out.print("SKU: "); String sku = scanner.nextLine().trim();
                System.out.print("Delta (+/-): "); int delta = readInt();
                boolean ok = inventory.adjustStock(sku, delta);
                if (!ok) System.out.println("Adjust failed.");
                else {
                    try { inventory.save(); } catch (IOException e) { System.err.println(e.getMessage()); }
                    System.out.println("Stock adjusted.");
                }
            }
            default -> System.out.println("Returning to menu.");
        }
    }

    private static int readInt() {
        while (true) {
            try {
                String s = scanner.nextLine().trim();
                return Integer.parseInt(s);
            } catch (NumberFormatException e) {
                System.out.print("Enter a valid integer: ");
            }
        }
    }

    private static double readDouble() {
        while (true) {
            try {
                String s = scanner.nextLine().trim();
                return Double.parseDouble(s);
            } catch (NumberFormatException e) {
                System.out.print("Enter a valid number: ");
            }
        }
    }
}