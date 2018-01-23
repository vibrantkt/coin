package models

import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.fasterxml.jackson.annotation.JsonTypeName
import org.vibrant.base.database.blockchain.models.BlockModel
import java.util.*

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY)
@JsonTypeName("block")
data class Block(val index: Int, val prevHash: String, val hash: String, val transactions: List<Transaction>, val nonce: Long, val timestamp: Long = Date().time): BlockModel()