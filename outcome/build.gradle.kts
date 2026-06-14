plugins {
    id("gradle-convention")
    id("publishing-convention")
}

publishingConvention {
    version = "1.0.0"
    description =
        "A functional wrapper for type-safe success and failure handling, supporting monadic chaining and error recovery."
}