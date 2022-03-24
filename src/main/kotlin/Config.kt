data class Config(val path: String, var backups: Array<Backup?>, var snapshot: Backup? = null) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Config

        if (path != other.path) return false
        if (!backups.contentEquals(other.backups)) return false
        if (snapshot != other.snapshot) return false

        return true
    }

    override fun hashCode(): Int {
        var result = path.hashCode()
        result = 31 * result + backups.contentHashCode()
        result = 31 * result + snapshot.hashCode()
        return result
    }


}