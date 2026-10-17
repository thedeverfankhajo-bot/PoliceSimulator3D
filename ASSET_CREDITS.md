# Asset credits and licenses

This project does not use ripped GTA V / LSPDFR files. Textures are original licensed assets from Poly Haven and are included under CC0 1.0 Universal. The game currently synthesizes its siren and engine at runtime. A curated Kenney CC0 audio library is now bundled for future/explicit playback integration; importing a file does not by itself mean the game triggers it yet.

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


## Imported Kenney CC0 vehicle and city models

Source mirror: [eturner58/game-assets](https://github.com/eturner58/game-assets), originally created/distributed by [Kenney](https://kenney.nl). Each source pack provides a `License.txt` declaring CC0 1.0 Universal. The following GLB assets are bundled in the app but are **not yet parsed by the custom OpenGL ES renderer**; current world geometry remains procedurally rendered.

| App asset | Source asset | License |
| --- | --- | --- |
| `app/src/main/assets/models/kenney/cars/police.glb` | [Car Kit / police.glb](https://github.com/eturner58/game-assets/blob/main/kenney/3D%20assets/Car%20Kit/Models/GLB%20format/police.glb) | [CC0 1.0](https://creativecommons.org/publicdomain/zero/1.0/) |
| `app/src/main/assets/models/kenney/cars/sedan.glb` | [Car Kit / sedan.glb](https://github.com/eturner58/game-assets/blob/main/kenney/3D%20assets/Car%20Kit/Models/GLB%20format/sedan.glb) | CC0 1.0 |
| `app/src/main/assets/models/kenney/cars/suv.glb` | [Car Kit / suv.glb](https://github.com/eturner58/game-assets/blob/main/kenney/3D%20assets/Car%20Kit/Models/GLB%20format/suv.glb) | CC0 1.0 |
| `app/src/main/assets/models/kenney/cars/taxi.glb` | [Car Kit / taxi.glb](https://github.com/eturner58/game-assets/blob/main/kenney/3D%20assets/Car%20Kit/Models/GLB%20format/taxi.glb) | CC0 1.0 |
| `app/src/main/assets/models/kenney/city/road-straight.glb` | [City Kit - Roads / road-straight.glb](https://github.com/eturner58/game-assets/blob/main/kenney/3D%20assets/City%20Kit%20-%20Roads/Models/GLB%20format/road-straight.glb) | CC0 1.0 |
| `app/src/main/assets/models/kenney/city/road-intersection.glb` | [City Kit - Roads / road-intersection.glb](https://github.com/eturner58/game-assets/blob/main/kenney/3D%20assets/City%20Kit%20-%20Roads/Models/GLB%20format/road-intersection.glb) | CC0 1.0 |
| `app/src/main/assets/models/kenney/city/road-crossing.glb` | [City Kit - Roads / road-crossing.glb](https://github.com/eturner58/game-assets/blob/main/kenney/3D%20assets/City%20Kit%20-%20Roads/Models/GLB%20format/road-crossing.glb) | CC0 1.0 |
| `app/src/main/assets/models/kenney/city/traffic-light.glb` | [City Kit - Roads / traffic-light.glb](https://github.com/eturner58/game-assets/blob/main/kenney/3D%20assets/City%20Kit%20-%20Roads/Models/GLB%20format/traffic-light.glb) | CC0 1.0 |

## Imported Kenney CC0 audio

Source packs: [Impact Sounds](https://github.com/eturner58/game-assets/tree/main/kenney/Audio/Impact%20Sounds) and [Interface Sounds](https://github.com/eturner58/game-assets/tree/main/kenney/Audio/Interface%20Sounds). Both source-pack `License.txt` files state CC0 1.0. These compact OGG clips are bundled as source assets; the existing procedural audio engine has not yet been changed to play them automatically.

- `app/src/main/assets/audio/kenney/footstep_concrete_000.ogg` — concrete footstep
- `app/src/main/assets/audio/kenney/impactMetal_medium_000.ogg` — medium metal impact
- `app/src/main/assets/audio/kenney/impactGlass_light_000.ogg` — light glass impact
- `app/src/main/assets/audio/kenney/confirmation_001.ogg` — UI confirmation
- `app/src/main/assets/audio/kenney/select_001.ogg` — UI selection
- `app/src/main/assets/audio/kenney/error_008.ogg` — UI error

## Additional Poly Haven textures

These diffuse maps are from the source repository's [Poly Haven CC0 license ledger](https://github.com/AetherRadar/operation-steel-tide/blob/main/assets/textures/LICENSE.md). They are bundled as optional materials; the current renderer's material mapping is unchanged by this import.

- `app/src/main/assets/textures/corrugated_iron_diff_1k.jpg` — [Corrugated Iron](https://polyhaven.com/a/corrugated_iron), Dimitrios Savva and Jenelle van Heerden.
- `app/src/main/assets/textures/rusty_painted_metal_diff_1k.jpg` — [Rusty Painted Metal](https://polyhaven.com/a/rusty_painted_metal), Amal Kumar.
