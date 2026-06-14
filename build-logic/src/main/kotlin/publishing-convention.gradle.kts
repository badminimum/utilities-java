plugins {
    id("com.vanniktech.maven.publish")
    signing
}

project.objects.domainObjectContainer(MavenPomLicense::class.java)
project.objects.domainObjectContainer(MavenPomDeveloper::class.java)
project.objects.domainObjectContainer(MavenPomScm::class.java)

val conventionExtension = makeExtension()
val capitalized = conventionExtension.name.map { name -> name.replaceFirstChar { char -> char.uppercaseChar() } }

afterEvaluate {
    mavenPublishing {
        coordinates("com.badminimum", "utilities-${conventionExtension.name.get()}", conventionExtension.version.get())

        pom {
            name.set(capitalized.map { "badminimum's Utility: $it" })
            description.set(conventionExtension.description)
            inceptionYear.set(conventionExtension.inceptionYear.map { it.toString() })
            url.set(conventionExtension.url)
            licenses {
                if (conventionExtension.licenses.isPresent) {
                    conventionExtension.licenses.get().forEach {
                        license {
                            name.set(it.name)
                            url.set(it.url)
                            distribution.set(it.distribution)
                            comments.set(comments)
                        }
                    }
                } else {
                    make<MavenPomLicense> {
                        name.set("The Apache License, Version 2.0")
                        url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                        distribution.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                    }
                }
            }
            developers {
                developer {
                    id.set("badminimum")
                    name.set("badminimum")
                    url.set("https://codeberg.org/badminimum/")
                    roles.set(setOf("lead", "developer"))
                    timezone.set("Europe/Berlin")
                }
                conventionExtension.developers.get().forEach {
                    developer {
                        id.set(it.id)
                        name.set(it.name)
                        email.set(it.email)
                        url.set(it.url)
                        organization.set(it.organization)
                        organizationUrl.set(it.organizationUrl)
                        roles.set(it.roles)
                        timezone.set(it.timezone)
                        properties.set(it.properties)
                    }
                }
            }
            scm {
                var convScm = conventionExtension.scm.get()
                connection.set(convScm.connection)
                developerConnection.set(convScm.developerConnection)
                url.set(convScm.url)
                tag.set(convScm.tag)
            }
        }

        publishToMavenCentral()
        signAllPublications()
    }
}

interface PublishingConventionExtension {
    val name: Property<String>
    val version: Property<String>
    val description: Property<String>
    val inceptionYear: Property<Int>
    val url: Property<String>
    val licenses: SetProperty<MavenPomLicense>
    val developers: SetProperty<MavenPomDeveloper>
    val scm: Property<MavenPomScm>
}

inline fun <reified M : Any> make(block: M.() -> Unit): M = project.objects.newInstance<M>().apply { block() }
fun makeExtension(): PublishingConventionExtension {
    val extension = extensions.create<PublishingConventionExtension>("publishingConvention")
    extension.name.convention(project.provider { project.name })
    extension.description.convention(project.provider { "A commonly used util by badminimum" })
    extension.version.convention(project.provider { project.version.toString() })
    extension.inceptionYear.convention(project.provider { 2026 })
    extension.url.convention(project.provider { "https://codeberg.org/badminimum/utilities-java" })
    extension.scm.convention(project.provider {
        make<MavenPomScm> {
            this.url.set("https://codeberg.org/badminimum/utilities-java/src/branch/main/${extension.name.get()}")
            this.connection.set("scm:git:git://codeberg.org/badminimum/utilities-java")
            this.developerConnection.set("scm:git:ssh://git@codeberg.org/badminimum/utilities-java")
        }
    })

    return extension
}