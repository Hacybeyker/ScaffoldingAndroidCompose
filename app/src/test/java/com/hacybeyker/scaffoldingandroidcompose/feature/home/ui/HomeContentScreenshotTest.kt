package com.hacybeyker.scaffoldingandroidcompose.feature.home.ui

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.takahirom.roborazzi.captureRoboImage
import com.hacybeyker.scaffoldingandroidcompose.core.ui.theme.ScaffoldingAndroidComposeTheme
import com.hacybeyker.scaffoldingandroidcompose.feature.home.domain.Greeting
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * Golden test of the stateless [HomeContent]. Dynamic color is off and the size is pinned so the
 * render is reproducible on any machine; the default Robolectric screen (320x470dp) would clip it.
 */
@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = "w411dp-h891dp")
class HomeContentScreenshotTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun homeContent_light() = captureHomeContent(darkTheme = false)

    @Test
    fun homeContent_dark() = captureHomeContent(darkTheme = true)

    private fun captureHomeContent(darkTheme: Boolean) {
        composeRule.setContent {
            ScaffoldingAndroidComposeTheme(darkTheme = darkTheme, dynamicColor = false) {
                HomeContent(
                    uiState = HomeUiState.Content(greeting = Greeting(name = "Android")),
                    onIntent = {}
                )
            }
        }
        composeRule.onRoot().captureRoboImage()
    }
}
