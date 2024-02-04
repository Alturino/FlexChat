package com.onirutla.flexchat.core.webrtc

import org.webrtc.SdpObserver
import org.webrtc.SessionDescription
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

suspend inline fun setSessionDescription(
    crossinline call: (SdpObserver) -> Unit,
): Unit = suspendCoroutine {
    val observer = object : SdpObserver {
        override fun onCreateSuccess(p0: SessionDescription?) = Unit
        override fun onCreateFailure(p0: String?) = Unit
        override fun onSetFailure(p0: String?) = it.resumeWithException(RuntimeException(p0))
        override fun onSetSuccess() = it.resume(Unit)
    }
    call(observer)
}

suspend inline fun getSessionDescription(
    crossinline call: (SdpObserver) -> Unit,
): SessionDescription = suspendCoroutine {
    val observer = object : SdpObserver {
        override fun onCreateSuccess(p0: SessionDescription?) {
            if (p0 != null) {
                it.resume(p0)
            } else {
                it.resumeWithException(RuntimeException("SessionDescription is null"))
            }
        }

        override fun onCreateFailure(p0: String?) = it.resumeWithException(RuntimeException(p0))
        override fun onSetFailure(p0: String?) = Unit
        override fun onSetSuccess() = Unit
    }
    call(observer)
}
