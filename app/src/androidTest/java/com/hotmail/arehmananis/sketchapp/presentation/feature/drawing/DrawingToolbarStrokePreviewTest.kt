package com.hotmail.arehmananis.sketchapp.presentation.feature.drawing

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.hotmail.arehmananis.sketchapp.domain.model.BrushType
import com.hotmail.arehmananis.sketchapp.presentation.theme.SketchAppTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DrawingToolbarStrokePreviewTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun strokeSizeLabelAlwaysVisible() {
        composeTestRule.setContent {
            SketchAppTheme {
                DrawingToolbar(
                    currentBrush = BrushType.PEN,
                    currentColor = Color.Black,
                    strokeWidth = 20f,
                    onBrushChange = {},
                    onColorChange = {},
                    onStrokeWidthChange = {},
                    onShapeToolChange = {},
                    onFilledChange = {}
                )
            }
        }
        composeTestRule.onNodeWithText("20px").assertIsDisplayed()
    }

    @Test
    fun strokePreviewBubbleHiddenInitially() {
        composeTestRule.setContent {
            SketchAppTheme {
                DrawingToolbar(
                    currentBrush = BrushType.PEN,
                    currentColor = Color.Black,
                    strokeWidth = 20f,
                    onBrushChange = {},
                    onColorChange = {},
                    onStrokeWidthChange = {},
                    onShapeToolChange = {},
                    onFilledChange = {}
                )
            }
        }
        // Only the header label shows "20px" on initial render — bubble is hidden
        composeTestRule.onAllNodesWithText("20px").assertCountEquals(1)
    }
}
