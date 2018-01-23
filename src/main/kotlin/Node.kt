import models.Block
import models.BlockChain
import models.Transaction
import models.TransactionPayload
import org.vibrant.base.node.JSONRPCNode
import org.vibrant.base.rpc.json.JSONRPCResponse
import org.vibrant.core.node.RemoteNode
import org.vibrant.example.chat.base.util.HashUtils
import java.net.Socket
import java.security.KeyPair

open class Node : JSONRPCNode<Peer>() {
    open val isMiner: Boolean = false
    override val peer = this.createPeer()

    val chain = Chain()

    private var keyPair: KeyPair? = null


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
                return Peer(port, JSONRPCProtocol(this))
            }
        }
    }




    override fun connect(remoteNode: RemoteNode): Boolean {
        val some = this.request(this.createRequest(
                "peer",
                arrayOf()
        ), remoteNode)
        this.peer.addUniqueRemoteNode(remoteNode)
        return true
    }

    fun handleLastBlock(remoteNode: RemoteNode, lastBlock: Block): Boolean {
        val localLatestBlock = this.chain.latestBlock()
        if(localLatestBlock != lastBlock){
            logger.info { " My chain is not in sync with peer $remoteNode" }
            when {
            // next block
                lastBlock.index - localLatestBlock.index == 1 && lastBlock.prevHash == localLatestBlock.hash -> {
                    this.chain.addBlock(
                            lastBlock
                    )
                    logger.info { "I just got next block. Chain good: ${chain.checkIntegrity()}" }
                }
            // block is ahead
                lastBlock.index > localLatestBlock.index -> {
                    logger.info { "My chain is behind, requesting full chain" }
                    val chainResponse = this.request(
                            this.createRequest("getFullChain", arrayOf()),
                            remoteNode
                    )

                    val model = chainResponse.deserialize<BlockChain>()
                    val tmpChain = Chain.instantiate(model)
                    val chainOK = tmpChain.checkIntegrity()

                    if(chainOK){
                        logger.info { "Received chain is fine, replacing" }
                        this.chain.dump(model)
                        logger.info { "Received chain is fine, replaced" }
                    }else{
                        logger.info { "Received chain is not fine, I ignore it" }
                    }
                }
            // block is behind
                else -> {
                    logger.info { "My chain is ahead, sending request" }
                    val response = this.request(this.createRequest("syncWithMe", arrayOf()), remoteNode)
                    logger.info { "Got response! $response" }
                }
            }
            return false
        }else{
            logger.info { "Chain in sync with peer $remoteNode" }
            return true
        }
    }

    fun synchronize(remoteNode: RemoteNode){
        val lastBlock = this.request(
                this.createRequest("getLastBlock", arrayOf()),
                remoteNode
        )
        val block = JSONSerializer.deserialize(lastBlock.stringResult().toByteArray()) as Block
        this.handleLastBlock(remoteNode, block)
    }


    fun transaction(to: String, amount: Long): List<JSONRPCResponse<*>> {
        this.checkAccount()
        val tp = TransactionPayload(amount)
        val transaction = Transaction.create(this.hexAccountAddress()!!, to, tp)
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