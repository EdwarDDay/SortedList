import de.edwardday.sortedlist.AbstractTree
import de.edwardday.sortedlist.AvlTree
import de.edwardday.sortedlist.RedBlackTree
import de.edwardday.sortedlist.Tree
import de.edwardday.sortedlist.avl.height
import de.edwardday.sortedlist.redblack.isBlack
import de.edwardday.sortedlist.redblack.isRed
import kotlin.math.abs
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BinaryTreeTest {

    @Suppress("UNCHECKED_CAST")
    private fun <T : Comparable<T>> createTree(vararg elements: T): List<Pair<AbstractTree<T, *>, (AbstractTree<T, *>) -> Unit>> {
        val avlTree = AvlTree<T>(naturalOrder()) + elements.asList()
        val redBlackTree = RedBlackTree<T>(naturalOrder()) + elements.asList()
        return listOf(
            avlTree to { tree: AbstractTree<T, *> -> (tree as AbstractTree<T, Int>).checkValidAvl() },
            redBlackTree to { tree: AbstractTree<T, *> -> (tree as AbstractTree<T, Boolean>).checkValidRedBlack() }
        )
    }

    @Test
    fun minus() {
        createTree(5, 3, 6, 2, 4, 7, 1).forEach {
            it.second(it.first - 5)
        }
    }

    @Test
    fun plusMinusRandom() {
        val random = Random(987654321)
        val list = List(1000) { random.nextInt(1000) }
        var trees = createTree<Int>().map { (tree, assertFunction) ->
            val workingTree = tree + list
            assertFunction(workingTree)
            workingTree to assertFunction
        }
        repeat(1000) {
            val element = random.nextInt(1000)
            trees = if (random.nextBoolean()) {
                trees.map { tree -> tree.first - element to tree.second }
            } else {
                trees.map { tree -> tree.first + element to tree.second }
            }
            trees.forEach { (tree, assertFunction) ->
                assertFunction(tree)
            }
            trees.map(Pair<AbstractTree<Int, *>, *>::first).windowed(2) { (tree1, tree2) ->
                assertEquals(tree1, tree2)
            }
        }
    }

    @Test
    fun subtree() {
        val list = listOf(5, 3, 7, 1, 4, 6, 8).sorted()
        createTree(5, 3, 7, 1, 4, 6, 8).forEach {
            for ((from, to) in listOf(0 to 7, 0 to 4, 4 to 7, 2 to 5)) {
                val subList = it.first.subList(from, to)
                it.second(subList)
                assertEquals(list.subList(from, to), subList, "sublist from $from to $to is wrong")
            }
        }
    }


    @Test
    fun sublistRandom() {
        val random = Random(987654321)
        val list = List(1000) { random.nextInt(1000) }
        val trees = createTree<Int>().map { (tree, assertFunction) ->
            val workingTree = tree + list
            assertFunction(workingTree)
            workingTree to assertFunction
        }
        repeat(100) {
            val from = random.nextInt(1000)
            val to = random.nextInt(1000)
            val subList = if (from <= to) list.sorted().subList(from, to) else emptyList()
            val subTrees = trees.map { (tree, assertFunction) -> tree.subList(from, to) to assertFunction }
            subTrees.forEach { (tree, assertFunction) ->
                assertFunction(tree)
            }
            val subLists = listOf(subList) + subTrees.map(Pair<List<Int>, *>::first)
            subLists.windowed(2) { (tree1, tree2) ->
                assertEquals(tree1, tree2)
            }
        }
    }

    private fun <T : Comparable<T>> AbstractTree<T, Int>.checkValidAvl() {
        root.checkValidBinaryTree()
        root.checkValidAvl()
    }

    private fun <T : Comparable<T>> AbstractTree<T, Boolean>.checkValidRedBlack() {
        root.checkValidBinaryTree()
        root.checkValidRedBlack()
    }

    private fun <T : Comparable<T>> Tree<T, Int>?.checkValidAvl() {
        if (this != null) {
            assertAtMost(1, "Height difference too big") { abs(left.height - right.height) }
            left.checkValidAvl()
            right.checkValidAvl()
        }
    }

    private fun <T : Comparable<T>> Tree<T, Boolean>?.checkValidRedBlack(): Int {
        return if (this != null) {
            if (this.isRed()) {
                assertTrue("2 red nodes in a row at $value") { left.isBlack() }
                assertTrue("2 red nodes in a row at $value") { right.isBlack() }
            }
            val leftBlackHeight = left.checkValidRedBlack()
            val rightBlackHeight = right.checkValidRedBlack()
            assertTrue("left and right black height should be the same but $leftBlackHeight != $rightBlackHeight") {
                leftBlackHeight == rightBlackHeight
            }
            leftBlackHeight + (if (this.isRed()) 0 else 1)
        } else {
            1
        }
    }

    private fun <T : Comparable<T>> Tree<T, *>?.checkValidBinaryTree(min: T? = null, max: T? = null) {
        if (this != null) {
            min?.also { assertAtLeast(it, value) }
            max?.also { assertAtMost(it, value) }
            left.checkValidBinaryTree(min, value)
            right.checkValidBinaryTree(value, max)
        }
    }
}
