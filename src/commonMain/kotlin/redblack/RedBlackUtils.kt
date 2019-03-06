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

package de.edwardday.sortedlist.redblack

import de.edwardday.sortedlist.Tree
import de.edwardday.sortedlist.comparePartitioned
import de.edwardday.sortedlist.compareTo
import de.edwardday.sortedlist.size
import kotlin.contracts.contract

internal fun Tree<*, Boolean>?.isRed(): Boolean {
    contract {
        returns(true) implies (this@isRed != null)
    }
    return this?.data == true
}

internal fun Tree<*, Boolean>?.isBlack(): Boolean {
    contract {
        returns(false) implies (this@isBlack != null)
    }
    return this?.data != true
}

internal fun <T> Tree<T, Boolean>.blacken(): Tree<T, Boolean> = if (this.isRed()) copy(data = false) else this
internal fun <T> Tree<T, Boolean>.redden(): Tree<T, Boolean> = if (this.isRed()) this else copy(data = true)

internal fun <T> Tree<T, Boolean>?.plus(element: T, comparator: Comparator<T>): Tree<T, Boolean> {
    return when {
        this == null -> Tree(element, null, null, true)
        element.compareTo(this, comparator) < 0 -> copy(left = left.plus(element, comparator)).colorBalanceLeft()
        else -> copy(right = right.plus(element, comparator)).colorBalanceRight()
    }
}

internal fun <T> Tree<T, Boolean>?.plus(elements: Collection<T>, comparator: Comparator<T>): Tree<T, Boolean>? =
    plusInternal(elements, comparator).tree

private fun <T> Tree<T, Boolean>?.plusInternal(elements: Collection<T>, comparator: Comparator<T>): JoinResult<T> {
    return when {
        elements.isEmpty() -> JoinResult(this, 0)
        this == null -> elements.first().let { value ->
            Tree(value, null, null, true).plusInternal(elements.drop(1), comparator)
        }
        else -> {
            val (lefts, rights) = elements.partition { it.compareTo(this, comparator) < 0 }
            val newLeft = left.plusInternal(lefts, comparator)
            val newRight = right.plusInternal(rights, comparator)
            return join(newLeft, value, newRight)
        }
    }
}

internal fun <T> Tree<T, Boolean>.minus(element: T, comparator: Comparator<T>): Tree<T, Boolean>? {
    val removeResult = minusInternal(element, comparator)
    return if (removeResult == null) this else removeResult.newChild
}

private fun <T> Tree<T, Boolean>.minusInternal(element: T, comparator: Comparator<T>): RemoveResult<T, Boolean>? {
    if (value == element) {
        return when {
            left == null -> RemoveResult(right, isBlack())
            right == null -> RemoveResult(left, isBlack())
            left.size >= right.size -> left.pollMax().let { copy(value = it.polled).heightBalanceLeft(it.removeResult) }
            else -> right.pollMin().let { copy(value = it.polled).heightBalanceRight(it.removeResult) }
        }
    }
    val compared = element.compareTo(this, comparator)
    val leftRemoveResult = left?.takeIf { compared <= 0 }?.minusInternal(element, comparator)
    return when (leftRemoveResult) {
        null -> right?.takeIf { compared >= 0 }?.minusInternal(element, comparator)?.let { heightBalanceRight(it) }
        else -> heightBalanceLeft(leftRemoveResult)
    }
}

internal fun <T> Tree<T, Boolean>?.minus(elements: Collection<T>, comparator: Comparator<T>): Tree<T, Boolean>? {
    return minusInternal(elements, comparator).joinResult.tree
}

