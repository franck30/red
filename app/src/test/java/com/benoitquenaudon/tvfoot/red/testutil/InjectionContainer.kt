package com.benoitquenaudon.tvfoot.red.testutil

import com.benoitquenaudon.tvfoot.red.injection.component.TestComponent

object InjectionContainer {
  val testComponentInstance: TestComponent by lazy {
    Dagger2Helper.buildComponent(TestComponent::class.java)
  }
}
