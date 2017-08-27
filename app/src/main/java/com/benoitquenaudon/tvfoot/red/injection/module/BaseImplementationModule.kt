package com.benoitquenaudon.tvfoot.red.injection.module

import com.benoitquenaudon.tvfoot.red.app.data.source.BaseMatchRepository
import com.benoitquenaudon.tvfoot.red.app.data.source.BaseMatchesRepository
import com.benoitquenaudon.tvfoot.red.app.data.source.MatchRepository
import com.benoitquenaudon.tvfoot.red.app.data.source.MatchesRepository
import dagger.Binds
import dagger.Module

@Module
abstract class BaseImplementationModule {
  @Binds
  abstract fun provideMatchesRepository(matchesRepository: MatchesRepository): BaseMatchesRepository

  @Binds
  abstract fun provideMatchRepository(matchRepository: MatchRepository): BaseMatchRepository
}