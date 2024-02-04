package com.onirutla.flexchat.core.webrtc

import org.webrtc.AddIceObserver
import org.webrtc.IceCandidate
import org.webrtc.PeerConnection
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

suspend fun PeerConnection.addRtcIceCandidate(
    iceCandidate: IceCandidate,
): Unit = suspendCoroutine {
    addIceCandidate(
        iceCandidate,
        object : AddIceObserver {
            override fun onAddSuccess() = it.resume(Unit)
            override fun onAddFailure(p0: String?) = it.resumeWithException(RuntimeException(p0))
        }
    )
}
