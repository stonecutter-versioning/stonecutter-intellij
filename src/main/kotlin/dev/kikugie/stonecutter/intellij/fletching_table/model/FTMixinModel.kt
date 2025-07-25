package dev.kikugie.stonecutter.intellij.fletching_table.model

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import java.nio.file.Path
import kotlin.io.path.inputStream

@Serializable
data class FTMixinModel(
    val implementation: String,
    val definition: String = "default",
    val environment: Env = Env.DEFAULT
) {
    @OptIn(ExperimentalSerializationApi::class)
    companion object {
        const val MIXIN_CONFIG = "fletching-table.mixins.config.json"
        const val MIXIN_DEF_FQ = "org.spongepowered.asm.mixin.Mixin"
        val LIST_SERIALIZER = ListSerializer(serializer())

        internal fun isOf(path: Path) = path.fileName.toString() == MIXIN_CONFIG
        internal fun readModel(path: Path): List<FTMixinModel> = path.inputStream().use {
            Json.decodeFromStream(LIST_SERIALIZER, it)
        }
    }

    @Serializable
    enum class Env {
        DEFAULT, MAIN, CLIENT, SERVER
    }
}
