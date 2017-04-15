package org.narwastu.gongkebyarnarwastu;

import android.os.Environment;

import java.io.File;
import java.util.ArrayList;

import static android.os.Environment.DIRECTORY_MUSIC;

public class SongsManager {
    private ArrayList<Song> songsList = new ArrayList<>();

    public ArrayList<Song> getPlayList() {
        File musicDir = Environment.getExternalStoragePublicDirectory(DIRECTORY_MUSIC);
        if (musicDir.exists()) {
            File[] files = musicDir.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.getName().toLowerCase().endsWith(".mp3")) {
                        Song song = new Song(file.getName().substring(0, (file.getName().length() - 4)), file.getPath());
                        songsList.add(song);
                    }
                }
            }
        }
        return songsList;
    }
}