package com.imut.autoclicker.di

import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

// Repositories use @Inject constructor with @Singleton,
// so they are auto-provided by Hilt. No manual bindings needed.
@Module
@InstallIn(SingletonComponent::class)
object AppModule
