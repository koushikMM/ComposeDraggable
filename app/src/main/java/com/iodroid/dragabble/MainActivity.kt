package com.iodroid.dragabble

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.consumeAllChanges
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.iodroid.dragabble.ui.theme.MyApplicationTheme
internal val LocalDragTargetInfo = compositionLocalOf { DraggableData() }

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyApplicationTheme {
                // A surface container using the 'background' color from the theme
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colors.background) {
                    Greeting("Android")
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String) {
    val state = remember { DraggableData() }
    CompositionLocalProvider(
        LocalDragTargetInfo provides state
    ) {
        var targetSize by remember {
            mutableStateOf(IntSize.Zero)
        }
        DragTarget(
            modifier = Modifier.wrapContentSize(),
            dataToDrop = ""
        ) {
            Text(
                text = "Hello World",
                modifier = Modifier.height(40.dp).width(100.dp),
            )
        }

        if (state.isDragging) {
            var targetSize by remember {
                mutableStateOf(IntSize.Zero)
            }
            Box(
                modifier = Modifier
                    .graphicsLayer {
                        val offset = (state.dragPosition + state.dragOffset)
                        scaleX = 1.3f
                        scaleY = 1.3f
                        alpha = if (targetSize == IntSize.Zero) 0f else .9f
                        translationX = offset.x
                        translationY = offset.y
                    }
                    .onGloballyPositioned {
                        targetSize = it.size
                    }
            ) {
                state.draggableComposable?.invoke()
            }
        }
    }
}

@Composable
fun <T> DragTarget(
    modifier: Modifier,
    dataToDrop: T,
    content: @Composable (() -> Unit)
) {

    var currentPosition by remember { mutableStateOf(Offset.Zero) }
    val currentState = LocalDragTargetInfo.current

    Box(
        modifier = modifier
            .onGloballyPositioned { layoutCoordinates ->
                val rect = layoutCoordinates.boundsInRoot()
                currentPosition = rect.center
            }
            .pointerInput(Unit) {
                detectDragGesturesAfterLongPress(onDragStart = {
                    currentState.clipData = dataToDrop
                    currentState.isDragging = true
                    currentState.dragPosition = currentPosition
                    currentState.draggableComposable = content
                }, onDrag = { change, dragAmount ->
                        change.consumeAllChanges()
                        currentState.dragOffset += Offset(dragAmount.x, dragAmount.y)
                    }, onDragEnd = {
                        currentState.isDragging = false
                        currentState.dragOffset = Offset.Zero
                    }, onDragCancel = {
                        currentState.dragOffset = Offset.Zero
                        currentState.isDragging = false
                    })
            }
    ) {
        content()
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    MyApplicationTheme {
        Greeting("Android")
    }
}
