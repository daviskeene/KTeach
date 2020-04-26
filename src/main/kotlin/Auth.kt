import java.security.MessageDigest

/*
Authentication methods and helpers
 */

// Returns SHA-256 Hash
fun hash_string_md5(input: String) : String {
    val bytes = input.toByteArray()
    val md = MessageDigest.getInstance("MD5")
    val digest = md.digest(bytes)
    return digest.fold("", { str, it -> str + "%02x".format(it)})
}