package dev.kikugie.stonecutter.intellij.fletching_table.model

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import java.nio.file.Path
import kotlin.io.path.inputStream

@Serializable
data class FTEntrypointModel(
    val kind: String,
    val reference: String,
    val adapter: String = "java"
) {
    @OptIn(ExperimentalSerializationApi::class)
    companion object {
        const val ENTRYPOINT_CONFIG = "fletching-table.entrypoints.config.json"
        const val ENTRYPOINT_FQ = "dev.kikugie.fletching_table.annotation.fabric.Entrypoint"
        val LIST_SERIALIZER = ListSerializer(serializer())

        internal fun isOf(path: Path) = path.fileName.toString() == ENTRYPOINT_CONFIG
        internal fun readModel(path: Path): List<FTEntrypointModel> = path.inputStream().use {
            Json.decodeFromStream(LIST_SERIALIZER, it)
        }
    }
}
