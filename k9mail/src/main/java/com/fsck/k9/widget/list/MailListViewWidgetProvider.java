package com.fsck.k9.widget.list;


import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.widget.RemoteViews;

import com.fsck.k9.R;
import com.fsck.k9.activity.MessageCompose;
import com.fsck.k9.activity.MessageList;


public class MailListViewWidgetProvider extends AppWidgetProvider {
    private static String ACTION_VIEW_MAIL_ITEM = "VIEW_MAIL_ITEM";
    private static String ACTION_COMPOSE_EMAIL = "COMPOSE_EMAIL";
    private static String ACTION_UPDATE_MESSAGE_LIST = "UPDATE_MESSAGE_LIST";


    public static void updateMailViewList(Context context) {
        Context appContext = context.getApplicationContext();
        AppWidgetManager widgetManager = AppWidgetManager.getInstance(appContext);
        ComponentName widget = new ComponentName(appContext, MailListViewWidgetProvider.class);
        int[] widgetIds = widgetManager.getAppWidgetIds(widget);

        Intent intent = new Intent(context, MailListViewWidgetProvider.class);
        intent.setAction(ACTION_UPDATE_MESSAGE_LIST);
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, widgetIds);
        context.sendBroadcast(intent);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }

    private void updateAppWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId) {
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.mail_list_view_widget_layout);

        views.setTextViewText(R.id.folder, context.getString(R.string.integrated_inbox_title));

        Intent intent = new Intent(context, MailListViewWidgetService.class);
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        intent.setData(Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME)));
        views.setRemoteAdapter(R.id.listView, intent);

        PendingIntent viewAction = viewActionTemplatePendingIntent(context);
        views.setPendingIntentTemplate(R.id.listView, viewAction);

        PendingIntent composeAction = composeActionPendingIntent(context);
        views.setOnClickPendingIntent(R.id.new_message, composeAction);

        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);

        String action = intent.getAction();
        if (action.equals(ACTION_UPDATE_MESSAGE_LIST)) {
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
            int[] appWidgetIds = intent.getIntArrayExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS);
            appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetIds, R.id.listView);
        } else if (action.equals(ACTION_VIEW_MAIL_ITEM)) {
            String messageUri = intent.getStringExtra(AppWidgetManager.EXTRA_CUSTOM_INFO);
            Intent viewMessageIntent = new Intent(context, MessageList.class);
            viewMessageIntent.setAction(Intent.ACTION_VIEW);
            viewMessageIntent.setData(Uri.parse(messageUri));
            startActivity(context, viewMessageIntent);
        } else if (action.equals(ACTION_COMPOSE_EMAIL)) {
            Intent composeIntent = new Intent(context, MessageCompose.class);
            composeIntent.setAction(MessageCompose.ACTION_COMPOSE);
            startActivity(context, composeIntent);
        }
    }

    private void startActivity(Context context, Intent intent) {
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    private PendingIntent viewActionTemplatePendingIntent(Context context) {
        Intent intent = new Intent(context, MailListViewWidgetProvider.class);
        intent.setAction(ACTION_VIEW_MAIL_ITEM);

        return PendingIntent.getBroadcast(context, MessageList.REQUEST_MASK_PENDING_INTENT, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private PendingIntent composeActionPendingIntent(Context context) {
        Intent intent = new Intent(context, MailListViewWidgetProvider.class);
        intent.setAction(ACTION_COMPOSE_EMAIL);

        return PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }
}
