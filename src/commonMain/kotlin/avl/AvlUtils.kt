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

package de.edwardday.sortedlist.avl

import de.edwardday.sortedlist.Tree
import de.edwardday.sortedlist.comparePartitioned
import de.edwardday.sortedlist.compareTo
import de.edwardday.sortedlist.size

internal val Tree<*, Int>?.height: Int get() = this?.data ?: 0

private fun <T> constructTree(value: T, left: Tree<T, Int>?, right: Tree<T, Int>?): Tree<T, Int> =
    Tree(value, left, right, calculateHeight(left, right))

internal fun calculateHeight(left: Tree<*, Int>?, right: Tree<*, Int>?) =
    maxOf(left.height, right.height) + 1

private fun <T> Tree<T, Int>.copyUpdated(
    value: T = this.value,
    left: Tree<T, Int>? = this.left,
    right: Tree<T, Int>? = this.right
): Tree<T, Int> {
    return if (left === this.left && right === this.right && value == this.value) this
    else copy(value = value, left = left, right = right, data = calculateHeight(left, right))
}

internal fun <T> Tree<T, Int>?.plus(element: T, comparator: Comparator<T>): Tree<T, Int> {
    return when {
        this == null -> constructTree(element, null, null)
        element.compareTo(this, comparator) < 0 -> balanceHeavyLeft(left = left.plus(element, comparator))
        else -> balanceHeavyRight(right = right.plus(element, comparator))
    }
}

internal fun <T> Tree<T, Int>?.plus(elements: Collection<T>, comparator: Comparator<T>): Tree<T, Int>? {
    return when {
        // isEmpty
        elements.isEmpty() -> this
        this == null -> elements.first().let { value ->
            constructTree(value, null, null).plus(elements.drop(1), comparator)
        }
        else -> {
            val (lefts, rights) = elements.partition { it.compareTo(this, comparator) < 0 }
            val newLeft = left.plus(lefts, comparator)
            val newRight = right.plus(rights, comparator)
            return join(newLeft, value, newRight)
        }
    }
}

internal fun <T> Tree<T, Int>.minus(element: T, comparator: Comparator<T>): Tree<T, Int>? {
    val removeResult = minusInternal(element, comparator)
    return if (removeResult == null) this else removeResult.newChild
}

private fun <T> Tree<T, Int>.minusInternal(element: T, comparator: Comparator<T>): RemoveResult<T, Int>? {
    if (value == element) {
        return when {
            left == null -> RemoveResult(right)
            right == null -> RemoveResult(left)
            left.height >= right.height -> left.pollMax().run {
                RemoveResult(copyUpdated(value = polled, left = newChild))
            }
            else -> right.pollMin().run {
                RemoveResult(copyUpdated(value = polled, right = newChild))
            }
        }
    } else {
        val compared = element.compareTo(this, comparator)
        val leftRemoveResult = left?.takeIf { compared <= 0 }?.minusInternal(element, comparator)
        return when (leftRemoveResult) {
            null -> right?.takeIf { compared >= 0 }?.minusInternal(element, comparator)?.run {
                RemoveResult(balanceHeavyLeft(right = newChild))
            }
            else -> RemoveResult(balanceHeavyRight(left = leftRemoveResult.newChild))
        }
    }
}

internal fun <T> Tree<T, Int>.minus(elements: Collection<T>, comparator: Comparator<T>): Tree<T, Int>? {
    val removeResult = minusInternal(elements, comparator)
    return removeResult.newChild
}

private fun <T> Tree<T, Int>?.minusInternal(
    elements: Collection<T>,
    comparator: Comparator<T>
): RemoveManyResult<T, Int> {
    return when {
        this == null || elements.isEmpty() -> RemoveManyResult(this, elements)
        else -> {
            val elementsWithoutThis = elements.minus(value)
            val removeThis = elementsWithoutThis.size != elements.size
            val (smaller, equal, greater) = elementsWithoutThis.comparePartitioned(value, comparator)
            val (newLeft, leftToRemove) = left.minusInternal(smaller + equal, comparator)
            val (newRight, rightToRemove) = right.minusInternal(leftToRemove - smaller + greater, comparator)
            val toRemove = leftToRemove - equal + rightToRemove
            if (removeThis) {
                when {
                    newLeft == null -> RemoveManyResult(newRight, toRemove)
                    newRight == null -> RemoveManyResult(newLeft, toRemove)
                    newLeft.height > newRight.height -> newLeft.pollMax().let { (leftPolled, newValue) ->
                        RemoveManyResult(join(leftPolled, newValue, newRight), toRemove)
                    }
                    else -> newRight.pollMin().let { (rightPolled, newValue) ->
                        RemoveManyResult(join(newLeft, newValue, rightPolled), toRemove)
                    }
                }
            } else {
                RemoveManyResult(join(newLeft, value, newRight), toRemove)
            }
        }
    }
}

