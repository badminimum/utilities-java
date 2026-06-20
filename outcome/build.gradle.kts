plugins {
    id("gradle-convention")
    id("publishing-convention")
}

publishingConvention {
    version.set("1.1.0")
    description.set("A functional wrapper for type-safe success and failure handling, supporting monadic chaining and error recovery.")
}