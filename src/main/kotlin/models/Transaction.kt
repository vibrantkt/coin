package models

import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.fasterxml.jackson.annotation.JsonTypeName
import node.JSONSerializer
import org.vibrant.base.database.blockchain.models.HashedTransaction
import org.vibrant.base.util.HashUtils
import org.vibrant.base.util.SHA1
import org.vibrant.base.util.SignTools
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