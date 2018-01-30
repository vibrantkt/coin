package models

import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.fasterxml.jackson.annotation.JsonTypeName
import org.vibrant.core.models.block.ClassicBlockModel
import java.util.*

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY)
@JsonTypeName("block")
data class Block(override val index: Long, override val previousHash: String, override val hash: String, val transactions: List<Transaction>, val nonce: Long, val timestamp: Long = Date().time): ClassicBlockModel(index, hash, previousHash)