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
    fun provideFindingService(apiRestaurant: ApiRestaurant): FindRestaurantUseCase {
        return object : FindRestaurantUseCase {
            override suspend fun findRestaurants(): List<RestaurantInfo> {
                return apiRestaurant.getAllRestaurant().map { it.toRestaurantInfo() }
            }

            override suspend fun filter(filter: Filter): List<RestaurantInfo> {
                try {
                    return apiRestaurant.getFilterRestaurant(
                        filter = filter.toFilter()
                    ).map { it.toRestaurantInfo() }
                } catch (e: HttpException) {
                    throw Exception(e.handle())
                }
            }
        }
    }

    @Provides
    fun provideSearchThisAreaModule(
        apiRestaurant: ApiRestaurant,
        mapRepository: MapRepository,
    ): SearchThisAreaUseCase {
        return object : SearchThisAreaUseCase {
            override suspend fun invoke(filter: Filter): List<RestaurantInfo> {

                val filter = filter.toFilter()
                filter.north = mapRepository.getNElon()
                filter.east = mapRepository.getNElat()
                filter.south = mapRepository.getSWlon()
                filter.west = mapRepository.getSWlat()
                filter.searchType = SearchType.BOUND
                try {
                    return apiRestaurant.getFilterRestaurant(
                        filter = filter
                    ).map { it.toRestaurantInfo() }
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
    ): SearchByKeywordUseCase {
        return object : SearchByKeywordUseCase {
            override suspend fun invoke(filter: Filter): List<RestaurantInfo> {

                val filter = filter.toFilter()
                filter.north = mapRepository.getNElon()
                filter.east = mapRepository.getNElat()
                filter.south = mapRepository.getSWlon()
                filter.west = mapRepository.getSWlat()
                filter.searchType = SearchType.BOUND
                filter.keyword = filter.keyword
                try {
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
