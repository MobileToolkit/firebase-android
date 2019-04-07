package org.mobiletoolkit.android.firebase.firestore.recyclerview

import android.support.v7.widget.RecyclerView
import org.mobiletoolkit.android.firebase.firestore.FirestoreModel
import org.mobiletoolkit.android.firebase.firestore.SimpleFirestoreRepository
import org.mobiletoolkit.android.repository.AsyncRepository
import org.mobiletoolkit.android.repository.AsyncRepositoryListener

/**
 * Created by Sebastian Owodzin on 16/12/2018.
 */
abstract class SimpleFirestoreRecyclerViewAdapter<ViewHolder : RecyclerView.ViewHolder, Entity : FirestoreModel>(
    private val repository: SimpleFirestoreRepository<Entity>
) : RecyclerView.Adapter<ViewHolder>() {

    protected open var data: List<Entity> = listOf()

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