package com.benoitquenaudon.tvfoot.red.app.mvi

import com.benoitquenaudon.tvfoot.red.app.common.firebase.BaseRedFirebaseAnalytics

abstract class RedStateBinder<I : MviIntent, S : MviViewState>(
    val firebaseAnalytics: BaseRedFirebaseAnalytics
) : MviViewModel<I, S>