package models

import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.fasterxml.jackson.annotation.JsonTypeName
import org.vibrant.core.models.transaction.TransactionPayload

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY)
@JsonTypeName("transaction-payload")
data class SimpleTransactionPayload(val amount: Long): TransactionPayload()