package com.sarang.torang.di.finding_di

import android.Manifest
import android.content.Context
import android.location.Location
import android.util.Log
import androidx.annotation.RequiresPermission
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.BottomSheetScaffoldState
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SheetValue
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.screen_finding.ui.FindScreen
import com.example.screen_finding.uistate.FindUiState
import com.example.screen_finding.viewmodel.FindViewModel
import com.example.screen_map.compose.MapScreenForFinding_
import com.example.screen_map.viewmodels.MapUIState
import com.example.screen_map.viewmodels.MapViewModel
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.maps.android.compose.CameraPositionState
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.rememberCameraPositionState
import com.sarang.torang.LocalRestaurantItemImageLoader
import com.sarang.torang.RestaurantItemUiState
import com.sarang.torang.RestaurantListBottomSheetViewModel
import com.sarang.torang.RestaurantListBottomSheet_
import com.sarang.torang.RootNavController
import com.sarang.torang.Sample
import com.sarang.torang.compose.Filter1
import com.sarang.torang.compose.FilterDrawer
import com.sarang.torang.compose.FilterUiState
import com.sarang.torang.compose.FilterViewModel
import com.sarang.torang.compose.LocalFilterImageLoader
import com.sarang.torang.compose.cardinfo.CardInfoViewModel
import com.sarang.torang.compose.cardinfo.LocalCardInfoImageLoader
import com.sarang.torang.compose.cardinfo.RestaurantCardPage1
import com.sarang.torang.compose.cardinfo.RestaurantCardUIState
import com.sarang.torang.di.restaurant_list_bottom_sheet_di.CustomRestaurantItemImageLoader
import com.sarang.torang.uistate.FilterCallback
import com.sarang.torang.uistate.FilterDrawerCallBack
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@OptIn(ExperimentalMaterial3Api::class)
@RequiresPermission(anyOf = [Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION])
@Composable
fun Find(
    findViewModel                       : FindViewModel                         = hiltViewModel(),
    filterViewModel                     : FilterViewModel                       = hiltViewModel(),
    restaurantListBottomSheetViewModel  : RestaurantListBottomSheetViewModel    = hiltViewModel(),
    cardInfoViewModel                   : CardInfoViewModel                     = hiltViewModel(),
    mapViewModel                        : MapViewModel                          = hiltViewModel(),
    navController                       : RootNavController                     = RootNavController(),
    snackBarHostState                   : SnackbarHostState                     = remember { SnackbarHostState() },
    isGrantedPermission                 : Boolean                               = false,
    onRequestPermission                 : () -> Unit                            = {}
) {
    val tag                 : String                        = "__Finding"
    val findUiState         : FindUiState                   = findViewModel.uiState
    val filterUiState       : FilterUiState                 = filterViewModel.uiState
    val cardUiState         : List<RestaurantCardUIState>   = cardInfoViewModel.cardInfos
    val mapUiState          : MapUIState                    = mapViewModel.uiState
    val bottonSheetUiState  : List<RestaurantItemUiState>   by restaurantListBottomSheetViewModel.uiState.collectAsState()
    val coroutineScope      : CoroutineScope                = rememberCoroutineScope()
    val cameraPositionState : CameraPositionState           = rememberCameraPositionState()

    LaunchedEffect(key1 = findUiState.errorMessage, block = { // error snack bar
        if(findUiState.errorMessage.isNotEmpty()){
            findUiState.errorMessage.let {
                snackBarHostState.showSnackbar(it[0], duration = SnackbarDuration.Short)
                findViewModel.clearErrorMessage()
            }
        }
    })

    Find1(
        uiState              = findUiState,
        mapUiState           = mapUiState,
        filterUiState        = filterUiState,
        cardUiState          = cardUiState,
        bottomSheetUiState   = bottonSheetUiState,
        cameraPositionState  = cameraPositionState,
        isGrantedPermission  = isGrantedPermission,
        onRequestPermission  = onRequestPermission,
        navController        = navController,
        onCurrentLocation    = { findViewModel.setCurrentLocation(it) },
        filterDrawerCallBack = FilterDrawerCallBack(
        onFilterFoodType     = { filterViewModel.setFoodType(it) },
        onFilterPrice        = { filterViewModel.setPrice(it) },
        onFilterDistance     = { filterViewModel.setDistance(it) },
        onFilterRating       = { filterViewModel.setRating(it) },
        onFilterCity         = { filterViewModel.onCity(it)},
        onFilterNation       = { filterViewModel.onNation(it)},
        onQueryChange        = { filterViewModel.setQuery(it) }),
        filterCallback       = FilterCallback(
        onFilter             = { filterViewModel.onFilter()},
        onSearch             = { /*onSearch.invoke(uiState)*/ },
        onThisArea           = { filterViewModel.onThisArea() },),
        onMark               = { mapViewModel.onMark(it) },
        onSaveCameraPosition = { mapViewModel.saveCameraPosition(it) },
        onMapLoaded = {
                if (!mapViewModel.uiState.isMapLoaded) { // 플래그 처리 안하면 지도화면으로 이동할때마다 이벤트 발생 처음에 한번만 동작하면 됨
                    coroutineScope.launch {
                        cameraPositionState.animate(CameraUpdateFactory.newLatLngZoom(mapViewModel.getLastPosition(), mapViewModel.getLastZoom()), durationMs = 1000)
                        delay(1000) //카메라 이동 전까지 플래그 비활성화
                    }
                    mapViewModel.onMapLoaded()
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
private fun Find1(
    modifier                : Modifier                      = Modifier,
    isGrantedPermission     : Boolean                       = false,
    topPadding              : Dp                            = 0.dp,
    boundary                : Double?                       = null,
    cameraSpeed             : Int                           = 300,
    markerDetailVisibleLevel: Float                         = 18f,
    uiState                 : FindUiState                   = FindUiState(),
    filterUiState           : FilterUiState                 = FilterUiState(),
    mapUiState              : MapUIState                    = MapUIState(),
    bottomSheetUiState      : List<RestaurantItemUiState>   = listOf(),
    cardUiState             : List<RestaurantCardUIState>   = listOf(),
    navController           : RootNavController             = RootNavController(),
    cameraPositionState     : CameraPositionState           = rememberCameraPositionState(),
    snackBarHostState       : SnackbarHostState             = remember { SnackbarHostState() },
    uiSettings              : MapUiSettings                 = MapUiSettings(zoomControlsEnabled = false, myLocationButtonEnabled = false, compassEnabled = false),
    filterCallback          : FilterCallback                = FilterCallback(),
    filterDrawerCallBack    : FilterDrawerCallBack          = FilterDrawerCallBack(),
    onRequestPermission     : () -> Unit                    = {},
    onCurrentLocation       : (Location)->Unit              = {},
    onSaveCameraPosition    : (CameraPositionState) -> Unit = {},
    onMark                  : (Int) -> Unit                 = {},
    onMapLoaded             : () -> Unit                    = {},
){
    val tag                 : String                        = "__Finding"
    val coroutineScope      : CoroutineScope                = rememberCoroutineScope()
    val context             : Context                       = LocalContext.current
    val locationClient      : FusedLocationProviderClient   = remember { LocationServices.getFusedLocationProviderClient(context) }
    val bottomSheetState    : BottomSheetScaffoldState      = rememberBottomSheetScaffoldState()
    val drawerState         : DrawerState                   = rememberDrawerState(initialValue = DrawerValue.Closed)
    var isVisible           : Boolean                       by remember { mutableStateOf(true) }
    var myLocation          : LatLng?                       by remember { mutableStateOf(null) }
    var cardPagerHeight     : Int                           by remember { mutableIntStateOf(0) }
    val cardPagerHeightDp   : Dp                            = with(LocalDensity.current){ cardPagerHeight.toDp() }
    val usePreciseLocation  : Boolean                       = true

    val filter : @Composable () -> Unit = {
        Filter1(
            uiState             = filterUiState,
            visible             = isVisible,
            filterCallback      = FilterCallback(
            onThisArea          = filterCallback.onThisArea,
            onFilter            = { filterCallback.onFilter(); coroutineScope.launch { drawerState.open() } },
            onFilterCity        = { filterCallback.onFilterCity(it); moveCamera(coroutineScope, cameraPositionState, it.latitude, it.longitude, it.zoom) },
            onFilterNation      = { filterCallback.onFilterNation(it); moveCamera(coroutineScope, cameraPositionState, it.latitude, it.longitude, it.zoom) },
            onSearch            = { filterCallback.onSearch.invoke() },
            onQueryChange       = { filterCallback.onQueryChange(it) },),
            topPadding          = topPadding
        )
    }

    val mapScreenForFinding : @Composable () -> Unit = {
        MapScreenForFinding_(
            uiState                     = mapUiState,
            cameraSpeed                 = cameraSpeed,
            onMapClick                  = { isVisible = !isVisible },
            myLocation                  = myLocation,
            boundary                    = boundary,
            logoBottomPadding           = cardPagerHeightDp,
            markerDetailVisibleLevel    = markerDetailVisibleLevel,
            uiSettings                  = uiSettings,
            onSaveCameraPosition        = onSaveCameraPosition,
            onMark                      = onMark,
            onMapLoaded                 = onMapLoaded,
            cameraPositionState         = cameraPositionState,
            showLog                     = false
        )
    }

    val restaurantCardPage : @Composable ()->Unit = {
        CompositionLocalProvider(LocalCardInfoImageLoader provides customImageLoader) {
            RestaurantCardPage1(
                restaurants = cardUiState,
                onClickCard = { navController.restaurant(it) },
                visible     = isVisible,
                onPosition  = { lat,lon-> moveCamera(coroutineScope, cameraPositionState, lat, lon, 17f) }
            )
        }
    }

    val findScreen : @Composable ()->Unit = {
        FindScreen(
            restaurantCardPage                  = restaurantCardPage ,
            mapScreen                           = mapScreenForFinding,
            onZoomIn                            = { zoomIn(coroutineScope, cameraPositionState) },
            onZoomOut                           = { zoomOut(coroutineScope, cameraPositionState) },
            filter                              = filter,
            onChangeRestaurantCardPageHeight    = { cardPagerHeight = it},
            buttonBottomPadding                 = 0.dp,
            onMyLocation                        = {
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
            }
        )
    }

    val filterDraw : @Composable (PaddingValues) -> Unit = {
        FilterDrawer(
            uiState              = filterUiState,
            drawerState          = drawerState,
            filterDrawerCallBack = FilterDrawerCallBack(
            onFilterFoodType     = filterDrawerCallBack.onFilterFoodType,
            onFilterPrice        = filterDrawerCallBack.onFilterPrice,
            onFilterDistance     = filterDrawerCallBack.onFilterDistance,
            onFilterRating       = filterDrawerCallBack.onFilterRating,
            onFilterCity         = { filterDrawerCallBack.onFilterCity(it); filterCallback.onFilterCity.invoke(it) },
            onFilterNation       = { filterDrawerCallBack.onFilterNation(it); filterCallback.onFilterNation.invoke(it) },
            onQueryChange        = { filterDrawerCallBack.onQueryChange(it) },),
            content             = findScreen
        )
    }

    val restaurantBottonSheet : @Composable () -> Unit = {
        RestaurantListBottomSheet_ (
            modifier                = Modifier,
            uiState                 = bottomSheetUiState,
            sheetPeekHeight         = 0.dp,
            scaffoldState           = bottomSheetState,
            onClickRestaurantName   = { coroutineScope.launch { bottomSheetState.bottomSheetState.partialExpand() } },
            content                 = filterDraw
        )
    }

    CompositionLocalProvider(
        LocalRestaurantItemImageLoader provides CustomRestaurantItemImageLoader,
        LocalFilterImageLoader provides filterImageLoader
    ){
        Box(modifier = modifier) {
            restaurantBottonSheet.invoke()
            FloatingActionButton (
                modifier = Modifier
                                .size(66.dp)
                                .padding(16.dp)
                                .align(if(isVisible)Alignment.CenterEnd else Alignment.BottomEnd),
                shape    = CircleShape,
                onClick  = { coroutineScope.launch {
                                if(bottomSheetState.bottomSheetState.currentValue != SheetValue.Expanded)
                                    bottomSheetState.bottomSheetState.expand()
                                else
                                    bottomSheetState.bottomSheetState.partialExpand()
                              }
                            },
                content = { Icon(Icons.AutoMirrored.Default.List, "") }
            )
        }
    }
}

fun zoomIn(coroutineScope: CoroutineScope, cameraPositionState: CameraPositionState){ coroutineScope.launch { cameraPositionState.animate(CameraUpdateFactory.zoomIn(), 300) } }
fun zoomOut(coroutineScope: CoroutineScope, cameraPositionState: CameraPositionState){ coroutineScope.launch { cameraPositionState.animate(CameraUpdateFactory.zoomOut(), 300) } }
fun moveCamera(coroutineScope: CoroutineScope,cameraPositionState : CameraPositionState, latitude : Double, longitude : Double, zoom : Float){ coroutineScope.launch { cameraPositionState.animate(CameraUpdateFactory.newLatLngZoom(LatLng(latitude, longitude), zoom), 1000) } }
fun moveCamera1(coroutineScope: CoroutineScope,cameraPositionState : CameraPositionState, lat : Double, lon : Double){ coroutineScope.launch { cameraPositionState.animate(CameraUpdateFactory.newLatLng(LatLng(lat, lon)), 300) } }

@Preview
@Composable
fun BottomAppBarTest(){
    Scaffold(
        bottomBar = {
            BottomAppBar(     actions = {         IconButton(onClick = { /* doSomething() */ }) {             Icon(Icons. Filled. Menu, contentDescription = "Localized description")         }     } )
        }
    ) {
        Find1(/*Preview*/
            modifier = Modifier.padding(it),
            bottomSheetUiState = listOf(RestaurantItemUiState.Sample,RestaurantItemUiState.Sample,RestaurantItemUiState.Sample,RestaurantItemUiState.Sample,RestaurantItemUiState.Sample,RestaurantItemUiState.Sample)
        )
    }

}