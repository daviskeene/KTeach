package Grading

import java.lang.Exception

import main.Virus as Virus

val cases = listOf(
    Case("Data class has (name) attribute", 1.0) {
        doesMethodExist("getName", "java.lang.String")
    },
    Case("Data class has (rate) attribute", 1.0) {
        doesMethodExist("getRate", "float")
    },
    Case("Data class has a (numberInfected) attribute", 1.0) {
        doesMethodExist("getNumberInfected", "int")
    },
    Case("Data class has an (infect) method", 1.0) {
        doesMethodExist("infect", "void")
    },
    Case("Method (infect) increases number infected by 1", .5) {
        val v = Virus("Polio", 10, 1.0f)
        v.infect()
        v.numberInfected == 11
    }
)

fun doesMethodExist(name : String, type: String) : Boolean {
    return try {
        val method = Virus::class.java.getMethod(name)
        method.genericReturnType.typeName == type
    } catch (e: Exception) {
        false
    }
}

fun main() {
    val (earned, total) = sumScore(cases)
    println(earned)
    println(total)
}
