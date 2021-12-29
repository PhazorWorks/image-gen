# Apollo Image Generator (Kotlin)
An experimental image generator for Apollo

## How to run:
  * It's recommended to run this jar with JDK 18 (Thanks to LOOM read more [here](https://wiki.openjdk.java.net/display/loom/Main))
  * Once these requirements have been met, download the release jar from the release page on GitHub.
  * The webserver should be on port `3002`
This repo contains a highly efficient and specially made image generation for Apollo Discord bot.

It uses java's 2DGraphics API part of the modern java.awt package to fill in the template image in record time!

## Current Speeds:
* With Brad's template:

`-` 20-30ms (With cached thumbnail) 

`-` 400-500ms (Without cached thumbnail)

* With default template:

`-` 1-10ms (With cached thumbnail)

`-` 400-500ms (Without cached thumbnail)

## Program Flags

* `--debug` (SysProp: `debug: <Boolean>`)

`-` This saves a local copy of the generated image to the jars current path for debugging purposes. (Since its run with CachedThreadPool this has virtually `0` performance penalties but still, isn't recommended for release)

* `--fallback` (SysProp: `fallback: <Boolean>`)

`-` This first makes the webserver check if maxresdefault exists for the YouTube video, which has performance penalties. Because of this it is not enabled by default and must be enabled explicity)

* `--bradgen` (SysProp: `bradgen: <Boolean>`

`-` This enables Brad's template generator to be used, it takes a bit longer due to its more complex nature. Because of this it needs to be explicity enabled)
