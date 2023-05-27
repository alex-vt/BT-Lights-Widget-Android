package com.alexvt.btlightswidget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.RemoteViews
import com.alexvt.btlightswidget.WidgetClickReceiver.Companion.EXTRA_COLOR

/**
 * Widgets are initiated without clicks processing, or even the application running.
 * Application has to run to enable widget clicks processing.
 * Application can be started via an entry point on an event.
 * Events to start the applications are: device reboot, app update, widget resize/update.
 * Broadcasts receivers for these events are the entry points.
 */

class BootEventReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            Intent.ACTION_BOOT_COMPLETED -> setupWidgetClicks(context)
        }
    }
}

class AppUpdateEventReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            Intent.ACTION_MY_PACKAGE_REPLACED -> setupWidgetClicks(context)
        }
    }
}

class WidgetChangeEventsProvider : AppWidgetProvider() {

    override fun onAppWidgetOptionsChanged(
        context: Context, widgetManager: AppWidgetManager, appWidgetId: Int, newOptions: Bundle
    ) {
        setupWidgetClicks(context)
        super.onAppWidgetOptionsChanged(context, widgetManager, appWidgetId, newOptions)
    }

    override fun onUpdate(
        context: Context, widgetManager: AppWidgetManager, widgetIds: IntArray
    ) {
        setupWidgetClicks(context)
        super.onUpdate(context, widgetManager, widgetIds)
    }
}

private fun setupWidgetClicks(context: Context) {
    AppWidgetManager.getInstance(context).getAppWidgetIds(
        ComponentName(context.packageName, WidgetChangeEventsProvider::class.java.name)
    ).forEach { widgetId ->
        RemoteViews(context.packageName, R.layout.widget_layout).apply {
            setViewVisibility(R.id.loading, View.GONE)
            setupClickForColor(context, R.id.selection1, R.color.selection1of6)
            setupClickForColor(context, R.id.selection2, R.color.selection2of6)
            setupClickForColor(context, R.id.selection3, R.color.selection3of6)
            setupClickForColor(context, R.id.selection4, R.color.selection4of6)
            setupClickForColor(context, R.id.selection5, R.color.selection5of6)
            setupClickForColor(context, R.id.selection6, R.color.selection6of6)
        }.let {
            AppWidgetManager.getInstance(context).updateAppWidget(widgetId, it)
        }
    }
}

private fun RemoteViews.setupClickForColor(
    context: Context,
    viewId: Int,
    colorId: Int
) {
    setColorStateList(
        viewId,
        "setBackgroundTintList",
        context.resources.getColorStateList(colorId, context.resources.newTheme())
    )
    setOnClickPendingIntent(viewId, context.getOnClickPendingIntent(colorId))
}

private fun Context.getOnClickPendingIntent(colorId: Int): PendingIntent =
    Intent(this, WidgetClickReceiver::class.java).apply {
        putExtra(EXTRA_COLOR, resources.getColor(colorId, resources.newTheme()))
    }.let { intent ->
        PendingIntent.getBroadcast(
            this,
            colorId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
        )
    }
