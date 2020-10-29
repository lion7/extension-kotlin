/*
 * Copyright (c) 2010-2020. Axon Framework
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.axonframework.extensions.kotlin

import io.mockk.clearMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.axonframework.messaging.responsetypes.AbstractResponseType
import org.axonframework.messaging.responsetypes.InstanceResponseType
import org.axonframework.queryhandling.QueryGateway
import java.util.*
import java.util.concurrent.CompletableFuture
import java.util.concurrent.TimeUnit
import java.util.stream.Stream
import kotlin.test.*

/**
 * Tests Query Gateway extensions.
 *
 * @author Stefan Andjelkovic
 * @author Henrique Sena
 */
class QueryGatewayExtensionsTest {

    private val queryName = ExampleQuery::class.qualifiedName.toString()
    private val exampleQuery = ExampleQuery(2)
    private val instanceReturnValue: CompletableFuture<String> = CompletableFuture.completedFuture("2")
    private val optionalReturnValue: CompletableFuture<Optional<String>> = CompletableFuture.completedFuture(Optional.of("Value"))
    private val listReturnValue: CompletableFuture<List<String>> = CompletableFuture.completedFuture(listOf("Value", "Second value"))
    private val subjectGateway = mockk<QueryGateway>()
    private val timeout: Long = 1000
    private val timeUnit = TimeUnit.SECONDS
    private val streamInstanceReturnValue = Stream.of("Value")
    private val streamMultipleReturnValue = Stream.of(listOf("Value", "Second Value"))
    private val streamOptionalReturnValue = Stream.of(Optional.of("Value"))

    @BeforeTest
    fun before() {
        every { subjectGateway.query(exampleQuery, matchInstanceResponseType<String>()) } returns instanceReturnValue
        every { subjectGateway.query(exampleQuery, matchOptionalResponseType<String>()) } returns optionalReturnValue
        every { subjectGateway.query(exampleQuery, matchMultipleInstancesResponseType<String>()) } returns listReturnValue
        every { subjectGateway.query(queryName, exampleQuery, matchInstanceResponseType<String>()) } returns instanceReturnValue
        every { subjectGateway.query(queryName, exampleQuery, matchOptionalResponseType<String>()) } returns optionalReturnValue
        every { subjectGateway.query(queryName, exampleQuery, matchMultipleInstancesResponseType<String>()) } returns listReturnValue
        every { subjectGateway.scatterGather(exampleQuery, matchInstanceResponseType<String>(), timeout, timeUnit) } returns streamInstanceReturnValue
        every { subjectGateway.scatterGather(exampleQuery, matchMultipleInstancesResponseType<String>(), timeout, timeUnit) } returns streamMultipleReturnValue
        every { subjectGateway.scatterGather(exampleQuery, matchOptionalResponseType<String>(), timeout, timeUnit) } returns streamOptionalReturnValue
        every { subjectGateway.scatterGather(queryName, exampleQuery, matchInstanceResponseType<String>(), timeout, timeUnit) } returns streamInstanceReturnValue
        every { subjectGateway.scatterGather(queryName, exampleQuery, matchMultipleInstancesResponseType<String>(), timeout, timeUnit) } returns streamMultipleReturnValue
        every { subjectGateway.scatterGather(queryName, exampleQuery, matchOptionalResponseType<String>(), timeout, timeUnit) } returns streamOptionalReturnValue
    }

    @AfterTest
    fun after() {
        clearMocks(subjectGateway)
    }

    @Test
    fun `Query without queryName for Single should invoke query method with correct generic parameters`() {
        val queryResult = subjectGateway.queryForSingle<String, ExampleQuery>(query = exampleQuery)
        assertSame(queryResult, instanceReturnValue)
        verify(exactly = 1) {
            subjectGateway.query(exampleQuery, matchExpectedResponseType(String::class.java))
        }
    }

    @Test
    fun `Query without queryName for Single should invoke query method and not require explicit generic types`() {
        val queryResult:CompletableFuture<String> = subjectGateway.queryForSingle(query = exampleQuery)
        assertSame(queryResult, instanceReturnValue)
        verify(exactly = 1) {
            subjectGateway.query(exampleQuery, matchExpectedResponseType(String::class.java))
        }
    }

