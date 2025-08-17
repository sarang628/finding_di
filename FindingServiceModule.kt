package com.sarang.torang.di.finding_di

import android.location.Location
import com.example.screen_finding.data.RestaurantInfo
import com.example.screen_finding.usecase.FindRestaurantUseCase
import com.example.screen_finding.usecase.SearchByKeywordUseCase
import com.example.screen_finding.usecase.SearchThisAreaUseCase
import com.example.screen_finding.viewmodel.Filter
import com.sarang.torang.api.ApiRestaurant
import com.sarang.torang.api.handle
import com.sarang.torang.data.SearchType
import com.sarang.torang.data.remote.response.FilterApiModel
import com.sarang.torang.repository.FindRepository
import com.sarang.torang.repository.MapRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.HttpException


@InstallIn(SingletonComponent::class)
@Module
class FindingServiceModule {
    @Provides
    fun provideFindingService(findRepository: FindRepository): FindRestaurantUseCase {
        return object : FindRestaurantUseCase {
            override suspend fun filter(filter: Filter) {
                try {
                    val filter1 = FilterApiModel();
                    filter1.keyword = filter.keyword
                    filter1.east = filter.east
                    filter1.west = filter.west
                    filter1.south = filter.south
                    filter1.north = filter.north
                    filter1.lat = filter.lat
                    filter1.lon = filter.lon
                    filter1.distances = filter.distances
                    if(filter.distances == "")
                        filter1.distances = null
                    filter1.searchType = filter.searchType.toString()
                    filter1.restaurantTypes = filter.restaurantTypes
                    filter1.ratings = filter.ratings
                    filter1.prices = filter.prices
                    findRepository.search(filter1)
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
            override suspend fun invoke(filter: Filter){

                val filter = filter.toFilter()
                filter.north = mapRepository.getNElon()
                filter.east = mapRepository.getNElat()
                filter.south = mapRepository.getSWlon()
                filter.west = mapRepository.getSWlat()
                filter.searchType = SearchType.BOUND.toString()
                try {
                    findRepository.search(filter)
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
            override suspend fun invoke(filter: Filter): List<RestaurantInfo> {

                val filter = filter.toFilter()
                filter.north = mapRepository.getNElon()
                filter.east = mapRepository.getNElat()
                filter.south = mapRepository.getSWlon()
                filter.west = mapRepository.getSWlat()
                filter.searchType = SearchType.BOUND.toString()
                filter.keyword = filter.keyword
                try {
                    findRepository.search(filter)
                    return apiRestaurant.getFilterRestaurant(
                        filter = filter
                    ).map { it.toRestaurantInfo() }
                } catch (e: HttpException) {
                    throw Exception(e.handle())
                }

            }
        }
    }

}

private fun newLocation(): Location {
    val location = Location("MyLocationProvider")
    return location
}
