/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.focus.session;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;

import org.mozilla.focus.FocusApplication;
import org.mozilla.focus.R;
import org.mozilla.focus.activity.MainActivity;
import org.mozilla.focus.architecture.NonNullObserver;
import org.mozilla.focus.telemetry.TelemetryWrapper;
import org.mozilla.focus.web.WebViewProvider;

import java.util.List;

/**
 * As long as a session is active this service will keep the notification (and our process) alive.
 */
public class SessionNotificationService extends Service {
    private static final int NOTIFICATION_ID = 83;

    private static final String ACTION_START = "start";
    private static final String ACTION_ERASE = "erase";

    /* package */ static void start(Context context) {
        final Intent intent = new Intent(context, SessionNotificationService.class);
        intent.setAction(ACTION_START);

        context.startService(intent);
    }

    /* package */ static void stop(Context context) {
        final Intent intent = new Intent(context, SessionNotificationService.class);

        context.stopService(intent);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        final String action = intent.getAction();
        if (action == null) {
            return START_NOT_STICKY;
        }

        switch (action) {
            case ACTION_START:
                startForeground(NOTIFICATION_ID, buildNotification());
                break;

            case ACTION_ERASE:
                SessionManager.getInstance().removeSessions();

                TelemetryWrapper.eraseNotificationEvent();
                break;

            default:
                throw new IllegalStateException("Unknown intent: " + intent);
        }

        return START_NOT_STICKY;
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        // If our task got removed then we might have been killed in the task switcher. In this case
        // our activity had no chance to cleanup the browsing data. Let's try to do it from here.
        WebViewProvider.performCleanup(this);
        // TODO: Perform this from session observer?

        SessionManager.getInstance().removeSessions();

        stopForeground(true);
        stopSelf();
    }

    private Notification buildNotification() {
        return new NotificationCompat.Builder(this)
                .setOngoing(true)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(getString(R.string.app_name))
                .setContentText(getString(R.string.notification_erase_text))
                .setContentIntent(createNotificationIntent())
                .setVisibility(Notification.VISIBILITY_SECRET)
                .setShowWhen(false)
                .setLocalOnly(true)
                .setColor(ContextCompat.getColor(this, R.color.colorFloatingActionButtonTint))
                .addAction(new NotificationCompat.Action(
                        R.drawable.ic_notification,
                        getString(R.string.notification_action_open),
                        createOpenActionIntent()))
                .addAction(new NotificationCompat.Action(
                        R.drawable.ic_delete,
                        getString(R.string.notification_action_erase_and_open),
                        createOpenAndEraseActionIntent()))
                .build();
    }

    private PendingIntent createNotificationIntent() {
        final Intent intent = new Intent(this, SessionNotificationService.class);
        intent.setAction(ACTION_ERASE);

        return PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_ONE_SHOT);
    }

    private PendingIntent createOpenActionIntent() {
        final Intent intent = new Intent(this, MainActivity.class);
        intent.setAction(MainActivity.ACTION_OPEN);

        return PendingIntent.getActivity(this, 1, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private PendingIntent createOpenAndEraseActionIntent() {
        final Intent intent = new Intent(this, MainActivity.class);

        intent.setAction(MainActivity.ACTION_ERASE);
        intent.putExtra(MainActivity.EXTRA_NOTIFICATION, true);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        return PendingIntent.getActivity(this, 2, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
