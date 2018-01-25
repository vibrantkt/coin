import node.Miner
import node.Node

fun main(args: Array<String>) {
    print("Enable miner(y/n)")
    val isMiner = readLine()!!.toLowerCase() == "y"
    val node = if(isMiner){
        Miner()
    }else{
        Node()
    }

    node.start()
    CLI(node).io()
}