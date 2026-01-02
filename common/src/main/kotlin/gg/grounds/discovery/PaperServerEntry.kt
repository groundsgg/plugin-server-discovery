package gg.grounds.discovery

data class PaperServerEntry(
    val name: String,
    val host: String,
    val port: Int,
    val lastSeenMillis: Long = 0L,
) {

    fun encode(): String = "$name|$host|$port|$lastSeenMillis"

    companion object {
        @JvmStatic
        fun decode(value: String?): PaperServerEntry? {
            if (value.isNullOrBlank()) {
                return null
            }

            val parts = value.split('|')
            if (parts.size != 4) {
                return null
            }

            val name = parts[0].trim().takeIf { it.isNotEmpty() } ?: return null
            val host = parts[1].trim().takeIf { it.isNotEmpty() } ?: return null
            val port = parts[2].trim().toIntOrNull() ?: return null
            val lastSeenMillis = parts[3].trim().toLongOrNull() ?: return null

            return PaperServerEntry(name, host, port, lastSeenMillis)
        }
    }
}
