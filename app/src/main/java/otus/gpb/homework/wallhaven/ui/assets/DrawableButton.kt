package otus.gpb.homework.wallhaven.ui.assets

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ButtonElevation
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun DrawableButton(
    onClick: () -> Unit,
    icon:ImageVector,
    text:String,
    modifier: Modifier = Modifier,
    iconWidth:Dp=24.dp,
    enabled: Boolean = true,
    shape: Shape = ButtonDefaults.shape,
    colors: ButtonColors = ButtonDefaults.buttonColors(),
    elevation: ButtonElevation? = ButtonDefaults.buttonElevation(),
    border: BorderStroke? = null,
    contentPadding: PaddingValues = ButtonDefaults.ContentPadding,
) {
    Button(
        onClick = onClick,
        modifier=modifier,
        shape=shape,
        elevation = elevation,
        border = border,
        contentPadding = contentPadding,
        enabled = enabled,
        colors = colors,
    ) {
        Column {
            Icon(
                imageVector = icon,
                modifier = Modifier.size(iconWidth),
                contentDescription = "",
                tint = Color.Unspecified
            )

        }
        Column {
            Text(
                text = text,
                color = colors.contentColor,
                textAlign = TextAlign.Center,
                /*modifier = Modifier
                    .offset(x= -iconWidth/2) //default icon width = 24.dp

                 */
            )
        }
    }
}