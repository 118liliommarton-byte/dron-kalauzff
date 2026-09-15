package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Drón Kalauz", appName)
  }

  @Test
  fun `verify drone rules countries are available`() {
    val countries = com.example.data.DroneRules.countries
    assert(countries.isNotEmpty())
    val hungary = countries.find { it.name == "Magyarország" }
    assert(hungary != null)
    assertEquals("🇭🇺", hungary?.flag)
    assertEquals("120 m", hungary?.maxAltitude)
  }
}
