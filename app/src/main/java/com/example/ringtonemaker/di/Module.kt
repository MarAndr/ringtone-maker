package com.example.ringtonemaker.di

import android.app.Application
import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewComponent
import dagger.hilt.android.internal.managers.ApplicationComponentManager
import dagger.hilt.android.qualifiers.ApplicationContext

@Module
@InstallIn(ViewComponent::class)
class Module {
    @Provides
    fun getContext(context: Application): Application{
        return context
    }
}