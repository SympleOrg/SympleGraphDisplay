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
