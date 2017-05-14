package xyz.jathak.musicwidget;

import android.content.*;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.media.*;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.widget.Toast;

public class NotificationListenerKK extends NotificationListenerService implements RemoteController.OnClientUpdateListener{
    private RemoteController remoteController;
    private AudioManager audioManager;
    @Override
    public void onCreate(){
        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        remoteController=new RemoteController(this,this);
        DisplayMetrics m = getResources().getDisplayMetrics();
        remoteController.setArtworkConfiguration(m.heightPixels,m.heightPixels);
        NotificationListener.online = audioManager.registerRemoteController(remoteController);
        if(!NotificationListener.online){
            Toast.makeText(this,"Enable notification access and then press any widget button to refresh.",Toast.LENGTH_SHORT).show();
        }
        sendBroadcast(new Intent(StandardWidget.WIDGET_UPDATE));
        registerReceiver(button,new IntentFilter(StandardWidget.WIDGET_ACTION));
    }

    @Override
    public int onStartCommand(Intent i, int startId, int i2){
        if(!NotificationListener.online){
            NotificationListener.online = audioManager.registerRemoteController(remoteController);
            if(!NotificationListener.online){
                Toast.makeText(this,"Enable notification access and then press any widget button to refresh.",Toast.LENGTH_SHORT).show();
            }
            sendBroadcast(new Intent(StandardWidget.WIDGET_UPDATE));
        }
        return START_STICKY;
    }

    @Override
    public void onNotificationPosted(StatusBarNotification statusBarNotification) {

    }

    @Override
    public void onNotificationRemoved(StatusBarNotification statusBarNotification) {

    }
    @Override
    public void onDestroy(){
        unregisterReceiver(button);
        NotificationListener.online=false;
    }

    public IBinder onBind(Intent intent) {
        return null;
    }
    private RemoteController.MetadataEditor meta;
    @Override
    public void onClientChange(boolean b) {
        if(meta!=null){
            meta.clear();
        }
    }

    @Override
    public void onClientPlaybackStateUpdate(int i) {
        StandardWidget.currentlyPlaying = i == RemoteControlClient.PLAYSTATE_PLAYING;
        sendBroadcast(new Intent(StandardWidget.WIDGET_UPDATE));
    }

    @Override
    public void onClientPlaybackStateUpdate(int i, long l, long l2, float v) {
        StandardWidget.currentlyPlaying = i == RemoteControlClient.PLAYSTATE_PLAYING;
        sendBroadcast(new Intent(StandardWidget.WIDGET_UPDATE));
    }

    @Override
    public void onClientTransportControlUpdate(int i) {

    }

    @Override
    public void onClientMetadataUpdate(RemoteController.MetadataEditor metadataEditor) {
        meta=metadataEditor;
        StandardWidget.currentArt=metadataEditor.getBitmap(MediaMetadataEditor.BITMAP_KEY_ARTWORK,null);
        StandardWidget.currentSquareArt = StandardWidget.currentArt;
        if(StandardWidget.currentArt!=null) {
            int cah = StandardWidget.currentArt.getHeight();
            int caw = StandardWidget.currentArt.getWidth();
            if (caw > cah * 1.02) {
                StandardWidget.currentSquareArt = Bitmap.createBitmap(StandardWidget.currentArt,
                        (int) (caw / 2 - cah * 0.51), 0, (int) (cah * 1.02), cah);
            }
        }
        StandardWidget.currentArtist=metadataEditor.getString(MediaMetadataRetriever.METADATA_KEY_ARTIST,"");
        StandardWidget.currentSong=metadataEditor.getString(MediaMetadataRetriever.METADATA_KEY_TITLE,"");
        StandardWidget.currentAlbum=metadataEditor.getString(MediaMetadataRetriever.METADATA_KEY_ALBUM,"");
        if(StandardWidget.currentArtist==null||StandardWidget.currentArtist.equals("")){
            StandardWidget.currentArtist = metadataEditor.getString(MediaMetadataRetriever.METADATA_KEY_ALBUMARTIST,"");
        }
        if(StandardWidget.currentArtist==null||StandardWidget.currentArtist.equals("")){
            StandardWidget.currentArtist = metadataEditor.getString(MediaMetadataRetriever.METADATA_KEY_AUTHOR,"");
        }
        sendBroadcast(new Intent(StandardWidget.WIDGET_UPDATE));
    }

    BroadcastReceiver button = new BroadcastReceiver(){
        @Override
        public void onReceive(Context context, Intent intent) {
            //Play is 0, next is 1, previous is 2
            int action = intent.getIntExtra("type",-1);
            if(action==0){
                remoteController.sendMediaKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN,KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE));
                remoteController.sendMediaKeyEvent(new KeyEvent(KeyEvent.ACTION_UP,KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE));
            }else if(action ==1){
                remoteController.sendMediaKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN,KeyEvent.KEYCODE_MEDIA_NEXT));
                remoteController.sendMediaKeyEvent(new KeyEvent(KeyEvent.ACTION_UP,KeyEvent.KEYCODE_MEDIA_NEXT));
            }else if (action==2){
                remoteController.sendMediaKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN,KeyEvent.KEYCODE_MEDIA_PREVIOUS));
                remoteController.sendMediaKeyEvent(new KeyEvent(KeyEvent.ACTION_UP,KeyEvent.KEYCODE_MEDIA_PREVIOUS));
            }else if (action==3){
                SharedPreferences p = PreferenceManager.getDefaultSharedPreferences(context);
                PackageManager m = context.getPackageManager();
                String pack = p.getString("appLaunch","");
                if(!pack.equals("")){
                    startActivity(m.getLaunchIntentForPackage(pack));
                }
            }
        }
    };
}
