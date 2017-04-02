package org.narwastu.gongkebyarnarwastu;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

public class JublagActivity extends AppCompatActivity implements View.OnTouchListener {

    private final static int DING = 1;
    private final static int DONG = 2;
    private final static int DENG = 3;
    private final static int DUNG = 4;
    private final static int DANG = 5;
    private int dingSoundId = -1;
    private int dongSoundId = -1;
    private int dengSoundId = -1;
    private int dungSoundId = -1;
    private int dangSoundId = -1;
    private int dingStreamId = -1;
    private int dongStreamId = -1;
    private int dengStreamId = -1;
    private int dungStreamId = -1;
    private int dangStreamId = -1;


    //batubulan setup
    /*
    private float dongRelativePitch = 1.19f;
    private float dengRelativePitch = 1.34f;
    private float dungRelativePitch = 1.6f;
    private float dangRelativePitch = 1.74f;
*/

    //ubud kaler setup
    private float dongRelativePitch = 1.04f;
    private float dengRelativePitch = 1.21f;
    private float dungRelativePitch = 1.52f;
    private float dangRelativePitch = 1.6f;


    private SoundPool sp;
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

        sp = new SoundPool(10, AudioManager.STREAM_MUSIC, 0);
        sp.setOnLoadCompleteListener(new SoundPool.OnLoadCompleteListener() {
            @Override
            public void onLoadComplete(SoundPool soundPool, int sampleId, int status) {
                soundsLoaded = true;
            }

        });
        //all the same but played at different pitch, because using the actual same id to play notes at different pitch seems buggy

        dingSoundId = sp.load(this, R.raw.ding, 1);
        dongSoundId = sp.load(this, R.raw.ding, 1);
        dengSoundId = sp.load(this, R.raw.ding, 1);
        dungSoundId = sp.load(this, R.raw.ding, 1);
        dangSoundId = sp.load(this, R.raw.ding, 1);

        toast("Touch a note to play, or touch in the lower part to stop a note.");
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
                    playNote(DING);
                else if (closeMatch(Color.rgb(0, 0, 204), touchColor, tolerance))
                    stopNote(DING);
                else if (closeMatch(Color.rgb(255, 0, 255), touchColor, tolerance))
                    playNote(DONG);
                else if (closeMatch(Color.rgb(204, 0, 204), touchColor, tolerance))
                    stopNote(DONG);
                else if (closeMatch(Color.rgb(255, 0, 0), touchColor, tolerance))
                    playNote(DENG);
                else if (closeMatch(Color.rgb(204, 0, 0), touchColor, tolerance))
                    stopNote(DENG);
                else if (closeMatch(Color.rgb(0, 255, 0), touchColor, tolerance))
                    playNote(DUNG);
                else if (closeMatch(Color.rgb(0, 204, 0), touchColor, tolerance))
                    stopNote(DUNG);
                else if (closeMatch(Color.rgb(0, 255, 255), touchColor, tolerance))
                    playNote(DANG);
                else if (closeMatch(Color.rgb(0, 204, 204), touchColor, tolerance))
                    stopNote(DANG);


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

    private void playNote(int note) {

        //  TODO: if already playing, stop and play again
        switch (note) {
            case DING:
                sp.stop(dingStreamId);
                dingStreamId = sp.play(dingSoundId, volume, volume, 1, 0, 1f);
                Log.d("JublagActivity", String.format("Played ding, stream id %d", dingStreamId));
                break;
            case DONG:
                sp.stop(dongStreamId);
                dongStreamId = sp.play(dongSoundId, volume, volume, 1, 0, dongRelativePitch);
                break;
            case DENG:
                sp.stop(dengStreamId);
                dengStreamId = sp.play(dengSoundId, volume, volume, 1, 0, dengRelativePitch);
                break;
            case DUNG:
                sp.stop(dungStreamId);
                dungStreamId = sp.play(dungSoundId, volume, volume, 1, 0, dungRelativePitch);
                break;
            case DANG:
                sp.stop(dangStreamId);
                dangStreamId = sp.play(dangSoundId, volume, volume, 1, 0, dangRelativePitch);
                break;

            default:
                break;
        }
    }

    private void stopNote(int note) {
        //// TODO: 31/03/2017 find in map of notes being played, stop it
        switch (note) {
            case DING:
                sp.stop(dingStreamId);
                break;
            case DONG:
                sp.stop(dongStreamId);
                break;
            case DENG:
                sp.stop(dengStreamId);
                break;
            case DUNG:
                sp.stop(dungStreamId);
                break;
            case DANG:
                sp.stop(dangStreamId);
                break;
            default:
                break;
        }
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

}
