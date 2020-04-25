# Development
-
This is the document where I will be posting milestones in the project's completion.

<i> 4/24/19 </i>
-
First real development day, luckily I was able to do some planning in advance so I'm not that far behind in where I want to be.
I was able to get Firestore connected to the API, and so now I can start to write the upload backend for kotlin files.

Schema
--
The main testing schema I want to write involves instructors writing both a problem.kt file and a test.kt file.
All assignments have a title and a description associated with them, but the problem.kt file contains main() function 
and is where the student will write their answer.

The test.kt file contains the tests, using a custom testing framework. The goal is for these tests to be concise and to return meaningful feedback to the user.

The idea is for the user to be able to work in IntelliJ and then submit their solution file into this site for grading. The file
would be named something specific to match it to the corresponding testing file, or use some other method of linking the two.