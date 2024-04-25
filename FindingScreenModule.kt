package com.sryang.findinglinkmodules.di.finding_di

import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.example.cardinfo.RestaurantCardPage
import com.example.screen_finding.ui.FindScreen
import com.example.screen_finding.viewmodel.FindingViewModel
import com.example.screen_map.compose.CurrentLocationScreen
import com.example.screen_map.compose.MapScreen
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.rememberCameraPositionState
import com.sryang.screen_filter.ui.FilterScreen
import com.sryang.screen_filter.ui.FilterViewModel
import kotlinx.coroutines.launch

@Composable
fun Finding(
    findingViewModel: FindingViewModel = hiltViewModel(),
    filterViewModel: FilterViewModel = hiltViewModel(),
    navController: NavHostController
) {
    val uiState by findingViewModel.uiState.collectAsState()
    val filterUiState by filterViewModel.uiState.collectAsState()
    val cameraPositionState = rememberCameraPositionState()
    val coroutineScope = rememberCoroutineScope()
    var isVisible by remember { mutableStateOf(true) }
    var myLocation: LatLng? by remember { mutableStateOf(null) }

    FindScreen(
        errorMessage = uiState.errorMessage,
        consumeErrorMessage = { findingViewModel.clearErrorMessage() },
        restaurantCardPage = {
            Log.d("__sryang", "selectedRestaurant : ${uiState.selectedRestaurant}")
            RestaurantCardPage(
                restaurants = uiState.restaurants?.map { it.toRestaurantCardData() },
                restaurantImageServerUrl = "http://sarang628.iptime.org:89/restaurant_images/",
                onChangePage = { page -> findingViewModel.selectPage(page) },
                onClickCard = { navController.navigate("restaurant/$it") },
                focusedRestaurant = uiState.selectedRestaurant?.toRestaurantCardData(),
                visible = isVisible
            )
        },
        mapScreen = {
            Box {
                MapScreen(
                    onMark = {
                        isVisible = true
                        findingViewModel.selectMarker(it)
                    },
                    cameraPositionState = cameraPositionState,
                    list = uiState.restaurants?.map { it.toMarkData() },
                    selectedMarkerData = uiState.selectedRestaurant?.toMarkData(),
                    onMapClick = {
                        isVisible = !isVisible
                        Log.d("Finding", "onMapClick $isVisible")
                    },
                    myLocation = myLocation,
                    boundary = filterUiState.distance.toBoundary()
                )
            }
        },
        onZoomIn = {
            coroutineScope.launch {
                cameraPositionState.animate(CameraUpdateFactory.zoomIn(), 300)
            }
        },
        onZoomOut = {
            coroutineScope.launch {
                cameraPositionState.animate(CameraUpdateFactory.zoomOut(), 300)
            }
        },
        filter = {
            FilterScreen(filterViewModel = filterViewModel,
                onFilter = {
                    val filter = it.toFilter()
                    filter.lat = myLocation?.latitude
                    filter.lon = myLocation?.longitude
                    findingViewModel.filter(filter)
                },
                visible = isVisible,
                onThisArea = {
                    findingViewModel.findThisArea(it.toFilter())
                },
                onNation = {
                    coroutineScope.launch {
                        cameraPositionState.animate(
                            CameraUpdateFactory.newLatLngZoom(
                                LatLng(
                                    it.latitude,
                                    it.longitude
                                ), it.zoom
                            ),
                            1000
                        )
                    }
                })
        },
        myLocation = {
            CurrentLocationScreen(onLocation = {
                findingViewModel.setCurrentLocation(it)
                coroutineScope.launch {
                    cameraPositionState.animate(
                        update = CameraUpdateFactory.newLatLngZoom(
                            LatLng(
                                it.latitude,
                                it.longitude
                            ),
                            if (cameraPositionState.position.zoom <= 10.0f) 17.0f else cameraPositionState.position.zoom
                        ),
                        if (cameraPositionState.position.zoom <= 10.0f) 2000 else 300
                    )
                }
                myLocation = LatLng(it.latitude, it.longitude)
            }
            )
        }
    )
}