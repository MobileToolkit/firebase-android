package org.mobiletoolkit.android.repository.recyclerview

import androidx.recyclerview.widget.RecyclerView
import org.mobiletoolkit.android.repository.Model
import org.mobiletoolkit.android.repository.Repository
import kotlin.properties.Delegates

/**
 * Created by Sebastian Owodzin on 02/02/2019.
 */
abstract class RecyclerViewAdapter<ViewHolder : RecyclerView.ViewHolder, Identifier, Entity : Model<Identifier>>(
    private val repository: Repository<Identifier, Entity>
) : RecyclerView.Adapter<ViewHolder>() {

    protected open var data: List<Entity> by Delegates.observable(listOf()) { _, oldValue, newValue ->
        notifyDataSetChanged()
    }

    init {
        this.reloadRepositoryData()
    }

    open fun reloadRepositoryData() {
        data = repository.get()
    }

    override fun getItemCount(): Int = data.count()

    protected open fun getDataItem(position: Int): Entity? = data[position]
}