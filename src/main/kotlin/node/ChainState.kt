package node

import account.Account
import models.Block
import models.Transaction
import mu.KotlinLogging

class ChainState(private val chain: Chain) {

    private val logger = KotlinLogging.logger {  }
    private val accounts = hashMapOf<String, Account>()

    init {
        this.initialState()
        this.chain.addNewBlockListener(object: org.vibrant.base.database.blockchain.BlockChain.NewBlockListener<Block> {
            override fun nextBlock(blockModel: Block) {
                logger.info { "Got new block" }
                this@ChainState.updateState(blockModel)
            }
        })
    }

    private fun handleTransaction(transaction: Transaction){
        logger.info { "Handling $transaction" }
        if(!this.accounts.containsKey(transaction.from) && transaction.from != "0x0") {
            throw Exception("Lol? first transaction spends. bad.")
        }
        if(!this.accounts.containsKey(transaction.to)) {
            this.accounts[transaction.to] = Account(transaction.from, 0, listOf(transaction))
        }

        val prevState1 = this.accounts[transaction.from]!!
        val prevState2 = this.accounts[transaction.to]!!

        this.accounts[transaction.from] = Account(transaction.from, prevState1.money - transaction.payload.amount, prevState1.transactions + transaction)
        this.accounts[transaction.to] = Account(transaction.to, prevState2.money + transaction.payload.amount, prevState2.transactions + transaction)

    }

    private fun initialState(){
        this.accounts.clear()
        this.chain.blocks().forEach {
                    this.updateState(it)
                }
    }

    private fun updateState(block: Block){
        block.transactions.forEach {
            logger.info { "New block handle $block" }
            this.handleTransaction(it)
        }
    }


    fun reset(){
        this.initialState()
    }

    fun getAccount(address: String): Account{
        return this.accounts[address]?: Account(address, 0, listOf())
    }
}