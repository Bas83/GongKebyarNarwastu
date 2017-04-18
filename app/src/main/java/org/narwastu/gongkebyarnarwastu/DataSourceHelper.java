package org.narwastu.gongkebyarnarwastu;

import android.content.Context;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class DataSourceHelper {

    private static String TAG = "DataSourceHelper";

    public static void setMediaPlayerDataSource(Context context,
                                                MediaPlayer mp, String fileInfo) throws IOException {

        String actualFileInfo = fileInfo;
        if (fileInfo.startsWith("content://")) {
            try {
                Uri uri = Uri.parse(fileInfo);
                actualFileInfo = getMediaPathFromContentUri(context, uri);
            } catch (Exception e) {
                Log.d(TAG, e.getMessage());
            }
        }

        try {
            setMediaPlayerDataSourcePostHoneyComb(context, mp, actualFileInfo);
        } catch (Exception e) {
            Log.d(TAG, "Failed to simply set datasource, trying again using file descriptor. Message: " + e.getMessage());
            try {
                setMediaPlayerDataSourceUsingFileDescriptor(mp, actualFileInfo);
            } catch (Exception ee) {
                Log.d(TAG, "Failed to set datasource using file descriptor, trying using media uri from path. Message: " + ee.getMessage());
                String uri = getMediaUriFromPath(context, actualFileInfo);
                mp.reset();
                mp.setDataSource(uri);
            }
        }
    }

    private static void setMediaPlayerDataSourcePostHoneyComb(Context context,
                                                              MediaPlayer mp, String fileInfo) throws IOException {
        mp.reset();
        //This code used to have Uri.encode around fileInfo, which replaced %20 with spaces in the fileInfo, after which
        //setDataSource failed. I removed it but I don't know why it was there in the first place due to lack of test cases.
        mp.setDataSource(context, Uri.parse(fileInfo));
    }

    private static void setMediaPlayerDataSourceUsingFileDescriptor(
            MediaPlayer mp, String fileInfo) throws IOException {
        File file = new File(fileInfo);
        FileInputStream inputStream = new FileInputStream(file);
        mp.reset();
        mp.setDataSource(inputStream.getFD());
        inputStream.close();
    }

    private static String getMediaUriFromPath(Context context, String path) {
        Uri mediaUri = MediaStore.Audio.Media.getContentUriForPath(path);
        Cursor mediaCursor = context.getContentResolver().query(
                mediaUri, null,
                MediaStore.Audio.Media.DATA + "='" + path + "'", null, null);
        mediaCursor.moveToFirst();

        long id = mediaCursor.getLong(mediaCursor
                .getColumnIndex(MediaStore.Audio.Media._ID));
        mediaCursor.close();

        if (!mediaUri.toString().endsWith(String.valueOf(id))) {
            return mediaUri + "/" + id;
        }
        return mediaUri.toString();
    }

    private static String getMediaPathFromContentUri(Context context,
                                                     Uri contentUri) {
        String[] proj = {MediaStore.Audio.Media.DATA};
        Cursor mediaCursor = context.getContentResolver().query(contentUri,
                proj, null, null, null);
        mediaCursor.moveToFirst();

        String path = mediaCursor.getString(mediaCursor
                .getColumnIndexOrThrow(MediaStore.Audio.Media.DATA));

        mediaCursor.close();
        return path;
    }
}
