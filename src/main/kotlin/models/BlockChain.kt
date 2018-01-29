package models

import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.fasterxml.jackson.annotation.JsonTypeName
import org.vibrant.base.database.blockchain.models.BlockChainModel
import java.util.*


@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY)
@JsonTypeName("chain")
data class BlockChain(private val difficulty: Int, val blocks: Array<Block>) : BlockChainModel() {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as BlockChain

        if (difficulty != other.difficulty) return false
        if (!Arrays.equals(blocks, other.blocks)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = difficulty
        result = 31 * result + Arrays.hashCode(blocks)
        return result
    }
}