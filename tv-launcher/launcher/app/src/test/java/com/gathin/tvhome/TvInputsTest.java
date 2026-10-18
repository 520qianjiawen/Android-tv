package com.gathin.tvhome;

import android.media.tv.TvInputManager;

import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class TvInputsTest {
    private static final int CONNECTED = TvInputManager.INPUT_STATE_CONNECTED;
    private static final int STANDBY = TvInputManager.INPUT_STATE_CONNECTED_STANDBY;
    private static final int DISCONNECTED = TvInputManager.INPUT_STATE_DISCONNECTED;

    private TvInputs.Source source(String id, String parent, String label, boolean hdmi, int state) {
        return new TvInputs.Source(id, parent, label, hdmi, state);
    }

    @Test public void shortcutsExcludeStandbyAndUnpluggedPorts() {
        List<TvInputs.Source> result = TvInputs.select(Arrays.asList(
                source("hdmi1", null, "HDMI 1", true, CONNECTED),
                source("hdmi2", null, "HDMI 2", true, STANDBY),
                source("av", null, "AV", false, DISCONNECTED)), true);
        assertEquals(1, result.size());
        assertEquals("hdmi1", result.get(0).id);
    }

    @Test public void activeCecChildShowsItsPhysicalPortOnlyOnceWithoutMutatingSnapshot() {
        TvInputs.Source port = source("hdmi1", null, "HDMI 1", true, STANDBY);
        List<TvInputs.Source> result = TvInputs.select(Arrays.asList(port,
                source("player", "hdmi1", "Player", true, CONNECTED)), true);
        assertEquals(1, result.size());
        assertEquals("hdmi1", result.get(0).id);
        assertEquals(CONNECTED, result.get(0).state);
        assertEquals(STANDBY, port.state);
    }

    @Test public void disconnectedCecChildDoesNotHideActivePort() {
        List<TvInputs.Source> result = TvInputs.select(Arrays.asList(
                source("hdmi1", null, "HDMI 1", true, CONNECTED),
                source("player", "hdmi1", "Player", true, DISCONNECTED)), true);
        assertEquals(1, result.size());
        assertEquals("hdmi1", result.get(0).id);
    }

    @Test public void childWithoutParentRemainsAccessible() {
        List<TvInputs.Source> result = TvInputs.select(Arrays.asList(
                source("player", "missing", "Player", true, CONNECTED)), true);
        assertEquals(1, result.size());
        assertEquals("player", result.get(0).id);
    }

    @Test public void menuIncludesUnpluggedPortsAndOrdersHdmiBeforeAv() {
        List<TvInputs.Source> result = TvInputs.select(Arrays.asList(
                source("av", null, "AV", false, DISCONNECTED),
                source("hdmi2", null, "HDMI 2", true, STANDBY),
                source("player", "hdmi2", "Player", true, DISCONNECTED),
                source("hdmi1", null, "HDMI 1", true, DISCONNECTED)), false);
        assertEquals(3, result.size());
        assertEquals("hdmi1", result.get(0).id);
        assertEquals("hdmi2", result.get(1).id);
        assertEquals("av", result.get(2).id);
        assertTrue(TvInputs.select(Arrays.asList(
                source("hdmi1", null, "HDMI 1", true, DISCONNECTED)), true).isEmpty());
    }
}
