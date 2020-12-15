package org.axonframework.extension.kotlin.test

import org.axonframework.commandhandling.CommandHandler
import org.axonframework.commandhandling.RoutingKey
import org.axonframework.modelling.command.AggregateIdentifier

internal data class ExampleCommand(@RoutingKey val aggregateId: String)

internal class ExampleAggregate {

    @AggregateIdentifier
    lateinit var aggregateId: String

    constructor()

    @CommandHandler
    constructor(command: ExampleCommand) {
        throw Exception()
    }
}

class ExampleSaga