/* Authentication backend methods and helper functions */

// DB Entry & Retrieval
import com.google.cloud.firestore.FirestoreOptions
import com.google.cloud.firestore.Firestore

// Constants
class Constants {
    companion object {
        val students_col = "Students"
        val teachers_col = "Teacher"
        val assignments_col = "Assignments"

        val student_form = "student_form"
        val teacher_form = "teacher_form"
        val assignment_form = "assignment_form"

        val db = FirestoreOptions.newBuilder()
            .setTimestampsInSnapshotsEnabled(true)
            .build()
            .service
    }
}

// Returns a document from the firestore instance given path
fun getDocumentFromDB(col: String, doc: String,  db: Firestore) : MutableMap<String, Any>? {
    return db.collection(col).document(doc)
        .get()
        .get()
        .data
}

// Student Methods

// Return student template
fun getStudentTemplate() : MutableMap<String, Any>? {
    val template = getDocumentFromDB(Constants.students_col, Constants.student_form, Constants.db)
    return template
}

// Generate new ID
fun createID(uname: String, pwd: String) : String {
    val input = "{}{}".format(uname, pwd)
    val output = hash_string_md5(input)
    return output
}

// Add student to the firestore
fun addNewStudent(fname: String, lname: String, pwd: String, email: String, classroom_id: String) : MutableMap<String, Any>? {
    val id = createID(email, pwd)
    // Get the student template
    var temp_student = getStudentTemplate()
    // Set fields
    temp_student?.set("first_name", fname)
    temp_student?.set("last_name", lname)
    temp_student?.set("email", email)
    temp_student?.set("classroom_id", classroom_id)
    // Add new student to firestore
    var doc_ref = Constants.db
        .collection(Constants.students_col)
        .document(id)

    doc_ref.set(temp_student!!)
    return temp_student
}

fun getStudent(id: String) : MutableMap<String, Any>? {
    return getDocumentFromDB(Constants.students_col, id, Constants.db)
}