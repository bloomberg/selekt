# Software bill of materials

Each published Selekt JAR has a CycloneDX SBOM. Maven Central publishes it beside the JAR using the `cyclonedx`
classifier, for example `selekt-jdbc-{selektVersion}-cyclonedx.json`. The identical document is embedded at
`META-INF/sbom/selekt-jdbc.cdx.json` for runtime inspection:

``` kotlin
val sbom = SelektDriver::class.java.classLoader
    .getResourceAsStream("META-INF/sbom/selekt-jdbc.cdx.json")
```

The JDBC, JVM, and native SQLCipher SBOMs also inventory the native code bundled in the runtime artifact: SQLCipher,
its SQLite and OpenSSL dependencies, the optional vec1 extension when enabled, and mimalloc for Linux variants.
Platform restrictions are recorded as component properties.

Run `./gradlew cyclonedxBom` to generate every final JVM artifact SBOM, or
`./gradlew :selekt-jdbc:enrichCycloneDxSbom` for the final JDBC document.
