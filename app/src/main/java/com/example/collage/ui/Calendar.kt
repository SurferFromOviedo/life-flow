package com.example.collage.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.zIndex
import coil.compose.AsyncImage
import coil.compose.AsyncImagePainter
import coil.compose.SubcomposeAsyncImage
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.example.collage.data.Date
import com.example.collage.ui.theme.CollageTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun Calendar(
    modifier: Modifier = Modifier,
    viewModel: MainViewModel
){
    val listState = rememberLazyListState()
    val uiState = viewModel.uiState.collectAsState()
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        listState.scrollToItem(Int.MAX_VALUE / 2)
        scope.launch{
            delay(1000)
            viewModel.completeCalendarLoading()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
    ){
        LazyColumn(
            modifier = modifier
                .fillMaxSize(),
            state = listState
        ) {
            items(Int.MAX_VALUE) { index ->
                CalendarRow(
                    weekDays = viewModel.getWeekDays(
                        index,
                        context = LocalContext.current,
                        layout = uiState.value.layout
                    ),
                    viewModel = viewModel,
                )
            }
        }

        if(!uiState.value.calendarLoadingComplete) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background),
                contentAlignment = Alignment.Center
            ){
                CircularProgressIndicator()
            }
        }
    }
}

@Composable
fun CalendarRow(
    weekDays: List<Date>,
    viewModel: MainViewModel

) {
    Row(
        modifier = Modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        for (date in weekDays) {
            CalendarDay(
                modifier = Modifier
                    .weight(1f),
                date = date,
                viewModel = viewModel
            )
        }
    }
}


@Composable
fun CalendarDay(
    modifier: Modifier = Modifier,
    viewModel: MainViewModel,
    date: Date
) {
    if(date.path == null){
        Box(
            modifier = modifier
                .aspectRatio(1f)
                .background(
                    color = if (isSystemInDarkTheme()) Color.Black else Color.White
                )
                .clickable {
                    viewModel.clickOnDate(date)
                }
                .border(
                    width = Dp.Hairline,
                    color = MaterialTheme.colorScheme.primary
                ),
            contentAlignment = Alignment.Center
        ){
            Text(
                text = date.day.toString(),
            )
        }
    }else{
        Box(
            modifier = modifier
                .aspectRatio(1f)
                .background(
                    color = if (isSystemInDarkTheme()) Color.Black else Color.White
                )
                .clickable {
                    viewModel.clickOnDate(date)
                },
            contentAlignment = Alignment.Center
        ){
            Image(
                painter = rememberAsyncImagePainter(date.path),
                contentDescription = "Image taken on ${date.day}/${date.month}/${date.year}",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
            )
        }
    }
}

@Composable
fun PhotoDialog(
    modifier: Modifier = Modifier,
    date: Date,
    onDismiss: () -> Unit,
    viewModel: MainViewModel
) {
    val context = LocalContext.current
    val painter = rememberAsyncImagePainter(
        model = ImageRequest.Builder(LocalContext.current)
            .data(date.path)
            .size(coil.size.Size.ORIGINAL)
            .build()

    )

    when (painter.state) {
        is AsyncImagePainter.State.Success -> {
            Dialog(
                onDismissRequest = { onDismiss() }
            ){
                Card(
                    modifier = modifier,
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Box{
                        Text(
                            text = "${date.day}/${date.month}/${date.year}",
                            modifier = Modifier
                                .align(Alignment.TopCenter)
                                .zIndex(1f)
                                .padding(8.dp),
                            style = MaterialTheme.typography.titleSmall,
                            color = Color(0xff90d5ff)
                        )
                        Image(
                            painter = painter,
                            contentDescription = "Image taken on ${date.day}/${date.month}/${date.year}",
                            contentScale = ContentScale.Fit,
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                        )

                        Row(
                            modifier = Modifier
                                .align(Alignment.BottomEnd),
                            horizontalArrangement = Arrangement.End
                        ){

                            TextButton(
                                onClick = {
                                    viewModel.exportPhoto(date, context)
                                    viewModel.closeImageDialog()
                                }
                            ) {
                                Text(
                                    text = "Export",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = Color(0xff90d5ff)
                                )
                            }

                            TextButton(
                                onClick = {
                                    viewModel.deletePhoto(date, context)
                                    viewModel.closeImageDialog()
                                }
                            ) {
                                Text(
                                    modifier = Modifier,
                                    text = "Delete",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = Color(0xff90d5ff)
                                )
                            }
                        }

                    }

                }
            }
        }
        else -> {
        }
    }
}



