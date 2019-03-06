import de.edwardday.sortedlist.AvlTree
import de.edwardday.sortedlist.RedBlackTree
import de.edwardday.sortedlist.SortedList
import kotlin.random.Random
import kotlin.test.*

class AbstractSortedListTest {

    private fun <T : Comparable<T>> createSortedList(vararg elements: T): List<SortedList<T>> {
        return listOf(
            AvlTree<T>(naturalOrder()) + elements.asList(),
            RedBlackTree<T>(naturalOrder()) + elements.asList()
        )
    }

    private fun <T> createSortedList(comparator: Comparator<T>, vararg elements: T): List<SortedList<T>> {
        return listOf(
            AvlTree(comparator) + elements.asList(),
            RedBlackTree(comparator) + elements.asList()
        )
    }

    private fun <T : Comparable<T>> Collection<T>.asSortedLists(): List<SortedList<T>> {
        return listOf(
            AvlTree<T>(naturalOrder()) + this,
            RedBlackTree<T>(naturalOrder()) + this
        )
    }

    @Test
    fun size() {
        createSortedList<Int>().forEach { assertEquals(0, it.size) }
        createSortedList(9, 8, 7, 6, 5, 4, 3, 2, 1, 0).forEach { assertEquals(10, it.size) }
        createSortedList(1, 1, 1).forEach { assertEquals(3, it.size) }
    }

    @Test
    fun sizeRandom() {
        val random = Random(5647382910)
        repeat(1000) {
            val list = List(random.nextInt(1000)) { TestComparable(random.nextInt(100), random.nextInt(100)) }
            list.asSortedLists().forEach { sortedList ->
                assertEquals(list.size, sortedList.size)
            }
        }
    }

    @Test
    fun contains() {
        createSortedList<Int>().forEach { assertFalse { it.contains(0) } }
        createSortedList(9, 8, 7, 6, 5, 4, 3, 2, 1, 0).forEach { assertTrue { it.contains(0) } }
        createSortedList(9, 8, 7, 6, 5, 4, 3, 2, 1, 0).forEach { assertFalse { it.contains(10) } }
        createSortedList(TestComparable(0, 0), TestComparable(1, 0), TestComparable(2, 0)).forEach {
            assertFalse { it.contains(TestComparable(1, 1)) }
        }
        createSortedList(TestComparable(1, 0), TestComparable(1, 1), TestComparable(1, 2)).forEach {
            assertTrue { it.contains(TestComparable(1, 0)) }
        }
        createSortedList(TestComparable(1, 0), TestComparable(1, 0), TestComparable(1, 0)).forEach {
            assertTrue { it.contains(TestComparable(1, 0)) }
        }
    }

    @Test
    fun containsRandom() {
        val random = Random(5647382910)
        repeat(100) {
            val list = List(random.nextInt(1000)) { TestComparable(random.nextInt(10), random.nextInt(10)) }
            list.asSortedLists().forEach { sortedList ->
                repeat(100) {
                    val checked = TestComparable(random.nextInt(10), random.nextInt(10))
                    assertEquals(list.contains(checked), sortedList.contains(checked))
                }
            }
        }
    }

    @Test
    fun containsAll() {
        createSortedList<Int>().forEach { assertFalse { it.containsAll(listOf(0, 1, 2)) } }
        createSortedList(9, 8, 7, 6, 5, 4, 3, 2, 1, 0).forEach { assertTrue { it.containsAll(listOf(0, 1, 2)) } }
        createSortedList(9, 8, 7, 6, 5, 4, 3, 2, 1, 0).forEach { assertFalse { it.containsAll(listOf(10, 5, 2)) } }
        createSortedList(TestComparable(0, 0), TestComparable(1, 0), TestComparable(2, 0)).forEach {
            assertFalse { it.containsAll(listOf(TestComparable(1, 0), TestComparable(1, 1))) }
        }
        createSortedList(TestComparable(1, 0), TestComparable(1, 1), TestComparable(1, 2)).forEach {
            assertTrue { it.containsAll(listOf(TestComparable(1, 0), TestComparable(1, 2))) }
        }
        createSortedList(TestComparable(1, 0), TestComparable(1, 0), TestComparable(1, 0)).forEach {
            assertTrue {
                it.containsAll(
                    listOf(TestComparable(1, 0), TestComparable(1, 0), TestComparable(1, 0), TestComparable(1, 0))
                )
            }
        }
    }

