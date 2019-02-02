package org.mobiletoolkit.android.firebase.firestore.recyclerview

import android.support.v7.widget.RecyclerView
import org.mobiletoolkit.android.firebase.firestore.FirestoreModel
import org.mobiletoolkit.android.firebase.firestore.FirestoreRepositoryListener
import org.mobiletoolkit.android.firebase.firestore.SimpleFirestoreRepository
import org.mobiletoolkit.android.repository.recyclerview.AsyncRecyclerViewAdapter

/**
 * Created by Sebastian Owodzin on 16/12/2018.
 */
abstract class FirestoreRecyclerViewAdapter<ViewHolder : RecyclerView.ViewHolder, Entity : FirestoreModel>(
    private val repository: SimpleFirestoreRepository<Entity>
) : AsyncRecyclerViewAdapter<ViewHolder, String, Entity>(repository) {

    protected open val repositoryListener: FirestoreRepositoryListener<List<Entity>, Entity> = { entities, _, _ ->
        data = entities ?: listOf()
    }

    init {
        this.reloadRepositoryData()
    }

    override fun reloadRepositoryData() {
        repository.get(repositoryListener)
    }
}