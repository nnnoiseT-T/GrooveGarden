package com.groovegarden.model;

public class CellularAutomaton {
    private int gridSize;
    private boolean[][] currentState;
    private boolean[][] nextState;
    
    public CellularAutomaton(int gridSize) {
        this.gridSize = gridSize;
        this.currentState = new boolean[gridSize][gridSize];
        this.nextState = new boolean[gridSize][gridSize];
    }
    
    public void update(boolean[][] inputGrid) {
        // Copy input grid to current state
        for (int row = 0; row < gridSize; row++) {
            for (int col = 0; col < gridSize; col++) {
                currentState[row][col] = inputGrid[row][col];
            }
        }
        
        // Apply Conway's Game of Life rules
        for (int row = 0; row < gridSize; row++) {
            for (int col = 0; col < gridSize; col++) {
                int neighbors = countNeighbors(row, col);
                boolean isAlive = currentState[row][col];
                
                if (isAlive) {
                    // Live cell: survives with 2-3 neighbors
                    nextState[row][col] = (neighbors == 2 || neighbors == 3);
                } else {
                    // Dead cell: becomes alive with exactly 3 neighbors
                    nextState[row][col] = (neighbors == 3);
                }
            }
        }
        
        // Swap states
        boolean[][] temp = currentState;
        currentState = nextState;
        nextState = temp;
    }
    
    private int countNeighbors(int row, int col) {
        int count = 0;
        
        for (int dr = -1; dr <= 1; dr++) {
            for (int dc = -1; dc <= 1; dc++) {
                if (dr == 0 && dc == 0) continue; // Skip self
                
                int nr = row + dr;
                int nc = col + dc;
                
                // Check bounds
                if (nr >= 0 && nr < gridSize && nc >= 0 && nc < gridSize) {
                    if (currentState[nr][nc]) {
                        count++;
                    }
                }
            }
        }
        
        return count;
    }
    
    public boolean[][] getCurrentState() {
        return currentState;
    }
    
    public void setCell(int row, int col, boolean alive) {
        if (row >= 0 && row < gridSize && col >= 0 && col < gridSize) {
            currentState[row][col] = alive;
        }
    }
    
    public void clear() {
        for (int row = 0; row < gridSize; row++) {
            for (int col = 0; col < gridSize; col++) {
                currentState[row][col] = false;
                nextState[row][col] = false;
            }
        }
    }
} 