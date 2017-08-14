/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.focus.session;

import android.arch.lifecycle.LiveData;
import android.content.Intent;
import android.net.Uri;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mozilla.focus.utils.SafeIntent;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;

import java.util.List;

import static org.junit.Assert.*;

@RunWith(RobolectricTestRunner.class)
public class SessionManagerTest {
    private static final String TEST_URL = "https://github.com/mozilla-mobile/focus-android";

    @Test
    public void testInitialState() {
        final SessionManager sessionManager = SessionManager.getInstance();

        assertNotNull(sessionManager.getSessions().getValue());
        assertEquals(0, sessionManager.getSessions().getValue().size());
    }

    @Test
    public void testViewIntent() {
        final SessionManager sessionManager = SessionManager.getInstance();

        final SafeIntent intent = new SafeIntent(new Intent(Intent.ACTION_VIEW, Uri.parse(TEST_URL)));
        sessionManager.handleIntent(RuntimeEnvironment.application, intent, null);

        final List<Session> sessions = sessionManager.getSessions().getValue();
        assertNotNull(sessions);
        assertEquals(1, sessions.size());

        final Session session = sessions.get(0);
        assertEquals(TEST_URL, session.getUrl().getValue());
    }
}