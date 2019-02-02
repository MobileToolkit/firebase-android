package org.mobiletoolkit.android.repository

/**
 * Created by Sebastian Owodzin on 14/08/2018.
 */
interface AsyncRepository<Identifier, Entity : Model<Identifier>> {

    fun exists(identifier: Identifier, callback: AsyncRepositoryCallback<Boolean>)

    fun get(identifier: Identifier, callback: AsyncRepositoryCallback<Entity?>)

    fun create(entity: Entity, identifier: Identifier? = null, callback: AsyncRepositoryCallback<Boolean>)
    fun create(vararg entities: Entity, callback: AsyncRepositoryCallback<Boolean>)
    fun create(
        entities: List<Entity>,
        identifiers: List<Identifier?>? = null,
        callback: AsyncRepositoryCallback<Boolean>
    )

    fun update(entity: Entity, callback: AsyncRepositoryCallback<Boolean>)
    fun update(vararg entities: Entity, callback: AsyncRepositoryCallback<Boolean>)

    fun delete(entity: Entity, callback: AsyncRepositoryCallback<Boolean>)
    fun delete(identifier: Identifier, callback: AsyncRepositoryCallback<Boolean>)
    fun delete(vararg entities: Entity, callback: AsyncRepositoryCallback<Boolean>)
    fun delete(vararg identifiers: Identifier, callback: AsyncRepositoryCallback<Boolean>)

    fun get(callback: AsyncRepositoryCallback<List<Entity>>)
}

typealias AsyncRepositoryCallback<DataType> = (data: DataType?, exception: Exception?) -> Unit