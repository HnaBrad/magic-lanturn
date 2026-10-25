# Magic Lanturn

Magic Lanturn comes in all 16 standard Minecraft dye colors, with white as the
default. Each lantern fades from darkness to maximum block light level 15 in
300 game ticks (15 seconds at normal speed). It also spawns 150 invisible,
non-colliding point entities and their matching invisible light sources in a
5-by-5-by-6 grid, spaced 5 blocks apart.
Each point randomly fades in during the first 600 game ticks (30 seconds), then
stays lit until the lantern is broken. Afterward, each point fades out and
despawns, removing its light source too. The lantern itself also fades out
after being broken.

The lanterns and their point lights use matching RGB colors with
[ColorLight](https://modrinth.com/mod/colorlight) installed on the client.
Without ColorLight, Minecraft's built-in block lighting is white. All 16
lanterns are available in the Magic Lanturn creative tab. GitHub Actions
publishes the built mod JAR as a GitHub Release for every push, with
automatically generated changelog notes.

## Setup

For setup instructions, please see the [Fabric Documentation page](https://docs.fabricmc.net/develop/getting-started/creating-a-project#setting-up) related to the IDE that you are using.

## License

This template is available under the CC0 license. Feel free to learn from it and incorporate it in your own projects.
