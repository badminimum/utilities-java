plugins {
    id("com.vanniktech.maven.publish")
    signing
}

val conventionExtension = extensions.create<PublishingConventionExtension>("publishingConvention")
project.objects.domainObjectContainer(MavenPomLicense::class.java)
project.objects.domainObjectContainer(MavenPomDeveloper::class.java)
project.objects.domainObjectContainer(MavenPomScm::class.java)

conventionExtension.defaults()
val capitalized = conventionExtension.name.map { name -> name.replaceFirstChar { char -> char.uppercaseChar() } }

mavenPublishing {
    coordinates("com.badminimum", "utilities-${conventionExtension.name.get()}", conventionExtension.version.get())

    pom {
        name.set("badminimum's Utility: $capitalized")
        description.set(conventionExtension.description)
        inceptionYear.set(conventionExtension.inceptionYear.map { it.toString() })
        url.set(conventionExtension.url)
        licenses {
            conventionExtension.licenses.get().forEach {
                license {
                    name.set(it.name)
                    url.set(it.url)
                    distribution.set(it.distribution)
                    comments.set(comments)
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

fun PublishingConventionExtension.defaults() {
    this.name.convention(project.name)
    this.version.convention(project.version.toString())
    this.inceptionYear.convention(2026)
    this.url.convention("")
    this.scm.convention(make<MavenPomScm> {
        this.url.set("https://codeberg.org/badminimum/utilities-java/src/branch/main/${name.get()}")
        this.connection.set("scm:git:git://codeberg.org/badminimum/utilities-java")
        this.developerConnection.set("scm:git:ssh://git@codeberg.org/badminimum/utilities-java")
    })
}

inline fun <reified M : Any> make(block: M.() -> Unit): M = project.objects.newInstance<M>().apply { block() }