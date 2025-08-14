package com.groovegarden.music;

import com.groovegarden.model.GridModel;

import javax.sound.midi.*;
import java.io.File;
import java.io.IOException;
import java.util.*;

public class MidiExporter {
    private static final int TICKS_PER_BEAT = 480;
    private static final int BARS_TO_EXPORT = 8;
    private static final int STEPS_PER_BAR = 16;
    
    public void export(GridModel gridModel, MidiEngine midiEngine, String filePath) {
        try {
            // Create MIDI sequence
            Sequence sequence = new Sequence(Sequence.PPQ, TICKS_PER_BEAT);
            Track track = sequence.createTrack();
            
            // Set tempo
            MetaMessage tempoMessage = createTempoMessage(120);
            track.add(new MidiEvent(tempoMessage, 0));
            
            // Export rhythm track
            exportRhythmTrack(track, gridModel, midiEngine);
            
            // Export melody track
            exportMelodyTrack(track, gridModel, midiEngine);
            
            // Export bass track
            exportBassTrack(track, gridModel, midiEngine);
            
            // Write to file
            MidiSystem.write(sequence, 1, new File(filePath));
            
        } catch (InvalidMidiDataException | IOException e) {
            System.err.println("Error exporting MIDI: " + e.getMessage());
        }
    }
    
    private void exportRhythmTrack(Track track, GridModel gridModel, MidiEngine midiEngine) {
        int channel = 9; // Drum channel
        
        for (int bar = 0; bar < BARS_TO_EXPORT; bar++) {
            for (int step = 0; step < STEPS_PER_BAR; step++) {
                long tick = (bar * STEPS_PER_BAR + step) * TICKS_PER_BEAT / 4; // 16th note
                
                // Generate rhythm based on grid state
                boolean shouldPlay = shouldPlayRhythm(gridModel, bar, step);
                
                if (shouldPlay) {
                    // Kick drum on strong beats
                    if (step % 4 == 0) {
                        addNote(track, channel, 36, 100, tick, 120); // Kick
                    }
                    
                    // Snare on weak beats
                    if (step % 4 == 2) {
                        addNote(track, channel, 38, 80, tick, 120); // Snare
                    }
                    
                    // Hi-hat on every pulse
                    addNote(track, channel, 42, 60, tick, 60); // Hi-hat
                }
            }
        }
    }
    
    private void exportMelodyTrack(Track track, GridModel gridModel, MidiEngine midiEngine) {
        int channel = 0; // Melody channel
        
        for (int bar = 0; bar < BARS_TO_EXPORT; bar++) {
            for (int step = 0; step < STEPS_PER_BAR; step++) {
                long tick = (bar * STEPS_PER_BAR + step) * TICKS_PER_BEAT / 4;
                
                // Generate melody based on grid state
                int note = generateMelodyNote(gridModel, bar, step, midiEngine.getCurrentScale());
                
                if (note > 0) {
                    addNote(track, channel, note, 80, tick, 240);
                }
            }
        }
    }
    
    private void exportBassTrack(Track track, GridModel gridModel, MidiEngine midiEngine) {
        int channel = 1; // Bass channel
        
        for (int bar = 0; bar < BARS_TO_EXPORT; bar++) {
            for (int step = 0; step < STEPS_PER_BAR; step++) {
                long tick = (bar * STEPS_PER_BAR + step) * TICKS_PER_BEAT / 4;
                
                // Generate bass based on grid state
                int note = generateBassNote(gridModel, bar, step, midiEngine.getCurrentScale());
                
                if (note > 0) {
                    addNote(track, channel, note, 70, tick, 480); // Longer bass notes
                }
            }
        }
    }
    
    private boolean shouldPlayRhythm(GridModel gridModel, int bar, int step) {
        // Use grid density to determine rhythm probability
        double density = gridModel.getGridDensity();
        return Math.random() < density * 0.8;
    }
    
    private int generateMelodyNote(GridModel gridModel, int bar, int step, Scale scale) {
        // Map grid position to scale degree
        int row = step % gridModel.getGridSize();
        int col = bar % gridModel.getGridSize();
        
        if (gridModel.isCellActive(row, col) && gridModel.getCellLayer(row, col) == 1) {
            int scaleDegree = row % scale.getSize();
            int octave = 4 + (col / 8);
            return scale.getNote(scaleDegree, octave);
        }
        
        return -1; // No note
    }
    
    private int generateBassNote(GridModel gridModel, int bar, int step, Scale scale) {
        // Bass follows root notes of the scale
        if (step % 4 == 0) { // On beat
            int scaleDegree = (bar + step / 4) % scale.getSize();
            return scale.getNote(scaleDegree, 2); // Lower octave
        }
        
        return -1; // No note
    }
    
    private void addNote(Track track, int channel, int note, int velocity, long tick, int duration) {
        try {
            // Note on
            ShortMessage noteOn = new ShortMessage();
            noteOn.setMessage(ShortMessage.NOTE_ON, channel, note, velocity);
            track.add(new MidiEvent(noteOn, tick));
            
            // Note off
            ShortMessage noteOff = new ShortMessage();
            noteOff.setMessage(ShortMessage.NOTE_OFF, channel, note, 0);
            track.add(new MidiEvent(noteOff, tick + duration));
            
        } catch (InvalidMidiDataException e) {
            System.err.println("Error adding note: " + e.getMessage());
        }
    }
    
    private MetaMessage createTempoMessage(int bpm) {
        try {
            // Convert BPM to microseconds per quarter note
            int tempo = 60000000 / bpm;
            byte[] data = new byte[3];
            data[0] = (byte) ((tempo >> 16) & 0xFF);
            data[1] = (byte) ((tempo >> 8) & 0xFF);
            data[2] = (byte) (tempo & 0xFF);
            
            return new MetaMessage(0x51, data, data.length);
            
        } catch (InvalidMidiDataException e) {
            System.err.println("Error creating tempo message: " + e.getMessage());
            return null;
        }
    }
} 