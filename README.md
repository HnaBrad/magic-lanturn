# Magic Lanturn

Lanturn fades from darkness to maximum block light level 15 in 300 game ticks
(15 seconds at normal speed). It also spawns 125 invisible, non-colliding point
entities and their matching invisible light sources in a 5-block-spaced grid
around it. Each point randomly fades in, holds its light, then fades out and
despawns within 600 game ticks (30 seconds), removing its light source too.
The lantern itself also fades out after being broken.

The item is available in the Magic Lanturn creative tab.
Each light point has its own randomized fade-in and fade-out timing; every
point completes its cycle within 600 game ticks (30 seconds). GitHub Actions
publishes the built mod JAR as a GitHub Release for every push.

## Setup

For setup instructions, please see the [Fabric Documentation page](https://docs.fabricmc.net/develop/getting-started/creating-a-project#setting-up) related to the IDE that you are using.

## License

This template is available under the CC0 license. Feel free to learn from it and incorporate it in your own projects.
