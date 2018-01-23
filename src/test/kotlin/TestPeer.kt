import models.Block
import models.Transaction
import models.TransactionPayload
import org.junit.Assert.assertEquals
import org.junit.Test
import org.vibrant.core.node.RemoteNode
import org.vibrant.example.chat.base.util.AccountUtils
import java.util.concurrent.CountDownLatch

class TestPeer {


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

        node.setAccount(AccountUtils.generateKeyPair())
        node.transaction("lol", 5)

        assertEquals(
                1,
                miner.pendingTransactions.size
        )
    }

    @Test
    fun `Test mining block`(){

        val latch = CountDownLatch(1)
        val miner = Miner({
            println("Block mined $it")
            latch.countDown()
        })
        miner.start()

        miner.addTransaction(Transaction.create("from", "to", TransactionPayload(100L)))

        assertEquals(
                1,
                miner.pendingTransactions.size
        )
        latch.await()
        assertEquals(
                0,
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

        miner.addTransaction(Transaction.create("from", "to", TransactionPayload(100L)))

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