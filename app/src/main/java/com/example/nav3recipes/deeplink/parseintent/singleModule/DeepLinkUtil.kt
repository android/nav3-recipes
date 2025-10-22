package com.example.nav3recipes.deeplink.parseintent.singleModule

import android.net.Uri
import android.util.Log
import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.SerialKind
import kotlinx.serialization.encoding.AbstractDecoder
import kotlinx.serialization.encoding.CompositeDecoder
import kotlinx.serialization.modules.EmptySerializersModule
import kotlinx.serialization.modules.SerializersModule
import java.io.Serializable

/**
 * parse the requested Uri and store it in a easily readable format
 *
 * @param uri the target deeplink uri to link to
 */
internal class DeepLinkRequest(
    val uri: Uri
) {
    /**
     * A list of path segments
     */
    val pathSegments: List<String> = uri.pathSegments

    /**
     * A map of query name to query value
     */
    val queries = buildMap {
        uri.queryParameterNames.forEach { argName ->
            this[argName] = uri.getQueryParameter(argName)!!
        }
    }

    /**
     * match this target with a supported deeplink
     *
     * returns a [DeepLinkMatchResult] if this matches the candidate, returns null otherwise
     */
    fun <T : NavKey> match(deepLinkMapper: DeepLinkMapper<T>): DeepLinkMatchResult<T>? {
        if (pathSegments.size != deepLinkMapper.pathSegments.size) return null
        // exact match (url does not contain any arguments)
        if (uri == deepLinkMapper.uriPattern)
            return DeepLinkMatchResult(deepLinkMapper.serializer, mapOf())

        val args = mutableMapOf<String, Any>()
        // match the path
        pathSegments
            .asSequence()
            // zip to compare the two objects side by side, order matters here so we
            // need to make sure the compared segments are at the same position within the url
            .zip(deepLinkMapper.pathSegments.asSequence())
            .forEach { it ->
                // retrieve the two path segments to compare
                val requestedSegment = it.first
                val candidateSegment = it.second
                // if the potential match expects a path arg for this segment, try to parse the
                // requested segment into the expected type
                if (candidateSegment.isParamArg) {
                    val parsedValue = try {
                        candidateSegment.typeParser.invoke(requestedSegment)
                    } catch (e: IllegalArgumentException) {
                        Log.e(TAG_LOG_ERROR, "Failed to parse path value:[$requestedSegment].", e)
                        return null
                    }
                    args[candidateSegment.stringValue] = parsedValue
                } else if(requestedSegment != candidateSegment.stringValue){
                    // if it's path arg is not the expected type, its not a match
                    return null
                }
            }
        // match queries (if any)
        queries.forEach { query ->
            val name = query.key
            val queryStringParser = deepLinkMapper.queryValueParsers[name]
            val queryParsedValue = try {
                queryStringParser!!.invoke(query.value)
            } catch (e: IllegalArgumentException) {
                Log.e(TAG_LOG_ERROR, "Failed to parse query name:[$name] value:[${query.value}].", e)
                return null
            }
            args[name] = queryParsedValue
        }
        // provide the serializer of the matching key and map of arg names to parsed arg values
        return DeepLinkMatchResult(deepLinkMapper.serializer, args)
    }
}

/**
 * Parse a supported deeplink and stores its metadata as a easily readable format
 *
 * The following notes applies specifically to this particular sample implementation:
 *
 * The supported deeplink is expected to be built from a serializable backstack key [T] that
 * supports deeplink. This means that if this deeplink contains any arguments (path or query),
 * the argument name must match any of [T] member field name.
 *
 * One [DeepLinkMapper] should be created for each supported deeplink. This means if [T]
 * supports two deeplink patterns:
 * ```
 *  val deeplink1 = www.nav3recipes.com/home
 *  val deeplink2 = www.nav3recipes.com/profile/{userId}
 *  ```
 * Then two [DeepLinkMapper] should be created
 * ```
 * val parsedDeeplink1 = DeepLinkCandidate(T.serializer(), deeplink1)
 * val parsedDeeplink2 = DeepLinkCandidate(T.serializer(), deeplink2)
 * ```
 *
 * This implementation assumes a few things:
 * 1. all path arguments are required/non-nullable - partial path matches will be considered a non-match
 * 2. all query arguments are optional by way of nullable/has default value
 *
 * @param T the backstack key type that supports the deeplinking of [uriPattern]
 * @param serializer the serializer of [T]
 * @param uriPattern the supported deeplink's uri pattern, i.e. "abc.com/home/{pathArg}"
 */
