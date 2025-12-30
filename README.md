# Spellbook
Spellbook is a library for Hytale, designed to provide helpful utility code, 
new components, and a variety of features to empower pack makers!

> **⚠️ Warning: Early Access**    
> The game Hytale is in early access, and so is this project! Features may be
> incomplete, unstable, or change frequently. Please be patient and understanding as development
> continues.

## Projects Using Spellbook
- Hopefully someone

## Maven
This project is in early access and is not available on maven yet! For now please use [CurseMaven](https://www.cursemaven.com/).

**build.gradle**
```groovy
repositories {
    exclusiveContent {
        forRepository {
            maven {
                url "https://cursemaven.com"
            }
        }
        filter {
            includeGroup "curse.maven"
        }
    }
}

dependencies {
    implementation "curse.maven:spellbook-238222:2724420"
}
```