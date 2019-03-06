/*
 * Copyright 2019 Eduard Wolf
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.edwardday.sortedlist

val Tree<*, *>?.size: Int
    get() = this?.size ?: 0

tailrec operator fun <T> Tree<T, *>?.get(index: Int, errorMessage: () -> String = { "$index >= $size" }): T {
    return when {
        this == null -> throw NoSuchElementException(errorMessage())
        left.size > index -> left[index, errorMessage]
        left.size == index -> value
        else -> right[index - left.size - 1, errorMessage]
    }
}

fun <T> Tree<T, *>?.indexOf(element: T, comparator: Comparator<T>): Int = indexOfInternal(element, comparator)

private tailrec fun <T> Tree<T, *>?.indexOfInternal(
    element: T,
    comparator: Comparator<T>,
    alreadyFoundIndex: Int = -1,
    indexOffset: Int = 0,
    treesToDo: List<Pair<Tree<T, *>, Int>> = emptyList()
): Int {
    return when (this) {
        null -> {
            if (treesToDo.isEmpty()) {
                alreadyFoundIndex
            } else {
                val (tree, treeOffset) = treesToDo.first()
                tree.indexOfInternal(element, comparator, alreadyFoundIndex, treeOffset, treesToDo.drop(1))
            }
        }
        else -> {
            val rightOffset = indexOffset + left.size + 1
            when (element) {
                value -> left.indexOfInternal(element, comparator, rightOffset, indexOffset, emptyList())
                else -> {
                    val compared = element.compareTo(this, comparator)
                    when {
                        compared < 0 ->
                            left.indexOfInternal(element, comparator, alreadyFoundIndex, indexOffset, treesToDo)
                        compared > 0 ->
                            right.indexOfInternal(element, comparator, alreadyFoundIndex, rightOffset, treesToDo)
                        else ->
                            left.indexOfInternal(
                                element,
                                comparator,
                                alreadyFoundIndex,
                                indexOffset,
                                treesToDo + listOfNotNull(right?.let { it to rightOffset })
                            )
                    }
                }
            }
        }
    }
}

fun <T> Tree<T, *>?.lastIndexOf(element: T, comparator: Comparator<T>): Int = lastIndexOfInternal(element, comparator)

private tailrec fun <T> Tree<T, *>?.lastIndexOfInternal(
    element: T,
    comparator: Comparator<T>,
    alreadyFoundIndex: Int = -1,
    indexOffset: Int = 0,
    treesToDo: List<Pair<Tree<T, *>, Int>> = emptyList()
): Int {
    return when (this) {
        null -> {
            if (treesToDo.isEmpty()) {
                alreadyFoundIndex
            } else {
                val (tree, treeOffset) = treesToDo.first()
                tree.lastIndexOfInternal(element, comparator, alreadyFoundIndex, treeOffset, treesToDo.drop(1))
            }
        }
        else -> {
            val rightOffset = indexOffset + left.size + 1
            when (element) {
                value -> right.lastIndexOfInternal(element, comparator, rightOffset, rightOffset, emptyList())
                else -> {
                    val compared = element.compareTo(this, comparator)
                    when {
                        compared < 0 ->
                            left.lastIndexOfInternal(element, comparator, alreadyFoundIndex, indexOffset, treesToDo)
                        compared > 0 ->
                            right.lastIndexOfInternal(element, comparator, alreadyFoundIndex, rightOffset, treesToDo)
                        else ->
                            right.lastIndexOfInternal(
                                element,
                                comparator,
                                alreadyFoundIndex,
                                rightOffset,
                                treesToDo + listOfNotNull(left?.let { it to indexOffset })
                            )
                    }
                }
            }
        }
    }
}

fun <T> T.compareTo(element: Tree<T, *>, comparator: Comparator<T>): Int = comparator.compare(this, element.value)

operator fun <T : Comparable<T>> Tree<T, *>?.contains(value: T): Boolean = contains(value, naturalOrder())

fun <T> Tree<T, *>?.contains(value: T, comparator: Comparator<T>): Boolean =
    contains(value, comparator, emptyList())

private tailrec fun <T> Tree<T, *>?.contains(
    element: T,
    comparator: Comparator<T>,
    todoNodes: List<Tree<T, *>?>
): Boolean {
    return when {
        this == null ->
            if (todoNodes.isEmpty()) false else todoNodes.first().contains(element, comparator, todoNodes.drop(1))
        value == element -> true
        else -> {
            val compared = element.compareTo(this, comparator)
            when {
                compared < 0 -> left.contains(element, comparator, todoNodes)
                compared > 0 -> right.contains(element, comparator, todoNodes)
                else -> left.contains(element, comparator, todoNodes + right)
            }
        }
    }
}

fun <T> Tree<T, *>?.containsAll(values: Collection<T>, comparator: Comparator<T>): Boolean =
    containsAllInternal(values.distinct(), comparator)

private tailrec fun <T> Tree<T, *>?.containsAllInternal(
    elements: Collection<T>,
    comparator: Comparator<T>,
    otherNodes: List<Tree<T, *>> = emptyList(),
    remainingContainsCheck: List<Pair<List<T>, List<Tree<T, *>>>> = emptyList()
): Boolean {
    return when {
        elements.isEmpty() -> {
            if (remainingContainsCheck.isEmpty()) {
                true
            } else {
                val (otherElements, trees) = remainingContainsCheck.first()
                val remainingNodes = remainingContainsCheck.drop(1)
                trees.firstOrNull().containsAllInternal(otherElements, comparator, trees.drop(1), remainingNodes)
            }
        }
        this == null -> {
            if (otherNodes.isEmpty()) {
                false
            } else {
                otherNodes.first().containsAllInternal(elements, comparator, otherNodes.drop(1), remainingContainsCheck)
            }
        }
        else -> {
            val nextElements = elements - value
            val (smaller, equal, greater) = nextElements.comparePartitioned(value, comparator)
            left.containsAllInternal(
                smaller, comparator, otherNodes,
                remainingContainsCheck + listOf(
                    greater to otherNodes + listOfNotNull(right),
                    equal to otherNodes + listOfNotNull(left, right)
                )
            )
        }
    }
}

fun <T> Tree<T, *>?.iterator(index: Int = 0): ListIterator<T> = TreeIterator(this, index)

tailrec fun <T> Tree<T, *>.min(): T = if (left == null) value else left.min()
tailrec fun <T> Tree<T, *>.max(): T = if (right == null) value else right.max()

internal fun <T, D> Tree<T, D>?.minSequence(): Sequence<Tree<T, D>> =
    generateSequence(this, Tree<T, D>::left)

internal fun <T, D> Tree<T, D>?.maxSequence(): Sequence<Tree<T, D>> =
    generateSequence(this, Tree<T, D>::right)

private class TreeIterator<T>(root: Tree<T, *>?, var cursor: Int = 0) : ListIterator<T> {

    private val nextNodePath = if (cursor == root.size) mutableListOf() else root.pathToCursor(cursor).toMutableList()
    private val previousNodePath = if (cursor == 0) mutableListOf() else root.pathToCursor(cursor - 1).toMutableList()

    override fun hasNext(): Boolean = nextNodePath.isNotEmpty()

    override fun hasPrevious(): Boolean = previousNodePath.isNotEmpty()

    override fun nextIndex(): Int = cursor

    override fun previousIndex(): Int = cursor - 1

    override fun next(): T {
        if (nextNodePath.isEmpty()) {
            throw NoSuchElementException()
        } else {
            val lastNode = nextNodePath.last()
            val result = lastNode.value

            previousNodePath.clear()
            previousNodePath += nextNodePath

            val lastRight = lastNode.right
            if (lastRight != null) {
                this.nextNodePath += lastRight.minSequence()
            } else {
                var node = lastNode
                nextNodePath.removeAt(nextNodePath.lastIndex)
                while (nextNodePath.isNotEmpty()) {
                    if (node === nextNodePath.last().left) {
                        break
                    } else {
                        node = nextNodePath.last()
                        nextNodePath.removeAt(nextNodePath.lastIndex)
                    }
                }
            }
            ++cursor
            return result
        }
    }

    override fun previous(): T {
        if (previousNodePath.isEmpty()) {
            throw NoSuchElementException()
        } else {
            val lastNode = previousNodePath.last()
            val result = lastNode.value

            nextNodePath.clear()
            nextNodePath += previousNodePath

            val lastLeft = lastNode.left
            if (lastLeft != null) {
                this.previousNodePath += lastLeft.maxSequence()
            } else {
                var node = lastNode
                previousNodePath.removeAt(previousNodePath.lastIndex)
                while (previousNodePath.isNotEmpty()) {
                    if (node === previousNodePath.last().right) {
                        break
                    } else {
                        node = previousNodePath.last()
                        previousNodePath.removeAt(nextNodePath.lastIndex)
                    }
                }
            }
            --cursor
            return result
        }
    }

    private tailrec fun Tree<T, *>?.pathToCursor(
        cursor: Int,
        resultList: List<Tree<T, *>> = emptyList()
    ): List<Tree<T, *>> {
        return when {
            this == null -> resultList
            cursor < left.size -> left.pathToCursor(cursor, resultList + this)
            cursor == left.size -> resultList + this
            else -> right.pathToCursor(cursor - left.size - 1, resultList + this)
        }
    }
}

internal fun <T> Iterable<T>.comparePartitioned(value: T, comparator: Comparator<T>): PartitionResult<T> {
    val smaller = ArrayList<T>()
    val equal = ArrayList<T>()
    val greater = ArrayList<T>()
    for (element in this) {
        val compareResult = comparator.compare(element, value)
        when {
            compareResult < 0 -> smaller.add(element)
            compareResult == 0 -> equal.add(element)
            else -> greater.add(element)
        }
    }
    return PartitionResult(smaller, equal, greater)
}

internal data class PartitionResult<T>(val smaller: List<T>, val equal: List<T>, val greater: List<T>)
