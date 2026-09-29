package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.rahulai.ui.screens.AssistantScreen
import com.example.rahulai.ui.screens.PhoneModesScreen
import com.example.rahulai.ui.screens.QuickAppsScreen
import com.example.rahulai.ui.screens.SystemControlScreen
import com.example.rahulai.ui.viewmodel.RahulAiViewModel
import com.example.ui.theme.DarkNavy
import com.example.ui.theme.RahulAiTheme
import com.example.ui.theme.SurfaceDark
import com.example.ui.theme.TextMuted

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            RahulAiTheme {
                val viewModel: RahulAiViewModel = viewModel()
                MainAppContent(viewModel = viewModel)
            }
        }
    }
}

data class NavigationTabItem(
    val title: String,
    val icon: ImageVector,
    val testTag: String
)

@Composable
fun MainAppContent(viewModel: RahulAiViewModel) {
    val currentTab by viewModel.currentTab.collectAsState()

    // Handle back button on secondary screens
    BackHandler(enabled = currentTab != 0) {
        viewModel.setTab(0)
    }

    val tabs = listOf(
        NavigationTabItem("Maya AI", Icons.Default.Favorite, "tab_assistant"),
        NavigationTabItem("Control", Icons.Default.Tune, "tab_system"),
        NavigationTabItem("Quick Apps", Icons.Default.Apps, "tab_quick_apps"),
        NavigationTabItem("Modes", Icons.Default.Smartphone, "tab_modes")
    )

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            NavigationBar(
                containerColor = SurfaceDark,
                tonalElevation = 8.dp
            ) {
                tabs.forEachIndexed { index, item ->
                    val isSelected = currentTab == index
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { viewModel.setTab(index) },
                        icon = {
                            Icon(
                                imageVector = item.icon,
                                contentDescription = item.title
                            )
                        },
                        label = {
                            Text(text = item.title)
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = DarkNavy,
                            selectedTextColor = Color(0xFFFF2A6D),
                            indicatorColor = Color(0xFFFF2A6D),
                            unselectedIconColor = TextMuted,
                            unselectedTextColor = TextMuted
                        ),
                        modifier = Modifier.testTag(item.testTag)
                    )
                }
            }
        }
    ) { innerPadding ->
        val screenModifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)

        when (currentTab) {
            0 -> AssistantScreen(viewModel = viewModel, modifier = screenModifier)
            1 -> SystemControlScreen(viewModel = viewModel, modifier = screenModifier)
            2 -> QuickAppsScreen(viewModel = viewModel, modifier = screenModifier)
            3 -> PhoneModesScreen(viewModel = viewModel, modifier = screenModifier)
        }
    }
}
