data class Config(val path: String, var backups: Array<Backup?>?) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Config

        if (path != other.path) return false
        if (backups != null) {
            if (other.backups == null) return false
            if (!backups.contentEquals(other.backups)) return false
        } else if (other.backups != null) return false

        return true
    }

    override fun hashCode(): Int {
        var result = path.hashCode()
        result = 31 * result + (backups?.contentHashCode() ?: 0)
        return result
    }
}