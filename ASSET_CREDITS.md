# Asset credits and licenses

This project does not use ripped GTA V / LSPDFR files. Textures are original licensed assets from Poly Haven and are included under CC0 1.0 Universal. The Android game synthesizes its siren, engine, footstep and impact sounds at runtime; no third-party audio sample is embedded.

## Included textures

| Asset in this repository | Original asset | Creator credit | License and source |
| --- | --- | --- | --- |
| `app/src/main/assets/textures/asphalt_03_diff_1k.jpg` | Asphalt 03 diffuse map | Charlotte Baglioni (photography), Dario Barresi (processing) | [CC0 1.0](https://creativecommons.org/publicdomain/zero/1.0/) · [Poly Haven asset page](https://polyhaven.com/a/asphalt_03) |
| `app/src/main/assets/textures/concrete_floor_diff_1k.jpg` | Concrete Floor diffuse map | eye-candy.xyz | [CC0 1.0](https://creativecommons.org/publicdomain/zero/1.0/) · [Poly Haven asset page](https://polyhaven.com/a/concrete_floor) |

The source asset repository's texture ledger also records these files and their CC0 status: [AetherRadar/operation-steel-tide texture license ledger](https://github.com/AetherRadar/operation-steel-tide/blob/main/assets/textures/LICENSE.md). The files retain the original Poly Haven content and are not relicensed as project-authored art.

## Runtime behavior

- OpenGL ES 2.0 loads diffuse images from Android assets on the GL thread.
- The road map repeats over tiled road planes; the concrete map is applied to generated building meshes.
- The officer has a light procedural walking gait made from the current low-poly character geometry.
- Sound uses Android `AudioTrack` on a background worker and is paused/resumed with the Activity lifecycle.
