import node.JSONSerializer
import org.vibrant.core.models.Model


fun Model.serialize(): String {
    return String(JSONSerializer.serialize(this))
}