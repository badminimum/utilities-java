plugins {
    id("gradle-convention")
    id("publishing-convention")
}

publishingConvention {
    version.set("1.0.0")
    description.set("A fluent, type-safe library for precise data size arithmetic and formatting across binary (IEC) and decimal (SI) units.")
}