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

operator fun <T> SortedList<T>.plus(elements: Iterable<T>): SortedList<T> =
    if (elements is Collection<T>) plus(elements)
    else plus(elements.toList())

operator fun <T> SortedList<T>.plus(elements: Array<out T>): SortedList<T> = plus(elements.asList())

operator fun <T> SortedList<T>.plus(elements: Sequence<T>): SortedList<T> = plus(elements.toList())

operator fun <T> SortedList<T>.minus(elements: Iterable<T>): SortedList<T> =
    if (elements is Collection<T>) minus(elements)
    else minus(elements.toList())

operator fun <T> SortedList<T>.minus(elements: Array<out T>): SortedList<T> = minus(elements.asList())

operator fun <T> SortedList<T>.minus(elements: Sequence<T>): SortedList<T> = minus(elements.toList())

fun <T : Comparable<T>> sortedCollectionOf(vararg elements: T): SortedList<T> =
    AvlTree<T>(naturalOrder()) + elements.asList()

fun <T> sortedCollectionOf(comparator: Comparator<T>, vararg elements: T): SortedList<T> =
    AvlTree(comparator) + elements

fun <T : Comparable<T>> Iterable<T>.toSortedCollection(): SortedList<T> = toSortedCollection(naturalOrder())

fun <T> Iterable<T>.toSortedCollection(comparator: Comparator<T>): SortedList<T> = AvlTree(comparator) + this

fun <T : Comparable<T>> Sequence<T>.toSortedCollection(): SortedList<T> = toSortedCollection(naturalOrder())

fun <T> Sequence<T>.toSortedCollection(comparator: Comparator<T>): SortedList<T> = AvlTree(comparator) + this

fun <T> SortedList<T>.drop(n: Int): SortedList<T> = subList(n, size)

fun <T> SortedList<T>.dropLast(n: Int): SortedList<T> = subList(0, size - n)

fun <T> SortedList<T>.take(n: Int): SortedList<T> = subList(0, n)

fun <T> SortedList<T>.takeLast(n: Int): SortedList<T> = subList(size - n, size)
