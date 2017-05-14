package xyz.jathak.musicwidget;

import android.appwidget.AppWidgetManager;
import android.content.Context;

public class TallWidget extends StandardWidget {
    @Override
    public void updateAppWidget(Context context, AppWidgetManager appWidgetManager,int appWidgetId) {
        isTall=true;
        super.updateAppWidget(context,appWidgetManager,appWidgetId);
    }
}
