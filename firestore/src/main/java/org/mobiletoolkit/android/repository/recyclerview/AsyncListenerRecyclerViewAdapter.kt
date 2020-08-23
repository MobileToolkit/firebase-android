package org.mobiletoolkit.android.repository.recyclerview

import androidx.recyclerview.widget.RecyclerView
import org.mobiletoolkit.android.repository.AsyncRepository
import org.mobiletoolkit.android.repository.AsyncRepositoryListener
import org.mobiletoolkit.android.repository.Model

/**
 * Created by Sebastian Owodzin on 13/02/2019.
 */
abstract class AsyncListenerRecyclerViewAdapter<ViewHolder : RecyclerView.ViewHolder, Identifier, Entity : Model<Identifier>>(
    private val repository: AsyncRepository<Identifier, Entity>
) : AsyncRecyclerViewAdapter<ViewHolder, Identifier, Entity>(repository) {

    override var data: List<Entity> = listOf()

    protected open val repositoryListener: AsyncRepositoryListener<List<Entity>, Entity> = { entities, changeSet, _ ->
        data = entities ?: listOf()

        if (changeSet?.isNotEmpty() == true) {
            changeSet.forEach {
                when (it.type) {
                    AsyncRepository.Change.Type.Added -> notifyItemInserted(it.newIndex)
                    AsyncRepository.Change.Type.Modified -> notifyItemChanged(it.newIndex)
                    AsyncRepository.Change.Type.Removed -> notifyItemRemoved(it.newIndex)
                }
            }
        } else {
            notifyDataSetChanged()
        }
    }

    init {
        this.reloadRepositoryData()
    }

    override fun reloadRepositoryData() {
        repository.releaseListener(repositoryListener)
        repository.get(repositoryListener)
    }
}