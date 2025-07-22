package dev.kikugie.stonecutter.intellij.editor

import com.intellij.util.indexing.DataIndexer
import com.intellij.util.indexing.FileBasedIndex
import com.intellij.util.indexing.FileBasedIndexExtension
import com.intellij.util.indexing.FileContent
import com.intellij.util.indexing.ID
import com.intellij.util.io.DataExternalizer
import com.intellij.util.io.KeyDescriptor
import dev.kikugie.stonecutter.intellij.editor.index.StitcherIndexKey
import dev.kikugie.stonecutter.intellij.editor.index.StitcherScriptIndexer
import org.jetbrains.kotlin.idea.core.util.readNullable
import org.jetbrains.kotlin.idea.core.util.writeNullable
import org.jetbrains.kotlin.idea.core.util.writeString
import org.jetbrains.kotlin.incremental.storage.readString
import java.io.DataInput
import java.io.DataOutput

private val EXT_ID: ID<StitcherIndexKey, Int> = ID.create("Stonecutter Definitions")
private val VF_FILTER = FileBasedIndex.InputFilter {
    it.nameSequence.endsWith(".gradle.kts")
}

class StitcherIndexExtension : FileBasedIndexExtension<StitcherIndexKey, Int>() {
    override fun getName() = EXT_ID
    override fun getVersion(): Int = 0
    override fun getInputFilter() = VF_FILTER
    override fun dependsOnFileContent(): Boolean = true
    override fun getKeyDescriptor(): KeyDescriptor<StitcherIndexKey> = MyKeyDescriptor
    override fun getValueExternalizer(): DataExternalizer<Int> = MyValueExternalizer
    override fun getIndexer(): DataIndexer<StitcherIndexKey, Int, FileContent> = StitcherScriptIndexer

    private object MyKeyDescriptor : KeyDescriptor<StitcherIndexKey> {
        override fun getHashCode(value: StitcherIndexKey?): Int = value.hashCode()
        override fun isEqual(val1: StitcherIndexKey?, val2: StitcherIndexKey?): Boolean =
            val1 == val2

        override fun save(output: DataOutput, value: StitcherIndexKey?) =
            output.writeNullable(value) { writeString(it.value) }

        override fun read(input: DataInput): StitcherIndexKey? =
            input.readNullable { StitcherIndexKey(readString()) }
    }

    private object MyValueExternalizer : DataExternalizer<Int> {
        override fun save(output: DataOutput, value: Int?) =
            output.writeNullable(value, DataOutput::writeInt)

        override fun read(input: DataInput): Int? =
            input.readNullable(DataInput::readInt)
    }
}