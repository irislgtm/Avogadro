# Avogadro

<img width="507" height="503" alt="Screenshot 2026-10-16 092501" src="https://github.com/user-attachments/assets/08180e12-de08-4206-80cc-b8909afbc3e6" />

Standalone molecule tooltip library for **NeoForge 1.21.1** and **Forge 1.20.1**.

Avogadro shows molecule formulas in item tooltips and renders spinning 3D
ball-and-stick models in a panel below the tooltip, plus a JEI **Molecules**
recipe category. Any modpack or mod can add molecules for any item, fluid, or
tag without writing code — just a JSON file.

> The machine-readable version of this reference lives in [`llms.txt`](llms.txt).
> This page is the full, human-readable documentation.

## Docs

### The example

Below is a single molecule file with **every schema field explained in place**.
All fields are optional except the geometry source, which is one of `smiles`,
`atoms`, or `copy`.

```jsonc
{
  // ─── Geometry (the required part: pick one of these three) ────────────

  // 1. Build the shape from a SMILES string. Supported: the organic subset
  //    B C N O P S F I; two-letter elements Si Cl Br Na Mg Al K Ca Fe Cu Zn and
  //    the rest of the element table; aromatic b c n o p s; bracket atoms for
  //    anything else such as [U] or [nH]; the bond symbols - = # : / and \;
  //    branches ( ); ring closures 0-9 and %nn; and . between disconnected
  //    fragments. Chiral @ markers and reaction > arrows are ignored. Implicit
  //    hydrogens are filled in from valence, then a force-directed layout is
  //    applied.
  //
  //    Examples: "O" water, "C" methane, "CC" ethane, "C=C" ethene,
  //    "C#C" acetylene, "O=C=O" carbon dioxide, "N" ammonia, "O=S=O" sulfur
  //    dioxide, "c1ccccc1" benzene, "CC(=O)C" acetone, "CCO" ethanol,
  //    "[Na]" sodium, "O[Ca]O" slaked lime.
  "smiles": "CC(=O)C",

  // 2. Or hand-place every atom instead. "e" is an element symbol; x/y/z are
  //    floats in bond-length units.
  // "atoms": [
  //   { "e": "O", "x":  0.000, "y": 0.000, "z": 0 },
  //   { "e": "H", "x": -0.593, "y": 0.459, "z": 0 },
  //   { "e": "H", "x":  0.593, "y": 0.459, "z": 0 }
  // ],

  //    Bonds for "atoms": [from, to] or [from, to, type], using 0-based atom
  //    indices. type is "SINGLE" (default), "DOUBLE", "TRIPLE", "QUADRUPLE",
  //    the numbers 1-4, or one of - = #. Omit "bonds" entirely to auto-bond any
  //    two atoms closer than 1.3x the sum of their radii.
  // "bonds": [[0, 1], [0, 2, "SINGLE"]],

  // 3. Or reuse another molecule's geometry by id. Your own "formula"
  //    overrides the copied one, and your own target lists below are unioned
  //    with the copied ones. Ideal in config files to bind new items to an
  //    existing shape without copying coordinates.
  //
  //    Priority when several are present: "copy" wins over "atoms", which wins
  //    over "smiles". A "copy" whose target does not exist skips this file.
  // "copy": "avogadro:water",

  // ─── Display ──────────────────────────────────────────────────────────

  // Text drawn under the molecule in JEI. Omit to auto-compute Hill order
  // (C first, then H, then alphabetical) with Unicode subscripts, so H2O
  // renders as "H" + subscript 2 + "O".
  "formula": "C3H6O",

  // ─── What this molecule binds to ──────────────────────────────────────

  // Item ids that show this molecule.
  "items": ["mymod:acetone_bucket", "mymod:acetone_bottle"],

  // Item tag ids, with or without a leading #.
  "item_tags": ["c:ketones"],

  // Fluid ids. List flowing variants explicitly.
  "fluids": ["mymod:acetone", "mymod:flowing_acetone"],

  // Fluid tag ids.
  "fluid_tags": ["c:acetone"],

  // GregTech Modern material ids, e.g. "gtceu:water". Matches any item or fluid
  // whose GTM material is listed, so one line covers every cell, bucket, ingot
  // and tank of that material. Inert when GT is not installed.
  "materials": ["gtceu:acetone"],

  // Default true. When true, any item that holds a bound fluid (a bucket, tank
  // or bottle with a fluid capability) also shows this molecule. Set false so
  // the molecule only matches the fluid itself in JEI.
  "match_containers": true
}
```

