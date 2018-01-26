package node

import models.Block
import models.BlockChain
import mu.KotlinLogging
import org.vibrant.base.database.blockchain.InMemoryBlockChain
import org.vibrant.base.database.blockchain.models.BlockChainModel
import org.vibrant.core.ModelSerializer

class Chain(val difficulty: Int = 0): InMemoryBlockChain<Block, BlockChain>() {

    private val logger = KotlinLogging.logger {  }

    val state = ChainState(this)

    override fun checkIntegrity(): Boolean {
        this.blocks.reduce{a, b ->
            if(a.hash == b.previousHash){
                b
            }else{
                logger.info { "Wrong hashes, expected ${b.previousHash} == ${a.hash}" }
                return false
            }
        }
        return true
    }

    override fun createGenesisBlock(): Block {
        return JSONSerializer.deserialize(Chain::class.java.classLoader.getResource("genesis.json").readBytes()) as Block
    }

    override fun produce(serializer: ModelSerializer): BlockChain {
        return BlockChain(this.difficulty, this.blocks.toTypedArray())
    }


    override fun dump(copy: BlockChain){
        this.blocks.clear()
        this.blocks.addAll(copy.blocks)
        this.state.reset()
    }

    fun setBlocks(blocks: List<Block>){
        this.blocks.clear()
        this.blocks.addAll(blocks)
    }


    fun blocks(): List<Block> {
        return this.blocks.toList()
    }

    companion object {

        fun instantiate(blockChainModel: BlockChain): Chain {
            val producer = Chain()
            producer.blocks.clear()
            producer.blocks.addAll(blockChainModel.blocks)
            return producer
        }
    }
}