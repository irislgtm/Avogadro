# Avogadro

<img width="507" height="503" alt="Screenshot 2026-10-16 092501" src="https://github.com/user-attachments/assets/08180e12-de08-4206-80cc-b8909afbc3e6" />




Standalone molecule tooltip library for NeoForge 1.21.1, Forge 1.20.1 and Forge 1.7.10

Note: I suspect most users will be using LLMs to write datapacks, so the standard format is visible in `llms.txt`. 

However a simple example is visible here:

```json
{
  "smiles": "CC(=O)C",
  "formula": "C3H6O",
  "items": ["mymod:acetone_bucket", "mymod:acetone_bottle"],
  "item_tags": ["c:ketones"],
  "fluids": ["mymod:acetone", "mymod:flowing_acetone"],
  "fluid_tags": ["c:acetone"],
  "match_containers": true,
  "copy": "avogadro:acetone"
}
```
