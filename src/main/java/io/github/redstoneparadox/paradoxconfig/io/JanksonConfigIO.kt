package io.github.redstoneparadox.paradoxconfig.io

import blue.endless.jankson.*
import io.github.cottonmc.jankson.JanksonFactory
import io.github.redstoneparadox.paradoxconfig.config.CollectionConfigOption
import io.github.redstoneparadox.paradoxconfig.config.ConfigCategory
import io.github.redstoneparadox.paradoxconfig.config.ConfigOption
import io.github.redstoneparadox.paradoxconfig.config.DictionaryConfigOption
import net.minecraft.util.Identifier

class JanksonConfigIO: ConfigIO {
    private val jankson: Jankson

    override val fileExtension: String
        get() = "json5"

    init {
        val builder = JanksonFactory.builder()


        builder

            .registerSerializer(Identifier::class.java) { id, marshaller ->
                return@registerSerializer marshaller.serialize(id.toString())
            }
            .registerDeserializer(String::class.java, Identifier::class.java) { string, marshaller ->
                return@registerDeserializer Identifier.tryParse(string)
            }

            /*
            .registerSerializer(MutableCollection::class.java) { collection, marshaller ->
                val array = JsonArray()
                collection.forEach { marshaller.serialize(it) }
                return@registerSerializer array
            }
            */


        jankson = builder.build()
    }

    override fun read(string: String, config: ConfigCategory) {
        val json = jankson.load(string)
        deserializeCategory(json, config)
    }

    private fun deserializeCategory(json: JsonObject, category: ConfigCategory) {
        for (subcategory in category.getSubcategories()) {
            val obj = json[subcategory.key]
            if (obj is JsonObject) deserializeCategory(obj, subcategory)
        }

        for (option in category.getOptions()) {
            val element = json[option.key]
            if (element != null) deserializeOption(element, option)
        }
    }

    private fun deserializeOption(json: JsonElement, option: ConfigOption<*>) {
        if (json is JsonArray && option is CollectionConfigOption<*, *>) {
            val collection = mutableListOf<Any>()
            json.forEach { collection.add(jankson.marshaller.marshall(option.getElementKClass().java, it)) }
            option.set(collection)
        }
        else if (json is JsonObject && option is DictionaryConfigOption<*, *>) {
            val map = mutableMapOf<String, Any>()
            json.forEach { map[it.key] = jankson.marshaller.marshall(option.getValueKClass().java, it.value) }
            option.set(map)
        }
        else {
            val any = jankson.marshaller.marshall(option.getKClass().java, json)
            option.set(any)
        }
    }

    override fun write(config: ConfigCategory): String {
        return serializeCategory(config).first.toJson(true, true)
    }

    private fun serializeCategory(category: ConfigCategory): Pair<JsonObject, String> {
        val obj = JsonObject()

        for (subcategory in category.getSubcategories()) {
            val pair = serializeCategory(subcategory)
            obj.put(subcategory.key, pair.first, pair.second)
        }

        for (option in category.getOptions()) {
            val pair = serializeOption(option)
            obj.put(option.key, pair.first, pair.second)
        }

        return Pair(obj, category.comment)
    }

    private fun serializeOption(option: ConfigOption<*>): Pair<JsonElement, String> {
        val any = option.get()

        if (any is Collection<*>) {
            val array = JsonArray()
            any.forEach { array.add(jankson.marshaller.serialize(it)) }
            return Pair(array, option.comment)
        }
        if (any is Map<*, *>) {
            val obj = JsonObject()
            any.keys.forEach { if (it is String) { obj[it] = jankson.marshaller.serialize(any[it]) } }
            return Pair(obj, option.comment)
        }

        val element = jankson.marshaller.serialize(any)
        return Pair(element, option.comment)
    }
}