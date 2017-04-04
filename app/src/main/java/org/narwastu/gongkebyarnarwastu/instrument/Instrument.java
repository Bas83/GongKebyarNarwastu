package org.narwastu.gongkebyarnarwastu.instrument;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class Instrument {
    private Map<Note, Sound> sounds = new HashMap<>();

    public Sound getSound(Note note) {
        return sounds.get(note);
    }

    public void addSound(Note note, Sound sound) {
        sounds.put(note, sound);
    }

    public Set<Note> getNotes() {
        return sounds.keySet();
    }
}
