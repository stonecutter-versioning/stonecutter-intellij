package dev.kikugie.stonecutter.intellij.fletching_table

import dev.kikugie.stonecutter.intellij.fletching_table.model.FTEntrypointModel
import dev.kikugie.stonecutter.intellij.fletching_table.model.FTMixinModel
import java.nio.file.Path
import kotlin.io.path.exists
import kotlin.io.path.walk

class FletchingTableAccessor(val directory: Path) {
    val resources: Sequence<Path> get() = directory.resolve("build/generated/ksp")
        .takeIf(Path::exists)?.walk().orEmpty()

    val mixins: List<FTMixinModel> by lazy {
        resources.filter(FTMixinModel::isOf).flatMap(FTMixinModel::readModel).toList()
    }

    val entrypoints: List<FTEntrypointModel> by lazy {
        resources.filter(FTEntrypointModel::isOf).flatMap(FTEntrypointModel::readModel).toList()
    }
}