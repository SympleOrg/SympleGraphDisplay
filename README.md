## How to install
### Option #1
in `build.dependencies.gradle` add
```groovy
repositories {
    maven { url = uri('https://Symple25125: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@maven.pkg.github.com/SympleOrg/SympleGraphDisplay') }
}

dependencies {
    implementation project(":top.symple:symplegraphdisplay:VERSION")
}
```
*NOTE: replace `VERSION` with the wanted version, (See [latest version](https://github.com/SympleOrg/SympleGraphDisplay/releases/latest))

### Option #2
1. download the [zip](https://github.com/SympleOrg/SympleGraphDisplay/releases/latest) file and extract it into the project
2. in `build.dependencies.gradle` add
```groovy
dependencies {
  implementation project(":symplegraphdisplay")
}
```
