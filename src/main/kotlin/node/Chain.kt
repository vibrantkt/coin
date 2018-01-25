package node

import account.Account
import models.Block
import models.BlockChain
import models.Transaction
import mu.KotlinLogging
import org.vibrant.base.database.blockchain.InMemoryBlockChain
import org.vibrant.core.ModelSerializer

class Chain(val difficulty: Int = 0): InMemoryBlockChain<Block, BlockChain>() {

    private val logger = KotlinLogging.logger {  }

    override fun checkIntegrity(): Boolean {
        this.blocks.reduce{a, b ->
            if(a.hash == b.prevHash){
                b
            }else{
                logger.info { "Wrong hashes, expected ${b.prevHash} == ${a.hash}" }
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



    fun getAllTransactions(predicate: (Transaction) -> Boolean): List<Transaction> {
        return this.blocks.flatMap { it.transactions.filter(predicate) }
    }

    fun getAccount(address: String): Account{
        val transactionsWithHim = getAllTransactions { it.from == address || it.to == address }
        val money = transactionsWithHim.fold(0L){ cum, transaction ->
            cum + if(transaction.from == address && transaction.to != address)-transaction.payload.amount else transaction.payload.amount
        }
        return Account(address, money, transactionsWithHim)
    }





    fun dump(blockChainModel: BlockChain){
        this.blocks.clear()
        this.blocks.addAll(blockChainModel.blocks)
        this.notifyNewBlock()
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