### Where molecule files live

Three sources, merged on the client. Later sources win when ids collide:

| Source | Path | Molecule id |
| --- | --- | --- |
| Datapacks (shared, synced to clients on login and `/reload`) | `data/<namespace>/molecules/<path>.json` | `<namespace>:<path>` |
| Resource packs and mod jars (client art overrides) | `assets/<namespace>/molecules/<path>.json` | `<namespace>:<path>` |
| Local config (per-user overrides, client only) | `config/avogadro/molecules/<name>.json` | `avogadro:<name>` |

Invalid files are skipped silently. `/reload` reloads datapack files on the
server and re-syncs them; a resource-pack reload (F3+T) reloads the asset and
config files on the client.

### Field reference

| Field | Type | Default | Meaning |
| --- | --- | --- | --- |
| `smiles` | string | — | Structure from a SMILES string. |
| `atoms` | array | — | Explicit geometry: `{"e": "O", "x": 0, "y": 0, "z": 0}`. |
| `bonds` | array | auto | Explicit bonds for `atoms`: `[from, to]` or `[from, to, type]`. |
| `copy` | string | — | Reuse another molecule's geometry, e.g. `"avogadro:water"`. |
| `formula` | string | Hill | Display string shown under the molecule in JEI. |
| `items` | string[] | `[]` | Item ids that show this molecule. |
| `item_tags` | string[] | `[]` | Item tag ids, leading `#` optional. |
| `fluids` | string[] | `[]` | Fluid ids. |
| `fluid_tags` | string[] | `[]` | Fluid tag ids. |
| `materials` | string[] | `[]` | GregTech Modern material ids. |
| `match_containers` | bool | `true` | Also match items holding a bound fluid. |

One geometry source is required: `smiles`, `atoms`, or `copy`.

### Resolution order for a hovered item

1. Direct item id match in `items`.
2. Item tag match in `item_tags`, in load order.
3. Fluid inside the item (bucket/tank contents) matched against `fluids`, then
   `fluid_tags`, when that molecule has `match_containers: true`.
4. GregTech Modern material of the item or fluid matched against `materials`,
   then the material path against a molecule path. Skipped without GT.
5. Fallback: a molecule whose path equals the item path (`mymod:acetone` matches
   molecule `anything:acetone`). Explicit bindings always beat the fallback.

For JEI fluid ingredients the same order applies starting at the fluid step.

## Config (`config/avogadro-common.toml`)

| Key | Type | Default | Meaning |
| --- | --- | --- | --- |
| `enableTooltips` | bool | `true` | Show molecules in item tooltips. |
| `tooltipSize` | int (16-128) | `64` | Molecule box in pixels. |
| `spinSeconds` | double (0.5-60) | `6.0` | Seconds per full rotation. |
| `jeiCategory` | bool | `true` | Register the JEI Molecules category. |
| `cubicAtoms` | bool | `false` | Render atoms as cubes and bonds as square sticks instead of spheres and tubes. |

## JEI notes

- Item tooltips in JEI gain the formula line, and the 3D panel draws below the
  tooltip wherever Forge fires the tooltip render event.
- The Molecules category lists every loaded molecule with its formula and up to
  4 representative bound items. Press U on a bound item to find it.
- Category contents are fixed at JEI startup: built-in, asset and config
  molecules always appear. Server datapack molecules appear in tooltips after
  sync; they join the category after a client restart.
- On 1.20.1 there is no tooltip-component system, so fluid ingredient hovers
  cannot embed the 3D view — use buckets, tanks and cells to see the full panel.
