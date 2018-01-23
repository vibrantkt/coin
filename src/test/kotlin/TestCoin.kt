import models.Transaction
import models.TransactionPayload
import org.junit.Assert
import org.junit.Assert.assertEquals
import org.junit.Test
import org.vibrant.core.node.RemoteNode
import org.vibrant.example.chat.base.util.AccountUtils
import java.util.concurrent.CountDownLatch


class TestCoin{


    @Test
    fun `Test transaction result`(){
        val node = Node()
        node.start()

        val node2 = Node()
        node2.start()

        val latch = CountDownLatch(1)

        val miner = Miner({
            println("Block mined $it")
            latch.countDown()
        })
        miner.start()

        node2.connect(RemoteNode("localhost", node.peer.port))
        node.connect(RemoteNode("localhost", miner.peer.port))

        node.setAccount(AccountUtils.generateKeyPair())


        assertEquals(
                1,
                node2.peer.peers.size
        )
        assertEquals(
                2,
                node.peer.peers.size
        )
        assertEquals(
                1,
                miner.peer.peers.size
        )


        val tp = Transaction.create(
                "0x0",
                node.hexAccountAddress()!!,
                TransactionPayload(100L)
        )

        node.peer.broadcast(node.createRequest(
                "addTransaction",
                arrayOf(tp.serialize())
        ))

        latch.await()

        println(node.chain.produce(JSONSerializer).serialize())
        println(miner.chain.produce(JSONSerializer).serialize())
        println(node2.chain.produce(JSONSerializer).serialize())

        assertEquals(
                100,
                node2.chain.getAccount(node.hexAccountAddress()!!)!!.money
        )


    }
}