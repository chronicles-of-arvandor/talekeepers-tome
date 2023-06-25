package net.arvandor.talekeeper.failure

import net.arvandor.talekeeper.failure.ServiceFailureType.CONFLICT
import net.arvandor.talekeeper.failure.ServiceFailureType.GENERAL

data class ServiceFailure(
    val type: ServiceFailureType,
    val message: String,
    val cause: Throwable,
)

enum class ServiceFailureType {
    NOT_FOUND,
    BAD_REQUEST,
    BAD_RESPONSE,
    AUTHENTICATION_REQUIRED,
    AUTHORIZATION,
    RULES_VIOLATION,
    DUPLICATE,
    CONFLICT,
    GENERAL,
}

fun Exception.toServiceFailure() = ServiceFailure(
    toServiceFailureType(),
    message ?: "Unknown error",
    this,
)

fun Exception.toServiceFailureType() = when (this) {
    is OptimisticLockingFailureException -> CONFLICT
    else -> GENERAL
}