    @Test
    fun containsAllRandom() {
        val random = Random(5647382910)
        repeat(100) {
            val list = List(random.nextInt(1000)) { TestComparable(random.nextInt(10), random.nextInt(10)) }
            list.asSortedLists().forEach { sortedList ->
                repeat(100) {
                    val checked = List(random.nextInt(10)) { TestComparable(random.nextInt(10), random.nextInt(10)) }
                    assertEquals(list.containsAll(checked), sortedList.containsAll(checked))
                }
            }
        }
    }

    @Test
    fun min() {
        createSortedList<Int>().forEach { assertNull(it.min()) }
        createSortedList(9, 8, 7, 6, 5, 4, 3, 2, 1, 0).forEach { assertEquals(0, it.min()) }
        createSortedList(TestComparable(0, 0), TestComparable(0, -1), TestComparable(2, 0)).forEach {
            assertEquals(TestComparable(0, 0), it.min())
        }
    }

    @Test
    fun minRandom() {
        val random = Random(5647382910)
        repeat(1000) {
            val list = List(random.nextInt(1000)) { TestComparable(random.nextInt(100), random.nextInt(100)) }
            list.asSortedLists().forEach { sortedList ->
                assertEquals(list.min(), sortedList.min())
            }
        }
    }

    @Test
    fun max() {
        createSortedList<Int>().forEach { assertNull(it.max()) }
        createSortedList(9, 8, 7, 6, 5, 4, 3, 2, 1, 0).forEach { assertEquals(9, it.max()) }
        createSortedList(TestComparable(0, 0), TestComparable(2, -1), TestComparable(2, 0)).forEach {
            assertEquals(TestComparable(2, 0), it.max())
        }
    }

    @Test
    fun maxRandom() {
        val random = Random(5647382910)
        repeat(1000) { i ->
            val list = List(random.nextInt(1000)) { TestComparable(random.nextInt(100), random.nextInt(100)) }
            list.asSortedLists().forEach { sortedList ->
                if (list.isEmpty()) {
                    assertNull(sortedList.max())
                } else {
                    val compared = naturalOrder<TestComparable>().compare(list.max()!!, sortedList.max()!!)
                    assertEquals(0, compared, "max failed at $i")
                }
            }
        }
    }

    @Test
    fun checkSorted() {
        createSortedList(9, 8, 7, 6, 5, 4, 3, 2, 1, 0).forEach {
            assertEquals(listOf(0, 1, 2, 3, 4, 5, 6, 7, 8, 9), it, "collection should be sorted")
        }
        val random = Random(123456789)
        val unsortedList = List(10000) { random.nextInt() }
        unsortedList.asSortedLists().forEach { sortedList ->
            assertEquals(unsortedList.sorted(), sortedList, "collection should be sorted")
        }
        createSortedList(
            TestComparable(2, 0), TestComparable(4, 0), TestComparable(2, 0),
            TestComparable(1, 0), TestComparable(0, 0)
        ).forEach {
            assertEquals(
                listOf(
                    TestComparable(0, 0), TestComparable(1, 0), TestComparable(2, 0),
                    TestComparable(2, 0), TestComparable(4, 0)
                ),
                it,
                "collection should be sorted"
            )
        }
    }

    @Test
    fun sortedRandom() {
        val random = Random(5647382910)
        repeat(1000) {
            val list = List(random.nextInt(1000)) { TestComparable(random.nextInt(100), random.nextInt(100)) }
            val listSorted = list.sorted()
            list.asSortedLists().forEach { sortedList -> assertEquals(listSorted, sortedList) }
        }
    }

