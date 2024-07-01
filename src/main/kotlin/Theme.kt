import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.material.MaterialTheme.typography
import androidx.compose.material.Surface
import androidx.compose.material.darkColors
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.sp

@Composable
fun Theme(
    isSystemInDarkTheme: Boolean,
    function: @Composable () -> Unit) {
     MaterialTheme(
        colors = darkColors().copy(
            primary = Color(	0xFF0366d6),
            primaryVariant = Color(0xFF0331f4),
            secondary = Color(0xFF2f363d),
            secondaryVariant = Color(0xFF9fadaf),
            background = Color(0xFF121212),
            surface = Color(0xFF1e1f22),
            error = Color(0xFFf44f03),
            onPrimary = Color(0xFF323232),
            onSecondary = Color.Black,
            onBackground = Color.White,
            onSurface = Color.White,
            onError = Color.Red
        ),
        typography = typography.copy(
            h1 = typography.h1.copy(fontSize = 20.sp),
            h2 = typography.h2.copy(fontSize = 18.sp),
            h3 = typography.h3.copy(fontSize = 16.sp),
            h4 = typography.h4.copy(fontSize = 14.sp),
            h5 = typography.h5.copy(fontSize = 12.sp),
            h6 = typography.h6.copy(fontSize = 10.sp),
            subtitle1 = typography.subtitle1.copy(fontSize = 16.sp),
            subtitle2 = typography.subtitle2.copy(fontSize = 14.sp),
            body1 = typography.body1.copy(fontSize = 12.sp),
            body2 = typography.body2.copy(fontSize = 10.sp),
            button = typography.button.copy(fontSize = 12.sp),
            caption = typography.caption.copy(fontSize = 12.sp),
            overline = typography.overline.copy(fontSize = 10.sp),
        )
    ){
         Surface(Modifier.fillMaxSize()) {
        function()
     }}
}