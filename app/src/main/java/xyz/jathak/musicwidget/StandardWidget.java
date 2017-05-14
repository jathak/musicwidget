package xyz.jathak.musicwidget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.preference.PreferenceManager;
import android.text.Html;
import android.text.SpannableStringBuilder;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.RemoteViews;
import 	android.support.v7.graphics.Palette;

public class StandardWidget extends AppWidgetProvider {
	public static boolean enabled = false;
	public static String currentSong="",currentArtist="",currentAlbum="";
    public static boolean currentlyPlaying=false;
	private int currentAlbumID=0;
	//private int currentSongID=-23;
	public static Bitmap currentArt = null;
    public static Bitmap currentSquareArt = null;
    public static String WIDGET_UPDATE = "xyz.jathak.musicwidget.APPWIDGET_UPDATE";
    public static String WIDGET_ACTION = "xyz.jathak.musicwidget.WIDGET_ACTION";
    protected boolean isTall = false;
	@Override
	public void onDeleted(Context context, int[] appWidgetIds) {
		super.onDeleted(context, appWidgetIds);
	}

	@Override
	public void onDisabled(Context context) {
		super.onDisabled(context);
		enabled = false;
	}

	private static int screenWidth = 100;

	@Override
	public void onEnabled(Context context) {
		super.onEnabled(context);
		enabled = true;
        if(Build.VERSION.SDK_INT>=21) {
            context.stopService(new Intent(context, NotificationListener.class));
            context.startService(new Intent(context, NotificationListener.class));
        }else{
            context.stopService(new Intent(context, NotificationListenerKK.class));
            context.startService(new Intent(context, NotificationListenerKK.class));
        }
		ComponentName thisAppWidget = new ComponentName(context.getPackageName(), getClass().getName());
		AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
		int ids[] = appWidgetManager.getAppWidgetIds(thisAppWidget);
        for (int appWidgetID: ids) {
			updateAppWidget(context, appWidgetManager, appWidgetID);
		}
	}


