package site.hanzhe.wuchang_liuyao

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import site.hanzhe.wuchang_liuyao.navigation.WuchangLiuyaoApp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            WuchangLiuyaoApp()
        }
    }
}
