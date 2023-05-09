import properties.toColor
import kotlin.math.min

val colorBorder get() = style.color_border.toColor()

val colorBackground get() = style.color_background.toColor()
val colorBackgroundSelection get() = style.color_background.copy(
    r = min(style.color_background.r + 5, 255),
    g = min(style.color_background.g + 5, 255),
    b = min(style.color_background.b + 5, 255),
).toColor()
val colorBackgroundLighter get() = style.color_background_lighter.toColor()
val colorBackgroundSecond get() = style.color_background_second.toColor()
val colorBackgroundSecondLighter get() = style.color_background_second_lighter.toColor()
val colorBackgroundDelete get() = style.color_background_delete.toColor()

val colorText get() = style.color_text.toColor()
val colorTextSecond get() = style.color_text_second.toColor()
val colorTextError get() = style.color_text_error.toColor()
val colorTextSuccess get() = style.color_text_success.toColor()
val colorTextItalic get() = style.color_text_italic.toColor()
val colorTextBold get() = style.color_text_bold.toColor()
val colorLink get() = style.color_link.toColor()
val colorCode get() = style.color_code.toColor()
val colorCodeBackground get() = style.color_code_background.toColor()

val colorNoConnection get() = style.color_no_connection.toColor()
val colorCheckConnection get() = style.color_check_connection.toColor()
val colorConnection get() = style.color_connection.toColor()