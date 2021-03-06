package Services

/* Authentication backend methods and helper functions */

// DB Entry & Retrieval
import com.google.cloud.firestore.Firestore
import com.google.cloud.firestore.FirestoreOptions
import org.joda.time.DateTime
import org.joda.time.LocalDate
import org.joda.time.LocalDateTime
import java.lang.Exception
import java.lang.NullPointerException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.streams.asSequence

// Services.Constants
class Constants {
    companion object {
        val students_col = "Students"
        val teachers_col = "Teachers"
        val classrooms_col = "Classrooms"
        val assignments_col = "Assignments"

        val student_form = "student_form"
        val teacher_form = "teacher_form"
        val classroom_form = "classroom_form"
        val assignment_form = "assignment_form"

        // db instance declared here, only needs to be declared once
        val db = FirestoreOptions.newBuilder()
            .setTimestampsInSnapshotsEnabled(true)
            .build()
            .service

        val alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
    }
}

fun getDB() : Any? {
    try {
        val firestore =  FirestoreOptions.newBuilder()
            .setTimestampsInSnapshotsEnabled(true)
            .build()
            .service
    } catch (e: Exception) {
        e.printStackTrace()
        return null
    }
    return 0
}

fun randomString(length: Long): String {
    return Random().ints(length, 0, Constants.alphabet.length)
        .asSequence()
        .map(Constants.alphabet::get)
        .joinToString("")
}

// Returns a document from the firestore instance given path
fun getDocumentFromDB(col: String, doc: String, db: Firestore): MutableMap<String, Any>? {
    val data = db.collection(col).document(doc)
        .get()
        .get()
        .data
    return data
}

// Generate new ID
fun createID(uname: String, pwd: String): String {
    val input = "$uname$pwd".format(uname, pwd)
    val output = hash_string_md5(input)
    return output
}

// Student Methods

// Return student template
fun getStudentTemplate(): MutableMap<String, Any>? {
    val template = getDocumentFromDB(
        Constants.students_col,
        Constants.student_form,
        Constants.db
    )
    return template
}

// Add student to the firestore
fun addNewStudent(fname: String, lname: String, pwd: String, email: String, classroom_id: String): MutableMap<String, Any>? {
    val id = createID(email, pwd)
    // Get the student template
    val temp_student = getStudentTemplate()
    // Set fields
    temp_student?.set("id", id)
    temp_student?.set("first_name", fname)
    temp_student?.set("last_name", lname)
    temp_student?.set("email", email)
    temp_student?.set("classroom_id", classroom_id)
    // Add new student to firestore
    val doc_ref = Constants.db
        .collection(Constants.students_col)
        .document(id)

    // Add student to classroom
    val classroom = getClassroom(classroom_id)
    if (classroom == null) {
        return null;
    }
    val students: MutableList<String> = classroom.get("students") as MutableList<String>
    students.add(id)
    classroom.set("students", students)
    val class_ref = Constants.db.collection(Constants.classrooms_col).document(classroom_id)
    class_ref.set(classroom)

    doc_ref.set(temp_student!!)

    return temp_student
}

fun updateStudent(
    id: String,
    firstName: String,
    lastName: String,
    pwd: String,
    email: String) {

}

fun getStudent(id: String): MutableMap<String, Any>? {
    return getDocumentFromDB(
        Constants.students_col,
        id,
        Constants.db
    )
}

// Teacher methods
fun getTeacherTemplate(): MutableMap<String, Any>? {
    return getDocumentFromDB(
        Constants.teachers_col,
        Constants.teacher_form,
        Constants.db
    )
}

fun addNewTeacher(fname: String, lname: String, pwd: String, email: String): MutableMap<String, Any>? {
    val id = createID(email, pwd)
    val temp_teacher = getTeacherTemplate()
    // Set fields
    temp_teacher?.set("id", id)
    temp_teacher?.set("first_name", fname)
    temp_teacher?.set("last_name", lname)
    temp_teacher?.set("email", email)
    // Add teacher to firestore
    val doc_ref = Constants.db
        .collection(Constants.teachers_col)
        .document(id)

    doc_ref.set(temp_teacher!!)

    val classroom = addNewClassroom(email, pwd, true)
    val courses: MutableList<String> = temp_teacher.get("courses") as MutableList<String>
    courses.add(classroom?.get("id") as String)
    temp_teacher.set("courses", courses)
    doc_ref.set(temp_teacher)
    return temp_teacher
}

fun getTeacher(id: String): MutableMap<String, Any>? {
    return getDocumentFromDB(
        Constants.teachers_col,
        id,
        Constants.db
    )
}

fun updateTeacher(
    id: String,
    firstName: String,
    lastName: String,
    pwd: String,
    email: String
) {

}

// Classroom methods
fun getClassroomTemplate(): MutableMap<String, Any>? {
    return getDocumentFromDB(
        Constants.classrooms_col,
        Constants.classroom_form,
        Constants.db
    )
}

// All classrooms must have a teacher associated with them
fun addNewClassroom(teacherEmail: String, pwd: String, newTeacher: Boolean): MutableMap<String, Any>? {
    // First, make sure that the teacher actually exists.
    val teacher_id = createID(teacherEmail, pwd)
    if (!newTeacher) {
        val teacher = getTeacher(teacher_id)
        if (teacher == null) {
            return null
        }
    }

    // Generate random string for new classrooms, no need to hash. Collisions rare.
    val id = randomString(6)

    val temp_classroom = getClassroomTemplate()
    // Set fields
    temp_classroom?.set("id", id)
    temp_classroom?.set("teacher", teacher_id)

    // Add course to teacher
    var teacher = getTeacher(teacher_id)
    val courses: MutableList<String> = teacher?.get("courses") as MutableList<String>
    courses.add(id)
    teacher.set("courses", courses)

    val doc_ref = Constants.db
        .collection(Constants.classrooms_col)
        .document(id)

    doc_ref.set(temp_classroom!!)

    val teacher_ref = Constants.db
        .collection(Constants.classrooms_col)
        .document(teacher_id)
    teacher_ref.set(teacher)

    return temp_classroom
}

