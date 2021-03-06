import models.Block
import models.Transaction
import models.SimpleTransactionPayload
import node.Chain
import node.*
import org.junit.Assert.assertEquals
import org.junit.Test
import org.vibrant.core.hash.HashUtils
import org.vibrant.core.hash.SHA1
import org.vibrant.core.hash.SHA256
import org.vibrant.core.hash.SignTools
import org.vibrant.core.node.RemoteNode
import java.util.*
import java.util.concurrent.CountDownLatch


class TestCoin{

    private fun mineBlock(chain: Chain, transactions: List<Transaction>): Block {
        val lastBlock = chain.latestBlock()
        var blockHash: String?
        var nonce = 0L
        val newIndex = lastBlock.index + 1
        val prevHash = lastBlock.hash
        val timestamp = Date().time
        do {
            nonce++
            val serializedTransactions = transactions.joinToString("") { it.serialize() }
            val content = newIndex.toString() + prevHash + serializedTransactions + nonce + timestamp
            blockHash = HashUtils.bytesToHex(SHA256.produceHash(content.toByteArray()))
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

        node.setAccount(SignTools.generateKeyPair("RSA"))
        node2.setAccount(SignTools.generateKeyPair("RSA"))


        node.chain.addBlock(
                mineBlock(node.chain, listOf(
                        Transaction(
                                "0x0",
                                node.hexAccountAddress()!!,
                                SimpleTransactionPayload(1000L),
                                HashUtils.bytesToHex(SHA1.produceHash(("0x0" + node.hexAccountAddress()!! + SimpleTransactionPayload(1000L).serialize()).toByteArray())),
                                "signature"
                        )
                ))
        )

        node.synchronize(RemoteNode("localhost", miner.peer.port))

        val tp = Transaction.create(
                node.hexAccountAddress()!!,
                node2.hexAccountAddress()!!,
                SimpleTransactionPayload(100L),
                node.keyPair!!
        )

        node.peer.broadcast(node.createRequest(
                "addTransaction",
                arrayOf(tp.serialize())
        ))

        latch.await()

        assertEquals(
                100,
                node2.chain.state.getAccount(node2.hexAccountAddress()!!).money
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

        node.setAccount(SignTools.generateKeyPair("RSA"))
        node2.setAccount(SignTools.generateKeyPair("RSA"))



        val tp = Transaction.create(
                node.hexAccountAddress()!!,
                node2.hexAccountAddress()!!,
                SimpleTransactionPayload(100L),
                SignTools.generateKeyPair("RSA")
        )

        node.peer.broadcast(node.createRequest(
                "addTransaction",
                arrayOf(tp.serialize())
        ))
        assertEquals(
                0,
                miner.chain.state.getAccount(node.hexAccountAddress()!!).money
        )

        assertEquals(
                0,
                miner.chain.state.getAccount(node.hexAccountAddress()!!).money
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

        node.setAccount(SignTools.generateKeyPair("RSA"))
        node2.setAccount(SignTools.generateKeyPair("RSA"))

        node.chain.addBlock(
            mineBlock(node.chain, listOf(
                    Transaction(
                            "0x0",
                            node.hexAccountAddress()!!,
                            SimpleTransactionPayload(1000L),
                            HashUtils.bytesToHex(SHA1.produceHash(("0x0" + node.hexAccountAddress()!! + SimpleTransactionPayload(1000L).serialize()).toByteArray())),
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
                SimpleTransactionPayload(100L),
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
                        SimpleTransactionPayload(200L),
                        node.keyPair!!
                ).serialize())
        ))

        latch.await()

//        println(node.chain.produce(JSONSerializer).serialize())
//        println(node2.chain.produce(JSONSerializer).serialize())
//        println(miner.chain.produce(JSONSerializer).serialize())



        assertEquals(
                800,
                node.chain.state.getAccount(node.hexAccountAddress()!!).money
        )

        assertEquals(
                200,
                node2.chain.state.getAccount(node2.hexAccountAddress()!!).money
        )
    }
}