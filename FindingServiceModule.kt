package com.sarang.torang.di.finding_di

import android.location.Location
import androidx.room.util.copy
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
                    val filter1 = FilterApiModel(
                        keyword = filter.keyword,
                                northEastLat = filter.east,
                                southWestLat = filter.west,
                                southWestLon = filter.south,
                                northEastLon = filter.north,
                                latitude = filter.lat,
                                longitude = filter.lon,
                                distances = filter.distances,
                                searchType = filter.searchType.toString(),
                                restaurantTypes = filter.restaurantTypes,
                                ratings = filter.ratings,
                                prices = filter.prices
                    );
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

                val filter = filter.toFilter().copy(
                    northEastLon = mapRepository.getNElon(),
                    northEastLat = mapRepository.getNElat(),
                    southWestLon = mapRepository.getSWlon(),
                    southWestLat = mapRepository.getSWlat(),
                    searchType = SearchType.BOUND.toString()
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
            override suspend fun invoke(filter: Filter): List<RestaurantInfo> {

                val filter = filter.toFilter().copy(
                    northEastLon = mapRepository.getNElon(),
                    northEastLat = mapRepository.getNElat(),
                    southWestLon = mapRepository.getSWlon(),
                    southWestLat = mapRepository.getSWlat(),
                    searchType = SearchType.BOUND.toString(),
                    keyword = filter.keyword
                )

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
