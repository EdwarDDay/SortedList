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

import de.edwardday.sortedlist.avl.*

class AvlTree<T> internal constructor(comparator: Comparator<T>, root: Tree<T, Int>?) :
    AbstractTree<T, Int>(comparator, root), SortedList<T> {

    constructor(comparator: Comparator<T>) : this(comparator, null)

    override fun subList(fromIndex: Int, toIndex: Int): AvlTree<T> =
        AvlTree(comparator, root?.subTree(fromIndex, toIndex))

    override fun subListByElement(from: T, to: T): AvlTree<T> = AvlTree(comparator, root?.subTree(from, to, comparator))

    override fun headList(to: T): AbstractTree<T, Int> = AvlTree(comparator, root?.subTreeTo(to, comparator))

    override fun tailList(from: T): AbstractTree<T, Int> = AvlTree(comparator, root?.subTreeFrom(from, comparator))

    override fun plus(element: T): AvlTree<T> = AvlTree(comparator, root.plus(element, comparator))

    override fun plus(elements: Collection<T>): AvlTree<T> = AvlTree(comparator, root.plus(elements, comparator))

    override fun minus(element: T): AvlTree<T> = AvlTree(comparator, root?.minus(element, comparator))

    override fun minus(elements: Collection<T>): AvlTree<T> = AvlTree(comparator, root?.minus(elements, comparator))
}
