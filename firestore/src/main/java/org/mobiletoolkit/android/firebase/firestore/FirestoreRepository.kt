package org.mobiletoolkit.android.firebase.firestore

import android.util.Log
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import org.mobiletoolkit.android.repository.AsyncRepository

/**
 * Created by Sebastian Owodzin on 14/08/2018.
 */
interface FirestoreRepository<Entity : FirestoreModel> : AsyncRepository<String, Entity> {

    companion object {
        private const val TAG = "FirestoreRepository"
    }

    val entityClazz: Class<Entity>  //TODO: make this automatic
//    fun <T: Entity> buildEntity(documentSnapshot: DocumentSnapshot?, ofClass: Class<T>): T? =
//        documentSnapshot?.toObjectWithReference(ofClass)
//
//    inline fun <reified T : Entity> buildEntity(documentSnapshot: DocumentSnapshot?): T? = buildEntity(documentSnapshot, T::class.java)

    val collectionPath: String

    val db: FirebaseFirestore
        get() = FirebaseFirestore.getInstance()

    val collectionReference: CollectionReference
        get() = db.collection(collectionPath)

    val debugEnabled: Boolean
        get() = false

    fun exists(
        identifier: String,
        onCompleteListener: OnCompleteListener<Boolean>
    ) {
        if (debugEnabled) {
            Log.d(TAG, "exists -> collectionPath: $collectionPath | identifier: $identifier")
        }

        documentExists(identifier).addOnCompleteListener(onCompleteListener)
    }

    fun get(
        identifier: String,
        onCompleteListener: OnCompleteListener<Entity?>
    ) {
        if (debugEnabled) {
            Log.d(TAG, "get -> collectionPath: $collectionPath | identifier: $identifier")
        }

        getDocument(identifier).addOnCompleteListener(onCompleteListener)
    }

    fun create(
        entity: Entity,
        identifier: String?,
        onCompleteListener: OnCompleteListener<Boolean>
    ) {
        if (debugEnabled) {
            Log.d(TAG, "create -> collectionPath: $collectionPath | entity: $entity | identifier: $identifier")
        }

        createDocument(entity, identifier).addOnCompleteListener(onCompleteListener)
    }

    fun create(
        vararg entities: Entity,
        onCompleteListener: OnCompleteListener<Boolean>
    ) {
        if (debugEnabled) {
            Log.d(TAG, "create -> collectionPath: $collectionPath | entities: $entities")
        }

        createDocuments(entities.toList()).addOnCompleteListener(onCompleteListener)
    }

    fun create(
        entities: List<Entity>,
        identifiers: List<String?>?,
        onCompleteListener: OnCompleteListener<Boolean>
    ) {
        if (debugEnabled) {
            Log.d(TAG, "create -> collectionPath: $collectionPath | entities: $entities | identifiers: $identifiers")
        }

        createDocuments(entities, identifiers).addOnCompleteListener(onCompleteListener)
    }

    fun update(
        entity: Entity,
        onCompleteListener: OnCompleteListener<Boolean>
    ) {
        if (debugEnabled) {
            Log.d(TAG, "update -> collectionPath: $collectionPath | entity: $entity")
        }

        updateDocument(entity).addOnCompleteListener(onCompleteListener)
    }

    fun update(
        vararg entities: Entity,
        onCompleteListener: OnCompleteListener<Boolean>
    ) {
        if (debugEnabled) {
            Log.d(TAG, "update -> collectionPath: $collectionPath | entities: $entities")
        }

        updateDocuments(entities.toList()).addOnCompleteListener(onCompleteListener)
    }

    fun delete(
        entity: Entity,
        onCompleteListener: OnCompleteListener<Boolean>
    ) {
        if (debugEnabled) {
            Log.d(TAG, "delete -> collectionPath: $collectionPath | entity: $entity")
        }

        deleteDocument(entity).addOnCompleteListener(onCompleteListener)
    }

    fun delete(
        identifier: String,
        onCompleteListener: OnCompleteListener<Boolean>
    ) {
        if (debugEnabled) {
            Log.d(TAG, "delete -> collectionPath: $collectionPath | identifier: $identifier")
        }

        deleteDocument(identifier).addOnCompleteListener(onCompleteListener)
    }

    fun delete(
        vararg entities: Entity,
        onCompleteListener: OnCompleteListener<Boolean>
    ) {
        if (debugEnabled) {
            Log.d(TAG, "delete -> collectionPath: $collectionPath | entities: $entities")
        }

        deleteDocuments(entities.toList()).addOnCompleteListener(onCompleteListener)
    }

    fun delete(
        vararg identifiers: String,
        onCompleteListener: OnCompleteListener<Boolean>
    ) {
        if (debugEnabled) {
            Log.d(TAG, "delete -> collectionPath: $collectionPath | identifiers: $identifiers")
        }

        deleteDocuments(identifiers = identifiers.toList()).addOnCompleteListener(onCompleteListener)
    }

    fun get(
        onCompleteListener: OnCompleteListener<List<Entity>>
    ) {
        if (debugEnabled) {
            Log.d(TAG, "get -> collectionPath: $collectionPath")
        }

        getDocuments().addOnCompleteListener(onCompleteListener)
    }

