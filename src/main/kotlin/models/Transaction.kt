package models

import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.fasterxml.jackson.annotation.JsonTypeName
import org.vibrant.base.database.blockchain.models.TransactionModel
import org.vibrant.base.database.blockchain.models.TransactionPayload
import org.vibrant.example.chat.base.util.HashUtils
import serialize


@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY)
@JsonTypeName("transaction")
data class Transaction(val from: String, val to: String, val payload: models.TransactionPayload, val hash: String): TransactionModel(){
    companion object {
        fun create(from: String, to: String, payload: models.TransactionPayload): Transaction {
            return Transaction(from, to, payload, HashUtils.bytesToHex(HashUtils.sha1((from + to + payload.serialize()).toByteArray())))
        }
    }
}