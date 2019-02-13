package org.mobiletoolkit.android.firebase.firestore

import android.annotation.SuppressLint
import android.util.Log
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.ListenerRegistration
import org.mobiletoolkit.android.repository.AsyncRepository
import org.mobiletoolkit.android.repository.AsyncRepositoryCallback
import org.mobiletoolkit.android.repository.AsyncRepositoryListener
import java.lang.ref.WeakReference

/**
 * Created by Sebastian Owodzin on 10/12/2018.
 */
@SuppressLint("LongLogTag")
abstract class SimpleFirestoreRepository<Entity : FirestoreModel>(
    override val debugEnabled: Boolean = false
) : FirestoreRepository<Entity>, AsyncRepository<String, Entity> {

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

    private val repositoryListeners: MutableMap<String, Pair<ListenerRegistration, WeakReference<*>>> = mutableMapOf()

    override fun get(identifier: String, listener: AsyncRepositoryListener<Entity, Entity>) {
        if (debugEnabled) {
            Log.d(TAG, "get -> collectionPath: $collectionPath | identifier: $identifier")
        }

        repositoryListeners.remove(identifier)?.first?.remove()

        val listenerRegistration = documentReference(identifier).addSnapshotListener { documentSnapshot, exception ->
            listener(
                documentSnapshot?.let {
                    if (it.exists()) {
                        it.toObjectWithReference(entityClazz)
                    } else null
                },
                null,
                exception
            )
        }

        repositoryListeners[identifier] = listenerRegistration to WeakReference(listener)
    }

    override fun get(listener: AsyncRepositoryListener<List<Entity>, Entity>) {
        if (debugEnabled) {
            Log.d(TAG, "get -> collectionPath: $collectionPath")
        }

        repositoryListeners.remove(collectionPath)?.first?.remove()

        val listenerRegistration = collectionReference.addSnapshotListener { querySnapshot, exception ->
            listener(
                querySnapshot?.documents?.mapNotNull { it.toObjectWithReference(entityClazz) } ?: listOf(),
                querySnapshot?.documentChanges?.mapNotNull {
                    FirebaseChange(
                        it.type.toChangeType(),
                        it.oldIndex,
                        it.newIndex,
                        it.document.toObjectWithReference(entityClazz)
                    )
                }?.toSet(),
                exception
            )
        }

        repositoryListeners[collectionPath] = listenerRegistration to WeakReference(listener)
    }

    override fun releaseListener(listener: AsyncRepositoryListener<*, Entity>) {
        repositoryListeners.entries.asSequence().filter {
            it.value.second.get() == listener
        }.map {
            it.value.first.remove() // killing ListenerRegistration

            it.key // returning key to remove from map in the next step
        }.forEach {
            repositoryListeners.remove(it)
        }
    }

    private fun DocumentChange.Type.toChangeType(): AsyncRepository.Change.Type =
        AsyncRepository.Change.Type.values().first { it.ordinal == ordinal }

    data class FirebaseChange<T>(
        override val type: AsyncRepository.Change.Type,
        override val oldIndex: Int = -1,
        override val newIndex: Int,
        override val data: T? = null
    ) : AsyncRepository.Change<T>
}