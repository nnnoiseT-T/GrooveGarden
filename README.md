# Groove Garden

A real-time algorithmic music composition game built with Java 17 and JavaFX.

## Features

- **8×8 Interactive Grid**: Place algorithm seeds to generate music
- **Real-time Music Generation**: Three algorithms working together
  - Cellular Automaton → Drum patterns
  - Euclidean Rhythm → Bass and percussion
  - Markov Chain → Melody generation
- **Live Controls**: Start/Stop, Tempo (60-180 BPM), Scale selection
- **Real-time Scoring**: Diversity, Flow, and Harmony metrics
- **MIDI Export**: Save your compositions

## Quick Start

### Requirements
- Java 17 or higher
- JavaFX support

### Run
```bash
# Build
./gradlew build

# Run
./gradlew run
```

### How to Use
1. **Left-click** on grid cells to activate/deactivate
2. **Right-click** to cycle through layers (Rhythm/Melody/Both)
3. **Adjust tempo** and select scale
4. **Click Start** to begin music generation
5. **Export** your composition as MIDI

## Architecture

- **UI**: JavaFX with optimized grid rendering
- **Music**: MIDI engine with real-time synthesis
- **Algorithms**: Cellular automaton, Euclidean rhythm, Markov chains
- **Performance**: Cached scoring, memory monitoring, configurable settings

## Configuration

The app automatically creates `groove-garden.properties` with customizable settings for:
- Grid size and appearance
- Music parameters (tempo, scales)
- Performance tuning
- MIDI settings

## Performance Features

- **Memory monitoring** with automatic GC
- **Optimized grid updates** to reduce redraws
- **Cached scoring** to avoid recalculation
- **Configurable update intervals**

## Error Handling

- **User-friendly error dialogs**
- **MIDI device detection** and fallback
- **File operation error handling**
- **Comprehensive logging**

Built with performance and stability in mind. 