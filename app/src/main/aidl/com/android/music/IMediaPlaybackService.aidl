// IMediaPlaybackService.aidl
package com.android.music;

import android.graphics.Bitmap;

// Declare any non-default types here with import statements

interface IMediaPlaybackService {
    /**
     * Demonstrates some basic types that you can use as parameters
     * and return values in AIDL.
     */
     void openFile(String path);
     void open(in long [] list, int position);
     int getQueuePosition();
     boolean isPlaying();
     void stop();
     void pause();
     void play();
     void prev();
     void next();
     long duration();
     long position();
     long seek(long pos);
     String getTrackName();
     String getAlbumName();
     long getAlbumId();
     String getArtistName();
     long getArtistId();
     void enqueue(in long [] list, int action);
     long [] getQueue();
     void moveQueueItem(int from, int to);
     void setQueuePosition(int index);
     String getPath();
     long getAudioId();
     void setShuffleMode(int shufflemode);
     int getShuffleMode();
     int removeTracks(int first, int last);
     int removeTrack(long id);
     void setRepeatMode(int repeatmode);
     int getRepeatMode();
     int getMediaMountedCount();
     int getAudioSessionId();
}
