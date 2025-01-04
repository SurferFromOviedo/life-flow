package com.example.collage.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.collage.ui.theme.CollageTheme
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionState
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState


@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun AppRoot(
    modifier: Modifier = Modifier,
    viewModel: MainViewModel,
    uiState: State<AppState>
){
    val cameraPermissionState: PermissionState = rememberPermissionState(android.Manifest.permission.CAMERA)

    CalendarScreen(
        modifier
            .fillMaxSize(),
        viewModel = viewModel
    )
    if (uiState.value.screenToShow == Screen.CAMERA) {
        CameraScreen(
            modifier = modifier
                .fillMaxWidth(),
            viewModel = viewModel
        )
    }

}

@Composable
fun CalendarScreen(
    modifier: Modifier = Modifier,
    viewModel: MainViewModel
){
    val uiState = viewModel.uiState.collectAsState()
    Calendar(
        modifier = modifier
            .fillMaxSize(),
        viewModel = viewModel,
    )
    if(uiState.value.showImageDialog){
        PhotoDialog(
            date = uiState.value.selectedDate!!,
            onDismiss = { viewModel.closeImageDialog() },
            viewModel = viewModel
        )
    }
}

@Composable
fun CameraScreen(
    modifier: Modifier = Modifier,
    viewModel: MainViewModel
){
    BackHandler {
        viewModel.closeCameraScreen()
    }
    Camera(
        modifier = modifier
            .fillMaxSize(),
        viewModel = viewModel
    )
}