    @Test
    fun usesComparator() {
        createSortedList(reverseOrder(), 0, 1, 2, 3, 4, 5, 6, 7, 8, 9).forEach { sortedList ->
            assertEquals(listOf(9, 8, 7, 6, 5, 4, 3, 2, 1, 0), sortedList)
        }

        createSortedList(nullsLast(reverseOrder()), 0, 1, 2, 3, null, 4, 5, 6, 7, 8, null, 9).forEach { sortedList ->
            assertEquals(listOf(9, 8, 7, 6, 5, 4, 3, 2, 1, 0, null, null), sortedList)
        }
    }

    @Test
    fun plus() {
        createSortedList<Int>().forEach { assertEquals(listOf(1), it + 1) }
        createSortedList(1, 2, 3, 4, 5, 6, 7, 8).forEach {
            assertEquals(listOf(1, 2, 3, 4, 5, 6, 7, 8, 9), it + 9)
        }
        createSortedList<Int>().forEach { assertEquals(listOf(1), it + listOf(1)) }
        createSortedList(1).forEach {
            assertEquals(listOf(1, 2, 3, 4, 5, 6, 7, 8, 9), it + listOf(9, 8, 7, 6, 5, 4, 3, 2))
        }
    }

    @Test
    fun plusRandom() {
        val random = Random(5647382910)
        repeat(100) {
            val list = List(random.nextInt(1000)) { TestComparable(random.nextInt(30), random.nextInt(30)) }
            list.asSortedLists().forEach { sortedList ->
                repeat(100) {
                    val checked = TestComparable(random.nextInt(30), random.nextInt(30))
                    val listSorted = (list + checked).sorted()
                    assertEquals(listSorted, sortedList + checked)
                }
            }
        }
    }

    @Test
    fun minus() {
        createSortedList(0, 1, 2, 3, 4, 5, 6, 7, 8, 9).forEach {
            assertEquals(listOf(1, 2, 3, 4, 5, 6, 7, 8, 9), it - 0)
        }
        createSortedList<Int>().forEach { assertEquals(listOf<Int>(), it - 1) }
        createSortedList(1).forEach { assertEquals(listOf<Int>(), it - 1) }
        createSortedList(1, 2, 3).forEach { assertEquals(listOf(1, 2, 3), it - 4) }
        createSortedList(0, 1, 2, 3, 4, 5, 6, 7, 8, 9).forEach {
            assertEquals(listOf(1, 2, 3, 4, 6, 7, 9), it - listOf(0, 8, 5))
        }
        createSortedList<Int>().forEach { assertEquals(listOf<Int>(), it - listOf(1, 5, 8)) }
        createSortedList(1).forEach { assertEquals(listOf<Int>(), it - listOf(1)) }
        createSortedList(1, 2, 3).forEach { assertEquals(listOf(1, 2, 3), it - listOf(4, 7, 0)) }
        createSortedList(1, 2, 3).forEach { assertEquals(listOf(1, 2, 3), it - emptyList()) }
    }

    @Test
    fun minusRandom() {
        val random = Random(5647382910)
        repeat(100) {
            val list = List(random.nextInt(1000)) { TestComparable(random.nextInt(30), random.nextInt(30)) }
            list.asSortedLists().forEach { sortedList ->
                repeat(100) {
                    val removed = TestComparable(random.nextInt(30), random.nextInt(30))
                    val listSorted = (list - removed).sorted()
                    val sortedListRemoved = sortedList - removed
                    assertEquals(listSorted.size, sortedListRemoved.size)
                    assertTrue(listSorted.containsAll(sortedListRemoved))
                }
            }
        }
    }

    data class TestComparable(val comparer: Int, val eq: Int) : Comparable<TestComparable> {
        override fun compareTo(other: TestComparable): Int = comparer.compareTo(other.comparer)
    }
}
