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

import de.edwardday.sortedlist.redblack.*

class RedBlackTree<T> internal constructor(
    comparator: Comparator<T>,
    root: Tree<T, Boolean>? = null
) :
    AbstractTree<T, Boolean>(comparator, root), SortedList<T> {

    constructor(comparator: Comparator<T>) : this(comparator, null)

    override fun subList(fromIndex: Int, toIndex: Int): RedBlackTree<T> =
        RedBlackTree(comparator, root?.subTree(fromIndex, toIndex)?.blacken())

    override fun subListByElement(from: T, to: T): RedBlackTree<T> =
        RedBlackTree(comparator, root?.subTree(from, to, comparator)?.blacken())

    override fun headList(to: T): AbstractTree<T, Boolean> =
        RedBlackTree(comparator, root?.subTreeTo(to, comparator)?.blacken())

    override fun tailList(from: T): AbstractTree<T, Boolean> =
        RedBlackTree(comparator, root?.subTreeFrom(from, comparator)?.blacken())

    override fun plus(element: T): RedBlackTree<T> =
        RedBlackTree(comparator, root.plus(element, comparator).blacken())

    override fun plus(elements: Collection<T>): RedBlackTree<T> =
        RedBlackTree(comparator, root.plus(elements, comparator)?.blacken())

    override fun minus(element: T): RedBlackTree<T> =
        RedBlackTree(comparator, root?.minus(element, comparator)?.blacken())

    override fun minus(elements: Collection<T>): RedBlackTree<T> =
        RedBlackTree(comparator, root.minus(elements, comparator)?.blacken())

}
