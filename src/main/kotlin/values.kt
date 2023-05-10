import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

val corners get() = style.corners.dp
val smallCorners get() = style.small_corners.dp
val padding get() = style.padding.dp
val biggerPadding get() = style.bigger_padding.dp
val border get() = style.border.dp
val smallBorder get() = style.small_border.dp

val imageWidth get() = style.image_width.dp
val imageHeight get() = style.image_height.dp
val messageImageHeight get() = style.message_image_height.dp
val bigIconSize get() = style.big_icon_size.dp
val iconSize get() = style.icon_size.dp
val smallIconSize get() = style.small_icon_size.dp
val tinyIconSize get() = style.tiny_icon_size.dp
val menuWidth get() = style.menu_width.dp
val menuItemHeight get() = style.menu_item_height.dp
val scrollbarThickness get() = style.scrollbar_thickness.dp

val transparency get() = style.transparency
val transparencySecond get() = style.transparency_second
val transparencyLight get() = style.transparency_light

val tinyText get() = style.tiny_text.sp
val smallText get() = style.small_text.sp
val normalText get() = style.normal_text.sp
val bigText get() = style.big_text.sp
val hugeText get() = style.huge_text.sp

var shortAnimationDuration: Int = style.shortAnimationDuration
var normalAnimationDuration: Int = style.normalAnimationDuration
var longAnimationDuration: Int = style.longAnimationDuration