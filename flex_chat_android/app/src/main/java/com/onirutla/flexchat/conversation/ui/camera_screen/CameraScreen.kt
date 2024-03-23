/*
 * Copyright 2024 Ricky Alturino
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.onirutla.flexchat.ui.screens.camera_screen

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCapture.FLASH_MODE_AUTO
import androidx.camera.core.ImageCapture.FLASH_MODE_OFF
import androidx.camera.core.ImageCapture.FLASH_MODE_ON
import androidx.camera.core.ImageCapture.OnImageSavedCallback
import androidx.camera.core.ImageCapture.OutputFileOptions
import androidx.camera.core.ImageCaptureException
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Camera
import androidx.compose.material.icons.rounded.FlashAuto
import androidx.compose.material.icons.rounded.FlashOff
import androidx.compose.material.icons.rounded.FlashOn
import androidx.compose.material.icons.rounded.FlipCameraAndroid
import androidx.compose.material.icons.rounded.Photo
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.onirutla.flexchat.R
import com.onirutla.flexchat.ui.components.OnImageIconButton
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import timber.log.Timber
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

@Composable
fun CameraScreen(
    modifier: Modifier = Modifier,
    onNavigateUp: () -> Unit,
    onNavigateToEditPhotoScreen: (savedImage: Uri) -> Unit,
) {

    val coroutineScope = rememberCoroutineScope()

    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var cameraSelector by remember { mutableStateOf(CameraSelector.DEFAULT_BACK_CAMERA) }
    var flashMode by remember { mutableIntStateOf(FLASH_MODE_ON) }

    val cameraController = remember {
        LifecycleCameraController(context).apply {
            bindToLifecycle(lifecycleOwner)
            this.cameraSelector = cameraSelector
        }
    }

    BackHandler {
        onNavigateUp()
    }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = PickVisualMedia(),
        onResult = {
            it?.let(onNavigateToEditPhotoScreen::invoke)
        }
    )

    Scaffold(modifier = modifier.fillMaxSize()) { contentPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(contentPadding),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Max),
                horizontalArrangement = Arrangement.End,
            ) {
                when (flashMode) {
                    FLASH_MODE_OFF -> {
                        OnImageIconButton(
                            onClick = {
                                flashMode = FLASH_MODE_ON
                                cameraController.imageCaptureFlashMode = flashMode
                            }
                        ) {
                            Icon(imageVector = Icons.Rounded.FlashOff, contentDescription = null)
                        }
                    }

                    FLASH_MODE_ON -> {
                        OnImageIconButton(
                            onClick = {
                                flashMode = FLASH_MODE_AUTO
                                cameraController.imageCaptureFlashMode = flashMode
                            }
                        ) {
                            Icon(imageVector = Icons.Rounded.FlashOn, contentDescription = null)
                        }
                    }

                    FLASH_MODE_AUTO -> {
                        OnImageIconButton(
                            onClick = {
                                flashMode = FLASH_MODE_OFF
                                cameraController.imageCaptureFlashMode = flashMode
                            }
                        ) {
                            Icon(imageVector = Icons.Rounded.FlashAuto, contentDescription = null)
                        }
                    }
                }
            }
            CameraPreview(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(350.dp),
                cameraController = cameraController
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Max),
                horizontalArrangement = Arrangement.SpaceAround,
            ) {
                OnImageIconButton(
                    onClick = {
                        photoPickerLauncher.launch(PickVisualMediaRequest(PickVisualMedia.ImageOnly))
                    }
                ) {
                    Icon(imageVector = Icons.Rounded.Photo, contentDescription = null)
                }
                OnImageIconButton(
                    modifier = Modifier.wrapContentSize(),
                    onClick = {
                        coroutineScope.launch {
                            val uri = context.takePicture(cameraController = cameraController)
                            onNavigateToEditPhotoScreen(uri)
                        }
                    }
                ) {
                    Icon(
                        modifier = Modifier.size(64.dp),
                        imageVector = Icons.Rounded.Camera,
                        contentDescription = null
                    )
                }
                OnImageIconButton(
                    modifier = Modifier.wrapContentSize(),
                    onClick = {
                        cameraSelector =
                            if (cameraSelector == CameraSelector.DEFAULT_FRONT_CAMERA) {
                                CameraSelector.DEFAULT_BACK_CAMERA
                            } else {
                                CameraSelector.DEFAULT_FRONT_CAMERA
                            }
                        cameraController.cameraSelector = cameraSelector
                    }
                ) {
                    Icon(imageVector = Icons.Rounded.FlipCameraAndroid, contentDescription = null)
                }
            }
        }
    }
}

@Composable
fun CameraPreview(
    modifier: Modifier = Modifier,
    cameraController: LifecycleCameraController,
) {
    AndroidView(
        modifier = modifier,
        factory = {
            val previewView = PreviewView(it).apply {
                scaleType = PreviewView.ScaleType.FILL_CENTER
                implementationMode = PreviewView.ImplementationMode.COMPATIBLE
                controller = cameraController
            }
            previewView
        },
    )
}

private suspend fun Context.takePicture(
    cameraController: LifecycleCameraController,
): Uri = suspendCancellableCoroutine { continuation ->

    val dateAdded = LocalDateTime.now()
    val dateFormat = DateTimeFormatter.ISO_DATE.format(dateAdded)
    val fileNameFormat = DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(dateAdded)
    val photoExtension = ".jpg"

    val metadata = ImageCapture.Metadata()
    val contentValues = ContentValues().apply {
        put(MediaStore.Images.Media.DATE_ADDED, dateFormat)
        put(MediaStore.Images.Media.DATE_MODIFIED, dateFormat)
        put(MediaStore.Images.Media.DATE_TAKEN, dateFormat)
        put(MediaStore.Images.Media.DISPLAY_NAME, "$fileNameFormat$photoExtension")
        put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
        put(
            MediaStore.Images.Media.RELATIVE_PATH,
            Environment.DIRECTORY_PICTURES + File.separator + getString(R.string.app_name)
        )
        put(MediaStore.Images.Media.TITLE, "$fileNameFormat$photoExtension")
        put(MediaStore.Images.Media.YEAR, dateAdded.year)
    }

    val outputFileOptions = OutputFileOptions.Builder(
        contentResolver,
        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
        contentValues
    ).setMetadata(metadata)
        .build()

    cameraController.takePicture(
        outputFileOptions,
        ContextCompat.getMainExecutor(this),
        object : OnImageSavedCallback {
            override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                val savedUri = outputFileResults.savedUri
                Timber.d("Success to save image with Uri: $savedUri")
                if (savedUri != null) {
                    continuation.resume(savedUri)
                } else {
                    continuation.resumeWithException(NullPointerException("Saved uri is null"))
                }
            }

            override fun onError(exception: ImageCaptureException) {
                Timber.d("Failed to save image with exception: $exception")
                continuation.resumeWithException(exception)
            }
        }
    )
}