fun getClassroom(classroom_id: String): MutableMap<String, Any>? {
    return getDocumentFromDB(
        Constants.classrooms_col,
        classroom_id,
        Constants.db
    )
}

// Gets classrooms with assignment data
fun getClassroomVerbose(classroom_id: String) : MutableMap<String, Any>? {
    val list : MutableList<MutableMap<String, Any>> = mutableListOf()
    val assignmentIDs : MutableList<String> = mutableListOf()
    val classroom = getClassroom(classroom_id) as MutableMap<String, Any>
    val assignments = classroom?.get("assignments") as MutableList<String>
    for (assignment in assignments) {
        val assignment_data = getAssignment(assignment)
        if (assignment_data != null) {
            list.add(assignment_data)
            assignmentIDs.add(assignment_data["id"] as String)
        }
    }
    classroom.set("assignments", list)
    // update cloud instance
    val classroom_ref = Constants.db
        .collection(Constants.classrooms_col)
        .document(classroom_id)
    val data = classroom_ref.get().get().data!!
    data.set("assignments", assignmentIDs)
    classroom_ref.set(data)
    return classroom
}

// Assignments methods
fun getAssignmentTemplate(): MutableMap<String, Any>? {
    return getDocumentFromDB(
        Constants.assignments_col,
        Constants.assignment_form,
        Constants.db
    )
}

fun addNewAssignment(
    classroom_id: String,
    title: String,
    desc: String,
    problem: String,
    tests: String,
    deadline: String
): MutableMap<String, Any>? {

    val id = randomString(8)
    val temp_assignment = getAssignmentTemplate()
    // Set fields
    temp_assignment?.set("title", title)
    temp_assignment?.set("description", desc)
    temp_assignment?.set("problem", problem)
    temp_assignment?.set("test", tests)
    temp_assignment?.set("id", id)
    // deadline logic
    temp_assignment?.set("deadline", deadline)
    // Add to firestore
    val doc_ref = Constants.db.collection(Constants.assignments_col).document(id)
    doc_ref.set(temp_assignment!!)

    // Link it to a classroom
    val classroom = getClassroom(classroom_id)
    val assignments: MutableList<String> = classroom?.get("assignments") as MutableList<String>
    assignments.add(id)
    classroom.set("assignments", assignments)
    val class_ref = Constants.db.collection(Constants.classrooms_col).document(classroom_id)
    class_ref.set(classroom)

    return temp_assignment
}

fun updateAssignment(id: String, title: String, description: String,
                     problem: String, tests: String, deadline: String) {
    val assignment = getAssignment(id)
    assignment?.set("title", title)
    assignment?.set("description", description)
    assignment?.set("problem", problem)
    assignment?.set("test", tests)
    assignment?.set("deadline", deadline)
    val assign_ref = Constants.db.collection(Constants.assignments_col).document(id)
    assign_ref.set(assignment!!)
}

fun getAssignment(id: String): MutableMap<String, Any>? {
    return getDocumentFromDB(
        Constants.assignments_col,
        id,
        Constants.db
    )
}

fun getScoreOnAssignment(classroomID: String, studentID: String, assignmentID: String) : List<Double> {
    try {
        val classroom = getClassroom(classroomID)
        val gradebook = classroom?.get("gradebook") as MutableMap<String, Any>?
        val studentRecord = gradebook?.get(studentID) as MutableMap<String, Any>?
        val assignmentResults = studentRecord?.get(assignmentID) as List<Double>
        return assignmentResults
    } catch (e: Exception) {
        return listOf(0.0, 1.0) // Placeholders
    }
}

fun updateGradebook(classroomID : String, studentID : String, assignmentID: String, pointsScored: Double, pointsTotal:Double) {
    val classroom_ref = Constants.db.collection(Constants.classrooms_col).document(classroomID)
    var classroom = getClassroom(classroomID)
    var gradebook = classroom?.get("gradebook") as MutableMap<String, Any>? // Map of students
    // Check to see if assignment record exists
    if (gradebook?.get(studentID) == null) {
        // If an assignment doesn't already exist, create one.
        gradebook?.set(studentID, mutableMapOf<String, Any>())
    }
    val studentRecords = gradebook?.get(studentID) as MutableMap<String, Any>? // Map of assignments

    if (studentRecords?.get(assignmentID) == null) {
        studentRecords?.set(assignmentID, mutableListOf(0.0, 10.0)) // Placeholders
    }
    // Update assignment record
    var assignmentRecords = studentRecords?.get(assignmentID) as MutableList<Double> // List of doubles
    // Only update scores if the points scored is greater than or equal to the best attempt
    if (pointsScored >= assignmentRecords.get(0)) {
        assignmentRecords.set(0, pointsScored)
        assignmentRecords.set(1, pointsTotal)
    } else if (assignmentRecords.get(1) < pointsTotal) {
        assignmentRecords.set(1, pointsTotal)
    }
    classroom_ref.set(classroom!!)
}