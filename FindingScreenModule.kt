package com.sarang.torang.di.finding_di

import android.Manifest
import android.content.Context
import android.location.Location
import android.util.Log
import androidx.annotation.RequiresPermission
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material3.BottomSheetScaffoldState
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.SheetValue
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.screen_finding.ui.FindScreen
import com.example.screen_finding.uistate.FindingUiState
import com.example.screen_finding.viewmodel.FindViewModel
import com.example.screen_map.compose.MapScreenForFinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.maps.android.compose.CameraPositionState
import com.google.maps.android.compose.rememberCameraPositionState
import com.sarang.torang.LocalRestaurantItemImageLoader
import com.sarang.torang.RestaurantListBottomSheet
import com.sarang.torang.RestaurantListBottomSheetViewModel
import com.sarang.torang.RestaurantListBottomSheet_
import com.sarang.torang.RootNavController
import com.sarang.torang.compose.Filter1
import com.sarang.torang.compose.FilterDrawer
import com.sarang.torang.compose.FilterDrawerScreen
import com.sarang.torang.compose.FilterImageLoader
import com.sarang.torang.compose.FilterScreen1
import com.sarang.torang.compose.FilterUiState
import com.sarang.torang.compose.FilterViewModel
import com.sarang.torang.compose.LocalFilterImageLoader
import com.sarang.torang.compose.cardinfo.CardInfoImageLoader
import com.sarang.torang.compose.cardinfo.CardInfoViewModel
import com.sarang.torang.compose.cardinfo.LocalCardInfoImageLoader
import com.sarang.torang.compose.cardinfo.RestaurantCardPage
import com.sarang.torang.compose.cardinfo.RestaurantCardPage1
import com.sarang.torang.compose.cardinfo.RestaurantCardUIState
import com.sarang.torang.data.City
import com.sarang.torang.data.Nation
import com.sarang.torang.di.image.provideTorangAsyncImage
import com.sarang.torang.di.restaurant_list_bottom_sheet_di.CustomRestaurantItemImageLoader
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@OptIn(ExperimentalMaterial3Api::class)
@RequiresPermission(anyOf = [Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION])
@Composable
fun Finding(
    findViewModel                       : FindViewModel                         = hiltViewModel(),
    filterViewModel                     : FilterViewModel                       = hiltViewModel(),
    restaurantListBottomSheetViewModel  : RestaurantListBottomSheetViewModel    = hiltViewModel(),
    cardInfoViewModel                   : CardInfoViewModel                     = hiltViewModel(),
    navController                       : RootNavController                     = RootNavController(),
    isGrantedPermission                 : Boolean                               = false,
    onRequestPermission                 : () -> Unit                            = {}
) {
    val tag                 : String                        = "__Finding"
    val uiState             : FindingUiState                = findViewModel.uiState
    val filterUiState       : FilterUiState                 = filterViewModel.uiState
    val cardUiState         : List<RestaurantCardUIState>   = cardInfoViewModel.cardInfos
    val coroutineScope      : CoroutineScope                = rememberCoroutineScope()
    Finding1(
        uiState = uiState,
        filterUiState = filterUiState,
        onClearErrorMessage = { findViewModel.clearErrorMessage() },
        isGrantedPermission = isGrantedPermission,
        onRequestPermission = onRequestPermission,
        navController =  navController,
        onCurrentLocation = { findViewModel.setCurrentLocation(it) },
        onFoodType =        { filterViewModel.setType("FoodType") },
        onPrice =           { filterViewModel.setType("Price") },
        onDistance =        { filterViewModel.setType("Distance") },
        onRating =          { filterViewModel.setType("Rating") },
        onFilterFoodType =  { filterViewModel.setFoodType(it) },
        onFilterPrice =     { filterViewModel.setPrice(it) },
        onFilterDistance =  { filterViewModel.setDistance(it) },
        onFilterRating =    { filterViewModel.setRating(it) },
        onNation =          { filterViewModel.onNation() },
        onThisArea =        { filterViewModel.onThisArea() },
        onFilter =          { filterViewModel.onFilter()},
        onFilterCity =      { filterViewModel.onCity(it)},
        onFilterNation =    { filterViewModel.onNation(it)},
        onSearch =          { /*onSearch.invoke(uiState)*/ },
        onQueryChange =     { filterViewModel.setQuery(it) },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
private fun Finding1(
    uiState             : FindingUiState    = FindingUiState(),
    filterUiState          : FilterUiState     = FilterUiState(),
    cardUiState : List<RestaurantCardUIState> = listOf(),
    navController       : RootNavController     = RootNavController(),
    isGrantedPermission : Boolean               = false,
    visible          : Boolean           = false,
    onClearErrorMessage : ()->Unit          = {},
    onRequestPermission : () -> Unit            = {},
    onCurrentLocation   : (Location)->Unit  = {},
    topPadding       : Dp = 0.dp,
    onFoodType       : () -> Unit        = {},
    onPrice          : () -> Unit        = {},
    onRating         : () -> Unit        = {},
    onDistance       : () -> Unit        = {},
    onNation         : () -> Unit        = {},
    onThisArea       : () -> Unit        = {},
    onFilter         : () -> Unit        = {},
    onFilterFoodType : (String) -> Unit  = {},
    onFilterPrice    : (String) -> Unit  = {},
    onFilterRating   : (String) -> Unit  = {},
    onFilterDistance : (String) -> Unit  = {},
    onFilterCity     : (City) -> Unit    = {},
    onFilterNation   : (Nation) -> Unit  = {},
    onSearch         : () -> Unit        = {},
    onQueryChange    : (String) -> Unit  = {},
){
   val tag                 : String                        = "__Finding"
    val cameraPositionState : CameraPositionState           = rememberCameraPositionState()
    val coroutineScope      : CoroutineScope                = rememberCoroutineScope()
    val snackBarHostState   : SnackbarHostState             = remember { SnackbarHostState() }
    val context             : Context                       = LocalContext.current
    val usePreciseLocation  : Boolean                       = true
    val locationClient      : FusedLocationProviderClient   = remember { LocationServices.getFusedLocationProviderClient(context) }
    val bottomSheetState    : BottomSheetScaffoldState      = rememberBottomSheetScaffoldState()
    val drawerState         : DrawerState                   = rememberDrawerState(initialValue = DrawerValue.Closed)
    var isVisible           : Boolean                       by remember { mutableStateOf(true) }
    var myLocation          : LatLng?                       by remember { mutableStateOf(null) }
    var cardPagerHeight     : Int                           by remember { mutableIntStateOf(0) }
    val cardPagerHeightDp   : Dp                            = with(LocalDensity.current){ cardPagerHeight.toDp() }

    LaunchedEffect(key1 = uiState.errorMessage, block = { // error snack bar
        uiState.errorMessage?.let {
            snackBarHostState.showSnackbar(it, duration = SnackbarDuration.Short)
            onClearErrorMessage.invoke()
        }
    })

    CompositionLocalProvider(LocalRestaurantItemImageLoader provides CustomRestaurantItemImageLoader,
        LocalFilterImageLoader provides filterImageLoader){
        val drawerState : DrawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
        Box {
            RestaurantListBottomSheet_(
                modifier = Modifier,
                sheetPeekHeight = 0.dp,
                scaffoldState = bottomSheetState,
                onClickRestaurantName = { coroutineScope.launch { bottomSheetState.bottomSheetState.partialExpand() } })
            {
                FilterDrawer(
                    uiState             = filterUiState,
                    drawerState         = drawerState,
                    onFilterFoodType    = onFilterFoodType,
                    onFilterPrice       = onFilterPrice,
                    onFilterDistance    = onFilterDistance,
                    onFilterRating      = onFilterRating,
                    onFilterCity        = { onFilterCity(it); onFilterCity.invoke(it) },
                    onFilterNation      = { onFilterNation(it); onFilterNation.invoke(it) },
                    onQueryChange       = { onQueryChange(it) },
                    content             = {
                        FindScreen(
                            restaurantCardPage = { CompositionLocalProvider(LocalCardInfoImageLoader provides customImageLoader) {
                                RestaurantCardPage1(
                                    restaurants = cardUiState,
                                    onClickCard = { navController.restaurant(it) },
                                    visible = isVisible,
                                    onPosition = { lat,lon-> Log.i(tag, "onPosition ${lat}, ${lon}"); moveCamera(coroutineScope, cameraPositionState, lat, lon, 17f) }
                                )
                            }
                            },
                            mapScreen = {
                                //MapScreenForFinding(cameraPositionState = cameraPositionState, onMapClick = { isVisible = !isVisible; Log.d("Finding", "onMapClick $isVisible") }, myLocation = myLocation, logoBottomPadding = cardPagerHeightDp)
                                        },
                            onZoomIn = { zoomIn(coroutineScope, cameraPositionState) },
                            onZoomOut = { zoomOut(coroutineScope, cameraPositionState) },
                            filter = {
                                Filter1(
                                    uiState             = filterUiState,
                                    visible             = visible,
                                    onFoodType          = onFoodType,
                                    onPrice             = onPrice,
                                    onDistance          = onDistance,
                                    onRating            = onRating,
                                    onFilterFoodType    = onFilterFoodType,
                                    onFilterPrice       = onFilterPrice,
                                    onFilterDistance    = onFilterDistance,
                                    onFilterRating      = onFilterRating,
                                    onNation            = onNation,
                                    onThisArea          = onThisArea,
                                    onFilter            = { onFilter(); coroutineScope.launch { drawerState.open() } },
                                    onFilterCity        = { onFilterCity(it); moveCamera(coroutineScope, cameraPositionState, it.latitude, it.longitude, it.zoom) },
                                    onFilterNation      = { onFilterNation(it); moveCamera(coroutineScope, cameraPositionState, it.latitude, it.longitude, it.zoom) },
                                    onSearch            = { onSearch.invoke() },
                                    onQueryChange       = { onQueryChange(it) },
                                    topPadding          = topPadding
                                )
                            },
                            onMyLocation = {
                                if(!isGrantedPermission){ onRequestPermission.invoke() }
                                else{
                                    coroutineScope.launch(Dispatchers.IO) {
                                        val priority = if (usePreciseLocation) { Priority.PRIORITY_HIGH_ACCURACY } else { Priority.PRIORITY_BALANCED_POWER_ACCURACY }
                                        val result : Location? = locationClient.getCurrentLocation(priority, CancellationTokenSource().token,).await()
                                        result?.let { it ->
                                            onCurrentLocation(it)
                                            coroutineScope.launch { cameraPositionState.animate(update = CameraUpdateFactory.newLatLngZoom(LatLng(it.latitude, it.longitude), if (cameraPositionState.position.zoom <= 10.0f) 17.0f else cameraPositionState.position.zoom), if (cameraPositionState.position.zoom <= 10.0f) 2000 else 300) }
                                            myLocation = LatLng(it.latitude, it.longitude)
                                        }
                                    }
                                }
                            },
                            buttonBottomPadding = 0.dp,
                            onChangeRestaurantCardPageHeight = { cardPagerHeight = it}
                        )
                    }
                )
            }
            FloatingActionButton(
                modifier = Modifier.size(66.dp).padding(16.dp).align(if(isVisible)Alignment.CenterEnd else Alignment.BottomEnd),
                shape = CircleShape,
                onClick = {coroutineScope.launch {
                    if(bottomSheetState.bottomSheetState.currentValue != SheetValue.Expanded) bottomSheetState.bottomSheetState.expand()
                    else bottomSheetState.bottomSheetState.partialExpand() }}
            ) { Icon(Icons.AutoMirrored.Default.List, "") }
        }
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

@Preview
@Composable
fun test(a : String = "a"){
    Text(a)
}