private fun <T> Tree<T, Boolean>?.minusInternal(
    elements: Collection<T>,
    comparator: Comparator<T>
): RemoveManyResult<T> {
    return when {
        this == null || elements.isEmpty() -> RemoveManyResult(JoinResult(this, 0), elements)
        else -> {
            val elementsWithoutThis = elements.minus(value)
            val removeThis = elementsWithoutThis.size != elements.size
            val (smaller, equal, greater) = elementsWithoutThis.comparePartitioned(value, comparator)
            val (newLeft, leftToRemove) = left.minusInternal(smaller + equal, comparator)
            val (newRight, rightToRemove) = right.minusInternal(leftToRemove - smaller + greater, comparator)
            val toRemove = leftToRemove - equal + rightToRemove
            if (removeThis) {
                when {
                    newLeft.tree == null -> RemoveManyResult(newRight, toRemove)
                    newRight.tree == null -> RemoveManyResult(newLeft, toRemove)
                    newLeft.blackHeightLoss < newRight.blackHeightLoss -> newLeft.tree.pollMax().let { (leftPolled, newValue) ->
                        val leftAdditionalHeightLoss = if (leftPolled.tooSmall) 1 else 0
                        val leftToJoin =
                            JoinResult(leftPolled.newChild, newLeft.blackHeightLoss + leftAdditionalHeightLoss)
                        RemoveManyResult(join(leftToJoin, newValue, newRight), toRemove)
                    }
                    else -> newRight.tree.pollMin().let { (rightPolled, newValue) ->
                        val rightAdditionalHeightLoss = if (rightPolled.tooSmall) 1 else 0
                        val rightToJoin =
                            JoinResult(rightPolled.newChild, newRight.blackHeightLoss + rightAdditionalHeightLoss)
                        RemoveManyResult(join(newLeft, newValue, rightToJoin), toRemove)
                    }
                }
            } else {
                RemoveManyResult(join(newLeft, value, newRight), toRemove)
            }.let { it.copy(joinResult = it.joinResult.fixFromTree(this)) }
        }
    }
}

private data class RemoveManyResult<T>(val joinResult: JoinResult<T>, val toRemove: Collection<T>)

private data class RemoveResult<T, D>(val newChild: Tree<T, D>?, val tooSmall: Boolean)
private data class PollResult<T, D>(val removeResult: RemoveResult<T, D>, val polled: T)

private fun <T> Tree<T, Boolean>.pollMax(): PollResult<T, Boolean> {
    return when (right) {
        null -> PollResult(RemoveResult(left?.blacken(), isBlack() && left == null), value)
        else -> right.pollMax().run { copy(removeResult = heightBalanceRight(removeResult)) }
    }
}

private fun <T> Tree<T, Boolean>.pollMin(): PollResult<T, Boolean> {
    return when (left) {
        null -> PollResult(RemoveResult(right?.blacken(), isBlack() && right == null), value)
        else -> left.pollMin().run { copy(removeResult = heightBalanceLeft(removeResult)) }
    }
}

private fun <T> Tree<T, Boolean>.colorBalance(): Tree<T, Boolean> {
    return when {
        left.isBlack() -> colorBalanceRight()
        right.isBlack() -> colorBalanceLeft()
        else -> {
            val leftBalanced = colorBalanceLeft()
            if (leftBalanced.isBlack()) colorBalanceRight()
            else leftBalanced.copy(right = leftBalanced.right.colorBalance())
        }
    }
}

private fun <T> Tree<T, Boolean>.colorBalanceLeft(): Tree<T, Boolean> {
    val x: Tree<T, Boolean>
    val y: Tree<T, Boolean>
    val z: Tree<T, Boolean> = this
    val b: Tree<T, Boolean>?
    when {
        left.isBlack() -> return this
        left.left.isRed() -> {
            y = left
            x = left.left
            b = x.right
        }
        left.right.isRed() -> {
            x = left
            y = left.right
            b = y.left
        }
        else -> return this
    }
    return y.copy(data = true, left = x.copy(data = false, right = b), right = z.copy(data = false, left = y.right))
}

private fun <T> Tree<T, Boolean>.colorBalanceRight(): Tree<T, Boolean> {
    val x: Tree<T, Boolean> = this
    val y: Tree<T, Boolean>
    val z: Tree<T, Boolean>
    val c: Tree<T, Boolean>?
    when {
        right.isBlack() -> return this
        right.isRed() && right.left.isRed() -> {
            z = right
            y = right.left
            c = y.right
        }
        right.isRed() && right.right.isRed() -> {
            y = right
            z = right.right
            c = z.left
        }
        else -> return this
    }
    return y.copy(data = true, left = x.copy(data = false, right = y.left), right = z.copy(data = false, left = c))
}

