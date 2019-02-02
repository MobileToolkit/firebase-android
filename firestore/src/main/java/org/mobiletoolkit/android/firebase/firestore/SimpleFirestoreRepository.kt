package org.mobiletoolkit.android.firebase.firestore

import android.annotation.SuppressLint
import android.util.Log
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.ListenerRegistration
import org.mobiletoolkit.android.repository.AsyncRepository
import org.mobiletoolkit.android.repository.AsyncRepositoryCallback

/**
 * Created by Sebastian Owodzin on 10/12/2018.
 */
@SuppressLint("LongLogTag")
abstract class SimpleFirestoreRepository<Entity : FirestoreModel>(
    override val debugEnabled: Boolean = false
) : FirestoreRepository<Entity>,
    AsyncRepository<String, Entity> {

    companion object {
        private const val TAG = "SimpleFirestoreRepository"
    }

    override fun exists(identifier: String, callback: AsyncRepositoryCallback<Boolean>) {
        if (debugEnabled) {
            Log.d(TAG, "exists -> collectionPath: $collectionPath | identifier: $identifier")
        }

        documentExists(identifier).addOnCompleteListener {
            callback(if (it.isSuccessful) it.result else null, it.exception)
        }
    }

    override fun get(identifier: String, callback: AsyncRepositoryCallback<Entity?>) {
        if (debugEnabled) {
            Log.d(TAG, "get -> collectionPath: $collectionPath | identifier: $identifier")
        }

        getDocument(identifier).addOnCompleteListener {
            callback(if (it.isSuccessful) it.result else null, it.exception)
        }
    }

    override fun create(entity: Entity, identifier: String?, callback: AsyncRepositoryCallback<Boolean>) {
        if (debugEnabled) {
            Log.d(TAG, "create -> collectionPath: $collectionPath | entity: $entity | identifier: $identifier")
        }

        createDocument(entity, identifier).addOnCompleteListener {
            callback(if (it.isSuccessful) it.result else null, it.exception)
        }
    }

    override fun create(vararg entities: Entity, callback: AsyncRepositoryCallback<Boolean>) {
        if (debugEnabled) {
            Log.d(TAG, "create -> collectionPath: $collectionPath | entities: $entities")
        }

        createDocuments(entities.toList()).addOnCompleteListener {
            callback(if (it.isSuccessful) it.result else null, it.exception)
        }
    }

    override fun create(
        entities: List<Entity>,
        identifiers: List<String?>?,
        callback: AsyncRepositoryCallback<Boolean>
    ) {
        if (debugEnabled) {
            Log.d(TAG, "create -> collectionPath: $collectionPath | entities: $entities | identifiers: $identifiers")
        }

        createDocuments(entities, identifiers).addOnCompleteListener {
            callback(if (it.isSuccessful) it.result else null, it.exception)
        }
    }

    override fun update(entity: Entity, callback: AsyncRepositoryCallback<Boolean>) {
        if (debugEnabled) {
            Log.d(TAG, "update -> collectionPath: $collectionPath | entity: $entity")
        }

        updateDocument(entity).addOnCompleteListener {
            callback(if (it.isSuccessful) it.result else null, it.exception)
        }
    }

    override fun update(vararg entities: Entity, callback: AsyncRepositoryCallback<Boolean>) {
        if (debugEnabled) {
            Log.d(TAG, "update -> collectionPath: $collectionPath | entities: $entities")
        }

        updateDocuments(entities.toList()).addOnCompleteListener {
            callback(if (it.isSuccessful) it.result else null, it.exception)
        }
    }

    override fun delete(entity: Entity, callback: AsyncRepositoryCallback<Boolean>) {
        if (debugEnabled) {
            Log.d(TAG, "delete -> collectionPath: $collectionPath | entity: $entity")
        }

        deleteDocument(entity).addOnCompleteListener {
            callback(if (it.isSuccessful) it.result else null, it.exception)
        }
    }

    override fun delete(identifier: String, callback: AsyncRepositoryCallback<Boolean>) {
        if (debugEnabled) {
            Log.d(TAG, "delete -> collectionPath: $collectionPath | identifier: $identifier")
        }

        deleteDocument(identifier).addOnCompleteListener {
            callback(if (it.isSuccessful) it.result else null, it.exception)
        }
    }

    override fun delete(vararg entities: Entity, callback: AsyncRepositoryCallback<Boolean>) {
        if (debugEnabled) {
            Log.d(TAG, "delete -> collectionPath: $collectionPath | entities: $entities")
        }

        deleteDocuments(entities.toList()).addOnCompleteListener {
            callback(if (it.isSuccessful) it.result else null, it.exception)
        }
    }

    override fun delete(vararg identifiers: String, callback: AsyncRepositoryCallback<Boolean>) {
        if (debugEnabled) {
            Log.d(TAG, "delete -> collectionPath: $collectionPath | identifiers: $identifiers")
        }

        deleteDocuments(identifiers = identifiers.toList()).addOnCompleteListener {
            callback(if (it.isSuccessful) it.result else null, it.exception)
        }
    }

    override fun get(callback: AsyncRepositoryCallback<List<Entity>>) {
        if (debugEnabled) {
            Log.d(TAG, "get -> collectionPath: $collectionPath")
        }

        getDocuments().addOnCompleteListener {
            callback(if (it.isSuccessful) it.result else null, it.exception)
        }
    }

    private var documentListenerRegistration: ListenerRegistration? = null

    open fun get(
        identifier: String,
        repositoryListener: FirestoreRepositoryListener<Entity, Entity>
    ): ListenerRegistration {
        if (debugEnabled) {
            Log.d(TAG, "get -> collectionPath: $collectionPath | identifier: $identifier")
        }

        documentListenerRegistration?.remove()
        documentListenerRegistration = null

        val listenerRegistration =
            collectionReference.document(identifier).addSnapshotListener { documentSnapshot, exception ->
                repositoryListener(
                    documentSnapshot?.let {
                        if (it.exists()) {
                            it.toObjectWithReference(entityClazz)
                        } else null
                    },
                    null,
                    exception
                )
            }

        documentListenerRegistration = listenerRegistration

        return listenerRegistration
    }

    private var collectionListenerRegistration: ListenerRegistration? = null

    open fun get(
        repositoryListener: FirestoreRepositoryListener<List<Entity>, Entity>
    ): ListenerRegistration {
        if (debugEnabled) {
            Log.d(TAG, "get -> collectionPath: $collectionPath")
        }

        collectionListenerRegistration?.remove()
        collectionListenerRegistration = null

        val listenerRegistration = collectionReference.addSnapshotListener { querySnapshot, exception ->
            repositoryListener(
                querySnapshot?.documents?.mapNotNull { it.toObjectWithReference(entityClazz) } ?: listOf(),
                querySnapshot?.documentChanges?.mapNotNull {
                    Change(
                        Change.Type.from(it.type),
                        it.oldIndex,
                        it.newIndex,
                        it.document.toObjectWithReference(entityClazz)
                    )
                }?.toSet(),
                exception
            )
        }

        documentListenerRegistration = listenerRegistration

        return listenerRegistration
    }

    data class Change<T>(
        val type: Type,
        val oldIndex: Int = -1,
        val newIndex: Int,
        val data: T? = null
    ) {

        enum class Type {
            Added, Modified, Removed;

            companion object {
                fun from(type: DocumentChange.Type): Type {
                    return Type.values().first { it.ordinal == type.ordinal }
                }
            }
        }
    }
}

typealias FirestoreRepositoryListener<DataType, ChangeType> = (
    data: DataType?,
    changeSet: Set<SimpleFirestoreRepository.Change<ChangeType>>?,
    exception: Exception?
) -> Unit