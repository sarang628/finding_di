package com.sarang.torang.di.finding_di

import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import com.sarang.torang.compose.FilterImageLoader
import com.sarang.torang.compose.cardinfo.CardInfoImageLoader
import com.sarang.torang.di.image.TorangAsyncImageData
import com.sarang.torang.di.image.provideTorangAsyncImage

val customImageLoader: CardInfoImageLoader = { modifier, url, width, height, scale -> provideTorangAsyncImage().invoke(
    TorangAsyncImageData(
        modifier = modifier,
        model = url,
        progressSize = width ?: 30.dp,
        errorIconSize = height ?: 30.dp,
        contentScale = scale ?: ContentScale.None)
)  }