package com.store.service;

import com.store.model.CartItem;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ReceiptPrinter {
    public static String print(String cashier, List<CartItem> items, double taxRate, double cashReceived) {
        StringBuilder sb = new StringBuilder();
        double subtotal = items.stream().mapToDouble(CartItem::getLineTotal).sum();
        double tax = subtotal * taxRate;
        double total = subtotal + tax;
        double change = cashReceived - total;

        sb.append("=== Receipt ===\n");
        sb.append("Date: ").append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))).append("\n");
        sb.append("Cashier: ").append(cashier).append("\n\n");
        for (CartItem item : items) {
            sb.append(String.format("%-20s x%-3d $%6.2f%n",
                    item.getProduct().getName(), item.getQuantity(), item.getLineTotal()));
        }
        sb.append("\n");
        sb.append(String.format("Subtotal:   $%6.2f%n", subtotal));
        sb.append(String.format("Tax:        $%6.2f%n", tax));
        sb.append(String.format("Total:      $%6.2f%n", total));
        sb.append(String.format("Cash:       $%6.2f%n", cashReceived));
        sb.append(String.format("Change:     $%6.2f%n", change));
        sb.append("================\n");
        return sb.toString();
    }
}