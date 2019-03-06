import kotlin.test.assertEquals
import kotlin.test.assertTrue

fun <T : Comparable<T>> assertAtMost(expected: T, actual: T, message: String? = null) {
    assertTrue(expected >= actual, messagePrefix(message) + "Expected: <$expected> >= <$actual>.")
}

fun <T : Comparable<T>> assertAtMost(expected: T, message: String? = null, block: () -> T) {
    assertAtMost(expected, block(), message)
}

fun <T : Comparable<T>> assertAtLeast(expected: T, actual: T, message: String? = null) {
    assertTrue(expected <= actual, messagePrefix(message) + "Expected: <$expected> <= <$actual>.")
}

fun <T> assertListEquals(expected: List<T>, actual: List<T>, message: String? = null) {
    assertEquals(expected.size, actual.size, messagePrefix(message) + "Size of lists do not match")
    val iterator = actual.iterator()
    for ((index, value) in expected.withIndex()) {
        assertEquals(value, iterator.next(), messagePrefix(message) + "Lists do not match at index $index")
    }
}

fun messagePrefix(message: String?) = if (message == null) "" else "$message. "
