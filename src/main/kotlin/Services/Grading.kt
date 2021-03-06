package Services

import java.time.LocalDate
import java.time.format.DateTimeFormatter

fun computeGrade(possible: Double, total: Double): Double {
    return possible / total
}

fun updateGrade(student_id: String, possible: Double, total: Double) {
    var student_ref = getStudent(student_id)
    student_ref?.set("possible", student_ref.get("possible") as Double + possible)
    student_ref?.set("total", student_ref.get("total") as Double + total)
    student_ref?.set("grade", (student_ref.get("possible") as Double) / student_ref.get("total") as Double)
}

fun availableAssignments(student_id: String): MutableList<MutableMap<String, Any>?> {
    var result: MutableList<MutableMap<String, Any>?> = mutableListOf()
    try {
        var student_ref = getStudent(student_id)
        val completed_assignments = student_ref?.get("completed_assignments") as List<String>
        // Get student's classroom
        val classroom_id = student_ref?.get("classroom_id") as String
        var classroom_ref = getClassroom(classroom_id)
        val assignments = classroom_ref?.get("assignments") as List<String>
        for (assignment in assignments) {
            if (!completed_assignments.contains(assignment)) {
                val assignmentToAdd = getAssignment(assignment)
                // Check the due date
                val current = LocalDate.now()
                val assignmentDate = LocalDate.parse(assignmentToAdd!!["deadline"] as String, DateTimeFormatter.ISO_DATE)

                if (current.isBefore(assignmentDate) || current.isEqual(assignmentDate)) {
                    try {
                        assignmentToAdd?.set(
                            "score", getScoreOnAssignment(
                                student_ref.get("classroom_id") as String,
                                student_id,
                                assignmentToAdd.get("id") as String
                            )
                        )
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                    result.add(assignmentToAdd)
                }
            }
        }
        return result
    } catch (e: Exception) {
        return emptyList<MutableMap<String, Any>>().toMutableList()
    }
}
