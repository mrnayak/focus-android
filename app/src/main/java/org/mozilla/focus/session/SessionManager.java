/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.focus.session;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import org.mozilla.focus.activity.MainActivity;
import org.mozilla.focus.shortcut.HomeScreen;
import org.mozilla.focus.telemetry.TelemetryWrapper;
import org.mozilla.focus.utils.SafeIntent;
import org.mozilla.focus.utils.UrlUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@SuppressWarnings("ConstantConditions")
public class SessionManager {
    private static final SessionManager INSTANCE = new SessionManager();

    // TODO: Maybe use something better than a plain list?
    private MutableLiveData<List<Session>> sessions;
    private String currentSessionUUID; // TODO: Keep UUID or session object?

    public static SessionManager getInstance() {
        return INSTANCE;
    }

    private SessionManager() {
        this.sessions = new MutableLiveData<>();
        this.sessions.setValue(Collections.unmodifiableList(Collections.<Session>emptyList()));
    }

    // TODO: How to add firstrun here? -- Or: How to let MainActivity handle that?
    public void handleIntent(final Context context, final SafeIntent intent, final Bundle savedInstanceState) {
        if ((intent.getFlags() & Intent.FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY) != 0) {
            return; // TODO: Document
        }

        if (savedInstanceState != null) {
            // We are restoring a previous session - No need to handle this Intent.
            return;
        }

        createSessionFromIntent(context, intent);
    }

    public void handleNewIntent(final Context context, final SafeIntent intent) {
        // TODO: Open new tab ?

        createSessionFromIntent(context, intent);
    }

    private void createSessionFromIntent(Context context, SafeIntent intent) {
        final String action = intent.getAction();

        if (Intent.ACTION_VIEW.equals(action)) {
            final Session session = createSession(context, intent, intent.getDataString());

            // TODO: Move telemetry somwhere else - it should not be here..
            /*
            if (intent.getBooleanExtra(MainActivity.EXTRA_TEXT_SELECTION, false)) {
                TelemetryWrapper.textSelectionIntentEvent();
            } else if (intent.hasExtra(HomeScreen.ADD_TO_HOMESCREEN_TAG)) {
                TelemetryWrapper.openHomescreenShortcutEvent();
            } else if (session.isCustomTab()) {
                TelemetryWrapper.customTabsIntentEvent(session.getCustomTabConfig().getOptionsList());
            } else {
                TelemetryWrapper.browseIntentEvent();
            }
            */

        } else if (Intent.ACTION_SEND.equals(action)) {
            final String dataString = intent.getStringExtra(Intent.EXTRA_TEXT);
            if (!TextUtils.isEmpty(dataString)) {
                final boolean isUrl = UrlUtils.isUrl(dataString);
                final String url = isUrl ? dataString : UrlUtils.createSearchUrl(context, dataString);

                createSession(url);

                TelemetryWrapper.shareIntentEvent(isUrl);
            }
        }
    }

    public Session getCurrentSession() {
        if (currentSessionUUID == null) {
            throw new IllegalAccessError("There's no active session");
        }

        for (Session session : sessions.getValue()) {
            if (currentSessionUUID.equals(session.getUUID())) {
                return session;
            }
        }

        throw new IllegalAccessError("There's no active session with the current UUID");
    }

    public Session getSessionByUUID(@NonNull String uuid) {
        for (Session session : sessions.getValue()) {
            if (uuid.equals(session.getUUID())) {
                return session;
            }
        }

        throw new IllegalAccessError("There's no active session with this UUID");
    }

    public LiveData<List<Session>> getSessions() {
        return sessions;
    }

    public Session createSession(@NonNull String url) {
        final Session session = new Session(url);
        addSession(session);
        return session;
    }

    private Session createSession(Context context, SafeIntent intent, String url) {
        final Session session = new Session(context, intent, url);
        addSession(session);
        return session;
    }

    private void addSession(Session session) {
        // TODO: The newly created session is always the current one until we actually support multiple.
        currentSessionUUID = session.getUUID();

        // TODO: Currently we only have one session at all times.
        final List<Session> sessions = new ArrayList<>();
        sessions.add(session);

        this.sessions.setValue(Collections.unmodifiableList(sessions));
    }

    public void removeSessions() {
        sessions.setValue(Collections.unmodifiableList(Collections.<Session>emptyList()));
    }
}
