import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.delay
import kotlinx.coroutines.experimental.newSingleThreadContext
import models.Block
import models.Transaction
import org.vibrant.example.chat.base.util.AccountUtils
import org.vibrant.example.chat.base.util.HashUtils
import java.security.PublicKey
import java.util.*

class Miner(val onBlock: (Block) -> Unit = {}): Node() {

    internal val pendingTransactions = arrayListOf<Transaction>()
    override val isMiner = true



    private val minerThread = async(newSingleThreadContext("miner loop")){
        while(true) {
            this@Miner.mine()
            if(this@Miner.pendingTransactions.size == 0)
                delay(1000)
        }
    }


    private fun mine(){
        if(this.pendingTransactions.isNotEmpty()){

            val transactions = this.pendingTransactions.toTypedArray().distinctBy { it.hash }.filter {
                val acc = this@Miner.chain.getAccount(it.from)
                val signatureFine = Transaction.verify(it)
                logger.info { "This transaction is from $acc"}
                logger.info { "This transaction is from $signatureFine"}
                acc.money >= it.payload.amount && signatureFine
            }
            synchronized(this.pendingTransactions) {
                this.pendingTransactions.clear()
                logger.info { "Cleared pending transactions" }
            }
            logger.info { "Validated transactions are $transactions" }
            if(transactions.isNotEmpty()) {
                val block = this.mineBlock(transactions)
                logger.info { "Block mined $block" }
                this@Miner.chain.addBlock(block)
                val response = this.peer.broadcast(
                        createRequest(
                                "onNewBlock",
                                arrayOf(block.serialize())
                        )
                )
                logger.info { "Broad casted $response" }
                this.onBlock(block)
            }
        }
    }

    private fun mineBlock(transactions: List<Transaction>): Block {
        val lastBlock = chain.latestBlock()
        var blockHash: String?
        var nonce = 0L
        val newIndex = lastBlock.index + 1
        val prevHash = lastBlock.hash
        val timestamp = Date().time
        do {
            nonce++
            val serializedTransactions = transactions.map { it.serialize() }.joinToString("")
            val content = newIndex.toString() + prevHash + serializedTransactions + nonce + timestamp
            blockHash = HashUtils.bytesToHex(HashUtils.sha256(content.toByteArray()))
        } while (blockHash!!.substring(0, chain.difficulty) != "0".repeat(chain.difficulty))

        return Block(newIndex, prevHash, blockHash, transactions, nonce, timestamp)
    }

    fun addTransaction(transaction: Transaction){
        logger.info { "Added pending transaction $transaction" }
        this.pendingTransactions.add(transaction)
    }
}