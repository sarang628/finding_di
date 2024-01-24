package com.sryang.findinglinkmodules.di.finding_di

import android.location.Location
import com.example.screen_finding.data.RestaurantInfo
import com.example.screen_finding.usecase.FindRestaurantUseCase
import com.example.screen_finding.usecase.SearchThisAreaUseCase
import com.example.screen_finding.viewmodel.Filter
import com.sryang.torang_repository.api.ApiRestaurant
import com.sryang.torang_repository.api.handle
import com.sryang.torang_repository.data.SearchType
import com.sryang.torang_repository.repository.MapRepository
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
                return apiRestaurant.getFilterRestaurant(
                    filter = filter.toFilter()
                ).map { it.toRestaurantInfo() }
            }
        }
    }

    @Provides
    fun provideSearchThisAreaModule(
        apiRestaurant: ApiRestaurant,
        mapRepository: MapRepository
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
                }catch (e: HttpException){
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
