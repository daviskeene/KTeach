package Services

import java.security.MessageDigest

/*
Authentication methods and helpers
 */

// Returns SHA-256 Hash
fun hash_string_md5(input: String): String {
    val bytes = input.toByteArray()
    val md = MessageDigest.getInstance("MD5")
    val digest = md.digest(bytes)
    return digest.fold("", { str, it -> str + "%02x".format(it) })
}

fun login(email: String, pwd: String): MutableMap<String, Any>? {
    val hash = hash_string_md5("$email$pwd")
    // Check if this account is a student or a teacher
    val student = getStudent(hash)
    val teacher = getTeacher(hash)

    if (student == null) {
        if (teacher == null) {
            return null
        }
        return teacher
    }
    return student
}
