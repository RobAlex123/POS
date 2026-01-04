package com.store.pos.app;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

public class POSApp extends Application {

    private TextArea receiptArea;
    private TextField productField;
    private TextField quantityField;

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Point of Sale System");

        // Input fields
        Label productLabel = new Label("Product SKU:");
        productField = new TextField();

        Label quantityLabel = new Label("Quantity:");
        quantityField = new TextField();

        GridPane inputGrid = new GridPane();
        inputGrid.setHgap(10);
        inputGrid.setVgap(10);
        inputGrid.add(productLabel, 0, 0);
        inputGrid.add(productField, 1, 0);
        inputGrid.add(quantityLabel, 0, 1);
        inputGrid.add(quantityField, 1, 1);

        // Receipt area
        receiptArea = new TextArea();
        receiptArea.setEditable(false);

        // Buttons
        Button addButton = new Button("Add to Cart");
        addButton.setOnAction(e -> {
            String sku = productField.getText();
            String qty = quantityField.getText();
            receiptArea.appendText("Added SKU " + sku + " x" + qty + "\n");
        });

        Button checkoutButton = new Button("Checkout");
        checkoutButton.setOnAction(e -> receiptArea.appendText("Checkout complete!\n"));

        HBox buttonBox = new HBox(10, addButton, checkoutButton);

        // Layout
        VBox root = new VBox(15, inputGrid, receiptArea, buttonBox);
        root.setStyle("-fx-padding: 15;");

        Scene scene = new Scene(root, 500, 400);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}