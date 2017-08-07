/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package android.support.v4.app;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.util.SparseArray;
import android.widget.RemoteViews;

import java.util.ArrayList;
import java.util.List;

@RequiresApi(19)
class NotificationCompatKitKat {
    public static class Builder implements NotificationBuilderWithBuilderAccessor,
            NotificationBuilderWithActions {
        protected Notification.Builder mBuilder;
        protected RemoteViews mContentView;
        protected RemoteViews mBigContentView;

        private Bundle mExtras;
        private List<Bundle> mActionExtrasList = new ArrayList<Bundle>();

        public Builder(Context context, Notification n,
                CharSequence contentTitle, CharSequence contentText, CharSequence contentInfo,
                RemoteViews tickerView, int number,
                PendingIntent contentIntent, PendingIntent fullScreenIntent, Bitmap largeIcon,
                int progressMax, int progress, boolean progressIndeterminate, boolean showWhen,
                boolean useChronometer, int priority, CharSequence subText, boolean localOnly,
                ArrayList<String> people, Bundle extras, String groupKey, boolean groupSummary,
                String sortKey, RemoteViews contentView, RemoteViews bigContentView,
                String channelId) {
            mBuilder = newBuilder(context, channelId)
                .setWhen(n.when)
                .setShowWhen(showWhen)
                .setSmallIcon(n.icon, n.iconLevel)
                .setContent(n.contentView)
                .setTicker(n.tickerText, tickerView)
                .setSound(n.sound, n.audioStreamType)
                .setVibrate(n.vibrate)
                .setLights(n.ledARGB, n.ledOnMS, n.ledOffMS)
                .setOngoing((n.flags & Notification.FLAG_ONGOING_EVENT) != 0)
                .setOnlyAlertOnce((n.flags & Notification.FLAG_ONLY_ALERT_ONCE) != 0)
                .setAutoCancel((n.flags & Notification.FLAG_AUTO_CANCEL) != 0)
                .setDefaults(n.defaults)
                .setContentTitle(contentTitle)
                .setContentText(contentText)
                .setSubText(subText)
                .setContentInfo(contentInfo)
                .setContentIntent(contentIntent)
                .setDeleteIntent(n.deleteIntent)
                .setFullScreenIntent(fullScreenIntent,
                        (n.flags & Notification.FLAG_HIGH_PRIORITY) != 0)
                .setLargeIcon(largeIcon)
                .setNumber(number)
                .setUsesChronometer(useChronometer)
                .setPriority(priority)
                .setProgress(progressMax, progress, progressIndeterminate);
            mExtras = new Bundle();
            if (extras != null) {
                mExtras.putAll(extras);
            }
            if (people != null && !people.isEmpty()) {
                mExtras.putStringArray(Notification.EXTRA_PEOPLE,
                        people.toArray(new String[people.size()]));
            }
            if (localOnly) {
                mExtras.putBoolean(NotificationCompatExtras.EXTRA_LOCAL_ONLY, true);
            }
            if (groupKey != null) {
                mExtras.putString(NotificationCompatExtras.EXTRA_GROUP_KEY, groupKey);
                if (groupSummary) {
                    mExtras.putBoolean(NotificationCompatExtras.EXTRA_GROUP_SUMMARY, true);
                } else {
                    mExtras.putBoolean(NotificationManagerCompat.EXTRA_USE_SIDE_CHANNEL, true);
                }
            }
            if (sortKey != null) {
                mExtras.putString(NotificationCompatExtras.EXTRA_SORT_KEY, sortKey);
            }
            mContentView = contentView;
            mBigContentView = bigContentView;
        }

        @Override
        public void addAction(NotificationCompatBase.Action action) {
            mActionExtrasList.add(
                    NotificationCompatJellybean.writeActionAndGetExtras(mBuilder, action));
        }

        @Override
        public Notification.Builder getBuilder() {
            return mBuilder;
        }

        @Override
        public Notification build() {
            SparseArray<Bundle> actionExtrasMap = NotificationCompatJellybean.buildActionExtrasMap(
                    mActionExtrasList);
            if (actionExtrasMap != null) {
                // Add the action extras sparse array if any action was added with extras.
                mExtras.putSparseParcelableArray(
                        NotificationCompatExtras.EXTRA_ACTION_EXTRAS, actionExtrasMap);
            }
            mBuilder.setExtras(mExtras);
            Notification notification = mBuilder.build();
            if (mContentView != null) {
                notification.contentView = mContentView;
            }
            if (mBigContentView != null) {
                notification.bigContentView = mBigContentView;
            }
            return notification;
        }

        protected Notification.Builder newBuilder(Context context, String channelId) {
            return new Notification.Builder(context);
        }
    }

    public static NotificationCompatBase.Action getAction(Notification notif,
            int actionIndex, NotificationCompatBase.Action.Factory factory,
            RemoteInputCompatBase.RemoteInput.Factory remoteInputFactory) {
        Notification.Action action = notif.actions[actionIndex];
        Bundle actionExtras = null;
        SparseArray<Bundle> actionExtrasMap = notif.extras.getSparseParcelableArray(
                NotificationCompatExtras.EXTRA_ACTION_EXTRAS);
        if (actionExtrasMap != null) {
            actionExtras = actionExtrasMap.get(actionIndex);
        }
        return NotificationCompatJellybean.readAction(factory, remoteInputFactory,
                action.icon, action.title, action.actionIntent, actionExtras);
    }
}
