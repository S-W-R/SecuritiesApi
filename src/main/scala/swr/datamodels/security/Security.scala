package swr.datamodels.security

// estatico newtypes не завезли в scala3, opaque types - неудобны, поэтому используем примитивы
final case class Security(
    id: Int,
    securityId: String,
    name: String,
    registrationNumber: String,
    emitentTitle: String
)
