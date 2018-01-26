package node

import deserialize
import models.Block
import models.BlockChain
import models.Transaction
import models.TransactionPayload
import org.vibrant.base.node.JSONRPCNode
import org.vibrant.base.rpc.json.JSONRPCResponse
import org.vibrant.core.node.RemoteNode
import org.vibrant.example.chat.base.util.HashUtils
import serialize
import stringResult
import java.io.*
import java.net.Socket
import java.security.KeyPair
import java.security.KeyFactory
import java.security.spec.PKCS8EncodedKeySpec
import java.nio.file.Paths
import java.nio.file.Files
import java.security.PrivateKey
import java.security.PublicKey
import java.security.spec.X509EncodedKeySpec
import java.io.ObjectInputStream
import java.io.ByteArrayInputStream




open class Node : JSONRPCNode<Peer>() {
    open val isMiner: Boolean = false
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


        private fun privateFromFile(path: String): PrivateKey {
            val keyBytes = Files.readAllBytes(Paths.get(path))
            val spec = PKCS8EncodedKeySpec(keyBytes)
            val kf = KeyFactory.getInstance("RSA")
            return kf.generatePrivate(spec)
        }

        private fun publicFromFile(path: String): PublicKey {
            val keyBytes = Files.readAllBytes(Paths.get(path))

            val spec = X509EncodedKeySpec(keyBytes)
            val kf = KeyFactory.getInstance("RSA")
            return kf.generatePublic(spec)
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
        val tp = TransactionPayload(amount)
        val transaction = Transaction.create(this.hexAccountAddress()!!, to, tp, this.keyPair!!)
        return this.peer.broadcast(createRequest(
                "addTransaction",
                arrayOf(transaction.serialize())
        ))
    }



    fun gimmeMoney(amount: Long): List<JSONRPCResponse<*>> {
        this.checkAccount()
        val transaction = Transaction.create("0x0", this.hexAccountAddress()!!, TransactionPayload(amount), this.keyPair!!)
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