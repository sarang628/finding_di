package com.sarang.torang.di.finding_di

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.CameraPositionState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

fun zoomIn(coroutineScope: CoroutineScope,
           cameraPositionState: CameraPositionState){
    coroutineScope.launch {
        cameraPositionState.animate(CameraUpdateFactory.zoomIn(), 300)
    }
}
fun zoomOut(coroutineScope: CoroutineScope,
            cameraPositionState: CameraPositionState){
    coroutineScope.launch {
        cameraPositionState.animate(CameraUpdateFactory.zoomOut(), 300)
    }
}
fun moveCamera(coroutineScope       : CoroutineScope,
               cameraPositionState  : CameraPositionState,
               latitude             : Double = 0.0,
               longitude            : Double = 0.0,
               zoom                 : Float = 0f){
    coroutineScope.launch {
        cameraPositionState.animate(update = CameraUpdateFactory.newLatLngZoom(LatLng(latitude, longitude), zoom),
                                    durationMs = 1000) }
}
fun moveCamera1(coroutineScope: CoroutineScope,
                cameraPositionState : CameraPositionState,
                lat : Double,
                lon : Double){
    coroutineScope.launch {
        cameraPositionState.animate(update = CameraUpdateFactory.newLatLng(LatLng(lat, lon)),
                                    durationMs = 300)
    }
}