    @Test
    fun `Query without queryName for Optional should invoke query method with correct generic parameters`() {
        val queryResult = subjectGateway.queryForOptional<String, ExampleQuery>(query = exampleQuery)

        assertSame(queryResult, optionalReturnValue)
        verify(exactly = 1) { subjectGateway.query(exampleQuery, matchExpectedResponseType(String::class.java)) }
    }

    @Test
    fun `Query without queryName for Optional should invoke query method and not require explicit generic types`() {
        val queryResult: CompletableFuture<Optional<String>> = subjectGateway.queryForOptional(query = exampleQuery)

        assertSame(queryResult, optionalReturnValue)
        verify(exactly = 1) { subjectGateway.query(exampleQuery, matchExpectedResponseType(String::class.java)) }
    }

    @Test
    fun `Query without queryName for Multiple should invoke query method with correct generic parameters`() {
        val queryResult = subjectGateway.queryForMultiple<String, ExampleQuery>(query = exampleQuery)

        assertSame(queryResult, listReturnValue)
        verify(exactly = 1) { subjectGateway.query(exampleQuery, matchExpectedResponseType(String::class.java)) }
    }

    @Test
    fun `Query without queryName for Multiple should invoke query method and not require explicit generic types`() {
        val queryResult: CompletableFuture<List<String>> = subjectGateway.queryForMultiple(query = exampleQuery)

        assertSame(queryResult, listReturnValue)
        verify(exactly = 1) { subjectGateway.query(exampleQuery, matchExpectedResponseType(String::class.java)) }
    }

    @Test
    fun `Query without queryName for Single should handle nullable responses`() {
        val nullInstanceReturnValue: CompletableFuture<String?> = CompletableFuture.completedFuture(null)
        val nullableQueryGateway = mockk<QueryGateway> {
            every { query(exampleQuery, match { i: AbstractResponseType<String?> -> i is InstanceResponseType }) } returns nullInstanceReturnValue
        }

        val queryResult = nullableQueryGateway.queryForSingle<String?, ExampleQuery>(query = exampleQuery)

        assertSame(queryResult, nullInstanceReturnValue)
        assertEquals(nullInstanceReturnValue.get(), null)
        verify(exactly = 1) { nullableQueryGateway.query(exampleQuery, matchExpectedResponseType(String::class.java)) }
    }


    @Test
    fun `Query for Single should invoke query method with correct generic parameters`() {
        val queryResult = subjectGateway.queryForSingle<String, ExampleQuery>(queryName = queryName, query = exampleQuery)

        assertSame(queryResult, instanceReturnValue)
        verify(exactly = 1) { subjectGateway.query(queryName, exampleQuery, matchExpectedResponseType(String::class.java)) }
    }

    @Test
    fun `Query for Single should invoke query method and not require explicit generic types`() {
        val queryResult: CompletableFuture<String> = subjectGateway.queryForSingle(queryName = queryName, query = exampleQuery)

        assertSame(queryResult, instanceReturnValue)
        verify(exactly = 1) { subjectGateway.query(queryName, exampleQuery, matchExpectedResponseType(String::class.java)) }
    }

    @Test
    fun `Query for Optional should invoke query method with correct generic parameters`() {
        val queryResult = subjectGateway.queryForOptional<String, ExampleQuery>(queryName = queryName, query = exampleQuery)

        assertSame(queryResult, optionalReturnValue)
        verify(exactly = 1) { subjectGateway.query(queryName, exampleQuery, matchExpectedResponseType(String::class.java)) }
    }

    @Test
    fun `Query for Optional should invoke query method and not require explicit generic types`() {
        val queryResult: CompletableFuture<Optional<String>> = subjectGateway.queryForOptional(queryName = queryName, query = exampleQuery)

        assertSame(queryResult, optionalReturnValue)
        verify(exactly = 1) { subjectGateway.query(queryName, exampleQuery, matchExpectedResponseType(String::class.java)) }
    }

    @Test
    fun `Query for Multiple should invoke query method with correct generic parameters`() {
        val queryResult = subjectGateway.queryForMultiple<String, ExampleQuery>(queryName = queryName, query = exampleQuery)

        assertSame(queryResult, listReturnValue)
        verify(exactly = 1) { subjectGateway.query(queryName, exampleQuery, matchExpectedResponseType(String::class.java)) }
    }

