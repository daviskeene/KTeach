/* Authentication backend methods and helper functions */

// DB Entry & Retrieval
import com.google.cloud.firestore.FirestoreOptions
import com.google.cloud.firestore.Firestore

fun getDocumentFromDB(col: String, doc: String,  db: Firestore) : MutableMap<String, Any>? {
    return db.collection(col).document(doc)
        .get()
        .get()
        .data
}

