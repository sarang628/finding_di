package com.sarang.torang.di.finding_di

import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Icon
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.screen_finding.ui.FindScreen
import com.example.screen_finding.viewmodel.FindViewModel
import com.example.screen_map.compose.MapScreenForFinding
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.CameraPositionState
import com.google.maps.android.compose.rememberCameraPositionState
import com.sarang.torang.RootNavController
import com.sarang.torang.compose.FilterImageLoader
import com.sarang.torang.compose.LocalFilterImageLoader
import com.sarang.torang.compose.cardinfo.CardInfoImageLoader
import com.sarang.torang.compose.cardinfo.LocalCardInfoImageLoader
import com.sarang.torang.compose.cardinfo.RestaurantCardPage
import com.sarang.torang.di.image.provideTorangAsyncImage
import com.sarang.torang.di.map_di.CurrentLocationScreen
import com.sarang.torang.ui.FilterScreen
import com.sarang.torang.ui.FilterViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Composable
fun Finding(findViewModel: FindViewModel = hiltViewModel(), filterViewModel: FilterViewModel = hiltViewModel(), navController: RootNavController) {
    val uiState = findViewModel.uiState
    val cameraPositionState = rememberCameraPositionState()
    val coroutineScope = rememberCoroutineScope()
    var isVisible by remember { mutableStateOf(true) }
    var myLocation: LatLng? by remember { mutableStateOf(null) }
    val snackBarHostState = remember { SnackbarHostState() }
    val tag = "__Finding"

    LaunchedEffect(key1 = uiState.errorMessage, block = { // error snack bar
        uiState.errorMessage?.let {
            snackBarHostState.showSnackbar(it, duration = SnackbarDuration.Short)
            findViewModel.clearErrorMessage()
        }
    })

    Box {
        FindScreen(
            restaurantCardPage = {
                Column {
                    CompositionLocalProvider(LocalCardInfoImageLoader provides customImageLoader) {
                        RestaurantCardPage(onClickCard = { navController.restaurant(it) }, visible = isVisible,
                            onPosition = { lat,lon-> Log.i(tag, "onPosition ${lat}, ${lon}")
                                moveCamera(coroutineScope, cameraPositionState, lat, lon, 17f) } )
                    }
                }
            },
            mapScreen = {
                MapScreenForFinding(cameraPositionState = cameraPositionState, onMapClick = { isVisible = !isVisible; Log.d("Finding", "onMapClick $isVisible") }, myLocation = myLocation)
            },
            onZoomIn = { zoomIn(coroutineScope, cameraPositionState) },
            onZoomOut = { zoomOut(coroutineScope, cameraPositionState) },
            filter = {
                CompositionLocalProvider(LocalFilterImageLoader provides filterImageLoader) {
                    FilterScreen(filterViewModel = filterViewModel,
                        visible = isVisible,
                        onNation = { moveCamera(coroutineScope, cameraPositionState, it.latitude, it.longitude, it.zoom) },
                        onCity = { moveCamera(coroutineScope, cameraPositionState, it.latitude, it.longitude, it.zoom)} )
                }
            },
            myLocation = {
                CurrentLocationScreen(onLocation = {
                    findViewModel.setCurrentLocation(it)
                    coroutineScope.launch { cameraPositionState.animate(update = CameraUpdateFactory.newLatLngZoom(LatLng(it.latitude, it.longitude), if (cameraPositionState.position.zoom <= 10.0f) 17.0f else cameraPositionState.position.zoom), if (cameraPositionState.position.zoom <= 10.0f) 2000 else 300) }
                    myLocation = LatLng(it.latitude, it.longitude)
                }, contents = {
                    AssistChip(it, label = { Icon(Icons.Default.LocationOn, "") })
                })
            },
            buttonBottomPadding = 0.dp
        )
    }
}

fun zoomIn(coroutineScope: CoroutineScope, cameraPositionState: CameraPositionState){
    coroutineScope.launch { cameraPositionState.animate(CameraUpdateFactory.zoomIn(), 300) }
}

fun zoomOut(coroutineScope: CoroutineScope, cameraPositionState: CameraPositionState){
    coroutineScope.launch { cameraPositionState.animate(CameraUpdateFactory.zoomOut(), 300) }
}

fun moveCamera(coroutineScope: CoroutineScope,cameraPositionState : CameraPositionState, latitude : Double, longitude : Double, zoom : Float){
    coroutineScope.launch {
        cameraPositionState.animate(CameraUpdateFactory.newLatLngZoom(LatLng(latitude, longitude), zoom), 1000)
    }
}
fun moveCamera1(coroutineScope: CoroutineScope,cameraPositionState : CameraPositionState, lat : Double, lon : Double){
    coroutineScope.launch {
        cameraPositionState.animate(CameraUpdateFactory.newLatLng(LatLng(lat, lon)), 300)
    }
}

val customImageLoader: CardInfoImageLoader = { modifier, url, width, height, scale ->
    // 여기서 실제 이미지 로딩 구현 예시
    provideTorangAsyncImage().invoke(modifier, url, width, height, scale)
}

val filterImageLoader: FilterImageLoader = { modifier, url, width, height, scale ->
    // 여기서 실제 이미지 로딩 구현 예시
    provideTorangAsyncImage().invoke(modifier, url, width, height, scale)
}