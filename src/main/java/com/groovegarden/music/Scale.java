package com.groovegarden.music;

import java.util.*;

public class Scale {
    private String name;
    private List<Integer> intervals;
    private int rootNote;
    
    private static final Map<String, Scale> PREDEFINED_SCALES = new HashMap<>();
    
    static {
        // C Dorian: C, D, Eb, F, G, A, Bb
        PREDEFINED_SCALES.put("C Dorian", new Scale("C Dorian", Arrays.asList(0, 2, 3, 5, 7, 9, 10), 60));
        
        // C Ionian (Major): C, D, E, F, G, A, B
        PREDEFINED_SCALES.put("C Ionian", new Scale("C Ionian", Arrays.asList(0, 2, 4, 5, 7, 9, 11), 60));
        
        // A Minor: A, B, C, D, E, F, G
        PREDEFINED_SCALES.put("A Minor", new Scale("A Minor", Arrays.asList(0, 2, 3, 5, 7, 8, 10), 57));
    }
    
    public Scale(String name, List<Integer> intervals, int rootNote) {
        this.name = name;
        this.intervals = new ArrayList<>(intervals);
        this.rootNote = rootNote;
    }
    
    public static Scale getScale(String name) {
        return PREDEFINED_SCALES.getOrDefault(name, PREDEFINED_SCALES.get("C Dorian"));
    }
    
    public int getNote(int degree, int octave) {
        if (degree < 0 || degree >= intervals.size()) {
            return rootNote;
        }
        
        int semitones = intervals.get(degree) + (octave * 12);
        return rootNote + semitones;
    }
    
    public List<Integer> getScaleDegrees() {
        return new ArrayList<>(intervals);
    }
    
    public int getRootNote() {
        return rootNote;
    }
    
    public String getName() {
        return name;
    }
    
    public boolean isInScale(int midiNote) {
        int relativeNote = midiNote - rootNote;
        int octave = relativeNote / 12;
        int degree = relativeNote % 12;
        
        return intervals.contains(degree);
    }
    
    public int getDegree(int midiNote) {
        int relativeNote = midiNote - rootNote;
        int degree = relativeNote % 12;
        
        for (int i = 0; i < intervals.size(); i++) {
            if (intervals.get(i) == degree) {
                return i;
            }
        }
        
        return -1; // Not in scale
    }
    
    public int getSize() {
        return intervals.size();
    }
    
    public static Set<String> getAvailableScales() {
        return new HashSet<>(PREDEFINED_SCALES.keySet());
    }
} 