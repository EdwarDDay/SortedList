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

interface SortedList<E> : List<E> {

    fun min(): E?

    fun max(): E?

    override fun subList(fromIndex: Int, toIndex: Int): SortedList<E>

    fun subListByElement(from: E, to: E): SortedList<E>

    fun headList(to: E): SortedList<E>

    fun tailList(from: E): SortedList<E>

    operator fun plus(element: E): SortedList<E>

    operator fun plus(elements: Collection<E>): SortedList<E>

    operator fun minus(element: E): SortedList<E>

    operator fun minus(elements: Collection<E>): SortedList<E>
}
