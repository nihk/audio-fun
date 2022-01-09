package nick.template.ui

import androidx.lifecycle.AbstractSavedStateViewModelFactory
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.savedstate.SavedStateRegistryOwner
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.merge
import nick.template.data.AudioRepository
import nick.template.data.CachedFileHandle
import nick.template.data.Effect
import nick.template.data.Event
import nick.template.data.Result
import nick.template.data.State

class MainViewModel(
    private val cachedFileHandle: CachedFileHandle,
    private val audioRepository: AudioRepository,
) : MviViewModel<Event, Result, State, Effect>(State()) {

    override fun Result.reduce(state: State): State {
        return when (this) {
            is Result.StartRecordingResult -> state.copy(cachedFilename = cachedFilename)
            is Result.CachedRecordingClearedResult -> state.copy(cachedFilename = null)
            else -> state
        }
    }

    override fun Flow<Event>.toResults(): Flow<Result> {
        return merge(
            filterIsInstance<Event.RecordEvent>().toRecordingResults(),
            filterIsInstance<Event.SaveRecordingEvent>().toSaveRecordingResults(),
            filterIsInstance<Event.CancelSaveRecordingEvent>().toCancelSaveRecordingResults()
        )
    }

    private fun Flow<Event.RecordEvent>.toRecordingResults(): Flow<Result> {
        // todo: make sure recording twice without stopping is never permitted
        return flatMapLatest { event ->
            if (event.start) {
                audioRepository.record().map { emission ->
                    when (emission) {
                        is AudioRepository.Emission.Error -> Result.ErrorRecordingResult(emission.throwable)
                        is AudioRepository.Emission.Recording -> {
                            cachedFileHandle.filename = emission.cachedFilename
                            Result.StartRecordingResult(
                                emission.cachedFilename
                            )
                        }
                    }
                }
            } else {
                val cachedFilename = cachedFileHandle.filename
                val result = if (cachedFilename != null) {
                    Result.StopRecordingResult
                } else {
                    Result.NoOpResult // An error happened previously, or recording didn't start yet; nothing to do here.
                }
                flowOf(result)
            }
        }
    }

    private fun Flow<Event.SaveRecordingEvent>.toSaveRecordingResults(): Flow<Result> {
        return mapLatest { event ->
            val cachedFilename = cachedFileHandle.requireFilename()
            audioRepository.save(
                cachedFilename = cachedFilename,
                destinationFilename = event.filename
            )
            audioRepository.deleteFromCache(cachedFilename)
            cachedFileHandle.filename = null
            Result.CachedRecordingClearedResult
        }
    }

    private fun Flow<Event.CancelSaveRecordingEvent>.toCancelSaveRecordingResults(): Flow<Result> {
        return mapLatest {
            audioRepository.deleteFromCache(cachedFileHandle.requireFilename())
            cachedFileHandle.filename = null
            Result.CachedRecordingClearedResult
        }
    }

    override fun Flow<Result>.toEffects(): Flow<Effect> {
        return merge(
            filterIsInstance<Result.ErrorRecordingResult>().toErrorRecordingEffects(),
            filterIsInstance<Result.StopRecordingResult>().toPromptSaveFileEffects()
        )
    }

    private fun Flow<Result.ErrorRecordingResult>.toErrorRecordingEffects(): Flow<Effect> {
        return mapLatest { result -> Effect.ErrorRecordingEffect(result.throwable) }
    }

    private fun Flow<Result.StopRecordingResult>.toPromptSaveFileEffects(): Flow<Effect> {
        return mapLatest { Effect.PromptSaveFileEffect }
    }

    class Factory @Inject constructor(
        private val audioRepository: AudioRepository
    ) {
        fun create(owner: SavedStateRegistryOwner): AbstractSavedStateViewModelFactory {
            return object : AbstractSavedStateViewModelFactory(owner, null) {
                override fun <T : ViewModel> create(
                    key: String,
                    modelClass: Class<T>,
                    handle: SavedStateHandle
                ): T {
                    @Suppress("UNCHECKED_CAST")
                    return MainViewModel(CachedFileHandle(handle), audioRepository) as T
                }
            }
        }
    }
}
