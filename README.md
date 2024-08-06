## How to install
### Option #1
in `build.common.gradle` add
```groovy
plugins {
    id("elect86.magik") version "0.3.3"
}
```

in `build.dependencies.gradle` add
```groovy
repositories {
    githubPackages("SympleOrg/SympleGraphDisplay")
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
