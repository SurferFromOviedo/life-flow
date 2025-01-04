package com.example.collage.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import com.example.collage.ui.theme.CollageTheme
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionState
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState

class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels{
        MainViewModel.Factory
    }
    @OptIn(ExperimentalPermissionsApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CollageTheme {
                val uiState = viewModel.uiState.collectAsState()
                val cameraPermissionState: PermissionState = rememberPermissionState(android.Manifest.permission.CAMERA)
                Scaffold(
                    topBar = {
                        if(uiState.value.screenToShow == Screen.CAMERA || uiState.value.screenToShow == Screen.CALENDAR){
                            CollageAppTopBar(
                                viewModel = viewModel,
                                screen = uiState.value.screenToShow
                            )
                        }
                    }
                ) {
                    innerPadding ->
                    AppRoot(
                        modifier = Modifier.padding(innerPadding),
                        viewModel = viewModel,
                        uiState = uiState
                    )

                }
            }
        }
    }
}
@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun CollageAppTopBar(
    modifier: Modifier = Modifier,
    viewModel: MainViewModel,
    screen: Screen
){
    val uiState = viewModel.uiState.collectAsState()
    val cameraPermissionState: PermissionState = rememberPermissionState(android.Manifest.permission.CAMERA)
    val title = when(screen){
        Screen.CALENDAR -> "Calendar"
        Screen.CAMERA -> "Camera"
    }
    TopAppBar(
        modifier = modifier,
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge
            )
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        ),
        navigationIcon = {
            if(screen == Screen.CAMERA){
                IconButton(onClick = { viewModel.closeCameraScreen() }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back"
                    )
                }
            }
        },
        actions = {
            if(screen == Screen.CALENDAR){
                if(uiState.value.calendarLoadingComplete) {
                    val photoRemainingTime = uiState.value.timerState.photoTimeRemaining
                    val camRemainingTime = uiState.value.timerState.camActivationTimeRemaining
                    if (photoRemainingTime != null) {
                        val hours = photoRemainingTime / (1000 * 60 * 60)
                        val minutes = (photoRemainingTime / (1000 * 60)) % 60
                        val seconds = (photoRemainingTime / 1000) % 60
                        Text(
                            text = String.format("%02d:%02d:%02d", hours, minutes, seconds)
                        )
                        IconButton(onClick = {
                            viewModel.openCameraScreen(cameraPermissionState)
                        }) {
                            Icon(
                                imageVector = Icons.Default.CameraAlt,
                                contentDescription = "Take a photo"
                            )
                        }
                    }else if (camRemainingTime != null) {
                        val hours = camRemainingTime / (1000 * 60 * 60)
                        val minutes = (camRemainingTime / (1000 * 60)) % 60
                        val seconds = (camRemainingTime / 1000) % 60
                        Text(
                            text = String.format("%02d:%02d:%02d", hours, minutes, seconds)
                        )
                    }else if(uiState.value.showCam) {
                        IconButton(onClick = {
                            viewModel.openCameraScreen(cameraPermissionState)
                        }) {
                            Icon(
                                imageVector = Icons.Default.CameraAlt,
                                contentDescription = "Take a photo"
                            )
                        }
                    }

                    IconButton(onClick = {
                        when (viewModel.uiState.value.layout) {
                            Layout.WEEK -> {
                                viewModel.changeLayout(Layout.TWO_WEEKS)
                            }

                            else -> {
                                viewModel.changeLayout(Layout.WEEK)
                            }
                        }
                    }) {
                        Icon(
                            imageVector = Icons.Default.GridView,
                            contentDescription = "Layout"
                        )
                    }
                }
            }
        }
    )
}