private fun <T> Tree<T, Boolean>.heightBalanceLeft(removeResult: RemoveResult<T, Boolean>): RemoveResult<T, Boolean> {
    return if (removeResult.tooSmall) {
        if (right.isBlack()) {
            val newChild = copy(data = false, left = removeResult.newChild, right = right?.redden())
                .colorBalanceRight()
            if (newChild.isRed() && isBlack()) RemoveResult(newChild = newChild.blacken(), tooSmall = false)
            else RemoveResult(newChild = newChild, tooSmall = isBlack())
        } else {
            val newChild = right.left?.copy(
                left = this.copy(left = removeResult.newChild, right = right.left.left),
                right = right.copy(
                    data = false,
                    left = right.left.right,
                    right = right.right?.redden()
                ).colorBalanceRight()
            )
            RemoveResult(newChild = newChild, tooSmall = false)
        }
    } else {
        removeResult.copy(copy(left = removeResult.newChild))
    }
}

private fun <T> Tree<T, Boolean>.heightBalanceRight(removeResult: RemoveResult<T, Boolean>): RemoveResult<T, Boolean> {
    return if (removeResult.tooSmall) {
        if (left.isBlack()) {
            val newChild = copy(data = false, left = left?.redden(), right = removeResult.newChild)
                .colorBalanceLeft()
            if (newChild.isRed() && isBlack()) RemoveResult(newChild = newChild.blacken(), tooSmall = false)
            else RemoveResult(newChild = newChild, tooSmall = isBlack())
        } else {
            val newChild = left.right?.copy(
                left = left.copy(
                    data = false,
                    left = left.left?.redden(),
                    right = left.right.left
                ).colorBalanceLeft(),
                right = this.copy(left = left.right.right, right = removeResult.newChild)
            )
            RemoveResult(newChild = newChild, tooSmall = false)
        }
    } else {
        removeResult.copy(copy(right = removeResult.newChild))
    }
}

internal fun <T> Tree<T, Boolean>.subTree(fromIndex: Int, toIndex: Int): Tree<T, Boolean>? {
    val leftSize = left.size
    return when {
        fromIndex >= toIndex -> null
        toIndex <= leftSize -> left?.subTree(fromIndex, toIndex)
        fromIndex > leftSize -> right?.subTree(fromIndex - leftSize - 1, toIndex - leftSize - 1)
        else -> {
            val leftSubtreeResult = left.subTreeFromInternal(fromIndex)
            val rightSubtreeResult = right.subTreeToInternal(toIndex - leftSize - 1)
            join(leftSubtreeResult, value, rightSubtreeResult).tree
        }
    }
}

internal fun <T> Tree<T, Boolean>.subTree(from: T, to: T, comparator: Comparator<T>): Tree<T, Boolean>? {
    return when {
        comparator.compare(from, to) >= 0 -> null
        to.compareTo(this, comparator) <= 0 -> left?.subTree(from, to, comparator)
        from.compareTo(this, comparator) > 0 -> right?.subTree(from, to, comparator)
        else -> {
            val leftSubtreeResult = left.subTreeFromInternal(from, comparator)
            val rightSubtreeResult = right.subTreeToInternal(to, comparator)
            join(leftSubtreeResult, value, rightSubtreeResult).tree
        }
    }
}

internal fun <T> Tree<T, Boolean>.subTreeFrom(from: T, comparator: Comparator<T>): Tree<T, Boolean>? =
    subTreeFromInternal(from, comparator).tree

private fun <T> Tree<T, Boolean>?.subTreeFromInternal(fromIndex: Int): JoinResult<T> {
    return when {
        this == null -> JoinResult(null, 0)
        fromIndex > left.size -> right.subTreeFromInternal(fromIndex - left.size - 1).fixFromTree(this)
        else -> left.subTreeFromInternal(fromIndex).let {
            join(it.tree, it.blackHeightLoss, value, right, 0).fixFromTree(this)
        }
    }
}

private fun <T> Tree<T, Boolean>?.subTreeFromInternal(from: T, comparator: Comparator<T>): JoinResult<T> {
    sequenceOf(1,2).take(2)
    return when {
        this == null -> JoinResult(null, 0)
        from.compareTo(this, comparator) > 0 -> right.subTreeFromInternal(from, comparator).fixFromTree(this)
        else -> left.subTreeFromInternal(from, comparator).let {
            join(it.tree, it.blackHeightLoss, value, right, 0).fixFromTree(this)
        }
    }
}

internal fun <T> Tree<T, Boolean>.subTreeTo(to: T, comparator: Comparator<T>): Tree<T, Boolean>? =
    subTreeToInternal(to, comparator).tree

