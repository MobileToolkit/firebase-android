package org.mobiletoolkit.android.repository.recyclerview

import android.support.v7.widget.RecyclerView
import org.mobiletoolkit.android.firebase.firestore.FirestoreModel
import org.mobiletoolkit.android.firebase.firestore.SimpleFirestoreRepository
import org.mobiletoolkit.android.repository.AsyncRepositoryListener

/**
 * Created by Sebastian Owodzin on 13/02/2019.
 */
abstract class ListenerRecyclerViewAdapter<ViewHolder : RecyclerView.ViewHolder, Entity : FirestoreModel>(
    private val repository: SimpleFirestoreRepository<Entity>
) : AsyncRecyclerViewAdapter<ViewHolder, String, Entity>(repository) {

    protected open val repositoryListener: AsyncRepositoryListener<List<Entity>, Entity> = { entities, _, _ ->
        data = entities ?: listOf()
    }

    init {
        this.reloadRepositoryData()
    }

    override fun reloadRepositoryData() {
        repository.releaseListener(repositoryListener)
        repository.get(repositoryListener)
    }
}