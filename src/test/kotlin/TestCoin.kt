import models.Block
import models.Transaction
import models.TransactionPayload
import node.Chain
import node.*
import org.junit.Assert.assertEquals
import org.junit.Test
import org.vibrant.core.node.RemoteNode
import org.vibrant.example.chat.base.util.AccountUtils
import org.vibrant.example.chat.base.util.HashUtils
import java.util.*
import java.util.concurrent.CountDownLatch


class TestCoin{

    fun mineBlock(chain: Chain, transactions: List<Transaction>): Block {
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

    @Test
    fun `Test transaction result`(){
        val node = Node()
        node.start()

        val node2 = Node()
        node2.start()

        val latch = CountDownLatch(1)

        val miner = Miner({
            latch.countDown()
        })
        miner.start()

        node2.connect(RemoteNode("localhost", node.peer.port))
        node.connect(RemoteNode("localhost", miner.peer.port))

        node.setAccount(AccountUtils.generateKeyPair())
        node2.setAccount(AccountUtils.generateKeyPair())


        node.chain.addBlock(
                mineBlock(node.chain, listOf(
                        Transaction(
                                "0x0",
                                node.hexAccountAddress()!!,
                                TransactionPayload(1000L),
                                HashUtils.bytesToHex(HashUtils.sha1(("0x0" + node.hexAccountAddress()!! + TransactionPayload(1000L).serialize()).toByteArray())),
                                "signature"
                        )
                ))
        )

        node.synchronize(RemoteNode("localhost", miner.peer.port))

        val tp = Transaction.create(
                node.hexAccountAddress()!!,
                node2.hexAccountAddress()!!,
                TransactionPayload(100L),
                node.keyPair!!
        )

        node.peer.broadcast(node.createRequest(
                "addTransaction",
                arrayOf(tp.serialize())
        ))

        latch.await()

        assertEquals(
                100,
                node2.chain.getAccount(node2.hexAccountAddress()!!).money
        )


    }


    @Test
    fun `Test bad transaction result`(){
        val node = Node()
        node.start()

        val node2 = Node()
        node2.start()

        val latch = CountDownLatch(1)

        val miner = Miner({
            latch.countDown()
        })
        miner.start()

        node2.connect(RemoteNode("localhost", node.peer.port))
        node.connect(RemoteNode("localhost", miner.peer.port))

        node.setAccount(AccountUtils.generateKeyPair())
        node2.setAccount(AccountUtils.generateKeyPair())



        val tp = Transaction.create(
                node.hexAccountAddress()!!,
                node2.hexAccountAddress()!!,
                TransactionPayload(100L),
                AccountUtils.generateKeyPair()
        )

        node.peer.broadcast(node.createRequest(
                "addTransaction",
                arrayOf(tp.serialize())
        ))
        assertEquals(
                0,
                miner.chain.getAccount(node.hexAccountAddress()!!).money
        )

        assertEquals(
                0,
                miner.chain.getAccount(node.hexAccountAddress()!!).money
        )
    }


    @Test
    fun `Test signature transaction result`(){
        val node = Node()
        node.start()

        val node2 = Node()
        node2.start()

        val latch = CountDownLatch(1)

        val miner = Miner({
            latch.countDown()
        })
        miner.start()

        node2.connect(RemoteNode("localhost", node.peer.port))
        node.connect(RemoteNode("localhost", miner.peer.port))

        node.setAccount(AccountUtils.generateKeyPair())
        node2.setAccount(AccountUtils.generateKeyPair())

        node.chain.addBlock(
            mineBlock(node.chain, listOf(
                    Transaction(
                            "0x0",
                            node.hexAccountAddress()!!,
                            TransactionPayload(1000L),
                            HashUtils.bytesToHex(HashUtils.sha1(("0x0" + node.hexAccountAddress()!! + TransactionPayload(1000L).serialize()).toByteArray())),
                            "signature"
                        )
            ))
        )

        node.synchronize(RemoteNode("localhost", miner.peer.port))


        assertEquals(
                node.chain.produce(JSONSerializer),
                miner.chain.produce(JSONSerializer)
        )

        val tp = Transaction.create(
                node.hexAccountAddress()!!,
                node2.hexAccountAddress()!!,
                TransactionPayload(100L),
                node2.keyPair!!
        )

        node.peer.broadcast(node.createRequest(
                "addTransaction",
                arrayOf(tp.serialize())
        ))

        node.peer.broadcast(node.createRequest(
                "addTransaction",
                arrayOf(Transaction.create(
                        node.hexAccountAddress()!!,
                        node2.hexAccountAddress()!!,
                        TransactionPayload(200L),
                        node.keyPair!!
                ).serialize())
        ))

        latch.await()

        println(node.chain.produce(JSONSerializer).serialize())
        println(node2.chain.produce(JSONSerializer).serialize())
        println(miner.chain.produce(JSONSerializer).serialize())



        assertEquals(
                800,
                node.chain.getAccount(node.hexAccountAddress()!!)!!.money
        )

        assertEquals(
                200,
                node2.chain.getAccount(node2.hexAccountAddress()!!)!!.money
        )
    }
}