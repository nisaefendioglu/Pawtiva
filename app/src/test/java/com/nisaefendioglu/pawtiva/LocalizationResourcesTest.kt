package com.nisaefendioglu.pawtiva

import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File
import javax.xml.parsers.DocumentBuilderFactory

/**
 * Guards against untranslated strings: every translatable key in a module's default
 * values/strings.xml must also exist in values-tr/strings.xml, and vice versa.
 * Plain JVM test that reads the XML files from the source tree.
 */
class LocalizationResourcesTest {

    private val modules = listOf(
        "app",
        "core/designsystem",
        "feature/onboarding",
        "feature/home",
        "feature/habits",
        "feature/shop",
        "feature/statistics",
        "feature/settings",
    )

    private fun projectRoot(): File {
        var dir: File? = File("").absoluteFile
        while (dir != null && !File(dir, "settings.gradle.kts").exists()) dir = dir.parentFile
        return requireNotNull(dir) { "Project root not found" }
    }

    private fun keys(file: File): Set<String> {
        if (!file.exists()) return emptySet()
        val doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(file)
        val result = mutableSetOf<String>()
        for (tag in listOf("string", "plurals", "string-array")) {
            val nodes = doc.getElementsByTagName(tag)
            for (i in 0 until nodes.length) {
                val element = nodes.item(i) as org.w3c.dom.Element
                if (element.getAttribute("translatable") != "false") result += element.getAttribute("name")
            }
        }
        return result
    }

    @Test
    fun englishAndTurkishStringKeysMatch() {
        val root = projectRoot()
        modules.forEach { module ->
            val res = File(root, "$module/src/main/res")
            val en = keys(File(res, "values/strings.xml"))
            val tr = keys(File(res, "values-tr/strings.xml"))
            assertTrue("$module: missing in values-tr: ${en - tr}", (en - tr).isEmpty())
            assertTrue("$module: only in values-tr: ${tr - en}", (tr - en).isEmpty())
        }
    }
}
