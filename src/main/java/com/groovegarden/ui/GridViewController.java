package com.groovegarden.ui;

import com.groovegarden.model.GridModel;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

import java.net.URL;
import java.util.ResourceBundle;

public class GridViewController implements Initializable {
    
    @FXML private GridPane gridPane;
    
    private GridModel gridModel;
    private StackPane[][] cells;
    private static final int GRID_SIZE = 8;
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        initializeGrid();
    }
    
    public void setGridModel(GridModel gridModel) {
        this.gridModel = gridModel;
        updateGridDisplay();
    }
    
    private void initializeGrid() {
        cells = new StackPane[GRID_SIZE][GRID_SIZE];
        
        for (int row = 0; row < GRID_SIZE; row++) {
            for (int col = 0; col < GRID_SIZE; col++) {
                StackPane cell = createCell(row, col);
                cells[row][col] = cell;
                gridPane.add(cell, col, row);
            }
        }
    }
    
    private StackPane createCell(int row, int col) {
        StackPane cell = new StackPane();
        cell.setPrefSize(60, 60);
        cell.setStyle("-fx-background-color: white; -fx-border-color: #ccc; -fx-border-width: 1;");
        
        Rectangle indicator = new Rectangle(50, 50);
        indicator.setFill(Color.WHITE);
        indicator.setStroke(Color.GRAY);
        indicator.setStrokeWidth(1);
        cell.getChildren().add(indicator);
        
        final int finalRow = row;
        final int finalCol = col;
        
        cell.setOnMouseClicked(event -> handleCellClick(event, finalRow, finalCol));
        
        return cell;
    }
    
    private void handleCellClick(MouseEvent event, int row, int col) {
        if (event.getButton() == MouseButton.PRIMARY) {
            // Left click: toggle active state
            gridModel.toggleCell(row, col);
        } else if (event.getButton() == MouseButton.SECONDARY) {
            // Right click: cycle through layers
            gridModel.cycleCellLayer(row, col);
        }
        
        updateGridDisplay();
    }
    
    public void updateGridDisplay() {
        if (gridModel == null) return;
        
        for (int row = 0; row < GRID_SIZE; row++) {
            for (int col = 0; col < GRID_SIZE; col++) {
                updateCellDisplay(row, col);
            }
        }
    }
    
    private void updateCellDisplay(int row, int col) {
        StackPane cell = cells[row][col];
        Rectangle indicator = (Rectangle) cell.getChildren().get(0);
        
        if (!gridModel.isCellActive(row, col)) {
            indicator.setFill(Color.WHITE);
            return;
        }
        
        int layer = gridModel.getCellLayer(row, col);
        switch (layer) {
            case 0: // Rhythm layer
                indicator.setFill(Color.BLUE);
                break;
            case 1: // Melody layer
                indicator.setFill(Color.GREEN);
                break;
            case 2: // Both layers
                indicator.setFill(Color.RED);
                break;
            default:
                indicator.setFill(Color.WHITE);
        }
    }
} 