package com.sarang.torang.di.finding_di

import com.example.screen_finding.data.RestaurantInfo
import com.example.screen_finding.viewmodel.Filter
import com.example.screen_map.data.MarkerData
import com.sarang.torang.BuildConfig
import com.sarang.torang.compose.cardinfo.RestaurantCardData
import com.sarang.torang.data.remote.response.RestaurantApiModel
import com.sarang.torang.ui.FilterUiState


fun String.toBoundary(): Double {
    return (
        if (this == "100m") 100.0
        else if (this == "300m") 300.0
        else if (this == "500m") 500.0
        else if (this == "1km") 1000.0
        else if (this == "3km") 3000.0
        else 0.0
    )
}

fun Filter.toFilter(): com.sarang.torang.data.Filter {
    return com.sarang.torang.data.Filter(
        restaurantTypes = this.restaurantTypes?.map { it.uppercase() }?.toList(),
        prices = this.prices,
        ratings = this.ratings?.toRating(),
        distances = this.distances?.toDistance(),
        lat = this.lat,
        lon = this.lon,
        north = this.north,
        south = this.south,
        east = this.east,
        west = this.west,
        keyword = keyword
    )
}

fun String.toDistance(): String? {
    return (if (this == "100m") "_100M"
    else if (this == "300m") "_300M"
    else if (this == "500m") "_500M"
    else if (this == "1km") "_1KM"
    else if (this == "3km") "_3KM"
    else return null
    )
}

fun List<String>.toRating(): List<String>? {
    return this.map {
        if (it == "*") "ONE"
        else if (it == "**") "TWO"
        else if (it == "***") "THREE"
        else if (it == "****") "FOUR"
        else if (it == "*****") "FIVE"
        else ""
    }.toList()
}

fun RestaurantApiModel.toRestaurantInfo(): RestaurantInfo {
    return RestaurantInfo(
        restaurantId = this.restaurantId,
        restaurantName = this.restaurantName,
        rating = this.rating,
        foodType = this.restaurantTypeCd,
        restaurantImage = BuildConfig.RESTAURANT_IMAGE_SERVER_URL + this.imgUrl1,
        price = "$$$",
        distance = "120m",
        lat = this.lat,
        lon = this.lon,
    )
}

fun RestaurantInfo.toRestaurantCardData(): RestaurantCardData {
    return RestaurantCardData(
        restaurantId = this.restaurantId,
        restaurantName = this.restaurantName,
        rating = this.rating,
        foodType = this.foodType,
        restaurantImage = this.restaurantImage,
        price = this.price,
        distance = this.distance
    )
}

fun RestaurantInfo.toMarkData(): MarkerData {
    return MarkerData(
        id = this.restaurantId,
        lat = this.lat,
        lon = this.lon,
        title = this.restaurantName,
        snippet = this.price,
        foodType = this.foodType
    )
}

fun FilterUiState.toFilter(): Filter {
    return Filter(
        restaurantTypes = if (this.foodType.isEmpty()) null else foodType,
        prices = if (price.isEmpty()) null else price,
        ratings = if (rating.isEmpty()) null else rating,
        distances = distance,
        keyword = keyword
    )
}