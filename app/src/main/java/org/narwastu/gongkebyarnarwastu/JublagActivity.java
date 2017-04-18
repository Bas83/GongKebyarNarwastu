package org.narwastu.gongkebyarnarwastu;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import org.narwastu.gongkebyarnarwastu.instrument.Instrument;
import org.narwastu.gongkebyarnarwastu.instrument.Note;
import org.narwastu.gongkebyarnarwastu.instrument.Sound;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class JublagActivity extends AppCompatActivity implements View.OnTouchListener {

    private final int SONG_SELECTED = 1;
    private String TAG = "JublagActivity";
    private Map<Note, Integer> soundIdsByNote = new HashMap<>();
    private Map<Note, Integer> streamIdsByNote = new HashMap<>();
    private Instrument jublag;
    private SoundPool sp;
    private MediaPlayer mp;
    private boolean soundsLoaded = false;
    private float volume;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_jublag);

        ImageView iv = (ImageView) findViewById(R.id.ImageViewJublag);
        if (iv != null) {
            iv.setOnTouchListener(this);
        }

        this.setVolumeControlStream(AudioManager.STREAM_MUSIC);
        AudioManager audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
        float actVolume = (float) audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        float maxVolume = (float) audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        volume = actVolume / maxVolume;


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            SoundPool.Builder sp21 = new SoundPool.Builder();
            sp21.setMaxStreams(5);
            sp = sp21.build();
        } else {
            sp = new SoundPool(5, AudioManager.STREAM_MUSIC, 0);
        }
        sp.setOnLoadCompleteListener(new SoundPool.OnLoadCompleteListener() {
            @Override
            public void onLoadComplete(SoundPool soundPool, int sampleId, int status) {
                soundsLoaded = true;
            }

        });

        jublag = initializeJublag();

        for (Note note : jublag.getNotes())
            soundIdsByNote.put(note, sp.load(this, jublag.getSound(note).getResourceId(), 1));


        mp = new MediaPlayer();
        mp.setVolume(volume, volume);

        toast("Touch a note to play, or touch in the lower part to stop a note.");
    }

    private Instrument initializeJublag() {
        float dongRelativePitch = 1.04f;
        float dengRelativePitch = 1.21f;
        float dungRelativePitch = 1.52f;
        float dangRelativePitch = 1.6f;

        Instrument jublag = new Instrument();
        //all the same but played at different pitch, because using the actual same id to play notes at different pitch seems buggy
        jublag.addSound(Note.M_DING, new Sound(R.raw.ding, 1f));
        jublag.addSound(Note.M_DONG, new Sound(R.raw.ding, dongRelativePitch));
        jublag.addSound(Note.M_DENG, new Sound(R.raw.ding, dengRelativePitch));
        jublag.addSound(Note.M_DUNG, new Sound(R.raw.ding, dungRelativePitch));
        jublag.addSound(Note.M_DANG, new Sound(R.raw.ding, dangRelativePitch));

        return jublag;
    }

    @Override
    public boolean onTouch(View v, MotionEvent ev) {
        boolean handledHere;

        final int action = ev.getAction();

        final int evX = (int) ev.getX();
        final int evY = (int) ev.getY();

        switch (action) {
            case MotionEvent.ACTION_DOWN:

                int touchColor = getHotspotColor(R.id.ImageViewJublagOverlay, evX, evY);

                // Compare the touchColor to the expected values. Switch to a different image, depending on what color was touched.
                // Note that we use a Color Tool object to test whether the observed color is close enough to the real color to
                // count as a match. We do this because colors on the screen do not match the map exactly because of scaling and
                // varying pixel density.
                int tolerance = 25;

                if (closeMatch(Color.rgb(0, 0, 255), touchColor, tolerance))
                    playNote(Note.M_DING);
                else if (closeMatch(Color.rgb(0, 0, 204), touchColor, tolerance))
                    stopNote(Note.M_DING);
                else if (closeMatch(Color.rgb(255, 0, 255), touchColor, tolerance))
                    playNote(Note.M_DONG);
                else if (closeMatch(Color.rgb(204, 0, 204), touchColor, tolerance))
                    stopNote(Note.M_DONG);
                else if (closeMatch(Color.rgb(255, 0, 0), touchColor, tolerance))
                    playNote(Note.M_DENG);
                else if (closeMatch(Color.rgb(204, 0, 0), touchColor, tolerance))
                    stopNote(Note.M_DENG);
                else if (closeMatch(Color.rgb(0, 255, 0), touchColor, tolerance))
                    playNote(Note.M_DUNG);
                else if (closeMatch(Color.rgb(0, 204, 0), touchColor, tolerance))
                    stopNote(Note.M_DUNG);
                else if (closeMatch(Color.rgb(0, 255, 255), touchColor, tolerance))
                    playNote(Note.M_DANG);
                else if (closeMatch(Color.rgb(0, 204, 204), touchColor, tolerance))
                    stopNote(Note.M_DANG);


                handledHere = true;

                break;
            case MotionEvent.ACTION_UP:
                handledHere = true;
                break;
            default:
                handledHere = false;
        }

        return handledHere;
    }

    private void playNote(Note note) {
        if (!soundsLoaded)
            return;

        //  TODO: stop only if already playing
        Integer streamId = streamIdsByNote.get(note);
        if (streamId != null)
            sp.setVolume(streamId, 0, 0);
        int newStreamId = sp.play(soundIdsByNote.get(note), volume, volume, 1, 0, jublag.getSound(note).getPitch());
        streamIdsByNote.put(note, newStreamId);
        Log.d(TAG, String.format("Played note %s, stream id %d", note, newStreamId));
    }

    private void stopNote(Note note) {
        // TODO: 31/03/2017 find in map of notes actually being played, stop it
        Integer streamId = streamIdsByNote.get(note);
        // Don't actually stop, this causes a clicking sound due to the waveform
        // being stopped in the middle of a wave, rather than at a zero-crossing.
        // The downside is that if someone is hammering a note, previous different notes will be automatically cut off
        // due to reaching the SoundPool limit, because all hits on the same note will trigger a new wave being played,
        // even though you only hear the last one.
        if (streamId != null)
            sp.setVolume(streamId, 0, 0);
    }

    private void toast(String msg) {
        Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG).show();
    }

    private int getHotspotColor(int hotspotId, int x, int y) {

        /*
        // Fix any offsets by the positioning of screen elements such as Activity titlebar.
// This part was causing me issues when I was testing out Bill Lahti's code.
        int[] location = new int[2];
        iv.getLocationOnScreen(location);
        x -= location[0];
        y -= location[1];
        */

        ImageView img = (ImageView) findViewById(hotspotId);
        if (img == null) {
            //Log.d ("ImageAreasActivity", "Hot spot image not found");
            return 0;
        } else {
            img.setDrawingCacheEnabled(true);
            Bitmap hotspots = Bitmap.createBitmap(img.getDrawingCache());
            if (hotspots == null) {
                //Log.d ("ImageAreasActivity", "Hot spot bitmap was not created");
                return 0;
            } else {
                img.setDrawingCacheEnabled(false);
                return hotspots.getPixel(x, y);
            }
        }
    }

    /**
     * Return true if the two colors are a pretty good match.
     * To be a good match, all three color values (RGB) must be within the tolerance value given.
     *
     * @param color1    int
     * @param color2    int
     * @param tolerance int - the max difference that is allowed for any of the RGB components
     * @return boolean
     */

    private boolean closeMatch(int color1, int color2, int tolerance) {
        if (Math.abs(Color.red(color1) - Color.red(color2)) > tolerance) return false;
        if (Math.abs(Color.green(color1) - Color.green(color2)) > tolerance) return false;
        if (Math.abs(Color.blue(color1) - Color.blue(color2)) > tolerance) return false;
        return true;
    } // end match

    public void open(View view) {
        //Simpler version below, but doesn't work for getting audio from recordings and I suspect the other version works
        //for SD cards as well, though I haven't been able to test it yet.
//        startActivityForResult(new Intent(Intent.ACTION_PICK,
//                android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI), SONG_SELECTED);

        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("audio/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        Intent finalIntent = Intent.createChooser(intent, "Select song");
        startActivityForResult(finalIntent, SONG_SELECTED);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case SONG_SELECTED: {
                try {
                    DataSourceHelper.setMediaPlayerDataSource(this, mp, data.getDataString());
                    mp.prepare();
                } catch (IOException e) {
                    Log.w(TAG, e.getMessage());
                    toast("Failed to load song.");
                }
            }
            break;
            default:
                break;
        }
    }

    public void play(View view) {
        //TODO: get state
        mp.start();
    }

    public void pause(View view) {
        if (mp.isPlaying())
            mp.pause();
        else mp.start();
    }
}
