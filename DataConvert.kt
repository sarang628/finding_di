package com.sryang.findinglinkmodules.di.finding_di

import com.example.cardinfo.RestaurantCardData
import com.example.screen_finding.data.RestaurantInfo
import com.example.screen_finding.viewmodel.Filter
import com.example.screen_map.data.MarkerData
import com.sarang.torang.data.remote.response.RestaurantApiModel
import com.sryang.screen_filter.ui.FilterUiState


fun String.toBoundary(): Double {
    if (this.equals("100m")) {
        return 100.0
    } else if (this.equals("300m")) {
        return 300.0
    } else if (this.equals("500m")) {
        return 500.0
    } else if (this.equals("1km")) {
        return 1000.0
    } else if (this.equals("3km")) {
        return 3000.0
    }
    return 0.0
}

fun Filter.toFilter(): com.sarang.torang.data.Filter {
    return com.sarang.torang.data.Filter(
        restaurantTypes = this.restaurantTypes?.stream()?.map { it.uppercase() }?.toList(),
        prices = this.prices,
        ratings = this.ratings?.toRating(),
        distances = this.distances?.toDistnace(),
        lat = this.lat,
        lon = this.lon,
        north = this.north,
        south = this.south,
        east = this.east,
        west = this.west,
        keyword = keyword
    )
}

fun String.toDistnace(): String? {
    if (this == "100m")
        return "_100M"
    else if (this == "300m")
        return "_300M"
    else if (this == "500m")
        return "_500M"
    else if (this == "1km")
        return "_1KM"
    else if (this == "3km")
        return "_3KM"
    return null
}

fun List<String>.toRating(): List<String>? {
    return this.stream().map {
        if (it == "*")
            "ONE"
        else if (it == "**")
            "TWO"
        else if (it == "***")
            "THREE"
        else if (it == "****")
            "FOUR"
        else if (it == "*****")
            "FIVE"
        else
            ""
    }.toList()
}

fun RestaurantApiModel.toRestaurantInfo(): RestaurantInfo {
    return RestaurantInfo(
        restaurantId = this.restaurantId,
        restaurantName = this.restaurantName,
        rating = this.rating,
        foodType = this.restaurantTypeCd,
        restaurantImage = this.imgUrl1,
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