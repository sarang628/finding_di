package com.sarang.torang.di.finding_di

import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.screen_finding.ui.FindScreen
import com.example.screen_finding.viewmodel.FindingViewModel
import com.example.screen_map.compose.CurrentLocationScreen
import com.example.screen_map.compose.MapScreenForFinding
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.rememberCameraPositionState
import com.sarang.torang.RootNavController
import com.sarang.torang.compose.cardinfo.CardInfoImageLoader
import com.sarang.torang.compose.cardinfo.LocalCardInfoImageLoader
import com.sarang.torang.compose.cardinfo.RestaurantCardPage1
import com.sarang.torang.di.image.provideTorangAsyncImage
import com.sarang.torang.ui.FilterScreen
import com.sarang.torang.ui.FilterViewModel
import kotlinx.coroutines.launch

@Composable
fun Finding(findingViewModel: FindingViewModel = hiltViewModel(), filterViewModel: FilterViewModel = hiltViewModel(), navController: RootNavController) {
    val uiState = findingViewModel.uiState
    val filterUiState = filterViewModel.uiState
    val cameraPositionState = rememberCameraPositionState()
    val coroutineScope = rememberCoroutineScope()
    var isVisible by remember { mutableStateOf(true) }
    var myLocation: LatLng? by remember { mutableStateOf(null) }
    val snackBarHostState = remember { SnackbarHostState() }

    LaunchedEffect(key1 = uiState.errorMessage, block = {
        uiState.errorMessage?.let {
            snackBarHostState.showSnackbar(it, duration = SnackbarDuration.Short)
            findingViewModel.clearErrorMessage()
        }
    })

    Box {
        FindScreen(
            restaurantCardPage = {
                CompositionLocalProvider(LocalCardInfoImageLoader provides customImageLoader) {
                    RestaurantCardPage1(
                        restaurants = uiState.restaurants.map { it.toRestaurantCardData() },
                        onChangePage = { page -> findingViewModel.selectPage(page) },
                        onClickCard = { navController.restaurant(it) },
                        focusedRestaurant = uiState.selectedRestaurant?.toRestaurantCardData(),
                        visible = isVisible,
                        onPosition = { findingViewModel.findPositionByRestaurantId(it)?.let { coroutineScope.launch { cameraPositionState.animate(CameraUpdateFactory.newLatLng(LatLng(it.lat, it.lon)), 300) } } },)
                }
            },
            mapScreen = {
                MapScreenForFinding(
                    onMark = { isVisible = true; findingViewModel.selectMarker(it) },
                    cameraPositionState = cameraPositionState,
                    selectedMarkerData = uiState.selectedRestaurant?.toMarkData(),
                    onMapClick = { isVisible = !isVisible; Log.d("Finding", "onMapClick $isVisible") },
                    myLocation = myLocation,
                    boundary = filterUiState.distance.toBoundary())
            },
            onZoomIn = { coroutineScope.launch { cameraPositionState.animate(CameraUpdateFactory.zoomIn(), 300) } },
            onZoomOut = { coroutineScope.launch { cameraPositionState.animate(CameraUpdateFactory.zoomOut(), 300) } },
            filter = {
                FilterScreen(filterViewModel = filterViewModel,
                    onFilter = { val filter = it.toFilter(); filter.lat = myLocation?.latitude; filter.lon = myLocation?.longitude; findingViewModel.filter(filter) },
                    visible = isVisible,
                    onThisArea = { findingViewModel.findThisArea(it.toFilter()) },
                    onNation = { coroutineScope.launch { cameraPositionState.animate(CameraUpdateFactory.newLatLngZoom(LatLng(it.latitude, it.longitude), it.zoom), 1000) } },
                    onCity = { coroutineScope.launch { cameraPositionState.animate(CameraUpdateFactory.newLatLngZoom(LatLng(it.latitude, it.longitude), it.zoom), 1000) } },
                    image = provideTorangAsyncImage(),
                    onSearch = { findingViewModel.onSearch(it.toFilter()) })
            },
            myLocation = {
                CurrentLocationScreen(onLocation = {
                    findingViewModel.setCurrentLocation(it)
                    coroutineScope.launch { cameraPositionState.animate(update = CameraUpdateFactory.newLatLngZoom(LatLng(it.latitude, it.longitude), if (cameraPositionState.position.zoom <= 10.0f) 17.0f else cameraPositionState.position.zoom), if (cameraPositionState.position.zoom <= 10.0f) 2000 else 300) }
                    myLocation = LatLng(it.latitude, it.longitude)
                })
            }
        )
    }
}

val customImageLoader: CardInfoImageLoader = { modifier, url, width, height, scale ->
    // 여기서 실제 이미지 로딩 구현 예시
    provideTorangAsyncImage().invoke(modifier, url, width, height, scale)
}