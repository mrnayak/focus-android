/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.focus.session;

import android.content.Context;
import android.os.Bundle;

import org.mozilla.focus.architecture.NonNullLiveData;
import org.mozilla.focus.architecture.NonNullMutableLiveData;
import org.mozilla.focus.customtabs.CustomTabConfig;
import org.mozilla.focus.utils.SafeIntent;

import java.util.UUID;

public class Session {
    private final String uuid;
    private final NonNullMutableLiveData<String> url;
    private final NonNullMutableLiveData<Integer> progress;
    private final NonNullMutableLiveData<Boolean> secure;
    private final NonNullMutableLiveData<Boolean> loading;
    private final NonNullMutableLiveData<Integer> trackersBlocked;
    private CustomTabConfig customTabConfig;
    private Bundle webviewState;

    /* package */ Session(String url) {
        this.uuid = UUID.randomUUID().toString();

        this.url = new NonNullMutableLiveData<>(url);
        this.progress = new NonNullMutableLiveData<>(0);
        this.secure = new NonNullMutableLiveData<>(false);
        this.loading = new NonNullMutableLiveData<>(false);
        this.trackersBlocked = new NonNullMutableLiveData<>(0);
    }

    /* package */ Session(Context context, SafeIntent intent, String url) {
        this(url);

        if (CustomTabConfig.isCustomTabIntent(intent)) {
            customTabConfig = CustomTabConfig.parseCustomTabIntent(context, intent);
        }
    }

    public String getUUID() {
        return uuid;
    }

    /* package */ void setUrl(String url) {
        this.url.setValue(url);
    }

    public NonNullLiveData<String> getUrl() {
        return url;
    }

    /* package */ void setProgress(int progress) {
        this.progress.setValue(progress);
    }

    public NonNullLiveData<Integer> getProgress() {
        return progress;
    }

    /* package */ void setSecure(boolean secure) {
        this.secure.setValue(secure);
    }

    public NonNullLiveData<Boolean> getSecure() {
        return secure;
    }

    /* package */ void setLoading(boolean loading) {
        this.loading.setValue(loading);
    }

    public NonNullLiveData<Boolean> getLoading() {
        return loading;
    }

    /* package */ void setTrackersBlocked(int trackersBlocked) {
        this.trackersBlocked.postValue(trackersBlocked);
    }

    public NonNullLiveData<Integer> getBlockedTrackers() {
        return trackersBlocked;
    }

    public void saveWebViewState(Bundle bundle) {
        this.webviewState = bundle;
    }

    public Bundle getWebViewState() {
        return webviewState;
    }

    public boolean isCustomTab() {
        return customTabConfig != null;
    }

    public CustomTabConfig getCustomTabConfig() {
        return customTabConfig;
    }
}