    @Test
    fun `Query for Multiple should invoke query method and not require explicit generic types`() {
        val queryResult: CompletableFuture<List<String>> = subjectGateway.queryForMultiple(queryName = queryName, query = exampleQuery)

        assertSame(queryResult, listReturnValue)
        verify(exactly = 1) { subjectGateway.query(queryName, exampleQuery, matchExpectedResponseType(String::class.java)) }
    }

    @Test
    fun `Query for Single should handle nullable responses`() {
        val nullInstanceReturnValue: CompletableFuture<String?> = CompletableFuture.completedFuture(null)
        val nullableQueryGateway = mockk<QueryGateway> {
            every { query(queryName, exampleQuery, match { i: AbstractResponseType<String?> -> i is InstanceResponseType }) } returns nullInstanceReturnValue
        }

        val queryResult = nullableQueryGateway.queryForSingle<String?, ExampleQuery>(queryName = queryName, query = exampleQuery)

        assertSame(queryResult, nullInstanceReturnValue)
        assertEquals(nullInstanceReturnValue.get(), null)
        verify(exactly = 1) { nullableQueryGateway.query(queryName, exampleQuery, matchExpectedResponseType(String::class.java)) }
    }

        @Test
        fun `ScatterGather for Single should invoke scatterGather method with correct generic parameters`() {
            val result = subjectGateway.scatterGatherForSingle<String, ExampleQuery>(
                    query = exampleQuery,
                    timeout = timeout,
                    timeUnit = timeUnit
            )

            assertSame(result, streamInstanceReturnValue)
            verify(exactly = 1) { subjectGateway.scatterGather(exampleQuery, matchExpectedResponseType(String::class.java), timeout, timeUnit) }
        }

        @Test
        fun `ScatterGather for Multiple should invoke scatterGather method with correct generic parameters`() {
            val result = subjectGateway.scatterGatherForMultiple<String, ExampleQuery>(
                    query = exampleQuery,
                    timeout = timeout,
                    timeUnit = timeUnit
            )

            assertSame(result, streamMultipleReturnValue)
            verify(exactly = 1) { subjectGateway.scatterGather(exampleQuery, matchMultipleInstancesResponseType<String>(), timeout, timeUnit) }
        }

    @Test
    fun `ScatterGather for Optional should invoke scatterGather method with correct generic parameters`() {
        val result = subjectGateway.scatterGatherForOptional<String, ExampleQuery>(
                query = exampleQuery,
                timeout = timeout,
                timeUnit = timeUnit
        )

        assertSame(result, streamOptionalReturnValue)
        verify(exactly = 1) { subjectGateway.scatterGather(exampleQuery, matchOptionalResponseType<String>(), timeout, timeUnit) }
    }

    @Test
    fun `ScatterGather for Single should invoke scatterGather method with explicit query name`() {
        val result = subjectGateway.scatterGatherForSingle<String, ExampleQuery>(
                queryName = queryName,
                query = exampleQuery,
                timeout = timeout,
                timeUnit = timeUnit
        )

        assertSame(result, streamInstanceReturnValue)
        verify(exactly = 1) { subjectGateway.scatterGather(queryName, exampleQuery, matchExpectedResponseType(String::class.java), timeout, timeUnit) }
    }

    @Test
    fun `ScatterGather for Multiple should invoke scatterGather method with explicit query name`() {
        val result = subjectGateway.scatterGatherForMultiple<String, ExampleQuery>(
                queryName = queryName,
                query = exampleQuery,
                timeout = timeout,
                timeUnit = timeUnit
        )

        assertSame(result, streamMultipleReturnValue)
        verify(exactly = 1) { subjectGateway.scatterGather(queryName, exampleQuery, matchMultipleInstancesResponseType<String>(), timeout, timeUnit) }
    }

    @Test
    fun `ScatterGather for Optional should invoke scatterGather method with explicit query name`() {
        val result = subjectGateway.scatterGatherForOptional<String, ExampleQuery>(
                queryName = queryName,
                query = exampleQuery,
                timeout = timeout,
                timeUnit = timeUnit
        )

        assertSame(result, streamOptionalReturnValue)
        verify(exactly = 1) { subjectGateway.scatterGather(queryName, exampleQuery, matchOptionalResponseType<String>(), timeout, timeUnit) }
    }

}
