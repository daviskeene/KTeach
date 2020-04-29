package Services

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
                result.add(getAssignment(assignment))
            }
        }
        return result
    } catch (e: Exception) {
        return emptyList<MutableMap<String, Any>>().toMutableList()
    }
}
