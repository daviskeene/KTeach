package Grading

/*
Your task is to create a method that returns whether or not a string is a palindrome.
The palindromes are NOT case sensitive.
 */

fun palindrome(str: String?) : Boolean {
    // Your method declaration should go here, and return the palindrome as a String.
    // Remember: can String be null according to the function declaration?	
    if (str == null) {
	return false
    }

    return str.reversed() == str
}

//    palindrome("racecar") // should return true
//    palindrome("davis keene") // should return false
