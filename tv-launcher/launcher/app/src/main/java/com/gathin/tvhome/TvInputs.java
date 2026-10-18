package com.gathin.tvhome;

import android.content.Context;
import android.content.Intent;
import android.media.tv.TvContract;
import android.media.tv.TvInputInfo;
import android.media.tv.TvInputManager;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/** A shared input snapshot for the home shortcuts and the Inputs menu. */
final class TvInputs {
    static final class Source {
        final String id;
        final String parentId;
        final String label;
        final boolean hdmi;
        final int state;

        Source(String id, String parentId, String label, boolean hdmi, int state) {
            this.id = id;
            this.parentId = parentId;
            this.label = label;
            this.hdmi = hdmi;
            this.state = state;
        }

        int iconRes() {
            return hdmi ? R.drawable.ic_input : R.drawable.ic_av;
        }

        Intent intent() {
            return new Intent(Intent.ACTION_VIEW).setDataAndType(
                    TvContract.buildChannelUriForPassthroughInput(id),
                    TvContract.Channels.CONTENT_ITEM_TYPE);
        }
    }

    static List<Source> load(Context context, boolean connectedOnly) {
        TvInputManager manager = (TvInputManager) context.getSystemService(Context.TV_INPUT_SERVICE);
        List<Source> snapshot = new ArrayList<>();
        if (manager == null) return snapshot;
        try {
            for (TvInputInfo info : manager.getTvInputList()) {
                if (!info.isPassthroughInput()) continue;
                try {
                    CharSequence label = info.loadCustomLabel(context);
                    if (label == null || label.length() == 0) label = info.loadLabel(context);
                    boolean hdmi = info.getType() == TvInputInfo.TYPE_HDMI;
                    String title = label == null || label.length() == 0
                            ? context.getString(R.string.inputs) : label.toString();
                    if (hdmi && !title.toUpperCase(Locale.ROOT).contains("HDMI")) title = "HDMI " + title;
                    snapshot.add(new Source(info.getId(), info.getParentId(), title, hdmi,
                            manager.getInputState(info.getId())));
                } catch (IllegalArgumentException removedDuringRead) {
                    // An input may disappear between reading the list and reading its state.
                }
            }
        } catch (SecurityException unavailable) {
            // Other launcher functions and Live TV remain usable without access to hardware inputs.
        }
        return select(snapshot, connectedOnly);
    }

    static List<Source> select(List<Source> snapshot, boolean connectedOnly) {
        Map<String, Source> sources = new LinkedHashMap<>();
        for (Source source : snapshot) sources.put(source.id, source);
        Map<String, Integer> states = new LinkedHashMap<>();
        for (Source source : sources.values()) states.put(source.id, source.state);
        for (Source source : sources.values()) {
            // A CEC child and its physical HDMI port describe the same socket. A running child
            // also confirms that its parent port is connected, even if the parent reports standby.
            if (source.state == TvInputManager.INPUT_STATE_CONNECTED && sources.containsKey(source.parentId)) {
                states.put(source.parentId, TvInputManager.INPUT_STATE_CONNECTED);
            }
        }
        List<Source> result = new ArrayList<>();
        for (Source source : sources.values()) {
            if (!source.id.equals(source.parentId) && sources.containsKey(source.parentId)) continue;
            int state = states.get(source.id);
            if (connectedOnly && state != TvInputManager.INPUT_STATE_CONNECTED) continue;
            result.add(new Source(source.id, source.parentId, source.label, source.hdmi, state));
        }
        result.sort((a, b) -> {
            if (a.hdmi != b.hdmi) return a.hdmi ? -1 : 1;
            int label = a.label.compareToIgnoreCase(b.label);
            return label != 0 ? label : a.id.compareTo(b.id);
        });
        return result;
    }

    static Intent liveTvIntent(Context context, String packageName) {
        if (packageName.isEmpty()) return null;
        Intent intent = context.getPackageManager().getLeanbackLaunchIntentForPackage(packageName);
        if (intent == null) intent = context.getPackageManager().getLaunchIntentForPackage(packageName);
        if (intent == null) {
            intent = new Intent(Intent.ACTION_VIEW)
                    .setDataAndType(TvContract.Channels.CONTENT_URI, TvContract.Channels.CONTENT_TYPE)
                    .setPackage(packageName);
            if (intent.resolveActivity(context.getPackageManager()) == null) return null;
        }
        return intent;
    }
}
