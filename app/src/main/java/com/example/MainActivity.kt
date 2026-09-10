package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    com.example.common.network.UserSessionManager.init(applicationContext)
    val db = com.example.common.data.local.AppDatabase.getInstance(applicationContext)
    val networkSource = com.example.common.data.remote.AcademicNetworkDataSource()
    com.example.common.repository.AcademicRepository.getInstance(
        networkDataSource = networkSource,
        profileDao = db.userAcademicProfileDao(),
        configDao = db.cachedAcademicConfigDao()
    )
    val courseApi = com.example.common.data.remote.CourseApiClientFactory.createService()
    com.example.common.repository.CourseRepositoryImpl.getInstance(
        apiService = courseApi,
        cachedMyCoursesDao = db.cachedMyCoursesDao(),
        profileDao = db.userAcademicProfileDao()
    )
    enableEdgeToEdge()
    setContent {
      MyApplicationTheme {
        com.example.common.ui.StudyAppMain()
      }
    }
  }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
  Text(text = "Hello $name!", modifier = modifier)
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
  MyApplicationTheme { Greeting("Android") }
}