    fun documentExists(
        identifier: String
    ): Task<Boolean> {
        if (debugEnabled) {
            Log.d(TAG, "documentExists -> collectionPath: $collectionPath | identifier: $identifier")
        }

        return collectionReference.document(identifier).get().continueWith {
            it.result?.exists() == true
        }
    }

    fun getDocument(
        identifier: String
    ): Task<Entity?> {
        if (debugEnabled) {
            Log.d(TAG, "getDocument -> collectionPath: $collectionPath | identifier: $identifier")
        }

        return collectionReference.document(identifier).get().continueWith {
            it.result?.let { doc ->
                if (doc.exists()) {
                    doc.toObjectWithReference(entityClazz)
                } else null
            }
        }
    }

    fun createDocument(
        entity: Entity,
        identifier: String? = null
    ): Task<Boolean> {
        if (debugEnabled) {
            Log.d(TAG, "createDocument -> collectionPath: $collectionPath | entity: $entity | identifier: $identifier")
        }

        return with(collectionReference) {
            (identifier?.let { docId ->
                document(docId)
            } ?: document()).set(entity).continueWith {
                it.isSuccessful
            }
        }
    }

    fun createDocuments(
        entities: List<Entity>,
        identifiers: List<String?>? = null
    ): Task<Boolean> {
        if (debugEnabled) {
            Log.d(
                TAG,
                "createDocuments -> collectionPath: $collectionPath | entities: $entities | identifier: $identifiers"
            )
        }

        //TODO - split into batches of 20

        val batch = db.batch()

        entities.forEachIndexed { index, entity ->
            val docRef = with(collectionReference) {
                (identifiers?.get(index)?.let { docId ->
                    document(docId)
                } ?: document())
            }

            batch.set(docRef, entity)
        }

        return batch.commit().continueWith {
            it.isSuccessful
        }
    }

    fun updateDocument(
        entity: Entity
    ): Task<Boolean> {
        if (debugEnabled) {
            Log.d(TAG, "updateDocument -> collectionPath: $collectionPath | entity: $entity")
        }

        return entity._identifier()?.let { identifier ->
            collectionReference.document(identifier).set(entity, SetOptions.merge()).continueWith {
                it.isSuccessful
            }
        } ?: Tasks.forResult(false)
    }

    fun updateDocuments(
        entities: List<Entity>
    ): Task<Boolean> {
        if (debugEnabled) {
            Log.d(TAG, "updateDocuments -> collectionPath: $collectionPath | entities: $entities")
        }

        //TODO - split into batches of 20

        val batch = db.batch()

        entities.forEach { entity ->
            entity.documentReference?.let { docRef ->
                batch.set(docRef, entity, SetOptions.merge())
            }
        }

        return batch.commit().continueWith {
            it.isSuccessful
        }
    }

    fun deleteDocument(
        entity: Entity
    ): Task<Boolean> {
        if (debugEnabled) {
            Log.d(TAG, "deleteDocument -> collectionPath: $collectionPath | entity: $entity")
        }

        return entity._identifier()?.let { identifier ->
            deleteDocument(identifier)
        } ?: Tasks.forResult(false)
    }

    fun deleteDocument(
        identifier: String
    ): Task<Boolean> {
        if (debugEnabled) {
            Log.d(TAG, "deleteDocument -> collectionPath: $collectionPath | identifier: $identifier")
        }

        return collectionReference.document(identifier).delete().continueWith {
            it.isSuccessful
        }
    }

    fun deleteDocuments(
        entities: List<Entity>? = null,
        identifiers: List<String?>? = null
    ): Task<Boolean> {
        if (debugEnabled) {
            Log.d(TAG, "deleteDocuments -> collectionPath: $collectionPath | entities: $entities | identifiers: $identifiers")
        }

        //TODO - split into batches of 20

        val batch = db.batch()

        entities?.forEach { entity ->
            entity.documentReference?.let { docRef ->
                batch.delete(docRef)
            }
        }

        identifiers?.filterNotNull()?.forEach { identifier ->
            batch.delete(collectionReference.document(identifier))
        }

        return batch.commit().continueWith {
            it.isSuccessful
        }
    }

//    fun batch(
//        createData: List<Pair<Entity, String?>>,
//        updateData: List<Entity>,
//        deleteData: List<Pair<Entity?, String?>>
//    ): Task<Boolean> {
//        if (debugEnabled) {
//            Log.d(
//                TAG,
//                "batch -> collectionPath: $collectionPath | createData: $createData | updateData: $updateData"
//            )
//        }
//
//        val operations = createData.map
//
//        createData.count().toIntRanges(20).forEach {
//
//        }
//    }

    fun getDocuments(): Task<List<Entity>> {
        if (debugEnabled) {
            Log.d(TAG, "getDocuments -> collectionPath: $collectionPath")
        }

        return collectionReference.get().continueWith {
            it.result?.mapNotNull { doc ->
                doc.toObjectWithReference(entityClazz)
            } ?: listOf()
        }
    }
}

//data class Operation<Entity, Identifier>(
//    val createData: Pair<Entity, Identifier?>? = null,
//    val updateData: Entity? = null,
//    val deleteData: Pair<Entity?, Identifier?>? = null
//)