private fun <T> Tree<T, Boolean>?.subTreeToInternal(toIndex: Int): JoinResult<T> {
    return when {
        this == null -> JoinResult(null, 0)
        toIndex <= left.size -> left.subTreeToInternal(toIndex).fixFromTree(this)
        else -> right.subTreeToInternal(toIndex - left.size - 1).let {
            join(left, 0, value, it.tree, it.blackHeightLoss).fixFromTree(this)
        }
    }
}

private fun <T> Tree<T, Boolean>?.subTreeToInternal(to: T, comparator: Comparator<T>): JoinResult<T> {
    return when {
        this == null -> JoinResult(null, 0)
        to.compareTo(this, comparator) <= 0 -> left.subTreeToInternal(to, comparator).fixFromTree(this)
        else -> right.subTreeToInternal(to, comparator).let {
            join(left, 0, value, it.tree, it.blackHeightLoss).fixFromTree(this)
        }
    }
}

private fun <T> JoinResult<T>.fixFromTree(oldTree: Tree<T, Boolean>): JoinResult<T> {
    return when {
        oldTree.isBlack() ->
            when {
                tree.isRed() -> copy(tree = tree.blacken())
                else -> copy(blackHeightLoss = blackHeightLoss + 1)
            }
        else -> this
    }
}

private fun <T> JoinResult<T>.fixFromTreeAdjusted(oldTree: Tree<T, Boolean>): JoinResult<T> {
    return when {
        oldTree.isBlack() && tree.isRed() -> copy(tree = tree.blacken(), blackHeightLoss = blackHeightLoss - 1)
        else -> this
    }
}

internal data class JoinResult<T>(val tree: Tree<T, Boolean>?, val blackHeightLoss: Int)

private fun <T> join(left: JoinResult<T>, value: T, right: JoinResult<T>): JoinResult<T> =
    join(left.tree, left.blackHeightLoss, value, right.tree, right.blackHeightLoss)

private fun <T> join(
    left: Tree<T, Boolean>?, leftHeightLoss: Int,
    value: T,
    right: Tree<T, Boolean>?, rightHeightLoss: Int
): JoinResult<T> {
    return when {
        leftHeightLoss == rightHeightLoss -> {
            val tree: Tree<T, Boolean>
            val heightIncreased: Boolean
            when {
                left.isRed() || right.isRed() -> {
                    tree = Tree(value, left, right, false).colorBalance()
                    heightIncreased = true
                }
                else -> {
                    tree = Tree(value, left, right, true)
                    heightIncreased = false
                }
            }
            if (heightIncreased) {
                if (tree.isBlack()) JoinResult(tree.redden(), leftHeightLoss)
                else JoinResult(tree, leftHeightLoss - 1)
            } else {
                JoinResult(tree, leftHeightLoss)
            }
        }
        left == null -> {
            val newHeightLoss = if (right.isRed()) rightHeightLoss - 1 else rightHeightLoss
            JoinResult(right?.blacken().insertMin(value), newHeightLoss)
        }
        right == null -> {
            val newHeightLoss = if (left.isRed()) leftHeightLoss - 1 else leftHeightLoss
            JoinResult(left.blacken().insertMax(value), newHeightLoss)
        }
        leftHeightLoss < rightHeightLoss -> {
            val leftChildHeightLoss = leftHeightLoss + if (left.isBlack()) 1 else 0
            val joinResult = join(left.right, leftChildHeightLoss, value, right, rightHeightLoss)
            join(left.left, leftChildHeightLoss, left.value, joinResult.tree, joinResult.blackHeightLoss)
                .fixFromTreeAdjusted(left)
        }
        else -> {
            val rightChildHeightLoss = rightHeightLoss + if (right.isBlack()) 1 else 0
            val joinResult = join(left, leftHeightLoss, value, right.left, rightChildHeightLoss)
            join(joinResult.tree, joinResult.blackHeightLoss, right.value, right.right, rightChildHeightLoss)
                .fixFromTreeAdjusted(right)
        }
    }
}


private fun <T> Tree<T, Boolean>?.insertMin(value: T): Tree<T, Boolean> {
    return when {
        this == null -> Tree(value, null, null, true)
        else -> copy(left = left.insertMin(value)).colorBalanceLeft()
    }
}

private fun <T> Tree<T, Boolean>?.insertMax(value: T): Tree<T, Boolean> {
    return when {
        this == null -> Tree(value, null, null, true)
        else -> copy(right = right.insertMax(value)).colorBalanceRight()
    }
}
