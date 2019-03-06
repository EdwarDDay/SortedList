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

abstract class AbstractTree<T, D> protected constructor(
    protected val comparator: Comparator<T>,
    val root: Tree<T, D>?
) :
    AbstractList<T>(), SortedList<T> {

    override val size: Int get() = root.size

    override fun isEmpty(): Boolean = root == null

    override fun min(): T? = root?.min()

    override fun max(): T? = root?.max()

    override fun get(index: Int): T = root[index]

    override fun indexOf(element: T): Int = root.indexOf(element, comparator)

    override fun lastIndexOf(element: T): Int = root.lastIndexOf(element, comparator)

    override fun contains(element: T): Boolean = root.contains(element, comparator)

    override fun containsAll(elements: Collection<T>): Boolean = root.containsAll(elements, comparator)

    override fun iterator(): Iterator<T> = listIterator(0)

    override fun listIterator(): ListIterator<T> = listIterator(0)

    override fun listIterator(index: Int): ListIterator<T> = root.iterator(index)

    abstract override fun subList(fromIndex: Int, toIndex: Int): AbstractTree<T, D>

    abstract override fun subListByElement(from: T, to: T): AbstractTree<T, D>

    abstract override fun headList(to: T): AbstractTree<T, D>

    abstract override fun tailList(from: T): AbstractTree<T, D>

    abstract override fun plus(element: T): AbstractTree<T, D>

    abstract override fun plus(elements: Collection<T>): AbstractTree<T, D>

    abstract override fun minus(element: T): AbstractTree<T, D>

    abstract override fun minus(elements: Collection<T>): AbstractTree<T, D>
}
