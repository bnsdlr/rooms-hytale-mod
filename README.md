# Rooms Hytale Plugin

> Only works for x and z coordinates from -33.554.432 to 33.554.431
> Only works for y coordinates from -2048 to 2047

## ToDo

- [ ] compress room positions instead of uncompressed (use packedbox not long)

- [ ] half a block precision for detecting current room (x and z)
- [ ] rooms
    - [ ] more complex room definitions
      - [x] block that matches regex
      - [ ] blocks of group
      - [ ] block type
      - [ ] conditional block (sth like or)
    - [ ] buffs for having room
    - [ ] requirements
- [ ] sets 
  - [ ] make sets way more responsible
    - [ ] support for blocks that occupy more than one block of space
      - [ ] place anywhere on tables
      - [ ] any rotation (of the set)
      - [ ] add required rotation (relative)
- [ ] if multiple rooms match, give ability to choose from them.
- [ ] always add root block ...
- [ ] override all blocks, even the filler blocks... BreakBlockEventSystem and PlaceBlockEventSystem

