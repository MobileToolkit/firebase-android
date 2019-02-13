package org.mobiletoolkit.android.firebase.firestore.recyclerview

import android.support.v7.widget.RecyclerView
import org.mobiletoolkit.android.firebase.firestore.FirestoreModel
import org.mobiletoolkit.android.firebase.firestore.SimpleFirestoreRepository
import org.mobiletoolkit.android.repository.AsyncRepositoryListener
import kotlin.properties.Delegates

/**
 * Created by Sebastian Owodzin on 16/12/2018.
 */
abstract class FirestoreRecyclerViewAdapter<ViewHolder : RecyclerView.ViewHolder, Entity : FirestoreModel>(
    private val repository: SimpleFirestoreRepository<Entity>
) : RecyclerView.Adapter<ViewHolder>() {

    protected open var data: List<Entity> by Delegates.observable(listOf()) { _, oldValue, newValue ->
        onDataChanged(oldValue, newValue)
    }

    protected open val repositoryListener: AsyncRepositoryListener<List<Entity>, Entity> = { entities, _, _ ->
        data = entities ?: listOf()
    }

    init {
        this.reloadRepositoryData()
    }

    open fun reloadRepositoryData() {
        repository.releaseListener(repositoryListener)
        repository.get(repositoryListener)
    }

    override fun getItemCount(): Int = data.count()

    protected open fun getDataItem(position: Int): Entity? = data[position]

    protected open fun onDataChanged(oldValue: List<Entity>, newValue: List<Entity>) {
        notifyDataSetChanged()
    }
}