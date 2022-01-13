package nick.template.ui.recordings

import androidx.fragment.app.FragmentManager
import androidx.fragment.app.commit
import androidx.fragment.app.replace
import javax.inject.Inject
import nick.template.di.FragmentContainerId
import nick.template.ui.RecorderFragment

interface RecordingsNavigator {
    fun toRecorder()
}

class FragmentRecordingsNavigator @Inject constructor(
    private val fragmentManager: FragmentManager,
    @FragmentContainerId private val containerId: Int
) : RecordingsNavigator {
    override fun toRecorder() {
        fragmentManager.commit {
            setReorderingAllowed(true)
            replace<RecorderFragment>(containerId)
            addToBackStack(null)
        }
    }
}
