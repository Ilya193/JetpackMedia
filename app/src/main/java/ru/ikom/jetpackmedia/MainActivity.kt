package ru.ikom.jetpackmedia

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import androidx.media3.ui.compose.PlayerSurface
import ru.ikom.jetpackmedia.ui.theme.JetpackMediaTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        val composeView = findViewById<ComposeView>(R.id.compose_view)

        composeView.apply {
            setContent {
                ExoPlayerExample()
            }
        }
    }
}

sealed interface Content {
    data object ContentA : Content
    data object ContentB : Content
}

@Composable
fun ExoPlayerExample() {
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        val content = remember { mutableStateOf<Content>(Content.ContentA) }

        when (content.value) {
            Content.ContentA -> ContentA(
                onOpenB = { content.value = Content.ContentB }
            )
            Content.ContentB -> ExoPlayerContent(
                onBack = { content.value = Content.ContentA }
            )
        }
    }
}

@Composable
fun ContentA(
    onOpenB: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Red)
            .clickable(onClick = onOpenB)
    ) {
        Text(
            modifier = Modifier
                .align(Alignment.Center),
            text = "ContentA"
        )
    }
}

@Composable
fun ExoPlayerContent(
    onBack: () -> Unit,
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    val context = LocalContext.current

    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            val url = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4"
            val mediaItem = MediaItem.fromUri(url)
            setMediaItem(mediaItem)
            prepare()
            playWhenReady = true
        }
    }

    DisposableEffect(Unit) {
        val lifecycleObserver = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_STOP -> exoPlayer.stop()
                else -> {}
            }
        }

        lifecycleOwner.lifecycle.addObserver(lifecycleObserver)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(lifecycleObserver)
            exoPlayer.release()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        PlayerSurface(
            modifier = Modifier.fillMaxSize(),
            player = exoPlayer,
        )

        // or AndroidView
        /*AndroidView(
            modifier = Modifier
                .fillMaxSize(),
            factory = {
                PlayerView(it).apply {
                    player = exoPlayer
                    useController = false
                }
            },
        )*/

        Box(
            modifier = Modifier
                .testTag("testbox")
                .fillMaxSize(0.5f)
                .background(Color.Green.copy(alpha = 0.2f))
                .clickable(
                    interactionSource = null,
                    indication = null,
                    onClick = onBack
                )
        )

        /*Button(
            modifier = Modifier
                .testTag("testbutton")
                .align(Alignment.CenterStart),
            onClick = onBack,
        ) {
            Text("go back")
        }*/
    }
}