package com.v2ray.ang.ui.main

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.click
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.performKeyInput
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.test.pressKey
import androidx.test.platform.app.InstrumentationRegistry
import com.v2ray.ang.R
import com.v2ray.ang.dto.entities.ProfileItem
import com.v2ray.ang.enums.EConfigType
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class MainRemoteNavigationTest {
    @get:Rule val compose = createComposeRule()
    private val context get() = InstrumentationRegistry.getInstrumentation().targetContext

    @Test
    fun dpadCanReachAndActivateServiceThenReturnToStatus() {
        val actions = mutableListOf<MainAction>()
        val running = mutableStateOf(false)
        compose.setContent {
            MaterialTheme { MainBottomBar("Status", running.value, false, actions::add) }
        }
        val status = compose.onNodeWithContentDescription("Status")
        status.performSemanticsAction(SemanticsActions.RequestFocus) { it() }
        status.performKeyInput { pressKey(Key.DirectionRight) }
        val start = compose.onNodeWithContentDescription(context.getString(R.string.acc_start))
        start.assertIsFocused().performKeyInput { pressKey(Key.DirectionCenter) }
        compose.runOnIdle {
            assertEquals(listOf(MainAction.ToggleService), actions)
            running.value = true
        }
        val stop = compose.onNodeWithContentDescription(context.getString(R.string.acc_stop))
        stop.assertIsFocused().performKeyInput { pressKey(Key.Enter) }
        compose.runOnIdle { assertEquals(2, actions.count { it == MainAction.ToggleService }) }
        stop.performKeyInput { pressKey(Key.DirectionLeft) }
        status.assertIsFocused()
    }

    @Test
    fun touchAndKeyboardUseTheSameServiceAction() {
        val actions = mutableListOf<MainAction>()
        compose.setContent {
            MaterialTheme { MainBottomBar("Status", false, true, actions::add) }
        }
        val start = compose.onNodeWithContentDescription(context.getString(R.string.acc_start))
        start.performTouchInput { click() }
        start.performKeyInput { pressKey(Key.Tab) }
        start.performSemanticsAction(SemanticsActions.RequestFocus) { it() }
        start.performKeyInput { pressKey(Key.Spacebar) }
        compose.runOnIdle { assertEquals(listOf(MainAction.ToggleService, MainAction.ToggleService), actions) }
    }

    @Test
    fun rowActionsDispatchOnlyTheFocusedAction() {
        val invoked = mutableListOf<String>()
        compose.setContent {
            MaterialTheme { ServerListItem(row("sample"), true, false, actions(invoked)) }
        }
        val server = compose.onNodeWithContentDescription("sample")
        server.assertIsSelected().performSemanticsAction(SemanticsActions.RequestFocus) { it() }
        server.performKeyInput { pressKey(Key.DirectionRight) }
        val share = compose.onNodeWithContentDescription(context.getString(R.string.title_configuration_share))
        share.assertIsFocused().performKeyInput { pressKey(Key.DirectionCenter) }
        share.performKeyInput { pressKey(Key.DirectionRight) }
        val edit = compose.onNodeWithContentDescription(context.getString(R.string.acc_edit))
        edit.assertIsFocused().performKeyInput { pressKey(Key.Enter) }
        edit.performKeyInput { pressKey(Key.DirectionRight) }
        val delete = compose.onNodeWithContentDescription(context.getString(R.string.acc_delete))
        delete.assertIsFocused().performKeyInput {
            pressKey(Key.DirectionCenter)
            pressKey(Key.DirectionLeft)
        }
        edit.assertIsFocused()
        compose.runOnIdle { assertEquals(listOf("share:sample", "edit:sample", "remove:sample"), invoked) }
    }

    @Test
    fun doubleColumnMoreButtonReturnsToItsOwnRow() {
        val invoked = mutableListOf<String>()
        compose.setContent {
            MaterialTheme { ServerListItem(row("sample"), false, true, actions(invoked)) }
        }
        val server = compose.onNodeWithContentDescription("sample")
        server.performSemanticsAction(SemanticsActions.RequestFocus) { it() }
        server.performKeyInput { pressKey(Key.DirectionRight) }
        val more = compose.onNodeWithContentDescription(context.getString(R.string.acc_more))
        more.assertIsFocused().performKeyInput { pressKey(Key.DirectionCenter) }
        more.performKeyInput { pressKey(Key.DirectionLeft) }
        server.assertIsFocused()
        compose.runOnIdle { assertEquals(listOf("more:sample"), invoked) }
    }

    @Test
    fun reorderingKeepsFocusWithTheServerIdentity() {
        val rows = mutableStateOf(listOf(row("first"), row("second")))
        compose.setContent {
            MaterialTheme {
                Column {
                    rows.value.forEach { item ->
                        key(item.guid) { ServerListItem(item, false, false, actions(mutableListOf())) }
                    }
                }
            }
        }
        val server = compose.onNodeWithContentDescription("second")
        server.performSemanticsAction(SemanticsActions.RequestFocus) { it() }
        compose.runOnIdle { rows.value = rows.value.reversed() }
        server.assertIsFocused()
    }

    private fun row(id: String) = ServerRowUiModel(
        guid = id,
        profile = ProfileItem(configType = EConfigType.VLESS),
        remarks = id,
        statistics = "",
        typeDescription = "VLESS",
        testDelayMillis = 0,
        subscriptionBadge = "",
    )

    private fun actions(invoked: MutableList<String>) = ServerRowActions(
        select = { invoked.add("select:$it") },
        edit = { id, _ -> invoked.add("edit:$id") },
        share = { id, _ -> invoked.add("share:$id") },
        more = { id, _ -> invoked.add("more:$id") },
        remove = { id, _ -> invoked.add("remove:$id") },
    )
}
