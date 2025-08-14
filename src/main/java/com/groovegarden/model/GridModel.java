package com.groovegarden.model;

import com.groovegarden.model.CellularAutomaton;

public class GridModel {
    private static final int GRID_SIZE = 8;
    private boolean[][] activeCells;
    private int[][] cellLayers; // 0: rhythm, 1: melody, 2: both
    private CellularAutomaton cellularAutomaton;
    private String currentScale = "C Dorian";
    
    public GridModel() {
        activeCells = new boolean[GRID_SIZE][GRID_SIZE];
        cellLayers = new int[GRID_SIZE][GRID_SIZE];
        cellularAutomaton = new CellularAutomaton(GRID_SIZE);
    }
    
    public boolean isCellActive(int row, int col) {
        return activeCells[row][col];
    }
    
    public int getCellLayer(int row, int col) {
        return cellLayers[row][col];
    }
    
    public void toggleCell(int row, int col) {
        activeCells[row][col] = !activeCells[row][col];
        if (!activeCells[row][col]) {
            cellLayers[row][col] = 0;
        }
    }
    
    public void cycleCellLayer(int row, int col) {
        if (!activeCells[row][col]) return;
        
        cellLayers[row][col] = (cellLayers[row][col] + 1) % 3;
    }
    
    public void setScale(String scale) {
        this.currentScale = scale;
    }
    
    public String getScale() {
        return currentScale;
    }
    
    public void update() {
        // Update cellular automaton
        cellularAutomaton.update(activeCells);
        
        // Apply cellular automaton rules to grid
        boolean[][] newState = cellularAutomaton.getCurrentState();
        for (int row = 0; row < GRID_SIZE; row++) {
            for (int col = 0; col < GRID_SIZE; col++) {
                if (newState[row][col]) {
                    activeCells[row][col] = true;
                }
            }
        }
    }
    
    public boolean[][] getActiveCells() {
        return activeCells;
    }
    
    public int[][] getCellLayers() {
        return cellLayers;
    }
    
    public int getGridSize() {
        return GRID_SIZE;
    }
    
    public int getActiveCellCount() {
        int count = 0;
        for (int row = 0; row < GRID_SIZE; row++) {
            for (int col = 0; col < GRID_SIZE; col++) {
                if (activeCells[row][col]) count++;
            }
        }
        return count;
    }
    
    public double getGridDensity() {
        return (double) getActiveCellCount() / (GRID_SIZE * GRID_SIZE);
    }
    
    public void clear() {
        for (int row = 0; row < GRID_SIZE; row++) {
            for (int col = 0; col < GRID_SIZE; col++) {
                activeCells[row][col] = false;
                cellLayers[row][col] = 0;
            }
        }
        // Also clear the cellular automaton
        cellularAutomaton.clear();
    }
} 