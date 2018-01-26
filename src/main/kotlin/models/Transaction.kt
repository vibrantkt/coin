package models

import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.fasterxml.jackson.annotation.JsonTypeName
import org.vibrant.base.database.blockchain.models.HashedTransaction
import org.vibrant.base.database.blockchain.models.TransactionModel
import org.vibrant.base.database.blockchain.models.TransactionPayload
import org.vibrant.example.chat.base.util.AccountUtils
import org.vibrant.example.chat.base.util.HashUtils
import serialize
import java.security.KeyPair
import java.security.PublicKey


@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY)
@JsonTypeName("transaction")
data class Transaction(
        val from: String,
        val to: String,
        val payload: models.TransactionPayload,
        override val hash: String,
        val signature: String): HashedTransaction(hash){
    companion object {
        fun create(from: String, to: String, payload: models.TransactionPayload, keyPair: KeyPair): Transaction {
            return Transaction(
                    from,
                    to,
                    payload,
                    HashUtils.bytesToHex(HashUtils.sha1((from + to + payload.serialize()).toByteArray())),
                    HashUtils.bytesToHex(AccountUtils.signData((from + to + payload.serialize()), keyPair)))
        }


        fun verify(it: Transaction): Boolean {
            return AccountUtils.verifySignature(it.from + it.to + it.payload.serialize(), object: PublicKey {
                override fun getAlgorithm(): String {
                    return "RSA"
                }

                override fun getEncoded(): ByteArray {
                    return HashUtils.hexToBytes(it.from)
                }

                override fun getFormat(): String {
                    return "X.509"
                }

            }, HashUtils.hexToBytes(it.signature))
        }
    }
}