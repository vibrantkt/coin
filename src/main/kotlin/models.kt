import org.vibrant.base.rpc.json.JSONRPCResponse
import org.vibrant.core.models.Model

fun JSONRPCResponse<*>.stringResult(): String {
    return this.result.toString()
}

fun <T: Model>JSONRPCResponse<*>.deserialize(): T {
    @Suppress("UNCHECKED_CAST")
    return JSONSerializer.deserialize(this.result.toString().toByteArray()) as T
}



fun Model.serialize(): String {
    return String(JSONSerializer.serialize(this))
}