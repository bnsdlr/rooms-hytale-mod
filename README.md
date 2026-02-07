# Rooms Hytale Plugin

## ToDo

- [ ] half a block precision for detecting current room (x and z)
- [ ] rooms
    - [ ] more complex room definitions
      - [x] block that matches regex
      - [ ] blocks of group
    - [ ] buffs for having room
    - [ ] requirements
- [ ] make sets way more responsible
    - [ ] support for blocks that occupy more than one block of space
    - [ ] place anywhere on tables
    - [ ] any rotation (of the set)
    - [ ] add required rotation (relative)
- [ ] if multiple rooms match, give ability to choose from them.
- [ ] separate each worlds rooms
- [ ] placing/breaking blocks support for blocks that are larger than 1x1x1
- [ ] always add root block ...
- [ ] override all blocks, even the filler blocks... BreakBlockEventSystem and PlaceBlockEventSystem

## Troubleshooting

Only works for x and z coordinates from -33.554.432 to 33.554.431
Only works for y coordinates from -2048 to 2047
