package com.sarang.torang.di.finding_di

import android.location.Location
import com.sarang.torang.api.ApiRestaurant
import com.sarang.torang.api.handle
import com.sarang.torang.data.Filter
import com.sarang.torang.data.SearchType
import com.sarang.torang.data.find.FindFilter
import com.sarang.torang.data.find.RestaurantInfo
import com.sarang.torang.data.remote.response.FilterApiModel
import com.sarang.torang.repository.FindRepository
import com.sarang.torang.repository.MapRepository
import com.sarang.torang.usecase.find.FindRestaurantUseCase
import com.sarang.torang.usecase.find.SearchByKeywordUseCase
import com.sarang.torang.usecase.find.SearchThisAreaUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.HttpException


@InstallIn(SingletonComponent::class)
@Module
class FindServiceModule {
    @Provides
    fun provideFindService(findRepository: FindRepository): FindRestaurantUseCase {
        return object : FindRestaurantUseCase {
            override suspend fun filter(filter: FindFilter) {
                try {
                    val filter = Filter(
                        keyword = filter.keyword,
                        northEastLat = filter.northEastLat,
                        southWestLat = filter.southWestLat,
                        southWestLon = filter.southWestLon,
                        northEastLon = filter.northEastLon,
                        lat = filter.latitude,
                        lon = filter.longitude,
                        distances = filter.distances,
                        searchType = SearchType.valueOf(filter.searchType),
                        restaurantTypes = filter.restaurantTypes,
                        ratings = filter.ratings,
                        prices = filter.prices
                    );
                    findRepository.search(filter)
                } catch (e: HttpException) {
                    throw Exception(e.handle())
                }
            }
        }
    }

    @Provides
    fun provideSearchThisAreaModule(
        mapRepository: MapRepository,
        findRepository: FindRepository
    ): SearchThisAreaUseCase {
        return object : SearchThisAreaUseCase {
            override suspend fun invoke(filter: FindFilter){

                val filter = filter.toFilter().copy(
                    northEastLon = mapRepository.getNElon(),
                    northEastLat = mapRepository.getNElat(),
                    southWestLon = mapRepository.getSWlon(),
                    southWestLat = mapRepository.getSWlat(),
                    searchType = SearchType.BOUND
                )
                try {
                    findRepository.findThisArea()
                } catch (e: HttpException) {
                    throw Exception(e.handle())
                }

            }
        }
    }

    @Provides
    fun provideSearchByKeywordModule(
        apiRestaurant: ApiRestaurant,
        mapRepository: MapRepository,
        findRepository: FindRepository
    ): SearchByKeywordUseCase {
        return object : SearchByKeywordUseCase {
            override suspend fun invoke(filter: FindFilter): List<RestaurantInfo> {

                val filter = filter.toFilter().copy(
                    northEastLon = mapRepository.getNElon(),
                    northEastLat = mapRepository.getNElat(),
                    southWestLon = mapRepository.getSWlon(),
                    southWestLat = mapRepository.getSWlat(),
                    searchType = SearchType.BOUND,
                    keyword = filter.keyword
                )

                try {
                    findRepository.search(filter)
                    return apiRestaurant.getFilterRestaurant(
                        filter = filter.toApiModel()
                    ).map { it.toRestaurantInfo() }
                } catch (e: HttpException) {
                    throw Exception(e.handle())
                }

            }
        }
    }
}

fun Filter.toApiModel() : FilterApiModel {
    return FilterApiModel(
        searchType = this.searchType.toString(),
        keyword = this.keyword,
        distances = this.distances,
        prices = this.prices,
        restaurantTypes = this.restaurantTypes,
        ratings = this.ratings?.toRating(),
        latitude = this.lat,
        longitude = this.lon,
        northEastLat = this.northEastLat,
        northEastLon = this.northEastLon,
        southWestLat = this.southWestLat,
        southWestLon = this.southWestLon
    )
}

private fun newLocation(): Location {
    val location = Location("MyLocationProvider")
    return location
}
