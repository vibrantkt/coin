package models

import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.fasterxml.jackson.annotation.JsonTypeName
import node.JSONSerializer
import org.vibrant.core.hash.HashUtils
import org.vibrant.core.hash.SHA1
import org.vibrant.core.hash.SignTools
import org.vibrant.core.models.transaction.HashedTransactionModel
import serialize
import java.security.KeyPair
import java.security.PublicKey


@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY)
@JsonTypeName("transaction")
data class Transaction(
        val from: String,
        val to: String,
        val payload: SimpleTransactionPayload,
        override val hash: String,
        val signature: String): HashedTransactionModel(hash){
    companion object {
        fun create(from: String, to: String, payload: SimpleTransactionPayload, keyPair: KeyPair): Transaction {
            return Transaction(
                    from,
                    to,
                    payload,
                    HashUtils.bytesToHex(SHA1.produceHash((from + to + payload.serialize()).toByteArray())),
                    HashUtils.bytesToHex(SignTools.signDataWith(from.toByteArray() + to.toByteArray() + JSONSerializer.serialize(payload), "SHA1withRSA", keyPair)))
        }


        fun verify(it: Transaction): Boolean {
            return SignTools.verifyDataSignature(it.from.toByteArray() + it.to.toByteArray() + JSONSerializer.serialize(it.payload), object: PublicKey {
                override fun getAlgorithm(): String {
                    return "RSA"
                }

                override fun getEncoded(): ByteArray {
                    return HashUtils.hexToBytes(it.from)
                }

                override fun getFormat(): String {
                    return "X.509"
                }

            }, HashUtils.hexToBytes(it.signature), "SHA1withRSA")
        }
    }
}