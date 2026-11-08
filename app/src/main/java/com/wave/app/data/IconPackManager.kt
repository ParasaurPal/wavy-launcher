package com.wave.app.data

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Resources
import android.graphics.drawable.Drawable
import android.util.Xml
import org.xmlpull.v1.XmlPullParser

data class InstalledIconPack(
    val packageName: String,
    val name: String
)

object IconPackManager {
    private val parsedPacks = mutableMapOf<String, Map<String, String>>()

    fun getInstalledPacks(context: Context): List<InstalledIconPack> {
        val pm = context.packageManager
        val intent = Intent("org.adw.ActivityStarter.THEMES")
        val resolved = pm.queryIntentActivities(intent, 0)
        return resolved.map {
            InstalledIconPack(it.activityInfo.packageName, it.loadLabel(pm).toString())
        }.distinctBy { it.packageName }
    }

    fun getIcon(context: Context, component: ComponentName, packPackageName: String): Drawable? {
        val pm = context.packageManager
        val map = parsedPacks.getOrPut(packPackageName) { parseAppFilter(pm, packPackageName) ?: emptyMap() }
        val key = "${component.packageName}/${component.className}"
        val drawableName = map[key] ?: return null
        
        return try {
            val res = pm.getResourcesForApplication(packPackageName)
            val id = res.getIdentifier(drawableName, "drawable", packPackageName)
            if (id != 0) res.getDrawable(id, null) else null
        } catch (e: Exception) {
            null
        }
    }

    private fun parseAppFilter(pm: PackageManager, pkg: String): Map<String, String>? {
        try {
            val res = pm.getResourcesForApplication(pkg)
            val parser = getParser(res, pkg) ?: return null
            val map = mutableMapOf<String, String>()
            var event = parser.eventType
            while (event != XmlPullParser.END_DOCUMENT) {
                if (event == XmlPullParser.START_TAG && parser.name == "item") {
                    val comp = parser.getAttributeValue(null, "component")
                    val draw = parser.getAttributeValue(null, "drawable")
                    if (comp != null && draw != null) {
                        val key = comp.removePrefix("ComponentInfo{").removeSuffix("}")
                        map[key] = draw
                    }
                }
                event = parser.next()
            }
            return map
        } catch (e: Exception) {
            return null
        }
    }

    private fun getParser(res: Resources, pkg: String): XmlPullParser? {
        val xmlId = res.getIdentifier("appfilter", "xml", pkg)
        if (xmlId != 0) return res.getXml(xmlId)

        val rawId = res.getIdentifier("appfilter", "raw", pkg)
        if (rawId != 0) {
            return Xml.newPullParser().apply { setInput(res.openRawResource(rawId), null) }
        }

        return try {
            val stream = res.assets.open("appfilter.xml")
            Xml.newPullParser().apply { setInput(stream, null) }
        } catch (e: Exception) {
            null
        }
    }
}
