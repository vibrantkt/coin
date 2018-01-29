import models.Block
import models.Transaction
import models.TransactionPayload
import node.Chain
import node.*
import org.junit.Assert.assertEquals
import org.junit.Test
import org.vibrant.base.util.HashUtils
import org.vibrant.base.util.SHA1
import org.vibrant.base.util.SHA256
import org.vibrant.base.util.SignTools
import org.vibrant.core.node.RemoteNode
import java.util.*
import java.util.concurrent.CountDownLatch

class TestPeer {
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
    fun `Successful connection`(){
        val node = Node()
        node.start()


        val miner = Miner()
        miner.start()

        node.connect(RemoteNode("localhost", miner.peer.port))


        assertEquals(
                1,
                node.peer.peers.size
        )

        assertEquals(
                1,
                miner.peer.peers.size
        )
    }




    @Test
    fun `Test sync peer ahead`(){
        val node = Node()
        node.start()


        val miner = Miner()
        miner.start()

        node.connect(RemoteNode("localhost", miner.peer.port))

        node.chain.addBlock(Block(
                1,
                node.chain.latestBlock().hash,
                "",
                listOf(),
                0
        ))

        node.synchronize(RemoteNode("localhost", miner.peer.port))
    }

    @Test
    fun `Test sync peer behind 1 block`(){
        val node = Node()
        node.start()


        val miner = Miner()
        miner.start()

        node.connect(RemoteNode("localhost", miner.peer.port))

        miner.chain.addBlock(Block(
                1,
                node.chain.latestBlock().hash,
                "",
                listOf(), 0
        ))

        node.synchronize(RemoteNode("localhost", miner.peer.port))
    }

    @Test
    fun `Test sync peer behind 2 blocks`(){
        val node = Node()
        node.start()


        val miner = Miner()
        miner.start()

        node.connect(RemoteNode("localhost", miner.peer.port))

        miner.chain.addBlock(Block(
                1,
                node.chain.latestBlock().hash,
                "",
                listOf(), 0
        ))

        miner.chain.addBlock(Block(
                2,
                "",
                "",
                listOf(), 0
        ))

        node.synchronize(RemoteNode("localhost", miner.peer.port))
    }

    @Test
    fun `Test sync peer ahead 2 blocks`(){
        val node = Node()
        node.start()


        val miner = Miner()
        miner.start()

        node.connect(RemoteNode("localhost", miner.peer.port))

        node.chain.addBlock(Block(
                1,
                node.chain.latestBlock().hash,
                "",
                listOf(), 0
        ))

        node.chain.addBlock(Block(
                2,
                "",
                "",
                listOf(), 0
        ))

        node.synchronize(RemoteNode("localhost", miner.peer.port))
    }

    @Test
    fun `Test mining accepted`(){
        val node = Node()
        node.start()

        val miner = Miner()
        miner.start()

        node.connect(RemoteNode("localhost", miner.peer.port))

        node.setAccount(SignTools.generateKeyPair("RSA"))
        node.transaction("lol", 5)

        assertEquals(
                1,
                miner.pendingTransactions.size
        )
    }

    @Test
    fun `Test mining broadcast`(){

        val node = Node()
        node.start()


        val node2 = Node()
        node2.start()


        val latch = CountDownLatch(1)
        val miner = Miner({
            latch.countDown()
        })
        miner.start()

        miner.connect(RemoteNode("localhost", node2.peer.port))
        node.connect(RemoteNode("localhost", node2.peer.port))

        node.setAccount(SignTools.generateKeyPair("RSA"))

        node.chain.addBlock(
                mineBlock(node.chain, listOf(
                        Transaction(
                                "0x0",
                                node.hexAccountAddress()!!,
                                TransactionPayload(1000L),
                                HashUtils.bytesToHex(SHA1.produceHash(("0x0" + node.hexAccountAddress()!! + TransactionPayload(1000L).serialize()).toByteArray())),
                                "signature"
                        )
                ))
        )

        node.synchronize(RemoteNode("localhost", miner.peer.port))

        miner.addTransaction(Transaction.create(node.hexAccountAddress()!!, node.hexAccountAddress()!!, TransactionPayload(100L), node.keyPair!!))


        assertEquals(
                1,
                miner.pendingTransactions.size
        )
        latch.await()
        assertEquals(
                0,
                miner.pendingTransactions.size
        )

        assertEquals(
                node.chain.produce(JSONSerializer),
                miner.chain.produce(JSONSerializer)
        )
        assertEquals(
                node.chain.produce(JSONSerializer),
                node2.chain.produce(JSONSerializer)
        )
        assertEquals(
                miner.chain.produce(JSONSerializer),
                node2.chain.produce(JSONSerializer)
        )


    }
}