internal class DeepLinkMapper<T : NavKey>(
    val serializer: KSerializer<T>,
    val uriPattern: Uri
) {
    /**
     * Help differentiate if a path segment is an argument or a static value
     */
    private val regexPatternFillIn = Regex("\\{(.+?)\\}")

    // TODO make these lazy
    /**
     * parse the path into a list of [PathSegment]
     *
     * order matters here - path segments need to match in value and order when matching
     * requested deeplink to supported deeplink
     */
    val pathSegments: List<PathSegment>  = buildList {
        uriPattern.pathSegments.forEach { segment ->
            // first, check if it is a path arg
            var result = regexPatternFillIn.find(segment)
            if (result != null) {
                // if so, extract the path arg name (the string value within the curly braces)
                val argName = result.groups[1]!!.value
                // from [T], read the primitive type of this argument to get the correct type parser
                val elementIndex = serializer.descriptor.getElementIndex(argName)
                val elementDescriptor = serializer.descriptor.getElementDescriptor(elementIndex)
                // finally, add the arg name and its respective type parser to the map
                add(PathSegment(argName, true,getTypeParser(elementDescriptor.kind)))
            } else {
                // if its not a path arg, then its just a static string path segment
                add(PathSegment(segment,false, getTypeParser(PrimitiveKind.STRING)))
            }
        }
    }

    /**
     * Parse supported queries into a map of queryParameterNames to [TypeParser]
     *
     * This will be used later on to parse a provided query value into the correct KType
     */
    val queryValueParsers: Map<String, TypeParser> = buildMap {
        uriPattern.queryParameterNames.forEach { paramName ->
            val elementIndex = serializer.descriptor.getElementIndex(paramName)
            val elementDescriptor = serializer.descriptor.getElementDescriptor(elementIndex)
            this[paramName] = getTypeParser(elementDescriptor.kind)
        }
    }

    /**
     * Metadata about a supported path segment
     */
    class PathSegment(
        val stringValue: String,
        val isParamArg: Boolean,
        val typeParser: TypeParser
    )
}

/**
 * Created when a requested deeplink matches with a supported deeplink
 *
 * @param [T] the backstack key associated with the deeplink that matched with the requested deeplink
 * @param serializer serializer for [T]
 * @param args The map of argument name to argument value. The value is expected to have already
 * been parsed from the raw url string back into its proper KType as declared in [T].
 * Includes arguments for all parts of the uri - path, query, etc.
 * */
internal data class DeepLinkMatchResult<T : NavKey>(
    val serializer: KSerializer<T>,
    val args: Map<String, Any>
)

/**
 * Decodes the list of arguments into a a backstack key
 *
 * **IMPORTANT** This decoder assumes that all argument types are Primitives.
 */
@OptIn(ExperimentalSerializationApi::class)
internal class KeyDecoder(
    private val arguments: Map<String, Any>,
) : AbstractDecoder() {

    override val serializersModule: SerializersModule = EmptySerializersModule()
    private var elementIndex: Int = -1
    private var elementName: String = ""

    /**
     * Decodes the index of the next element to be decoded. Index represents a position of the
     * current element in the [descriptor] that can be found with [descriptor].getElementIndex.
     *
     * The returned index will trigger deserializer to call [decodeValue] on the argument at that
     * index.
     *
     * The decoder continually calls this method to process the next available argument until this
     * method returns [CompositeDecoder.DECODE_DONE], which indicates that there are no more
     * arguments to decode.
     *
     * This method should sequentially return the element index for every element that has its value
     * available within [arguments].
     */
    override fun decodeElementIndex(descriptor: SerialDescriptor): Int {
        var currentIndex = elementIndex
        while (true) {
            // proceed to next element
            currentIndex++
            // if we have reached the end, let decoder know there are not more arguments to decode
            if (currentIndex >= descriptor.elementsCount) return CompositeDecoder.DECODE_DONE
            val currentName = descriptor.getElementName(currentIndex)
            // Check if bundle has argument value. If so, we tell decoder to process
            // currentIndex. Otherwise, we skip this index and proceed to next index.
            if (arguments.contains(currentName)) {
                elementIndex = currentIndex
                elementName = currentName
                return elementIndex
            }
        }
    }

    /**
     * Returns argument value from the [arguments] for the argument at the index returned by
     * [decodeElementIndex]
     */
    override fun decodeValue(): Any {
        val arg = arguments[elementName]
        checkNotNull(arg) { "Unexpected null value for non-nullable argument $elementName" }
        return arg
    }

    override fun decodeNull(): Nothing? = null

    // we want to know if it is not null, so its !isNull
    override fun decodeNotNullMark(): Boolean = arguments[elementName] != null
}

/**
 * Parses a String into a Serializable Primitive
 */
private typealias TypeParser = (String) -> Serializable

private fun getTypeParser(kind: SerialKind): TypeParser {
    return when (kind) {
        PrimitiveKind.STRING -> Any::toString
        PrimitiveKind.INT -> toInt
        PrimitiveKind.BOOLEAN -> String::toBoolean
        PrimitiveKind.BYTE -> toByte
        PrimitiveKind.CHAR -> toChar
        PrimitiveKind.DOUBLE -> String::toDouble
        PrimitiveKind.FLOAT -> String::toFloat
        PrimitiveKind.LONG -> toLong
        PrimitiveKind.SHORT -> toShort
        else -> Any::toString
    }
}

val toInt: (String) -> Int = String::toInt
val toByte: (String) -> Byte = String::toByte
val toChar: (String) -> Char = String::first
val toLong: (String) -> Long = String::toLong
val toShort: (String) -> Short = String::toShort
const val TAG_LOG_ERROR = "Nav3RecipesDeepLink"