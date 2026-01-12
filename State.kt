package com.sarang.torang.di.finding_di

import androidx.compose.material3.BottomSheetScaffoldState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.runtime.Composable

class FindState @OptIn(ExperimentalMaterial3Api::class) constructor(
    val bottomSheetState        : BottomSheetScaffoldState,
    val selectedRestaurantId    : Int? = null
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun rememberFindState() : FindState {
    val bottomSheetState    : BottomSheetScaffoldState      = rememberBottomSheetScaffoldState()
    return FindState(bottomSheetState)
}