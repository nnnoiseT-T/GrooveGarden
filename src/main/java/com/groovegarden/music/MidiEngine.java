package com.groovegarden.music;

import com.groovegarden.algo.EuclideanRhythm;
import com.groovegarden.algo.MarkovMelody;
import com.groovegarden.model.GridModel;

import javax.sound.midi.*;
import java.util.*;

public class MidiEngine {
    private Synthesizer synthesizer;
    private MidiChannel[] channels;
    private Scale currentScale;
    private boolean isPlaying;
    private int currentStep;
    private int currentBar;
    
    // Algorithm instances
    private EuclideanRhythm euclideanRhythm;
    private MarkovMelody markovMelody;
    
    // Music state
    private List<Integer> melodyHistory;
    private boolean[] rhythmPattern;
    private int tempo;
    
    // Channel assignments
    private static final int DRUM_CHANNEL = 9;
    private static final int BASS_CHANNEL = 1;
    private static final int MELODY_CHANNEL = 0;
    
    public MidiEngine() {
        this.currentScale = Scale.getScale("C Dorian");
        this.isPlaying = false;
        this.currentStep = 0;
        this.currentBar = 0;
        this.tempo = 120;
        
        this.euclideanRhythm = new EuclideanRhythm(16, 4);
        this.markovMelody = new MarkovMelody(2);
        this.melodyHistory = new ArrayList<>();
        this.rhythmPattern = new boolean[16];
        
        initializeMidi();
        updateMarkovScale();
    }
    
    private void initializeMidi() {
        try {
            synthesizer = MidiSystem.getSynthesizer();
            synthesizer.open();
            channels = synthesizer.getChannels();
            
            // Set up channels
            if (channels[MELODY_CHANNEL] != null) {
                channels[MELODY_CHANNEL].programChange(0); // Acoustic Grand Piano
            }
            if (channels[BASS_CHANNEL] != null) {
                channels[BASS_CHANNEL].programChange(32); // Acoustic Bass
            }
            if (channels[DRUM_CHANNEL] != null) {
                channels[DRUM_CHANNEL].programChange(0); // Standard Kit
            }
            
        } catch (MidiUnavailableException e) {
            System.err.println("MIDI synthesizer not available: " + e.getMessage());
        }
    }
    
    public void start() {
        isPlaying = true;
        currentStep = 0;
        currentBar = 0;
    }
    
    public void stop() {
        isPlaying = false;
        // Stop all notes
        if (channels != null) {
            for (MidiChannel channel : channels) {
                if (channel != null) {
                    channel.allNotesOff();
                }
            }
        }
    }
    
    public void tick(GridModel gridModel) {
        if (!isPlaying) return;
        
        // Update algorithms based on grid state
        updateAlgorithms(gridModel);
        
        // Generate and play music
        generateRhythm();
        generateMelody();
        
        // Advance step
        currentStep = (currentStep + 1) % 16;
        if (currentStep == 0) {
            currentBar++;
        }
    }
    
    private void updateAlgorithms(GridModel gridModel) {
        // Update Euclidean rhythm based on grid density
        double density = gridModel.getGridDensity();
        int pulses = Math.max(1, (int) (density * 8));
        euclideanRhythm.setPulses(pulses);
        rhythmPattern = euclideanRhythm.generate();
        
        // Update Markov melody based on grid color distribution
        updateMarkovScale();
    }
    
    private void generateRhythm() {
        if (channels[DRUM_CHANNEL] == null) return;
        
        if (rhythmPattern[currentStep]) {
            // Kick drum on strong beats
            if (currentStep % 4 == 0) {
                channels[DRUM_CHANNEL].noteOn(36, 100); // Kick
                scheduleNoteOff(DRUM_CHANNEL, 36, 200);
            }
            
            // Snare on weak beats
            if (currentStep % 4 == 2) {
                channels[DRUM_CHANNEL].noteOn(38, 80); // Snare
                scheduleNoteOff(DRUM_CHANNEL, 38, 200);
            }
            
            // Hi-hat on every pulse
            channels[DRUM_CHANNEL].noteOn(42, 60); // Hi-hat
            scheduleNoteOff(DRUM_CHANNEL, 42, 100);
        }
    }
    
    private void generateMelody() {
        if (channels[MELODY_CHANNEL] == null) return;
        
        // Generate melody note using Markov chain
        int scaleDegree = markovMelody.generateNextNote(melodyHistory);
        int octave = 4 + (currentStep / 8); // Vary octave by position
        int midiNote = currentScale.getNote(scaleDegree, octave);
        
        // Play note
        channels[MELODY_CHANNEL].noteOn(midiNote, 80);
        scheduleNoteOff(MELODY_CHANNEL, midiNote, 300);
        
        // Update history
        melodyHistory.add(scaleDegree);
        if (melodyHistory.size() > 8) {
            melodyHistory.remove(0);
        }
    }
    
    private void scheduleNoteOff(int channel, int note, int delayMs) {
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (channels[channel] != null) {
                    channels[channel].noteOff(note);
                }
                timer.cancel();
            }
        }, delayMs);
    }
    
    private void updateMarkovScale() {
        markovMelody.setScaleDegrees(currentScale.getScaleDegrees());
    }
    
    public void setScale(String scaleName) {
        this.currentScale = Scale.getScale(scaleName);
        updateMarkovScale();
    }
    
    public void setTempo(int tempo) {
        this.tempo = tempo;
    }
    
    public Scale getCurrentScale() {
        return currentScale;
    }
    
    public boolean isPlaying() {
        return isPlaying;
    }
    
    public int getCurrentStep() {
        return currentStep;
    }
    
    public int getCurrentBar() {
        return currentBar;
    }
    
    public void close() {
        if (synthesizer != null && synthesizer.isOpen()) {
            synthesizer.close();
        }
    }
} 