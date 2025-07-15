@file:UseSerializers(PathSerializer::class)
package dev.kikugie.stonecutter.intellij.model

import dev.kikugie.stonecutter.intellij.model.serialized.PathSerializer
import kotlinx.serialization.UseSerializers
import java.nio.file.Path

interface GradleMember {
    val hierarchy: GradleProjectHierarchy
    val location: Path
}