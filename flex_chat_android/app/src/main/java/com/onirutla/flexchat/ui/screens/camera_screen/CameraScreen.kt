package com.onirutla.flexchat.ui.screens.camera_screen

import android.net.Uri
import android.os.Environment
import android.view.ViewGroup.LayoutParams
import android.webkit.MimeTypeMap
import androidx.activity.compose.BackHandler
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Camera
import androidx.compose.material.icons.rounded.FlipCameraAndroid
import androidx.compose.material.icons.rounded.Photo
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.net.toFile
import com.onirutla.flexchat.R
import com.onirutla.flexchat.ui.theme.FlexChatTheme
import com.onirutla.flexchat.ui.util.Constants.FILE_PHOTO_EXTENSION
import timber.log.Timber
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.concurrent.Executors

@Composable
fun CameraScreen(modifier: Modifier = Modifier, onNavigateUp: () -> Unit) {

    val context = LocalContext.current
    val appContext = context.applicationContext
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraController = remember { LifecycleCameraController(context) }
    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }

    cameraController.bindToLifecycle(lifecycleOwner)

    BackHandler {
        onNavigateUp()
    }

    Scaffold(modifier = modifier.fillMaxSize()) { contentPadding ->
        Box(modifier = modifier.fillMaxSize()) {
            AndroidView(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(350.dp)
                    .align(Alignment.Center)
                    .padding(contentPadding),
                factory = { context ->
                    PreviewView(context).apply {
                        layoutParams = LayoutParams(
                            LayoutParams.MATCH_PARENT,
                            LayoutParams.MATCH_PARENT
                        )
                        scaleType = PreviewView.ScaleType.FILL_CENTER
                        controller = cameraController
                        implementationMode = PreviewView.ImplementationMode.COMPATIBLE
                    }
                },
                update = {

                }
            )
            Row(
                modifier = Modifier
                    .padding(start = 16.dp, end = 16.dp, bottom = 32.dp)
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                val outputDir = File(
                    appContext.getExternalFilesDir(Environment.DIRECTORY_PICTURES),
                    stringResource(id = R.string.app_name)
                ).apply { mkdirs() }

                val localDateTime = LocalDateTime.now()
                val filenameFormat = DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(localDateTime)
                val photoFile = File(
                    outputDir,
                    "$filenameFormat$FILE_PHOTO_EXTENSION"
                )

                val outputFileOptions = ImageCapture.OutputFileOptions
                    .Builder(photoFile)
                    .setMetadata(ImageCapture.Metadata())
                    .build()

                IconButton(
                    modifier = Modifier.wrapContentSize(),
                    onClick = {
                        cameraController.takePicture(
                            outputFileOptions,
                            cameraExecutor,
                            object : ImageCapture.OnImageSavedCallback {
                                override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                                    Timber.d("Image captured with uri: ${outputFileResults.savedUri}")
                                    val savedUri =
                                        outputFileResults.savedUri ?: Uri.fromFile(photoFile)

                                    // TODO: Implement saving picture to gallery ref: https://github.com/IOH-C22-HY-4/MobileDevelopment/blob/main/app/src/main/java/com/ioh_c22_h2_4/hy_ponics/CameraFragment.kt
                                    val mimeType = MimeTypeMap.getSingleton()
                                        .getMimeTypeFromExtension(savedUri.toFile().extension)

                                }

                                override fun onError(exception: ImageCaptureException) {
                                    TODO("Not yet implemented")
                                }

                            }
                        )
                    }
                ) {
                    Icon(
                        modifier = Modifier.size(24.dp),
                        imageVector = Icons.Rounded.Photo,
                        contentDescription = null
                    )
                }
                IconButton(modifier = Modifier.wrapContentSize(), onClick = { /*TODO*/ }) {
                    Icon(
                        modifier = Modifier.size(64.dp),
                        imageVector = Icons.Rounded.Camera,
                        contentDescription = null
                    )
                }
                IconButton(modifier = Modifier.wrapContentSize(), onClick = {
                    if (cameraController.cameraSelector == CameraSelector.DEFAULT_BACK_CAMERA) {
                        cameraController.cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA
                    } else if (cameraController.cameraSelector == CameraSelector.DEFAULT_FRONT_CAMERA) {
                        cameraController.cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
                    }
                }) {
                    Icon(
                        modifier = Modifier.size(24.dp),
                        imageVector = Icons.Rounded.FlipCameraAndroid,
                        contentDescription = null
                    )
                }
            }
        }
    }
}

@Preview
@Composable
private fun CameraScreenPreview() {
    FlexChatTheme {
        CameraScreen(onNavigateUp = {})
    }
}
