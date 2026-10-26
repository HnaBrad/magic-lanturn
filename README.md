# Magic Lanturn

Magic Lanturn comes in all 16 standard Minecraft dye colors, with white as the
default. Each lantern fades from darkness to maximum block light level 15 in
300 game ticks (15 seconds at normal speed). It also places 1,000 invisible,
non-colliding light blocks, randomly distributed across a 50-by-50-block area
over 12 vertical layers.
Each point randomly fades in during the first 600 game ticks (30 seconds), then
stays lit until the lantern is broken. Afterward, each point fades out and is
removed. The lantern itself also fades out after being broken.

The lanterns and their point lights use matching RGB colors with
[ColorLight](https://modrinth.com/mod/colorlight) installed on the client.
Without ColorLight, Minecraft's built-in block lighting is white. All 16
lanterns are available in the Magic Lanturn creative tab and can be crafted
with a vanilla lantern in the center, surrounded by glass. The default white
lantern uses regular glass; each colored variant uses matching stained glass.
The Mining Dimension has flat, water-free terrain with its surface at Y=128,
all vanilla Overworld biomes, and their natural ore features. Rain, thunder,
and natural mob spawning are disabled there. Arrivals are placed above Y=128,
and its clock is paused at noon. The Void Dimension is empty except for a
3-by-3 arrival platform at Y=60, with its portal in the center and stone bricks
surrounding it.
The Mining Dimension Portal is crafted with a diamond pickaxe surrounded by
obsidian; the Void Dimension Portal uses an ender pearl surrounded by obsidian.
Both are also available in the creative tab; matching portals appear at the
destination, and players return to the exact entrance location they used.
GitHub Actions builds the project and runs its configured tests on pushes and
pull requests. It does not automatically upload build artifacts or publish
GitHub releases.

## Setup

For setup instructions, please see the [Fabric Documentation page](https://docs.fabricmc.net/develop/getting-started/creating-a-project#setting-up) related to the IDE that you are using.

## License

This template is available under the CC0 license. Feel free to learn from it and incorporate it in your own projects.
