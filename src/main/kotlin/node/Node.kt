package node

import models.Transaction
import models.SimpleTransactionPayload
import org.vibrant.core.hash.HashUtils
import org.vibrant.core.node.JSONRPCNode
import org.vibrant.core.node.RemoteNode
import org.vibrant.core.rpc.json.JSONRPCResponse
import serialize
import java.io.*
import java.net.Socket
import java.security.KeyPair


open class Node : JSONRPCNode<Peer>() {
    open val isMiner: Boolean = false

    @Suppress("LeakingThis")
    private val rpc = JSONRPCProtocol(this)

    override val peer = this.createPeer()

    val chain = Chain()

    internal var keyPair: KeyPair? = null

    fun setAccount(keyPair: KeyPair){
        this.keyPair = keyPair
    }

    fun hexAccountAddress(): String? {
        return if(this.keyPair != null){
            HashUtils.bytesToHex(this.keyPair!!.public.encoded)
        }else
            null
    }




    private fun createPeer(): Peer {
        var port = 7000
        while(true){
            port++
            try {
                Socket("localhost", port).close()
            }catch (e: Exception){
                return Peer(port, rpc)
            }
        }
    }


    companion object {
        fun saveKeyPair(keyPair: KeyPair, path: String){
            val b = ByteArrayOutputStream()
            val o = ObjectOutputStream(b)
            o.writeObject(keyPair)
            val res = b.toByteArray()
            val fos = FileOutputStream(path)
            fos.write(res)
            fos.close()
        }


        fun loadFromFile(path1: String): KeyPair {
            val fis = FileInputStream(path1)
            val bytes = fis.readBytes()
            val bis = ByteArrayInputStream(bytes)
            val a = ObjectInputStream(bis)
            return a.readObject() as KeyPair
        }
    }



    override fun connect(remoteNode: RemoteNode): Boolean {
        return try {
            this.request(this.createRequest(
                    "peer",
                    arrayOf()
            ), remoteNode)
            this.peer.addUniqueRemoteNode(remoteNode)
            true
        }catch (e: Exception){
            false
        }
    }

    fun synchronize(remoteNode: RemoteNode){
        this.rpc.synchronize(remoteNode)
    }


    fun transaction(to: String, amount: Long): List<JSONRPCResponse<*>> {
        this.checkAccount()
        val tp = SimpleTransactionPayload(amount)
        val transaction = Transaction.create(this.hexAccountAddress()!!, to, tp, this.keyPair!!)
        return this.peer.broadcast(createRequest(
                "addTransaction",
                arrayOf(transaction.serialize())
        ))
    }



    fun gimmeMoney(amount: Long): List<JSONRPCResponse<*>> {
        this.checkAccount()
        val transaction = Transaction.create("0x0", this.hexAccountAddress()!!, SimpleTransactionPayload(amount), this.keyPair!!)
        return this.peer.broadcast(createRequest(
                "addTransaction",
                arrayOf(transaction.serialize())
        ))
    }


    private fun checkAccount(){
        if(this.keyPair == null)
            throw Exception("No account selected")
    }
}