internal fun <T> Tree<T, Int>.balanceHeavyLeft(
    left: Tree<T, Int>? = this.left,
    right: Tree<T, Int>? = this.right
): Tree<T, Int> {
    return if (left != null && left.height - right.height > 1) {
        if (left.right != null && left.right.height > left.left.height) {
            // double rotate
            left.right.copyUpdated(
                left = left.copyUpdated(right = left.right.left),
                right = this.copyUpdated(left = left.right.right, right = right)
            )
        } else {
            // rotate
            left.copyUpdated(right = this.copyUpdated(left = left.right, right = right))
        }
    } else {
        this.copyUpdated(left = left, right = right)
    }
}

internal fun <T> Tree<T, Int>.balanceHeavyRight(
    left: Tree<T, Int>? = this.left,
    right: Tree<T, Int>? = this.right
): Tree<T, Int> {
    return if (right != null && right.height - left.height > 1) {
        if (right.left != null && right.left.height > right.right.height) {
            // double rotate
            right.left.copyUpdated(
                left = this.copyUpdated(left = left, right = right.left.left),
                right = right.copyUpdated(left = right.left.right)
            )
        } else {
            // rotate
            right.copyUpdated(left = this.copyUpdated(left = left, right = right.left))
        }
    } else {
        this.copyUpdated(left = left, right = right)
    }
}

internal fun <T> Tree<T, Int>.pollMin(): PollResult<T, Int> {
    return when (left) {
        null -> PollResult(right, value)
        else -> {
            left.pollMin().let {
                it.copy(newChild = balanceHeavyRight(left = it.newChild))
            }
        }
    }
}

internal fun <T> Tree<T, Int>.pollMax(): PollResult<T, Int> {
    return when (right) {
        null -> PollResult(left, value)
        else -> {
            right.pollMax().let {
                it.copy(newChild = balanceHeavyLeft(right = it.newChild))
            }
        }
    }
}

internal data class PollResult<T, D>(val newChild: Tree<T, D>?, val polled: T)

private data class RemoveResult<T, D>(val newChild: Tree<T, D>?)

private data class RemoveManyResult<T, D>(val newChild: Tree<T, D>?, val toRemove: Collection<T>)

internal fun <T> Tree<T, Int>.subTree(fromIndex: Int, toIndex: Int): Tree<T, Int>? {
    val leftSize = left.size
    return when {
        fromIndex >= toIndex -> null
        toIndex <= leftSize -> left?.subTree(fromIndex, toIndex)
        fromIndex > leftSize -> right?.subTree(fromIndex - leftSize - 1, toIndex - leftSize - 1)
        else -> join(left?.subTreeFrom(fromIndex), value, right?.subTreeTo(toIndex - leftSize - 1))
    }
}

internal fun <T> Tree<T, Int>.subTree(from: T, to: T, comparator: Comparator<T>): Tree<T, Int>? {
    return when {
        comparator.compare(from, to) >= 0 -> null
        to.compareTo(this, comparator) <= 0 -> left?.subTree(from, to, comparator)
        from.compareTo(this, comparator) > 0 -> right?.subTree(from, to, comparator)
        else -> join(left?.subTreeFrom(from, comparator), value, right?.subTreeTo(to, comparator))
    }
}

internal fun <T> Tree<T, Int>.subTreeFrom(fromIndex: Int): Tree<T, Int>? {
    val leftSize = left.size
    return when {
        fromIndex > leftSize -> right?.subTreeFrom(fromIndex - leftSize - 1)
        fromIndex <= 0 -> this
        else -> join(left?.subTreeFrom(fromIndex), value, right)
    }
}

internal fun <T> Tree<T, Int>.subTreeFrom(from: T, comparator: Comparator<T>): Tree<T, Int>? {
    return if (from.compareTo(this, comparator) > 0) {
        right?.subTreeFrom(from, comparator)
    } else {
        join(left?.subTreeFrom(from, comparator), value, right)
    }
}

internal fun <T> Tree<T, Int>.subTreeTo(toIndex: Int): Tree<T, Int>? {
    val leftSize = left.size
    return when {
        toIndex <= leftSize -> left?.subTreeTo(toIndex)
        toIndex >= size -> this
        else -> join(left, value, right?.subTreeTo(toIndex - leftSize - 1))
    }
}

internal fun <T> Tree<T, Int>.subTreeTo(to: T, comparator: Comparator<T>): Tree<T, Int>? {
    return if (to.compareTo(this, comparator) <= 0) {
        left?.subTreeTo(to, comparator)
    } else {
        join(left, value, right?.subTreeTo(to, comparator))
    }
}

private fun <T> join(left: Tree<T, Int>?, value: T, right: Tree<T, Int>?): Tree<T, Int> {
    return when {
        left == null -> right.insertMin(value)
        right == null -> left.insertMax(value)
        left.height - right.height >= 2 -> left.balanceHeavyRight(right = join(left.right, value, right))
        right.height - left.height >= 2 -> right.balanceHeavyLeft(left = join(left, value, right.left))
        else -> constructTree(value, left, right)
    }
}

private fun <T> Tree<T, Int>?.insertMin(value: T): Tree<T, Int> {
    return when {
        this == null -> constructTree(value, null, null)
        else -> balanceHeavyLeft(left = left.insertMin(value))
    }
}

private fun <T> Tree<T, Int>?.insertMax(value: T): Tree<T, Int> {
    return when {
        this == null -> constructTree(value, null, null)
        else -> balanceHeavyRight(right = right.insertMax(value))
    }
}