	@Override
	public void onReceive(Context context, Intent intent) {

		super.onReceive(context, intent);
        ComponentName thisAppWidget = new ComponentName(context.getPackageName(), getClass().getName());
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        int ids[] = appWidgetManager.getAppWidgetIds(thisAppWidget);
        for (int appWidgetID: ids) {
            updateAppWidget(context, appWidgetManager, appWidgetID);
        }
	}
    static int[] lastABC = null;
    static Bitmap lastAlbum = null;
    static Bitmap lastTallAlbum = null;
    public final static String[] FONTS = {"sans-serif-light","normal","sans-serif-thin","sans-serif-condensed","serif","monospace"};
    static String lastTSong="",lastTArtist="",lastTAlbumname="";
    static String lastSong="",lastArtist="",lastAlbumname="";
    boolean isLightAlbum = false;
    static int darkLevel = 30;
    public void updateAppWidget(Context context, AppWidgetManager appWidgetManager,int appWidgetId) {
        SharedPreferences p = PreferenceManager.getDefaultSharedPreferences(context);
        int style = p.getInt("style", R.id.albummatch);
        int layout = R.layout.standard_widget;
        if(style== R.id.light)layout= R.layout.now_widget;
        if(style== R.id.transparent)layout= R.layout.light_widget;
        if(style== R.id.transparent2)layout= R.layout.trans_widget;
        if(style== R.id.albummatch)layout= R.layout.album_widget;
        if(isTall){
            layout= R.layout.standard_widget_tall;
        }
        String song = currentSong;
        String artist = currentArtist;
        String album = currentAlbum;
        if(p.getBoolean("songCaps",false))song = song.toUpperCase();
        if(p.getBoolean("artistCaps",false))artist = artist.toUpperCase();
        if(p.getBoolean("albumCaps",false))album = album.toUpperCase();
        String songString = "<font face='"+FONTS[p.getInt("songFont",0)]+"'>"+song+"</font>";
        String artistString = "<font face='"+FONTS[p.getInt("artistFont",0)]+"'>"+artist+"</font>";
        String albumString = "<font face='"+FONTS[p.getInt("albumFont",0)]+"'>"+album+"</font>";
        if(p.getBoolean("songBold",false))songString="<b>"+songString+"</b>";
        if(p.getBoolean("artistBold",false))artistString="<b>"+artistString+"</b>";
        if(p.getBoolean("albumBold",false))albumString="<b>"+albumString+"</b>";
        if(p.getBoolean("songItalic",false))songString="<i>"+songString+"</i>";
        if(p.getBoolean("artistItalic",false))artistString="<i>"+artistString+"</i>";
        if(p.getBoolean("albumItalic",false))albumString="<i>"+albumString+"</i>";

        RemoteViews remoteView = new RemoteViews(context.getPackageName(),layout);
        if(style != R.id.light) {
            remoteView.setInt(R.id.artist, "setTextColor", Color.WHITE);
            remoteView.setInt(R.id.album, "setTextColor", Color.WHITE);
            remoteView.setInt(R.id.song, "setTextColor", Color.WHITE);
            remoteView.setImageViewResource(R.id.play, R.drawable.ic_action_play);
            remoteView.setImageViewResource(R.id.next, R.drawable.ic_action_next);
            remoteView.setImageViewResource(R.id.prev, R.drawable.ic_action_previous);
        }
        if(!isTall&&currentArt==null){
            remoteView.setViewVisibility(R.id.albumArt, View.GONE);
        }else remoteView.setViewVisibility(R.id.albumArt, View.VISIBLE);
        if(style == R.id.albummatch&&!isTall){
            if((currentSquareArt!=null&&(lastAlbum==null||!currentSquareArt.equals(lastAlbum))
                    &&(!currentSong.equals(lastSong)||!currentArtist.equals(lastArtist)
                    ||!currentAlbum.equals(lastAlbumname)))||lastABC==null){
                lastABC = getAlbumBasedColors(currentSquareArt);
            }
            if(currentSquareArt!=null){
                lastAlbum = currentSquareArt;
                lastSong=currentSong;
                lastArtist=currentArtist;
                lastAlbumname=currentAlbum;
            }else{
                lastAlbum = null;
                lastSong="";
                lastArtist="";
                lastAlbumname="";
            }
            int color = lastABC[0];
            int textColor = lastABC[1];
            isLightAlbum = lastABC[2]==1;
            if(color==Color.TRANSPARENT)color=Color.rgb(34,34,34);
            remoteView.setInt(R.id.layoutwidget, "setBackgroundColor", color);
            remoteView.setInt(R.id.artist, "setTextColor", textColor);
            remoteView.setInt(R.id.album, "setTextColor", textColor);
            remoteView.setInt(R.id.song, "setTextColor", textColor);
            if(isLightAlbum){
                remoteView.setImageViewResource(R.id.play, R.drawable.ic_action_play_d);
                remoteView.setImageViewResource(R.id.next, R.drawable.ic_action_next_d);
                remoteView.setImageViewResource(R.id.prev, R.drawable.ic_action_previous_d);
            }
        }
        if(isTall){
            if((currentArt!=null&&(lastTallAlbum==null||!currentArt.equals(lastTallAlbum))
                &&(!currentSong.equals(lastTSong)||!currentArtist.equals(lastTArtist)
                    ||!currentAlbum.equals(lastTAlbumname)))||lastABC==null){
                if(currentArt == null){
                    int darkLevel = 30;
                }else{
                    double r = 0;
                    double g = 0;
                    double b = 0;
                    Bitmap source = Bitmap.createBitmap(currentArt,0,0,100,100);
                    for(int i=0;i<source.getWidth();i++){
                        for(int j=0;j<source.getHeight();j++){
                            int color = source.getPixel(i,j);
                            r += Color.red(color);
                            g += Color.green(color);
                            b += Color.blue(color);
                        }
                    }
                    r /= source.getWidth()*source.getHeight();
                    g /= source.getWidth()*source.getHeight();
                    b /= source.getWidth()*source.getHeight();
                    int coeff = (int) ((r * 299 + g * 587 + b * 144) / 1000);
                    if(coeff>100) darkLevel = 38;
                    if(coeff>130) darkLevel = 46;
                    if(coeff>160) darkLevel = 54;
                    if(coeff>190) darkLevel = 62;
                    if(coeff>220) darkLevel = 70;
                    if(coeff>250) darkLevel = 78;
                    System.out.println("Dark Level: "+darkLevel);
                    lastTallAlbum=currentArt;
                    lastTSong=currentSong;
                    lastTArtist=currentArtist;
                    lastTAlbumname=currentAlbum;
                }
            }
            remoteView.setInt(R.id.tallback, "setBackgroundColor",Color.argb(darkLevel,0,0,0));
            remoteView.setInt(R.id.artist, "setTextColor", Color.WHITE);
            remoteView.setInt(R.id.album, "setTextColor", Color.WHITE);
            remoteView.setInt(R.id.song, "setTextColor", Color.WHITE);
        }
        if(currentSong.equals("")){
            remoteView.setViewVisibility(R.id.song,View.GONE);
        }else remoteView.setViewVisibility(R.id.song,View.VISIBLE);
        if(currentArtist.equals("")){
            remoteView.setViewVisibility(R.id.artist,View.GONE);
        }else remoteView.setViewVisibility(R.id.artist,View.VISIBLE);
        if(!p.getBoolean("showAlbum",false)||currentAlbum.equals("")){
            remoteView.setViewVisibility(R.id.album, View.GONE);
        }else remoteView.setViewVisibility(R.id.album,View.VISIBLE);
        remoteView.setTextViewText(R.id.artist, new SpannableStringBuilder
                (Html.fromHtml(artistString)));
        remoteView.setTextViewTextSize(R.id.artist, TypedValue.COMPLEX_UNIT_SP,p.getInt("artistSize",2)*2+10);
        remoteView.setTextViewText(R.id.album, new SpannableStringBuilder
                (Html.fromHtml(albumString)));
        remoteView.setTextViewTextSize(R.id.album, TypedValue.COMPLEX_UNIT_SP,p.getInt("albumSize",2)*2+10);

        remoteView.setTextViewTextSize(R.id.song, TypedValue.COMPLEX_UNIT_SP,p.getInt("songSize",4)*2+10);
        remoteView.setTextViewText(R.id.song, new SpannableStringBuilder
                (Html.fromHtml(songString)));
		if(currentArt!=null&&isTall){
            remoteView.setImageViewBitmap(R.id.albumArt, currentArt);
        }else if(currentSquareArt!=null&&!isTall){
            remoteView.setImageViewBitmap(R.id.albumArt, currentSquareArt);
        }
		else remoteView.setImageViewUri(R.id.albumArt, Uri.parse(""));
		Intent playIntent = new Intent(WIDGET_ACTION);
		playIntent.putExtra("type",0);
		PendingIntent pendingIntent3 = PendingIntent.getBroadcast(context, 1, playIntent, 0);
        if(((style== R.id.light||isLightAlbum)&&!isTall)){
		    if(currentlyPlaying){
			    remoteView.setImageViewResource(R.id.play, R.drawable.ic_action_pause_d);
		    }else remoteView.setImageViewResource(R.id.play, R.drawable.ic_action_play_d);
        }else{
            if(currentlyPlaying){
                remoteView.setImageViewResource(R.id.play, R.drawable.ic_action_pause);
            }else remoteView.setImageViewResource(R.id.play, R.drawable.ic_action_play);
        }
        if(Build.VERSION.SDK_INT>=21) {
            if (!NotificationListener.online)
                pendingIntent3 = PendingIntent.getService(context, 1, new Intent(context, NotificationListener.class), 0);
            remoteView.setOnClickPendingIntent(R.id.play, pendingIntent3);
            Intent nextIntent = new Intent(WIDGET_ACTION);
            nextIntent.putExtra("type", 1);
            PendingIntent pendingIntent2 = PendingIntent.getBroadcast(context, 2, nextIntent, 0);
            if (!NotificationListener.online)
                pendingIntent2 = PendingIntent.getService(context, 2, new Intent(context, NotificationListener.class), 0);
            Intent prevIntent = new Intent(WIDGET_ACTION);
            prevIntent.putExtra("type", 2);
            PendingIntent pendingIntent5 = PendingIntent.getBroadcast(context, 3, prevIntent, 0);
            if (!NotificationListener.online)
                pendingIntent5 = PendingIntent.getService(context, 3, new Intent(context, NotificationListener.class), 0);
            remoteView.setOnClickPendingIntent(R.id.next, pendingIntent2);
            remoteView.setOnClickPendingIntent(R.id.prev, pendingIntent5);
        }else{
            if (!NotificationListener.online)
                pendingIntent3 = PendingIntent.getService(context, 1, new Intent(context, NotificationListenerKK.class), 0);
            remoteView.setOnClickPendingIntent(R.id.play, pendingIntent3);
            Intent nextIntent = new Intent(WIDGET_ACTION);
            nextIntent.putExtra("type", 1);
            PendingIntent pendingIntent2 = PendingIntent.getBroadcast(context, 2, nextIntent, 0);
            if (!NotificationListener.online)
                pendingIntent2 = PendingIntent.getService(context, 2, new Intent(context, NotificationListenerKK.class), 0);
            Intent prevIntent = new Intent(WIDGET_ACTION);
            prevIntent.putExtra("type", 2);
            PendingIntent pendingIntent5 = PendingIntent.getBroadcast(context, 3, prevIntent, 0);
            if (!NotificationListener.online)
                pendingIntent5 = PendingIntent.getService(context, 3, new Intent(context, NotificationListenerKK.class), 0);
            remoteView.setOnClickPendingIntent(R.id.next, pendingIntent2);
            remoteView.setOnClickPendingIntent(R.id.prev, pendingIntent5);
        }

        Intent appIntent = new Intent(WIDGET_ACTION);
        appIntent.putExtra("type",3);
        PendingIntent pendingIntentA = PendingIntent.getBroadcast(context, 4,appIntent , 0);
        PendingIntent pendingIntentA2 = PendingIntent.getBroadcast(context, 5,appIntent , 0);
        PendingIntent pendingIntentA3 = PendingIntent.getBroadcast(context, 6,appIntent , 0);
        PendingIntent pendingIntentA4 = PendingIntent.getBroadcast(context, 7,appIntent , 0);
        PendingIntent pendingIntentA5 = PendingIntent.getBroadcast(context, 8,appIntent , 0);
        remoteView.setOnClickPendingIntent(R.id.albumArt,pendingIntentA);
        remoteView.setOnClickPendingIntent(R.id.layoutwidget,pendingIntentA2);
        remoteView.setOnClickPendingIntent(R.id.song,pendingIntentA3);
        remoteView.setOnClickPendingIntent(R.id.artist,pendingIntentA4);
        remoteView.setOnClickPendingIntent(R.id.album,pendingIntentA5);
		appWidgetManager.updateAppWidget(appWidgetId, remoteView);
	}

	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] ids) {
        if(Build.VERSION.SDK_INT>=21) {
            context.stopService(new Intent(context, NotificationListener.class));
            context.startService(new Intent(context, NotificationListener.class));
        }else{
            context.stopService(new Intent(context, NotificationListenerKK.class));
            context.startService(new Intent(context, NotificationListenerKK.class));
        }
			for (int appWidgetID: ids) {
				updateAppWidget(context, appWidgetManager, appWidgetID);
			}
	}


    public static int[] getAlbumBasedColors(Bitmap art){
        if(art==null)return new int[]{Color.TRANSPARENT,Color.WHITE,0};
        double rAve = 0;
        double gAve = 0;
        double bAve = 0;
        double rSD = 0;
        double gSD = 0;
        double bSD = 0;
        int width = art.getWidth();
        int height = art.getHeight();
        int[] colors = new int[height];
        int target = width-1;
        if(target<0) return getPaletteColors(art);
        for(int i=0; i<height; i++){
            int color = art.getPixel(target,i);
            colors[i] = color;
            rAve += Color.red(color);
            gAve += Color.green(color);
            bAve += Color.blue(color);
        }
        rAve /= height;
        gAve /= height;
        bAve /= height;
        for(int i=0; i<height;i++){
            rSD += Math.pow(Math.abs(rAve-Color.red(colors[i])),2);
            gSD += Math.pow(Math.abs(gAve-Color.green(colors[i])),2);
            bSD += Math.pow(Math.abs(bAve-Color.blue(colors[i])),2);
        }
        rSD = Math.sqrt(rSD/height);
        gSD = Math.sqrt(gSD/height);
        bSD = Math.sqrt(bSD/height);
        Log.d("ALBUMART", "SD: " + ((int)rSD) + " " + ((int)gSD) + " " + ((int)bSD) +"  "+currentAlbum);
        int ri = (int)Math.round(rAve);
        int gi = (int)Math.round(gAve);
        int bi = (int)Math.round(bAve);
        Log.d("ALBUMART", "Av: "+ri+" "+gi+" "+bi+"  "+currentAlbum);
        if(rSD<30&&gSD<30&&bSD<30&&rSD+gSD+bSD<55){
            Log.d("ALBUMART", "Using edge colors");
            int realColor = Color.rgb(ri,gi,bi);
            int textColor = Color.WHITE;
            int isLight = 0;
            //((Red value X 299) + (Green value X 587) + (Blue value X 114)) / 1000
            //white is 263, black is 0
            double coeff = (rAve*299+gAve*587+bAve*144)/1000;
            boolean testA = coeff>=137;
            boolean testB = ri+gi+bi>420;
            if(testA&&testB){
                textColor = Color.BLACK;
                isLight = 1;
            }else if(testA||testB){
                double r = 0;
                double g = 0;
                double b = 0;
                Bitmap source = Bitmap.createBitmap(currentArt,0,0,100,100);
                for(int i=0;i<source.getWidth();i++){
                    for(int j=0;j<source.getHeight();j++){
                        int color = source.getPixel(i,j);
                        r += Color.red(color);
                        g += Color.green(color);
                        b += Color.blue(color);
                    }
                }
                r /= (double)source.getWidth()*source.getHeight();
                g /= (double)source.getWidth()*source.getHeight();
                b /= (double)source.getWidth()*source.getHeight();
                double fullCoeff = ((r * 299 + g * 587 + b * 144) / 1000);
                Log.d("ALBUMART","Coeffs "+coeff+" "+fullCoeff);
                Log.d("ALBUMART","RGB "+(r+g+b)+" "+(rAve+gAve+bAve));
                if(coeff*5+r+g+b<fullCoeff*5+rAve+gAve+bAve){
                    textColor = Color.BLACK;
                    isLight = 1;
                }
            }
            return new int[]{realColor,textColor,isLight};
        }else return getPaletteColors(art);
    }

    public static int[] getPaletteColors(Bitmap art){
        int color = Color.TRANSPARENT;
        int textColor = Color.WHITE;
        int isLight = 0;
        if(art != null){
            Palette lastPalette = Palette.generate(currentArt);
            color = lastPalette.getDarkVibrantColor(color);
            if(color==Color.TRANSPARENT){
                color = lastPalette.getLightVibrantColor(color);
                if(color!=Color.TRANSPARENT){
                    textColor = Color.BLACK;
                    isLight = 1;
                }else{
                    color = lastPalette.getLightMutedColor(color);
                    if(color!=Color.TRANSPARENT){
                        textColor = Color.BLACK;
                        isLight = 1;
                    }else{
                        color = lastPalette.getDarkMutedColor(color);
                        isLight = 0;
                    }
                }
            }
        }
        return new int[]{color,textColor,isLight};
    }

}