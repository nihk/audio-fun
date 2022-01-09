package nick.template.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class CachedFilename(
    val simple: String,
    val absolute: String,
    val extension: String
) : Parcelable
