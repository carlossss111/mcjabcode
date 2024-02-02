# Using High-Capacity Encodings to represent Game Data
('Hybrid-Arcade' project for short)

## Description
This is part of a research project for the University of Nottingham. The research is about analysing and selecting  different physical data representations (e.g. barcodes) so that a large amount of game data can be stored and transported physically without using a network or a database.

Minecraft was selected as an examplea, since it is very modifiable and it has 3-dimensional spaces that would need a high capacity to be stored. The mod should work like so:
1. A player can select a 3-dimensional space in the game.
1. The selected space can be printed out as an encoding on a card.
1. Later, the encoding can be scanned to bring the selected space back into the game's memory, even on other game-worlds or PCs.
1. The selected space can be placed back into the game, along with all the blocks that were originally in the space.

## Usage
Run with:
```
./gradlew runClient
```
Build and installation instructions coming soon.

## Authors
Written by: Daniel Robinson / psydr2@nottingham.ac.uk

Supervised by: Steve Bagley

## Licenses
* The code written by me is licensed under GNU GPLv3 in LICENSE.txt, and a copy of the source code will be made available.
* The Minecraft Forge library is licensed under GNU LGPLv2 and is supplied in the forgeinfo directory.
