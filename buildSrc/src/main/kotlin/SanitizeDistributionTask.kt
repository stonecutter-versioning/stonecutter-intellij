import net.lingala.zip4j.ZipFile
import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.SetProperty
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.util.PatternSet
import java.nio.file.Path
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream
import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.createTempDirectory
import kotlin.io.path.deleteRecursively
import kotlin.io.path.inputStream

abstract class SanitizeDistributionTask : DefaultTask() {
    @get:InputFile
    abstract val file: RegularFileProperty

    @get:Input
    abstract val patterns: SetProperty<String>

    @TaskAction
    fun run() {
        val file = ZipFile(file.asFile.get())
        val matchers = patterns.get()
        val headers = file.fileHeaders.filter { check(it.fileName, matchers) }
        for (header in headers) file.removeFile(header)
    }

    private fun check(filename: String, prefixes: Set<String>): Boolean {
        val file = filename.substringAfterLast('/')
        return prefixes.any { file.startsWith(it) }
    }
}