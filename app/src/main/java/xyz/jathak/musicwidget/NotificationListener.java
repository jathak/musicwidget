package xyz.jathak.musicwidget;

import android.annotation.TargetApi;
import android.content.*;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.media.*;
import android.media.session.MediaController;
import android.media.session.MediaSession;
import android.media.session.MediaSessionManager;
import android.media.session.PlaybackState;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.view.KeyEvent;

import java.util.List;

@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class NotificationListener extends NotificationListenerService{
    private MediaSessionManager mediaSessionManager;
    private MediaController mediaController;
    public static boolean online;
    private ComponentName componentName = new ComponentName("xyz.jathak.musicwidget","xyz.jathak.musicwidget.NotificationListener");
    @Override
    public void onCreate(){
        registerReceiver(button,new IntentFilter(StandardWidget.WIDGET_ACTION));
        mediaSessionManager = (MediaSessionManager) getSystemService(Context.MEDIA_SESSION_SERVICE);
        mediaSessionManager.addOnActiveSessionsChangedListener(sessionListener, componentName);
        List<MediaController> controllers = mediaSessionManager.getActiveSessions(componentName);
        mediaController = pickController(controllers);
        if(mediaController!=null) {
            mediaController.registerCallback(callback);
            meta = mediaController.getMetadata();
            updateMetadata();
        }
        online = true;
        sendBroadcast(new Intent(StandardWidget.WIDGET_UPDATE));
    }

    @Override
    public int onStartCommand(Intent i, int startId, int i2){
        if(mediaController==null){
            List<MediaController> controllers = mediaSessionManager.getActiveSessions(componentName);
            mediaController = pickController(controllers);
            if(mediaController!=null) {
                mediaController.registerCallback(callback);
                meta = mediaController.getMetadata();
                updateMetadata();
            }
            sendBroadcast(new Intent(StandardWidget.WIDGET_UPDATE));
        }
        return START_STICKY;
    }

    MediaController.Callback callback = new MediaController.Callback() {
        @Override
        public void onSessionDestroyed() {
            super.onSessionDestroyed();
            mediaController = null;
            meta = null;
        }

        @Override
        public void onSessionEvent(String event, Bundle extras) {
            super.onSessionEvent(event, extras);
            updateMetadata();
            sendBroadcast(new Intent(StandardWidget.WIDGET_UPDATE));
        }

        @Override
        public void onPlaybackStateChanged(PlaybackState state) {
            super.onPlaybackStateChanged(state);
            StandardWidget.currentlyPlaying = state.getState() == PlaybackState.STATE_PLAYING;
            updateMetadata();
            sendBroadcast(new Intent(StandardWidget.WIDGET_UPDATE));
        }

        @Override
        public void onMetadataChanged(MediaMetadata metadata) {
            super.onMetadataChanged(metadata);
            meta = metadata;
            updateMetadata();
            sendBroadcast(new Intent(StandardWidget.WIDGET_UPDATE));
        }

        @Override
        public void onQueueChanged(List<MediaSession.QueueItem> queue) {
            super.onQueueChanged(queue);
        }

        @Override
        public void onQueueTitleChanged(CharSequence title) {
            super.onQueueTitleChanged(title);
        }

        @Override
        public void onExtrasChanged(Bundle extras) {
            super.onExtrasChanged(extras);
        }

        @Override
        public void onAudioInfoChanged(MediaController.PlaybackInfo info) {
            super.onAudioInfoChanged(info);
        }
    };

    @Override
    public void onNotificationPosted(StatusBarNotification statusBarNotification) {

    }

    @Override
    public void onNotificationRemoved(StatusBarNotification statusBarNotification) {

    }
    @Override
    public void onDestroy(){
        unregisterReceiver(button);
        mediaController = null;
        online = false;
        mediaSessionManager.removeOnActiveSessionsChangedListener(sessionListener);
    }


    public void updateMetadata(){
        if(mediaController!=null&&mediaController.getPlaybackState()!=null){
            StandardWidget.currentlyPlaying = mediaController.getPlaybackState().getState() == PlaybackState.STATE_PLAYING;
        }
        if(meta==null)return;
        StandardWidget.currentArt=meta.getBitmap(MediaMetadata.METADATA_KEY_ART);
        if(StandardWidget.currentArt==null){
            StandardWidget.currentArt = meta.getBitmap(MediaMetadata.METADATA_KEY_ALBUM_ART);
        }
        if(StandardWidget.currentArt==null){
            StandardWidget.currentArt = meta.getBitmap(MediaMetadata.METADATA_KEY_DISPLAY_ICON);
        }
        StandardWidget.currentSquareArt = StandardWidget.currentArt;
        if(StandardWidget.currentArt!=null) {
            int cah = StandardWidget.currentArt.getHeight();
            int caw = StandardWidget.currentArt.getWidth();
            if (caw > cah * 1.02) {
                StandardWidget.currentSquareArt = Bitmap.createBitmap(StandardWidget.currentArt,
                        (int) (caw / 2 - cah * 0.51), 0, (int) (cah * 1.02), cah);
            }
        }
        StandardWidget.currentArtist=meta.getString(MediaMetadata.METADATA_KEY_ARTIST);
        StandardWidget.currentSong=meta.getString(MediaMetadata.METADATA_KEY_TITLE);
        if(StandardWidget.currentSong==null){
            StandardWidget.currentSong=meta.getString(MediaMetadata.METADATA_KEY_DISPLAY_TITLE);
        }
        StandardWidget.currentAlbum=meta.getString(MediaMetadata.METADATA_KEY_ALBUM);
        if(StandardWidget.currentArtist==null){
            StandardWidget.currentArtist = meta.getString(MediaMetadata.METADATA_KEY_ALBUM_ARTIST);
        }
        if(StandardWidget.currentArtist==null) {
            StandardWidget.currentArtist = meta.getString(MediaMetadata.METADATA_KEY_AUTHOR);
        }
        if(StandardWidget.currentArtist==null) {
            StandardWidget.currentArtist = meta.getString(MediaMetadata.METADATA_KEY_DISPLAY_SUBTITLE);
        }
        if(StandardWidget.currentArtist==null) {
            StandardWidget.currentArtist = meta.getString(MediaMetadata.METADATA_KEY_WRITER);
        }
        if(StandardWidget.currentArtist==null) {
            StandardWidget.currentArtist = meta.getString(MediaMetadata.METADATA_KEY_COMPOSER);
        }
        if(StandardWidget.currentArtist==null)StandardWidget.currentArtist = "";
        if(StandardWidget.currentSong==null)StandardWidget.currentSong = "";
        if(StandardWidget.currentAlbum==null)StandardWidget.currentAlbum = "";
        sendBroadcast(new Intent(StandardWidget.WIDGET_UPDATE));
    }

    private MediaController pickController(List<MediaController> controllers){
        for(int i=0;i<controllers.size();i++){
            MediaController mc = controllers.get(i);
            if(mc!=null&&mc.getPlaybackState()!=null&&
                    mc.getPlaybackState().getState()==PlaybackState.STATE_PLAYING){
                return mc;
            }
        }
        if(controllers.size()>0) return controllers.get(0);
        return null;
    }

    MediaSessionManager.OnActiveSessionsChangedListener sessionListener = new MediaSessionManager.OnActiveSessionsChangedListener() {
        @Override
        public void onActiveSessionsChanged(List<MediaController> controllers) {
            mediaController = pickController(controllers);
            if(mediaController==null)return;
            mediaController.registerCallback(callback);
            meta = mediaController.getMetadata();
            updateMetadata();
            sendBroadcast(new Intent(StandardWidget.WIDGET_UPDATE));
        }
    };

    public IBinder onBind(Intent intent) {
        return null;
    }
    private MediaMetadata meta;

    /*
    public void onClientChange(boolean b) {
        if(meta!=null){
            meta.clear();
        }
    }
    public void onClientPlaybackStateUpdate(int i) {
        if(i== RemoteControlClient.PLAYSTATE_PLAYING) StandardWidget.currentlyPlaying=true;
        else StandardWidget.currentlyPlaying=false;
        sendBroadcast(new Intent(StandardWidget.WIDGET_UPDATE));
    }
    public void onClientPlaybackStateUpdate(int i, long l, long l2, float v) {

        if(i== RemoteControlClient.PLAYSTATE_PLAYING) StandardWidget.currentlyPlaying=true;
        else StandardWidget.currentlyPlaying=false;
        sendBroadcast(new Intent(StandardWidget.WIDGET_UPDATE));
    }
    public void onClientTransportControlUpdate(int i) {

    }
    public void onClientMetadataUpdate(RemoteController.MetadataEditor metadataEditor) {
        meta=metadataEditor;
        StandardWidget.currentArt=metadataEditor.getBitmap(MediaMetadataEditor.BITMAP_KEY_ARTWORK,null);
        StandardWidget.currentArtist=metadataEditor.getString(MediaMetadataRetriever.METADATA_KEY_ARTIST,"");
        StandardWidget.currentSong=metadataEditor.getString(MediaMetadataRetriever.METADATA_KEY_TITLE,"");
        StandardWidget.currentAlbum=metadataEditor.getString(MediaMetadataRetriever.METADATA_KEY_ALBUM,"");
        if(StandardWidget.currentArtist==null|| StandardWidget.currentArtist==""){
            StandardWidget.currentArtist = metadataEditor.getString(MediaMetadataRetriever.METADATA_KEY_ALBUMARTIST,"");
        }
        if(StandardWidget.currentArtist==null|| StandardWidget.currentArtist==""){
            StandardWidget.currentArtist = metadataEditor.getString(MediaMetadataRetriever.METADATA_KEY_AUTHOR,"");
        }
        sendBroadcast(new Intent(StandardWidget.WIDGET_UPDATE));
    }*/

    BroadcastReceiver button = new BroadcastReceiver(){
        @Override
        public void onReceive(Context context, Intent intent) {
            //Play is 0, next is 1, previous is 2
            int action = intent.getIntExtra("type",-1);
            if(mediaController!=null&&action==0){
                mediaController.dispatchMediaButtonEvent(new KeyEvent(KeyEvent.ACTION_DOWN,KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE));
                mediaController.dispatchMediaButtonEvent(new KeyEvent(KeyEvent.ACTION_UP,KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE));
            }else if(mediaController!=null&&action ==1){
                mediaController.dispatchMediaButtonEvent(new KeyEvent(KeyEvent.ACTION_DOWN,KeyEvent.KEYCODE_MEDIA_NEXT));
                mediaController.dispatchMediaButtonEvent(new KeyEvent(KeyEvent.ACTION_UP,KeyEvent.KEYCODE_MEDIA_NEXT));
            }else if (mediaController!=null&&action==2){
                mediaController.dispatchMediaButtonEvent(new KeyEvent(KeyEvent.ACTION_DOWN,KeyEvent.KEYCODE_MEDIA_PREVIOUS));
                mediaController.dispatchMediaButtonEvent(new KeyEvent(KeyEvent.ACTION_UP,KeyEvent.KEYCODE_MEDIA_PREVIOUS));
            }else if (action==3){
                PackageManager m = context.getPackageManager();
                if(mediaController==null) {
                    SharedPreferences p = PreferenceManager.getDefaultSharedPreferences(context);
                    String pack = p.getString("appLaunch", "");
                    if (!pack.equals("")) {
                        startActivity(m.getLaunchIntentForPackage(pack));
                    }
                }else{
                    startActivity(m.getLaunchIntentForPackage(mediaController.getPackageName()));
                }
            }
        }
    };
}
