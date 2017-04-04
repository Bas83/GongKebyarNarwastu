package org.narwastu.gongkebyarnarwastu.instrument;

public class Sound {
    private int resourceId;
    private float pitch;

    public Sound(int resourceId, float pitch) {
        this.resourceId = resourceId;
        this.pitch = pitch;
    }

    public int getResourceId() {
        return resourceId;
    }

    public float getPitch() {
        return pitch;
    }
}
