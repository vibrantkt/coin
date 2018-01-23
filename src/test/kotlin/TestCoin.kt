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
            latch.countDown()
        })
        miner.start()

        node2.connect(RemoteNode("localhost", node.peer.port))
        node.connect(RemoteNode("localhost", miner.peer.port))

        node.setAccount(AccountUtils.generateKeyPair())



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

        assertEquals(
                100,
                node2.chain.getAccount(node.hexAccountAddress()!!)!!.money
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
                TransactionPayload(100L)
        )

        node.peer.broadcast(node.createRequest(
                "addTransaction",
                arrayOf(tp.serialize())
        ))

        latch.await()

        assertEquals(
                0,
                node2.chain.getAccount(node.hexAccountAddress()!!)!!.money
        )

        assertEquals(
                0,
                node.chain.getAccount(node.hexAccountAddress()!!)!!.money
        )


    }
}