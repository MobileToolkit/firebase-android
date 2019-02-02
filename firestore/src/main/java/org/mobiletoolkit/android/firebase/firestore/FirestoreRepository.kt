package org.mobiletoolkit.android.firebase.firestore

import android.util.Log
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.WriteBatch
import org.mobiletoolkit.android.extensions.kotlin.toRanges

/**
 * Created by Sebastian Owodzin on 14/08/2018.
 */
interface FirestoreRepository<Entity : FirestoreModel> {

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

    fun documentExists(
        identifier: String
    ): Task<Boolean> {
        if (debugEnabled) {
            Log.d(
                TAG, "documentExists -> collectionPath: $collectionPath | identifier: $identifier"
            )
        }

        return collectionReference.document(identifier).get().continueWith {
            it.result?.exists() == true
        }
    }

    fun getDocument(
        identifier: String
    ): Task<Entity?> {
        if (debugEnabled) {
            Log.d(
                TAG, "getDocument -> collectionPath: $collectionPath | identifier: $identifier"
            )
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
            Log.d(
                TAG, "createDocument -> collectionPath: $collectionPath " +
                        "\n  * entity: $entity " +
                        "\n  * identifier: $identifier"
            )
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
                TAG, "createDocuments -> collectionPath: $collectionPath " +
                        "\n  * entities: $entities " +
                        "\n  * identifier: $identifiers"
            )
        }

//        //TODO - split into batches of 20
//
//        val batch = db.batch()
//
//        entities.forEachIndexed { index, entity ->
//            val docRef = with(collectionReference) {
//                (identifiers?.get(index)?.let { docId ->
//                    document(docId)
//                } ?: document())
//            }
//
//            batch.set(docRef, entity)
//        }

        return batch(createData = entities.zip(identifiers ?: listOf())).continueWith {
            it.isSuccessful
        }
    }

    fun updateDocument(
        entity: Entity
    ): Task<Boolean> {
        if (debugEnabled) {
            Log.d(
                TAG, "updateDocument -> collectionPath: $collectionPath " +
                        "\n  * entity: $entity"
            )
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
            Log.d(
                TAG, "updateDocuments -> collectionPath: $collectionPath " +
                        "\n  * entities: $entities"
            )
        }

//        //TODO - split into batches of 20
//
//        val batch = db.batch()
//
//        entities.forEach { entity ->
//            entity.documentReference?.let { docRef ->
//                batch.set(docRef, entity, SetOptions.merge())
//            }
//        }

        return batch(updateData = entities).continueWith {
            it.isSuccessful
        }
    }

    fun deleteDocument(
        entity: Entity
    ): Task<Boolean> {
        if (debugEnabled) {
            Log.d(
                TAG, "deleteDocument -> collectionPath: $collectionPath\n" +
                        "  * entity: $entity"
            )
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
            Log.d(
                TAG, "deleteDocuments -> collectionPath: $collectionPath\n" +
                        "  * entities: $entities\n" +
                        "  * identifiers: $identifiers"
            )
        }

//        //TODO - split into batches of 20
//
//        val batch = db.batch()
//
//        entities?.forEach { entity ->
//            entity.documentReference?.let { docRef ->
//                batch.delete(docRef)
//            }
//        }
//
//        identifiers?.filterNotNull()?.forEach { identifier ->
//            batch.delete(collectionReference.document(identifier))
//        }

        return batch(deleteData = entities?.zip(identifiers ?: listOf())).continueWith {
            it.isSuccessful
        }
    }

    fun batch(
        createData: List<Pair<Entity, String?>>? = null,
        updateData: List<Entity>? = null,
        deleteData: List<Pair<Entity?, String?>>? = null
    ): Task<Boolean> {
        if (debugEnabled) {
            Log.d(
                TAG, "batch -> collectionPath: $collectionPath\n" +
                        "  * createData: $createData\n" +
                        "  * updateData: $updateData\n" +
                        "  * deleteData: $deleteData"
            )
        }

        val operations = (createData?.map { BatchOperation(createData = it) } ?: listOf())
            .plus(updateData?.map { BatchOperation(updateData = Pair(it, it._identifier())) } ?: listOf())
            .plus(deleteData?.map { BatchOperation(deleteData = it) } ?: listOf())

        if (debugEnabled) {
            Log.d(TAG, "batch -> operations: $operations")
        }

        val batches: List<WriteBatch> = operations.count().toRanges(500).map {
            val batch = db.batch()

            operations.slice(it).forEach { slice ->
                when {
                    slice.createData != null -> {
                        val entity = slice.createData.first
                        val identifier = slice.createData.second

                        val docRef = with(collectionReference) {
                            identifier?.let { docId -> document(docId) } ?: document()
                        }

                        batch.set(docRef, entity)
                    }

                    slice.updateData != null -> {
                        val entity = slice.updateData.first
                        val identifier = slice.updateData.second

                        with(collectionReference) {
                            entity.documentReference ?: identifier?.let { docId -> document(docId) }
                        }?.let { docRef ->
                            batch.set(docRef, entity, SetOptions.merge())
                        }
                    }

                    slice.deleteData != null -> {
                        val entity = slice.deleteData.first
                        val identifier = slice.deleteData.second

                        with(collectionReference) {
                            entity?.documentReference ?: identifier?.let { docId -> document(docId) }
                        }?.let { docRef ->
                            batch.delete(docRef)
                        }
                    }
                }
            }

            batch
        }

        return Tasks.whenAll(batches.map { it.commit() }).continueWith {
            it.isSuccessful
        }
    }

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

    data class BatchOperation<Entity, Identifier>(
        val createData: Pair<Entity, Identifier?>? = null,
        val updateData: Pair<Entity, Identifier>? = null,
        val deleteData: Pair<Entity?, Identifier?>? = null
    )
}
