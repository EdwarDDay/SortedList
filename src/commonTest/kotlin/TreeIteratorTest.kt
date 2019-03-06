import de.edwardday.sortedlist.sortedCollectionOf
import kotlin.test.*

class TreeIteratorTest {

    @Test
    fun hasNext() {
        assertFalse { sortedCollectionOf<Int>().iterator().hasNext() }
        assertTrue { sortedCollectionOf(9, 8, 7, 6, 5, 4, 3, 2, 1, 0).iterator().hasNext() }
    }

    @Test
    fun next() {
        assertFailsWith<NoSuchElementException> { sortedCollectionOf<Int>().iterator().next() }
        expect(0) { sortedCollectionOf(9, 8, 7, 6, 5, 4, 3, 2, 1, 0).iterator().next() }
    }
}
