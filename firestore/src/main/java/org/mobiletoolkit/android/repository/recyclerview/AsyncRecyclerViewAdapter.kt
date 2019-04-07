package org.mobiletoolkit.android.repository.recyclerview

import android.support.v7.widget.RecyclerView
import org.mobiletoolkit.android.repository.AsyncRepository
import org.mobiletoolkit.android.repository.Model
import kotlin.properties.Delegates

/**
 * Created by Sebastian Owodzin on 02/02/2019.
 */
abstract class AsyncRecyclerViewAdapter<ViewHolder : RecyclerView.ViewHolder, Identifier, Entity : Model<Identifier>>(
    private val repository: AsyncRepository<Identifier, Entity>
) : RecyclerView.Adapter<ViewHolder>() {

    protected open var data: List<Entity> by Delegates.observable(listOf()) { _, oldValue, newValue ->
        notifyDataSetChanged()
    }

    init {
        this.reloadRepositoryData()
    }

    open fun reloadRepositoryData() {
        repository.get { entities, _ ->
            data = entities ?: listOf()
        }
    }

    override fun getItemCount(): Int = data.count()

    protected open fun getDataItem(position: Int): Entity? = data[position]
}