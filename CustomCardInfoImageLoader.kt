package com.sarang.torang.di.finding_di

import com.sarang.torang.compose.FilterImageLoader
import com.sarang.torang.compose.cardinfo.CardInfoImageLoader
import com.sarang.torang.di.image.provideTorangAsyncImage

val customImageLoader: CardInfoImageLoader = { modifier, url, width, height, scale -> provideTorangAsyncImage().invoke(modifier, url, width, height, scale)  }