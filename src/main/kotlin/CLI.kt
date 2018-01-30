import node.Node
import org.vibrant.core.node.RemoteNode
import node.JSONSerializer
import org.vibrant.core.hash.SignTools

class CLI<out T: Node>(val node: T) {

    init {
        this.node.setAccount(Node.loadFromFile("${System.getProperty("user.home")}/ssh/coin"))
        this.node.connect(RemoteNode("localhost", 7002))
        if(node.chain.state.getAccount(this.node.hexAccountAddress()!!).money == 0L){
            this.node.gimmeMoney(1000)
        }
    }


    fun io(){
        while(true){
            print("     >")
            val input = readLine()!!
            val params = input.split(Regex("\\s+"))
            when(params[0]){
                "auth" -> {
                    val privatePath = params[1]
                    this.auth(privatePath)
                    println("Address now ${node.hexAccountAddress()}")
                }
                "create" -> {
                    val privatePath = params[1]
                    this.createAccount(privatePath)
                    println("Address now ${node.hexAccountAddress()}")
                    println("Saved to $privatePath")
                }
                "gimme" -> {
                    val moneyToGive = params[1].toLong()
                    this.node.gimmeMoney(moneyToGive)
                    println("Requested money.")
                }
                "connect" -> {
                    val address = params[1]
                    val port = params[2].toInt()
                    val success = this.node.connect(RemoteNode(address, port))
                    if(success){
                        this.node.synchronize(RemoteNode(address, port))
                        println("Connected and synced to $address:$port")
                    }else{
                        println("Can't connect to $address:$port")
                    }
                }
                "peers" -> {
                    println(this.node.peer.peers.joinToString(", ") {
                        it.address + ":" + it.port
                    })
                }
                "money" -> {
                    val account = this.node.chain.state.getAccount(this.node.hexAccountAddress()!!)
                    println(account.money.toString() + " coins")
                }
                "chain" -> {
                    println(
                            node.chain.produce(JSONSerializer).serialize()
                    )
                }
                "transaction" -> {
                    val address = params[1]
                    val amount = params[2].toLong()
                    this.node.transaction(
                            address,
                            amount
                    )
                    println("Transaction requested")
                }
                else -> {
                    println("Unrecognized command ${params[0]}")
                }
            }

        }
    }

    private fun createAccount(path: String){
        val keyPair = SignTools.generateKeyPair("RSA")
        this.node.setAccount(keyPair)
        Node.saveKeyPair(keyPair, path)
    }

    private fun auth(path1: String){
        val keyPair = Node.loadFromFile(path1)
        this.node.setAccount(keyPair)
    }
}
// auth /home/enchanting/ssh/coin