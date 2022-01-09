package nick.template.ui.extensions

import android.widget.EditText
import androidx.core.widget.doOnTextChanged
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow

fun EditText.textChanges() = callbackFlow {
    val textWatcher = doOnTextChanged { text, _, _, _ ->
        trySend(text)
    }

    awaitClose { removeTextChangedListener(textWatcher) }
}
