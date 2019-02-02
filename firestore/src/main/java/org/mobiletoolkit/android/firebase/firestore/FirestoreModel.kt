package org.mobiletoolkit.android.firebase.firestore

import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.IgnoreExtraProperties
import org.mobiletoolkit.android.repository.Model

/**
 * Created by Sebastian Owodzin on 14/08/2018.
 */
@IgnoreExtraProperties
abstract class FirestoreModel : Model<String> {

    @get:Exclude
    var documentReference: DocumentReference? = null

    override fun _identifier() = documentReference?.id
}

fun <Entity : FirestoreModel> DocumentSnapshot.toObjectWithReference(valueType: Class<Entity>): Entity? =
    toObject(valueType)?.let {
        it.documentReference = reference

        it
    }
